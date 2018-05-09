#include "Stdafx.h"
#include "Cfg.h"

static char cfgRedirectListenIp[16] = { 0 };
static unsigned cfgRedirectListenPort = 0;

static char cfgSocks5ServerHost[128] = { 0 };
static unsigned cfgSocks5ServerPort = 0;
 
static char cfgSocks5UserName[128] = { '\0' };
static char cfgSocks5Password[128] = { '\0' };

  
const char* Cfg::redirectListenIp(void)
{
	return cfgRedirectListenIp;
}

unsigned Cfg::redirectListenPort(void)
{
	return cfgRedirectListenPort;
}

void Cfg::setRedirect(const char *listenIp, unsigned listenPort)
{
	strcpy(cfgRedirectListenIp, listenIp);
	cfgRedirectListenPort = listenPort;
}

const char* Cfg::socks5ServerHost(void)
{
	return cfgSocks5ServerHost;
}

unsigned Cfg::socks5ServerPort(void)
{
	return cfgSocks5ServerPort;
}


void Cfg::setSocks5Server(const char *host, unsigned port)
{
	strcpy(cfgSocks5ServerHost, host);
	cfgSocks5ServerPort = port;
}
 

const char* Cfg::socks5UserName(void)
{
	return cfgSocks5UserName;
}

const char* Cfg::socks5Password(void)
{
	return cfgSocks5Password;
}

void Cfg::setSocks5User(const char *userName, const char *password)
{
	strcpy(cfgSocks5UserName, userName);
	strcpy(cfgSocks5Password, password);
}