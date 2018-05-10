#include "Stdafx.h"
#include <event2/util.h>
#include <event2/event.h>
#include <event2/bufferevent.h>
#include "RedirectListener.h"

#include "Utils/Event/ListenerEvD.h"
#include "Utils/Utils.h"
#include "Utils/AutoBuffer.h"
#include "Utils/ObjectPool.h"
#include "Utils/Event/SocketUtils.h"

#include "RedirectClient.h"

#include "Cfg.h"
#include "Socks5.h"

static ObjectPool<RedirectClient> _s_client_pool_(1024);
  
#ifndef SOL_IP
	#define SOL_IP 0
#endif
#ifndef SO_ORIGINAL_DST
	#define SO_ORIGINAL_DST 80
#endif


extern event_base *__g_evbase__;
extern evdns_base *__g_evdns__;

RedirectListener::RedirectListener(void)
{ 
}

RedirectListener::~RedirectListener(void)
{ 
}
 

void RedirectListener::stop(void)
{
	for (RedirectClient *c = clients_.popHead(); c != NULL; c = clients_.popHead())
		_s_client_pool_.destroy(c);

	TcpAcceptor::stop();
}
 

// ++++++++++++++ acceptor ++++++++++++++

RedirectClient* RedirectListener::createNewClient(bufferevent *bev, unsigned remoteIp)
{
	bufferevent_disable(bev, EV_READ | EV_WRITE);

	RedirectClient *redirectClient = _s_client_pool_.create(this, bev);
	clients_.addTail(redirectClient);
	return redirectClient;
}

void RedirectListener::onNewConn(evutil_socket_t fd, sockaddr_in *addr)
{
#ifdef _WIN32
	SockAddrIn dstAddr("192.168.128.128",8110);
#else
	SockAddrIn dstAddr;
	ev_socklen_t len = sizeof(dstAddr);
	if (getsockopt(fd, SOL_IP, SO_ORIGINAL_DST, (char*)&dstAddr, &len) != 0) {
		LOG_DEBUG("Only redirect socket supported");
		evutil_closesocket(fd);
	}
	else
#endif
	{
		LOG_DEBUG(inetNtoa(dstAddr.ip()) << dstAddr.port());
		RedirectClient *redirectClient = ListenerEvD<RedirectListener, RedirectClient>::evStartNewClient(this, fd, (unsigned&)addr->sin_addr);
		if (redirectClient != NULL) {
			redirectClient->setDst(dstAddr.ip(),dstAddr.port());
			connectToSocks5Server(redirectClient);
		}
	}
}

// -------------- acceptor --------------

// ++++++++++++++ socks5 ++++++++++++++
void RedirectListener::onSocks5Write(RedirectClient *redirectClient)
{
	if (redirectClient->getRequestBufferEvent() == NULL)
		termClient(redirectClient);
}

void RedirectListener::onSocks5Event(RedirectClient *redirectClient,short what)
{ 
	LOG_INFO(redirectClient << __FUNCTION__ << what << (int)redirectClient->getState());
	if (what == BEV_EVENT_CONNECTED) {
		sendSocks5MethodPacket(redirectClient);
	}
	else {
		redirectClient->closeSocks5BufferEvent(false);

		bufferevent *bevRequest = redirectClient->getRequestBufferEvent();
		if (bevRequest==NULL || evbuffer_get_length(bufferevent_get_output(bevRequest)) == 0)
			termClient(redirectClient);
	}
}

int RedirectListener::connectToSocks5Server(RedirectClient *redirectClient)
{
	redirectClient->setState(RedirectClient::eConnectToSocks5Server);
	bufferevent *bevSocks5 = bufferevent_socket_new(__g_evbase__, -1, BEV_OPT_CLOSE_ON_FREE);
	if (bevSocks5 == NULL)
		return -1;

	struct H {
		static void cbRead(bufferevent *bev, void *ctx) {
			RedirectClient *redirectClient = (RedirectClient*)ctx;
			redirectClient->getListener()->onSocks5Read(redirectClient);
		}
		static void cbWrite(bufferevent *bev, void *ctx) {
			RedirectClient *redirectClient = (RedirectClient*)ctx;
			redirectClient->getListener()->onSocks5Write(redirectClient);
		}
		static void cbEvent(bufferevent *bev, short what, void *ctx) {
			RedirectClient *redirectClient = (RedirectClient*)ctx;
			redirectClient->getListener()->onSocks5Event(redirectClient, what);
		}
	};

	redirectClient->setSocks5BufferEvent(bevSocks5);
	bufferevent_enable(bevSocks5, EV_READ | EV_WRITE);
	bufferevent_setcb(bevSocks5, H::cbRead, H::cbWrite, H::cbEvent, redirectClient);

	if (bufferevent_socket_connect_hostname(bevSocks5, __g_evdns__, AF_INET,
		Cfg::socks5ServerHost(), Cfg::socks5ServerPort()) == 0)
		return 0;

	termClient(redirectClient);
	return -2;
}

void RedirectListener::onSocks5Read(RedirectClient *redirectClient)
{
	switch (redirectClient->getState()) {
	case RedirectClient::eSocks5MethodReq:
		onSocks5MethodReply(redirectClient);
		break;

	case RedirectClient::eSocks5Auth:
		onSocks5AuthReply(redirectClient);
		break;

	case RedirectClient::eSocks5Connect:
		onSocks5ConnectReply(redirectClient);
		break;

	case RedirectClient::eSocks5Connected: {
			bufferevent *bevSocks5 = redirectClient->getSocks5BufferEvent();
			bufferevent *bevRequest = redirectClient->getRequestBufferEvent();
			if (bevRequest != NULL)
				bufferevent_write_buffer(bevRequest, bufferevent_get_input(bevSocks5));
		}
		break;
	default:
		break;
	}
}

void RedirectListener::sendSocks5MethodPacket(RedirectClient *redirectClient)
{
	redirectClient->setState(RedirectClient::eSocks5MethodReq);
	
	char buffer[4096];
	uint8_t methods[] = { Socks5::AUTH_METHOD_NON,Socks5::AUTH_METHOD_PWD };
	uint8_t nmethods = Cfg::socks5UserName()[0] == '\0' ? 1 : 2;
	size_t len = Socks5::makeMethodReq(buffer, methods, nmethods);
	bufferevent_write(redirectClient->getSocks5BufferEvent(),buffer,len);
}

void RedirectListener::onSocks5MethodReply(RedirectClient *redirectClient)
{
	evbuffer *evbuf = bufferevent_get_input(redirectClient->getSocks5BufferEvent());
	size_t lengthOfInput = evbuffer_get_length(evbuf);
	
	Socks5::MethodReply *reply = (Socks5::MethodReply*)evbuffer_pullup(evbuf, lengthOfInput);
	if (!reply->check(lengthOfInput)) {
		LOG_DEBUG("socks5 method response:" << lengthOfInput <<
			(unsigned)reply->VER << (unsigned)reply->METHOD);
		termClient(redirectClient);
	}
	else {
		if (reply->METHOD == Socks5::AUTH_METHOD_NON)
			sendSock5ConnectPacket(redirectClient);
		else
			sendSocks5AuthPacket(redirectClient);
		evbuffer_drain(evbuf, lengthOfInput);
	}
}


void RedirectListener::sendSocks5AuthPacket(RedirectClient *redirectClient)
{
	char buffer[4096];
	size_t len = Socks5::makeAuthReq(buffer, Cfg::socks5UserName(), Cfg::socks5Password());
	bufferevent_write(redirectClient->getSocks5BufferEvent(), buffer, len);
	redirectClient->setState(RedirectClient::eSocks5Auth);

}

void RedirectListener::onSocks5AuthReply(RedirectClient *redirectClient)
{
	evbuffer *evbuf = bufferevent_get_input(redirectClient->getSocks5BufferEvent());
	size_t lengthOfInput = evbuffer_get_length(evbuf);

	Socks5::AuthReply *reply = (Socks5::AuthReply*)evbuffer_pullup(evbuf, lengthOfInput);
	if (!reply->check(lengthOfInput)) {
		LOG_DEBUG("socks5 auth response:" << lengthOfInput <<
			(unsigned)reply->VER << (unsigned)reply->STATUS);
		termClient(redirectClient);
	}
	else {
		sendSock5ConnectPacket(redirectClient);
		evbuffer_drain(evbuf, lengthOfInput);
	}
}

void RedirectListener::sendSock5ConnectPacket(RedirectClient *redirectClient)
{
	char buffer[4096];
	size_t len = Socks5::makeConnectReq(buffer, redirectClient->getDstIp(),redirectClient->getDstPort());
	bufferevent_write(redirectClient->getSocks5BufferEvent(), buffer, len);

	redirectClient->setState(RedirectClient::eSocks5Connect);
}

void RedirectListener::onSocks5ConnectReply(RedirectClient *redirectClient)
{
	evbuffer *evbuf = bufferevent_get_input(redirectClient->getSocks5BufferEvent());
	size_t lengthOfInput = evbuffer_get_length(evbuf);

	Socks5::ConnectReply *reply = (Socks5::ConnectReply*)evbuffer_pullup(evbuf, lengthOfInput);
	if (!reply->check(lengthOfInput)) {
		LOG_DEBUG("socks5 connect response:" << lengthOfInput << (unsigned)reply->VER 
			<< (unsigned)reply->REP << (unsigned)reply->ATYP);
		termClient(redirectClient);
	}
	else {
		evbuffer_drain(evbuf, lengthOfInput);
		redirectClient->setState(RedirectClient::eSocks5Connected);

		bufferevent *bevRequest = redirectClient->getRequestBufferEvent();
		bufferevent_enable(bevRequest, EV_READ | EV_WRITE);
	}
}


// -------------- socks5 --------------
 
 
 

// ++++++++++++++ request ++++++++++++++
void RedirectListener::onBufferEventRead(RedirectClient *redirectClient)
{
	bufferevent *bevSocks5 = redirectClient->getSocks5BufferEvent();
	bufferevent *bevRequest = redirectClient->getRequestBufferEvent();
	if (bevSocks5 != NULL)
		bufferevent_write_buffer(bevSocks5, bufferevent_get_input(bevRequest));
}

void RedirectListener::onBufferEventWrite(RedirectClient *redirectClient)
{
	if (redirectClient->getSocks5BufferEvent() == NULL)
		termClient(redirectClient);
}

void RedirectListener::onBufferEventEvent(RedirectClient *redirectClient, short what)
{
	LOG_INFO(redirectClient << __FUNCTION__ << what);

	redirectClient->closeRequestBufferEvent(false);

	bufferevent *bevSocks5 = redirectClient->getSocks5BufferEvent();
	if (bevSocks5 == NULL || evbuffer_get_length(bufferevent_get_output(bevSocks5)) == 0)
		termClient(redirectClient);
}
// -------------- request --------------
 
void RedirectListener::termClient(RedirectClient *redirectClient)
{
	LOG_INFO(redirectClient << __FUNCTION__);
	clients_.remove(redirectClient);
	_s_client_pool_.destroy(redirectClient);
}

