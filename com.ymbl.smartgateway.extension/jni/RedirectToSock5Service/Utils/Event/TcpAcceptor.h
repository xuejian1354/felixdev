#ifndef __TCP_ACCEPTOR__
#define __TCP_ACCEPTOR__
 
struct sockaddr_in; 
struct evconnlistener;
struct bufferevent;

class TcpAcceptor
{
public:
	TcpAcceptor(void);
	~TcpAcceptor(void);

	virtual int start(const char *listenIp, unsigned short listenPort);
	virtual void stop(void);
	 
	virtual void onNewConn(int fd, sockaddr_in *addr) = 0;

protected:
	evconnlistener *evListener_;
};



#endif
