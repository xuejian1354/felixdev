#include <jni.h>
#include <unistd.h>
#include "dlog.h"

void create_daemon()  
{
	pid_t pid;
	pid = fork();  

	if(pid == -1)
	{
		printf("fork error\n");
		exit(1);
	}
	else if(pid)
	{
		exit(0);
	}  

	if(-1 == setsid())
	{
		printf("setsid error\n");
		exit(1);
	}  

	pid = fork();
	if(pid == -1)
	{
		printf("fork error\n");
		exit(1);
	}
	else if(pid)
	{
		exit(0);
	}  

	chdir("/");
	/*int i;
	for(i = 0; i < 3; ++i)
	{
		close(i);
	}*/
	umask(0);
	return;  
}

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_XL2tpd_start
  (JNIEnv *jenv, jobject jobj)
{
	if(fork() > 0)
	{
		create_daemon();

		int ret = execl("/tmp/transite-target/bin/sudo", "sudo", "/tmp/transite-target/bin/xl2tpd", "-c", "/tmp/transite-target/etc/xl2tpd.conf", "-D", NULL);
		AT_PRINTF("[JNI] execl xl2tpd %d\n", ret);
		return;
	}
}

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_XL2tpd_route
  (JNIEnv *jenv, jobject jobj)
{
	
}
