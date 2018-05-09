#include "Stdafx.h" 
#include "RedirectListener.h"
#include "RedirectToSocks5Service.h"
#include "Cfg.h" 




/*+++++++++++++++++++++++++
usage:
	XX redirectListenIp redirectListenPort socks5ServerHost socks5ServerPort socks5UserName socks5Password 
 --*/
int main(int argc, char *argv[])
{
	if (argc != 7 && argc != 5) {
		printf(
			"usage:\n"
			"\tXX redirectListenIp redirectListenPort socks5ServerHost socks5ServerPort socks5UserName socks5Password\n"
		);
		return 1;
	}

	Cfg::setRedirect(argv[1], (unsigned short)atoi(argv[2]));
	Cfg::setSocks5Server(argv[3], (unsigned short)atoi(argv[4]));
	if (argc == 7)
		Cfg::setSocks5User(argv[5], argv[6]);

	return redirectToSocks5Service(true);
}
