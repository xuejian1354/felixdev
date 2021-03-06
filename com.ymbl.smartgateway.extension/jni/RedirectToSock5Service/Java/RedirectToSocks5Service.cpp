#include "RedirectToSocks5Service.h"
#include "../RedirectToSocks5Service.h"

// +++++++++++++ JStrVal ++++++++++++++++ 
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
// ------------ JStrVal ------------------


JNIEXPORT void JNICALL Java_RedirectToSocks5Service_start
  (JNIEnv *jenv, jclass jcls, jstring redirectListenIp, jint redirectListenPort, 
	jstring socks5ServerHost, jint socks5ServerPort,
	 jstring socks5UserName, jstring socks5Password)
{
	JStrVal jRedirectListenIp(jenv,redirectListenIp);
	JStrVal jSocksServerHost(jenv,socks5ServerHost);
	JStrVal jSocks5UserName(jenv,socks5UserName);
	JStrVal jSocks5Password(jenv,socks5Password);

	startAsyncRedirectToSocks5Service(
		jRedirectListenIp.get(),(unsigned short)redirectListenPort,
		jSocksServerHost.get(),(unsigned short)socks5ServerPort,
		jSocks5UserName.get(),jSocks5Password.get());
	
}


JNIEXPORT void JNICALL Java_RedirectToSocks5Service_stop
  (JNIEnv *, jclass)
{
	endAsyncRedirectToSocks5Service();
}

