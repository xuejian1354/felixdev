#ifndef __TINY_LOG__
#define __TINY_LOG__
 
#include <stdio.h>
class TLog
{
public:
	enum { MaxNativeTypeChars = 128 };

public:
	TLog(void);
	~TLog(void);

	int initWithFile(FILE *file);
	int initWithAuto(const char *logFileNamePrefix, 
		unsigned singleLogFileMaxSize=0, 
		unsigned memoryCacheForWriting= 128 * 1024);

	unsigned flushCache(void);

	TLog& operator << (bool v);
	TLog& operator << (char v);
	TLog& operator << (unsigned char v);
	TLog& operator << (short v);
	TLog& operator << (unsigned short v);
	TLog& operator << (int v);
	TLog& operator << (unsigned int v);
	TLog& operator << (long v);
	TLog& operator << (unsigned long v);
	TLog& operator << (long long v);
	TLog& operator << (unsigned long long v);
	TLog& operator << (float v);
	TLog& operator << (double v);
	TLog& operator << (const char *v);
	TLog& operator << (const void *v);

	struct PR {
		PR(const void *v, unsigned l) : v_(v),l_(l) {}
		const void *v_; unsigned l_;
	};
	struct HX {
		template<typename T> HX(const T &v) : v_(&v),l_(sizeof(v)), buffer_(false) {}
		HX(const void *v, unsigned l) : v_(v), l_(l), buffer_(true) {}
		const void *v_; unsigned l_; bool buffer_;
	};

	TLog& operator << (PR pr);
	TLog& operator << (HX pr);

	TLog& newln(bool autSpace);
	TLog& endln(void);
	
	TLog& out(const void *v, unsigned vlen);
	TLog& hex(const void *v, unsigned vlen);

private:
	template<typename V> TLog& o(V v); 

	TLog& vhex(const void *v, unsigned vlen);

	int makeNewFile(void);
	int addFileSize(unsigned n);
	 
	bool autoSpace_;

	FILE *file_;
	unsigned fileSize_;

	char *filePrefix_;
	unsigned maxFileSize_;

	char *cache_;
	unsigned cacheSize_;
	unsigned cached_;
};


//////////////////////////////////////////////////////////////////////////

extern TLog ___LOG___;


#define LOG() ___LOG___


#define LOG_FILE_INT LOG().initWithFile

#define LOG_AUTO_INIT LOG().initWithAuto

#define LOG_INFO(x) (LOG().newln(true) << x).endln()

#define LOG_DEBUG(x) (LOG().newln(true) << __FILE__ \
		<< __LINE__  << __FUNCTION__ \
		<< ':'  << x).endln()
 

#endif

