#include "TcpAcceptor.h" 

#ifndef _WIN32
#include <arpa/inet.h>
#endif

#include <event2/event.h>
#include <event2/buffer.h>
#include <event2/bufferevent.h>
#include <event2/listener.h>

#include "SocketUtils.h"
 

TcpAcceptor::TcpAcceptor(void)
	:evListener_(NULL)
{
}


TcpAcceptor::~TcpAcceptor(void)
{
	stop();
}

static void evAcceptCallback(evconnlistener *listener, int fd,
	sockaddr *addr, int addrLen, void *ctx)
{
	TcpAcceptor *pthis = (TcpAcceptor*)ctx;
	pthis->onNewConn(fd, (sockaddr_in*)addr);
}

int TcpAcceptor::start(const char *listenIp, unsigned short listenPort)
{
	extern event_base *__g_evbase__; 
	 
	evListener_ = evconnlistener_new_bind(__g_evbase__, evAcceptCallback, this,
		LEV_OPT_CLOSE_ON_FREE | LEV_OPT_REUSEABLE, -1, SockAddrIn(listenIp, listenPort), sizeof(sockaddr_in));
	return evListener_==NULL ? -1 : 0;
}

void TcpAcceptor::stop(void)
{
	if (evListener_ != NULL) {
		evconnlistener_free(evListener_);
		evListener_ = NULL;
	}
}

