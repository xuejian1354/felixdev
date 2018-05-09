#include <stdlib.h>
#include <event2/util.h>
#include <event2/event.h>
#include "EventDefer.h"
#ifdef _WIN32
#include <afunix.h>
#endif
 
int EventDefer::init(event_base *evbase)
{
	int fds[2];
	if (evutil_socketpair(AF_UNIX, SOCK_STREAM, 0, fds) == -1)
		return -1;

	evutil_make_socket_nonblocking(fds[0]);
	evutil_make_socket_nonblocking(fds[1]);

	fdread_ = fds[0];
	fdwrite_ = fds[1];
	
	struct H {
		static void cb(evutil_socket_t sock, short which,void *arg) 
			{ ((EventDefer*)arg)->onCallComes(); }
	};

	event_ = event_new(evbase,fdread_,EV_READ | EV_PERSIST,&H::cb, this);
	event_add(event_, NULL);
	return 0;
}

void EventDefer::uninit(void)
{
	event_free(event_);
	evutil_closesocket(fdread_);
	evutil_closesocket(fdwrite_);
}

struct CallItem
{
	EventDefer::Func func;
	void *arg;
};

void EventDefer::onCallComes(void)
{
	CallItem ci;
	while (recv(fdread_, (char*)&ci, sizeof(ci), 0) == sizeof(CallItem)) {
		ci.func(ci.arg);
	}
}


int EventDefer::call(EventDefer::Func func, void *arg)
{
	if (func == NULL)
		return -1;

	CallItem ci = { func, arg };
	if (send(fdwrite_, (const char*)&ci, sizeof(CallItem), 0) <= 0)
		return -2;

	return 0;
}
