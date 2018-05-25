#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <net/if.h>
#include <linux/sockios.h>
#include <netinet/if_ether.h>
#include <jni.h>
#include "dlog.h"

static char* jTostring(JNIEnv*, jstring);

JNIEXPORT jbyteArray JNICALL Java_com_ymbl_smartgateway_extension_Lua_getMacAddr
  (JNIEnv *env, jobject obj, jstring macdev)
{
	struct ifreq ifreq;
	int sock;

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
			return NULL;
	}

	char *mstr = jTostring(env, macdev);
	AT_PRINTF("[JNI] macdev: %s\n", mstr);

	strcpy(ifreq.ifr_name, mstr);
	free(mstr);

	if (ioctl(sock, SIOCGIFHWADDR, &ifreq) < 0) {
			return NULL;
	}

	jbyteArray bytes = (*env)->NewByteArray(env, 6);
	if (bytes != NULL) {
		(*env)->SetByteArrayRegion(env, bytes, 0, 6, (jbyte*)ifreq.ifr_hwaddr.sa_data);
	}

	return bytes;
}

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_Lua_exec
  (JNIEnv *jenv, jobject jobj, jstring cmd)
{
	execl("/tmp/transite-target/bin/lua", "lua", "/tmp/transite-target/etc/exec.lua", cmd, NULL);
}

char* jTostring(JNIEnv* env, jstring jstr)
{
	char* rtn = NULL;
	jclass clsstring = (*env)->FindClass(env, "java/lang/String");
	jstring strencode = (*env)->NewStringUTF(env, "utf-8");
	jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode);
	jsize alen = (*env)->GetArrayLength(env, barr);
	jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
	if (alen > 0)
	{
		rtn = (char*)malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, barr, ba, 0);
	return rtn;
}
