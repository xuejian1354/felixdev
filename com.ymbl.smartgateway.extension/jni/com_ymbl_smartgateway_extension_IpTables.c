#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <iptables.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <net/if.h>
#include <linux/sockios.h>
#include <netinet/if_ether.h>
#include "dlog.h"
#include "com_ymbl_smartgateway_extension_IpTables.h"

extern int iptables_main(int, char **);

static char* jTostring(JNIEnv*, jstring);
//static jstring stoJstring(JNIEnv*, const char*);


JNIEXPORT jbyteArray JNICALL Java_com_ymbl_smartgateway_extension_IpTables_getMacAddr
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

JNIEXPORT jint JNICALL Java_com_ymbl_smartgateway_extension_IpTables_rule
  (JNIEnv *env, jobject obj, jstring rule)
{
	int ret = -1;
	char *jrule = jTostring(env, rule);

	char *cmdargs[64];
	char *cmdlen = 0;

	int i=0;
	int j = 0;
	cmdargs[j++] = jrule;
	int rlen = strlen(jrule);
	for(i=0; i<rlen; i++)
	{
		if(jrule[i] == ' ')
		{
			jrule[i] = '\0';
			cmdargs[j++] = jrule + i + 1;
		}
	}

	if(strcmp(cmdargs[0], "iptables"))
	{
		AO_PRINTF("[JNI] Error %s, The command must be iptables\n", cmdargs[0]);
		return (jint)ret;
	}

	for(i=0; i<j; i++)
	{
		AO_PRINTF("%s ", cmdargs[i]);
	}
	AO_PRINTF("\n");

	ret = iptables_main(j, cmdargs);	
	free(jrule);
	return (jint)ret;
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

/*jstring stoJstring(JNIEnv* env, const char* pat)
{
	jclass strClass = (*env)->FindClass(env, "Ljava/lang/String;");
	jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
	jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
	(*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte*)pat);
	jstring encoding = (*env)->NewStringUTF(env, "utf-8");
	return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}*/
