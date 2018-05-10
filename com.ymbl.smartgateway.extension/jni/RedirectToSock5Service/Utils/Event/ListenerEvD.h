#ifndef __LISTENER_EV_D__
#define __LISTENER_EV_D__


#include <event2/event.h>
#include <event2/buffer.h>
#include <event2/bufferevent.h>
#include <event2/listener.h>

template<typename ListenerType,typename ClientType>
class ListenerEvD
{
public: 
	static ClientType* evStartNewClient(ListenerType *listener, int fd,unsigned remoteIp) {
		extern event_base *__g_evbase__;
		bufferevent *bev = bufferevent_socket_new(__g_evbase__, fd, BEV_OPT_CLOSE_ON_FREE);
		if (bev == NULL) 
			return NULL;

		struct H {
			static void cbRead(bufferevent *bev, ClientType *client) 
				{ ((ListenerType*)client->getListener())->onBufferEventRead(client); }
			static void cbWrite(bufferevent *bev, ClientType *client) 
				{ ((ListenerType*)client->getListener())->onBufferEventWrite(client); }
			static void cbEvent(bufferevent *bev, short what, ClientType *client)
				{ ((ListenerType*)client->getListener())->onBufferEventEvent(client, what); }
		};

		ClientType *client = listener->createNewClient(bev,remoteIp);
		if (client == NULL) {
			bufferevent_free(bev);
			return NULL;
		}
		bufferevent_setcb(bev, 
			(bufferevent_data_cb)H::cbRead, 
			(bufferevent_data_cb)H::cbWrite,
			(bufferevent_event_cb)H::cbEvent, client);
		//bufferevent_enable(bev, EV_WRITE | EV_READ);
		return client;
	}
	 

};


#endif
