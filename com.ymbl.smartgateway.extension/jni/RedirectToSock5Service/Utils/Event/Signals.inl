#ifndef __SIGNALS_INL__
#define __SIGNALS_INL__

#include <signal.h>

static void registerSignals(void)
{
	extern event_base *__g_evbase__;
	
#ifndef _WIN32
	signal(SIGCHLD, SIG_IGN);
	signal(SIGPIPE, SIG_IGN);
#endif

	struct H {
		static void cbSignal(evutil_socket_t fd, short ev, void *arg) {
			LOG_INFO("EXIT......" << event_base_loopbreak(__g_evbase__));
			event_free((event*)arg);
		}
	};
	event *evSignal = evsignal_new(__g_evbase__, 0, NULL, NULL);
	evsignal_assign(evSignal, __g_evbase__, SIGINT, H::cbSignal, evSignal);
	event_add(evSignal, NULL);
}

#endif

