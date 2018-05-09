#ifndef __CFG__
#define __CFG__

class Cfg
{
public:
	static const char* redirectListenIp(void);
	static unsigned redirectListenPort(void);
	static void setRedirect(const char *listenIp, unsigned listenPort);

	static const char* socks5ServerHost(void);
	static unsigned socks5ServerPort(void);
	static void setSocks5Server(const char *host, unsigned port);

	static const char* socks5UserName(void);
	static const char* socks5Password(void);
	static void setSocks5User(const char *userName, const char *password);

 
};

#endif

