#include "com_ymbl_smartgateway_extension_RedirectToSocks5Service.h"
#include "RedirectToSocks5Service.h"
#include <stdlib.h>
#include <cstring>

class JStrVal
{
public:
	JStrVal(JNIEnv *env,jstring jstr) {
		env_ = env;
		jstr_ = jstr;
		val_ = jstr==0 ? 0 : env_->GetStringUTFChars(jstr,0);
	}

	~JStrVal(void) {
		if (jstr_ != 0)
			env_->ReleaseStringUTFChars(jstr_,val_);
	}

	const char* get(void) const {
		return val_;
	}

private:
	JNIEnv *env_;
	jstring jstr_;
	const char *val_;
};

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_RedirectToSocks5Service_start
  (JNIEnv *jenv, jobject jobj, jint redirectPort, jstring proxyHost, jstring auth)
{
	char *proxyAddr;
	int proxyPort = 1080;
	char *user = NULL;
	char *pass = NULL;

	JStrVal jProxyHost(jenv, proxyHost);
	JStrVal jAuth(jenv, auth);

	proxyAddr = strtok((char *)jProxyHost.get(), ":");
	char *pstr = strtok(NULL, ":");
	if(pstr != NULL)
	{
		proxyPort = atoi(pstr);
	}

	if(auth != NULL)
	{
		user = strtok((char *)jAuth.get(), ":");
		pass = strtok(NULL, ":");
	}

	startAsyncRedirectToSocks5Service("0.0.0.0", (unsigned short)redirectPort,
		proxyAddr, proxyPort, user, pass);
}

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_RedirectToSocks5Service_stop
  (JNIEnv *jenv, jobject jobj)
{
	endAsyncRedirectToSocks5Service();
}
