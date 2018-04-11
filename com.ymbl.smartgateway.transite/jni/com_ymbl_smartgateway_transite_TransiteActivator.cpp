#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <net/if.h>
#include <linux/sockios.h>
#include <netinet/if_ether.h>
#include "com_ymbl_smartgateway_transite_TransiteActivator.h"

char* jTostring(JNIEnv*, jstring);
jstring stoJstring(JNIEnv*, const char*);

JNIEXPORT jstring JNICALL Java_com_ymbl_smartgateway_transite_TransiteActivator_getMacAddr
  (JNIEnv *env, jobject obj, jstring macdev)
{
	struct ifreq ifreq;
	int sock;

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
			return NULL;
	}

	char *mstr = jTostring(env, macdev);
	printf("[JNI] macdev: %s\n", mstr);
	FILE *fp = NULL;
	if((fp = fopen("/tmp/transtest", "a+")) != NULL) {
        fprintf(fp, "%s\n", "Hello, I'm transite test");
        fclose(fp);
    }
	strcpy(ifreq.ifr_name, mstr);
	free(mstr);

	if (ioctl(sock, SIOCGIFHWADDR, &ifreq) < 0) {
			return NULL;
	}

	char macaddr[64] = {0};
	sprintf(macaddr, "%02X%02X%02X%02X%02X%02X",
					(unsigned char) ifreq.ifr_hwaddr.sa_data[0],
					(unsigned char) ifreq.ifr_hwaddr.sa_data[1],
					(unsigned char) ifreq.ifr_hwaddr.sa_data[2],
					(unsigned char) ifreq.ifr_hwaddr.sa_data[3],
					(unsigned char) ifreq.ifr_hwaddr.sa_data[4],
					(unsigned char) ifreq.ifr_hwaddr.sa_data[5]);

	if (!strcmp(macaddr, "000000000000")) {
			return NULL;
	}

    return stoJstring(env, macaddr);
}

char* jTostring(JNIEnv* env, jstring jstr)
{
	char* rtn = NULL;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0)
	{
		rtn = (char*)malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	return rtn;
}

jstring stoJstring(JNIEnv* env, const char* pat)
{
	jclass strClass = env->FindClass("Ljava/lang/String;");
	jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
	jbyteArray bytes = env->NewByteArray(strlen(pat));
	env->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
	jstring encoding = env->NewStringUTF("utf-8");
	return (jstring)env->NewObject(strClass, ctorID, bytes, encoding);
}