#ifndef __SOCKET_UTILS__
#define __SOCKET_UTILS__

#ifdef _WIN32
#include <WinSock2.h>
#else
#include <netinet/in.h>
#endif 

#ifdef _WIN32
#define SHUT_RD SD_RECEIVE
#define SHUT_WR SD_SEND
#define SHUT_RDWR SD_BOTH
#endif

int setKeepAlive(int fd, unsigned keepIdle, unsigned keepInterval, unsigned keepCount);

const char* inetNtoa(unsigned ip);

struct SockAddrIn : public sockaddr_in {	
	SockAddrIn(void);
	SockAddrIn(int fd, bool peer);
	SockAddrIn(unsigned ip, unsigned short port);
	SockAddrIn(const char *ip, unsigned short port);

	unsigned ip(void) const { return (const unsigned&) this->sin_addr; }
	unsigned short port(void) const { return this->sin_port; }
	operator sockaddr*() { return (sockaddr*)this; }

};
 
#endif

