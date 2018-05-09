#ifndef __REDIRECT_LISTENER__
#define __REDIRECT_LISTENER__
#include "Utils/LinkedList.h"
#include "Utils/Event/TcpAcceptor.h"

class RedirectClient;

class RedirectListener : public TcpAcceptor
{
public:
	RedirectListener(void);
	~RedirectListener(void);


	virtual void stop(void);
	 
	// -------------- acceptor --------------
	RedirectClient* createNewClient(bufferevent *bev, unsigned remoteIp);
	 
	// -------------- bufferevent --------------
	void onBufferEventRead(RedirectClient *redirectClient);
	void onBufferEventWrite(RedirectClient *redirectClient);
	void onBufferEventEvent(RedirectClient *redirectClient,short what);
	  

protected:
	void onSocks5Read(RedirectClient *redirectClient);
	void onSocks5Write(RedirectClient *redirectClient);
	void onSocks5Event(RedirectClient *redirectClient,short what);
 
	// -------------- acceptor --------------
	void onNewConn(int fd, sockaddr_in *addr); 
	 
private:
	int connectToSocks5Server(RedirectClient *client);
	void termClient(RedirectClient *redirectClient);

	void sendSocks5MethodPacket(RedirectClient *redirectClient);
	void onSocks5MethodReply(RedirectClient *redirectClient);


	void sendSocks5AuthPacket(RedirectClient *redirectClient);
	void onSocks5AuthReply(RedirectClient *redirectClient);

	void sendSock5ConnectPacket(RedirectClient *redirectClient);
	void onSocks5ConnectReply(RedirectClient *redirectClient);

	LinkedList<RedirectClient> clients_;
};

#endif
