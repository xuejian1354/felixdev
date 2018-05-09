#include "TinyLog.h"
#include <time.h>
#include <sys/timeb.h>
#include <assert.h>
#include <string.h> 
#include <stdlib.h>

TLog ___LOG___;

TLog::TLog(void)
{
	memset(this,0,sizeof(*this));
	initWithFile(stdout);
}
 

TLog::~TLog(void)
{
	if (cache_ != NULL)
		flushCache();

	if (filePrefix_ != NULL) {
		free(filePrefix_);
		filePrefix_ = NULL;

		if (file_ != NULL) {
			fclose(file_);
			file_ = NULL;
		}

		if (cache_ != NULL) {
			free(cache_);
			cache_ = NULL;
		}
	}
}

int TLog::initWithFile(FILE *file)
{ 
	file_ = file;
	return 0;
}

int TLog::initWithAuto(const char *filePrefix, 
	unsigned maxFileSize, unsigned cacheSize)
{ 
	filePrefix_ = strdup(filePrefix);
	maxFileSize_ = maxFileSize;
	if (cacheSize != 0) {
		cacheSize_ = cacheSize;
		cache_ = (char*)malloc(cacheSize + MaxNativeTypeChars);
	}
	makeNewFile();
	return 0;
}

int TLog::makeNewFile(void)
{
	assert(filePrefix_ != NULL);

	if (file_ != NULL) {
		flushCache();
		fclose(file_);
	}

	char fileName[260];
	struct timeb tb;
	ftime(&tb);
	tm* t = localtime(&tb.time);
	sprintf(fileName, "%s_%02d-%02d-%02d_%02d_%02d_%02d.log",
		filePrefix_, t->tm_year % 100, t->tm_mon + 1,
		t->tm_mday, t->tm_hour, t->tm_min, t->tm_sec);

	file_ = fopen(fileName, "wb");
	if (file_ == NULL)
		return -1;
	
	fileSize_ = 0;
	return 0;
}
 

unsigned TLog::flushCache(void)
{
	if (cache_ == NULL)
		return 0;

	unsigned cached = cached_;
	fwrite(cache_, 1, cached_, (FILE*)file_);
	cached_ = 0;
	return cached;
}

int TLog::addFileSize(unsigned n)
{
	if (maxFileSize_ == 0)
		return 0;
	
	fileSize_ += n;
	if (fileSize_ < maxFileSize_)
		return 0;
	
	makeNewFile();
	return 1;
}

TLog& TLog::newln(bool autoSpace)
{
	char s[256];
	struct timeb tb;
	ftime(&tb);
	tm* t = localtime(&tb.time);
	int n = sprintf(s, (autoSpace_ = autoSpace) ? 
		"%02d:%02d:%02d:%03d" :"%02d:%02d:%02d:%03d ",
		t->tm_hour, t->tm_min, t->tm_sec,tb.millitm);
	return out(s, n);
}

TLog& TLog::endln(void)
{
	return *this << '\n';
}


TLog& TLog::out(const void *v, unsigned vlen)
{ 
	if (cache_ == NULL) {
		if (autoSpace_)
			addFileSize(fwrite(" ", 1, 1, file_));
		addFileSize(fwrite(v, 1, vlen, file_));
	}
	else {
		if (cached_ + vlen > cacheSize_ + MaxNativeTypeChars) {
			addFileSize(flushCache());
			if (autoSpace_)
				addFileSize(fwrite(" ", 1, 1, file_));
			addFileSize(fwrite(v, 1, vlen, file_));
		}
		else {
			if (autoSpace_)
				cache_[cached_++] = ' ';
			if (vlen == 1)
				cache_[cached_] = *(char*)v;
			else
				memcpy(cache_ + cached_, v, vlen);
			cached_ += vlen;
			if (cached_ >= cacheSize_) {
				addFileSize(flushCache());
			}
		}
	}
	return *this;
}

static const char *__s_hexChars__ = "0123456789ABCDEF";
TLog& TLog::hex(const void *v, unsigned vlen)
{
	char buf[1024 * 72];
	unsigned char *input = (unsigned char*)v;
	  
	for (unsigned i=0,j=0,vlast=vlen-1; i <= vlast; ++i) {
		unsigned char b = input[i];
		buf[j++] = __s_hexChars__[b >> 4];
		buf[j++] = __s_hexChars__[b & 0xF];
		buf[j++] = ' ';
		if (j == sizeof(buf) || i == vlast) {
			out(buf, j);
			j = 0;
		}
	} 
	return *this;
}


TLog& TLog::vhex(const void *v, unsigned vlen)
{
	char buf[1024];
	unsigned char *input = (unsigned char*)v;

	unsigned j = 0;
	buf[j++] = '0';
	buf[j++] = 'x';
	for (int i = vlen-1; i >=0; --i) {
		unsigned char b = input[i];
		buf[j++] = __s_hexChars__[b >> 4];
		buf[j++] = __s_hexChars__[b & 0xF];
	}
	return out(buf, j);
}

struct PrintF
{
	static const char *fmt(short v,bool withSpace) { return withSpace ? " %d" : "%d"; }
	static const char *fmt(unsigned short v,bool withSpace) { return withSpace ? " %u" : "%u"; }
	static const char *fmt(int v, bool withSpace) { return withSpace ? " %d" : "%d"; }
	static const char *fmt(unsigned int v, bool withSpace) { return withSpace ? " %u" : "%u"; }
	static const char *fmt(long v, bool withSpace) { return withSpace ? " %ld" : "%ld"; }
	static const char *fmt(unsigned long v, bool withSpace) { return withSpace ? " %lu" : "%lu"; }
	static const char *fmt(long long v, bool withSpace) { return withSpace ? " %lld" : "%lld"; }
	static const char *fmt(unsigned long long v, bool withSpace) { return withSpace ? " %llu" : "%llu"; }
	static const char *fmt(float v, bool withSpace) { return withSpace ? " %f" : "%f"; }
	static const char *fmt(double v, bool withSpace) { return withSpace ? " %f" : "%f"; }
	static const char *fmt(const void *v,bool withSpace) { return withSpace ? " %p" : "%p"; }
};

template<typename V>
inline TLog& TLog::o(V v)
{
	if (cache_ == NULL) {
		char vstr[MaxNativeTypeChars];
		int n = sprintf(vstr,PrintF::fmt(v,autoSpace_), v);
		addFileSize(fwrite(vstr, 1, n, file_));
	}
	else {
		int n = sprintf(cache_ + cached_, PrintF::fmt(v,autoSpace_), v);
		cached_ += n;
		if (cached_ >= cacheSize_)
			addFileSize(flushCache());
	}
	return *this;
}
 
TLog& TLog::operator << (bool v) { return o((int)v); }
TLog& TLog::operator << (char v) { return out(&v,1); }
TLog& TLog::operator << (unsigned char v) { return out(&v,1); }
TLog& TLog::operator << (short v) { return o(v); }
TLog& TLog::operator << (unsigned short v) { return o(v); }
TLog& TLog::operator << (int v) { return o(v); }
TLog& TLog::operator << (unsigned int v) { return o(v); }
TLog& TLog::operator << (long v) { return o(v); }
TLog& TLog::operator << (unsigned long v) { return o(v); }
TLog& TLog::operator << (long long v) { return o(v); }
TLog& TLog::operator << (unsigned long long v) { return o(v); }
TLog& TLog::operator << (float v) { return o(v); }
TLog& TLog::operator << (double v) { return o(v); }
TLog& TLog::operator << (const char *v) { if (!v) v = "nil"; return out(v, strlen(v)); }
TLog& TLog::operator << (const void *v) { return o(v); }
 
TLog& TLog::operator << (PR pr) { return out(pr.v_, pr.l_); }
TLog& TLog::operator << (HX hx) { return hx.buffer_ ? hex(hx.v_, hx.l_) : vhex(hx.v_, hx.l_); }
