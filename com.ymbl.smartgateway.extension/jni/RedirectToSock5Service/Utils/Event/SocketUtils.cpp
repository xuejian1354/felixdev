#include <string.h>
#include <event2/util.h>
#include "SocketUtils.h"

#ifdef _WIN32
#include <ws2ipdef.h>
#include <crtdbg.h>

struct WinSocketInitializer
{
	WinSocketInitializer(void) { WSAData wsa; ::WSAStartup(MAKEWORD(2, 2), &wsa); }
	~WinSocketInitializer(void) { ::WSACleanup(); _CrtDumpMemoryLeaks(); }
} winSocketInitializer__;

#else
#include <netinet/tcp.h> 
#include <arpa/inet.h>

#endif


int setKeepAlive(int fd, unsigned keepIdle, unsigned keepInterval, unsigned keepCount)
{
	unsigned val = 1;
	if (setsockopt(fd, SOL_SOCKET, SO_KEEPALIVE, (const char*)&val, sizeof(unsigned)) == -1)
		return -1;

	if (setsockopt(fd, IPPROTO_TCP, TCP_KEEPIDLE, (const char*)&keepIdle, sizeof(unsigned)) < 0)
		return -1;
	 
	if (setsockopt(fd, IPPROTO_TCP, TCP_KEEPINTVL, (const char*)&keepInterval, sizeof(unsigned)) < 0)
		return -1;

#ifndef _WIN32	 
	if (setsockopt(fd, IPPROTO_TCP, TCP_KEEPCNT, (const char*)&keepCount, sizeof(unsigned)) < 0)
		return -1;
#endif	
	return 0;
}

const char* inetNtoa(unsigned ip) 
{
	return inet_ntoa((in_addr&)ip);
}

static void setSockAddrIn(SockAddrIn &sa, unsigned ip, unsigned short port)
{
	memset(&sa, 0, sizeof(sa));
	sa.sin_family = AF_INET;
	sa.sin_port = htons(port);
	sa.sin_addr.s_addr = ip;

}

SockAddrIn::SockAddrIn(void)
{

}

SockAddrIn::SockAddrIn(unsigned ip, unsigned short port) 
{
	setSockAddrIn(*this,ip, port);
}

SockAddrIn::SockAddrIn(const char *ip, unsigned short port) 
{
	setSockAddrIn(*this, inet_addr(ip), port); 
}

SockAddrIn::SockAddrIn(int fd, bool peer)
{
	ev_socklen_t len = sizeof(*this);
	if ((peer ? getpeername : getsockname)(fd, *this, &len) != 0)
		memset(this, 0, sizeof(*this));
}

