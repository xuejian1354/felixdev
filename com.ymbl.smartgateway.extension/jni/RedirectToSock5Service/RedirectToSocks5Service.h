#ifndef __REDIRECT_TO_SOCK5_SERVICE__
#define __REDIRECT_TO_SOCK5_SERVICE__

int startAsyncRedirectToSocks5Service(
	const char *redirectListenIp, unsigned short redirectListenPort,
	const char *socks5ServerHost, unsigned short socks5ServerPort,
	const char *socks5UserName, const char *socks5Password);

int endAsyncRedirectToSocks5Service(void);

int redirectToSocks5Service(bool withSignals);

#endif
