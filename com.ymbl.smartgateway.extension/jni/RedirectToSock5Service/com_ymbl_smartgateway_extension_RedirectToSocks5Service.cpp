#include "com_ymbl_smartgateway_extension_RedirectToSocks5Service.h"
#include "RedirectToSocks5Service.h"
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
  (JNIEnv *jenv, jobject jobj, jint redirectPort,  jstring proxyIp,jint proxyPort, jstring auth)
{
	JStrVal jProxyIp(jenv, proxyIp);
	JStrVal jAuth(jenv, auth);
	char *user = strtok((char *)jAuth.get(), ":");
	char *pass = strtok(NULL, ":");

	startAsyncRedirectToSocks5Service("127.0.0.1", (unsigned short)redirectPort,
		jProxyIp.get(), (unsigned short)proxyPort, user, pass);
}

JNIEXPORT void JNICALL Java_com_ymbl_smartgateway_extension_RedirectToSocks5Service_stop
  (JNIEnv *jenv, jobject jobj)
{
	endAsyncRedirectToSocks5Service();
}
