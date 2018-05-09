public class RedirectToSocks5Service
{
	static native void start(
		String redirectListenIp, int redirectListenPort,
		String socks5ServerHost, int socks5ServerPort,
		String socks5UserName, String socks5Password);
		
	static native void stop();

	static {
		System.loadLibrary("RedirectToSocks5Service");
	} 
}
