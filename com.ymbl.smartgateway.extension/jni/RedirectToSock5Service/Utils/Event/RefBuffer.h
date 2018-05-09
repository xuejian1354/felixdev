#ifndef __REF_BUFFER__
#define __REF_BUFFER__

/*++
	Usage:
		char data[] = "hello world";
		RefBuffer rb(sizeof(data));
		memcpy(rb.data(),data,rb.size());
		extern void useBuffer(const void *data,unsigned size,void (*cleanupFunc)(void *arg),void *arg);
		userBuffer(rb.data(),rb.size(),RefBuffer::unref,rb.ref());
  --*/
class RefBuffer
{
public:
	RefBuffer(unsigned size) {
		d_ = (D*)malloc(offsetof(D, data) + size);
		d_->ref = 1;
		size_ = size;
	}

	~RefBuffer(void) {
		unref(d_);
	}

	void* ref(void) {
		++d_->ref;
		return d_;
	}

	unsigned size(void) const {
		return size_;
	}

	char* data(void) {
		return d_->data;
	}

public:
	static void unref(void *ctx) {
		D *d = (D*)ctx;
		if (--d->ref == 0)
			free(d);
	}

private:
	struct D {
		unsigned ref;
		char data[1];
	} *d_;
	unsigned size_;
};

#endif
