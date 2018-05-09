#ifndef __REDIRECT_CLIENT__
#define __REDIRECT_CLIENT__


#include "Utils/LinkedList.h"
 
class RedirectListener;
struct bufferevent; 

class RedirectClient : public LinkedObject<RedirectClient>
{
public:
	enum { eStateInit, eConnectToSocks5Server, eSocks5MethodReq, eSocks5Auth, eSocks5Connect, eSocks5Connected };

	RedirectClient(RedirectListener* listener, bufferevent *bevReqeust) {
		bevRequest_ = bevReqeust;
		listener_ = listener;
		bevSocks5_ = NULL;
		state_ = eStateInit;
	}
	~RedirectClient(void) {
		closeRequestBufferEvent(true);
		closeSocks5BufferEvent(true);
	}

	RedirectListener* getListener(void) {
		return listener_;
	}

	bool isSocks5Connected(void) const {
		return state_ == eSocks5Connected;
	}

	unsigned char getState(void) const {
		return state_;
	}

	void setState(unsigned char state) {
		state_ = state;
	}


	unsigned getDstIp(void) const {
		return dstIp_;
	}

	unsigned short getDstPort(void) const {
		return dstPort_;
	}

	void setDst(unsigned ip, unsigned short port) {
		dstIp_ = ip, dstPort_ = port;
	}

	bufferevent* getRequestBufferEvent(void) {
		return bevRequest_;
	}

	void closeRequestBufferEvent(bool withShutdown) {
		freeBufferEvent(bevRequest_, withShutdown);
	}

	bufferevent* getSocks5BufferEvent(void) {
		return bevSocks5_;
	}

	void setSocks5BufferEvent(bufferevent *bevSocks5) {
		bevSocks5_ = bevSocks5;
	}

	void closeSocks5BufferEvent(bool withShutdown) {
		freeBufferEvent(bevSocks5_, withShutdown);
	}

private:
	void freeBufferEvent(bufferevent *&bev, bool withShutdown) {
		if (bev != NULL) {
			if (withShutdown) {
				shutdown(bufferevent_getfd(bev), SHUT_RDWR);
				bufferevent_free(bev);
				bev = NULL;
			}
		}
	}

	unsigned char state_;

	unsigned short dstPort_;
	
	unsigned dstIp_;

	bufferevent *bevSocks5_;
	bufferevent *bevRequest_;
	RedirectListener *listener_;
	 
};

#endif
