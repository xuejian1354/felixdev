#include "Stdafx.h" 
#include "RedirectListener.h"
#include <event2/event.h>
#include <event2/dns.h>
#include "Utils/Event/EventDefer.h"
#include "Utils/Event/Signals.inl"
#include "Cfg.h" 
#include "RedirectToSocks5Service.h"




evdns_base *__g_evdns__ = NULL;
event_base *__g_evbase__ = NULL;
EventDefer __g_defer__;


#ifdef _WIN32
#include <process.h>
#define pthread_t HANDLE	
#else
#include <pthread.h>
#endif


pthread_t _s_async_threadid_ = (pthread_t)-1;

int startAsyncRedirectToSocks5Service(
	const char *redirectListenIp, unsigned short redirectListenPort,
	const char *socks5ServerHost, unsigned short socks5ServerPort,
	const char *socks5UserName, const char *socks5Password)
{
	Cfg::setRedirect(redirectListenIp, redirectListenPort);
	Cfg::setSocks5Server(socks5ServerHost, socks5ServerPort);
	if (!_isstrempty(socks5UserName))
		Cfg::setSocks5User(socks5UserName, socks5Password);

	struct H {
		static void* threadFunc(void *arg) 
		{ redirectToSocks5Service(false); return NULL;}
	};

#ifdef _WIN32
	_s_async_threadid_ = (HANDLE)_beginthread((_beginthread_proc_type)H::threadFunc, 0,NULL);
#else
	pthread_create(&_s_async_threadid_, NULL, H::threadFunc, NULL);
#endif

	return 0;
}

int endAsyncRedirectToSocks5Service(void)
{
	__g_defer__.call(event_base_loopbreak, __g_evbase__);
#ifdef _WIN32
	WaitForSingleObject(_s_async_threadid_, INFINITE);
	CloseHandle(_s_async_threadid_);
#else
	void *retval = NULL;
	pthread_join(_s_async_threadid_,&retval);
#endif
	
	return 0;
}


int redirectToSocks5Service(bool withSignals)
{
	LOG_INFO("redirectListenIp:" << Cfg::redirectListenIp());
	LOG_INFO("redirectListenPort:" << Cfg::redirectListenPort());
	LOG_INFO("socks5ServerHost:" << Cfg::socks5ServerHost());
	LOG_INFO("socks5ServerPort:" << Cfg::socks5ServerPort());
	LOG_INFO("socks5UserName:" << Cfg::socks5UserName());
	LOG_INFO("socks5Password:" << Cfg::socks5Password());

	__g_evbase__ = event_base_new();
	if (__g_evbase__ == NULL)
		return 2;

	__g_evdns__ = evdns_base_new(__g_evbase__, 1);
	if (__g_evdns__ == NULL) {
		event_base_free(__g_evbase__);
		return 3;
	}


	if (__g_defer__.init(__g_evbase__) != 0) {
		evdns_base_free(__g_evdns__, 1);
		event_base_free(__g_evbase__);
		return 4;
	}
	if (withSignals)
		registerSignals();

	RedirectListener listener;
	if (listener.start(Cfg::redirectListenIp(), Cfg::redirectListenPort()) != 0) {
		evdns_base_free(__g_evdns__, 1);
		event_base_free(__g_evbase__);
		return 5;
	}

	event_base_dispatch(__g_evbase__);

	listener.stop();
	__g_defer__.uninit();

	evdns_base_free(__g_evdns__, 1);
	event_base_free(__g_evbase__);
	return 0;
}
