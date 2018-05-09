#ifndef __STACK_BUFFER__
#define __STACK_BUFFER__

template<unsigned AutoSize = 128*1024>
class AutoBuffer
{
	enum { autocc = AutoSize};

public:
	AutoBuffer(void)
		: heap_(NULL), heapSize_(0) {
	}

	AutoBuffer(unsigned bytes) {
		heapSize_ = 0;
		alloc(bytes);
	}
	~AutoBuffer(void) {
		dealloc();
	}

	char* alloc(unsigned bytes) {
		if (heapSize_ >= bytes)
			return heap_;

		if (bytes <= autocc)
			return auto_;
		
		heap_ = (char*)realloc(heap_, bytes);
		heapSize_ = bytes;
		return heap_;
	}

	void dealloc(void) {
		if (heap_ != NULL) {
			free(heap_);
			heapSize_ = 0;
			heap_ = NULL;
		}

	}
	
	char* buf() {
		return heap_ == NULL ? auto_ : heap_;
	}

private:
	char *heap_;
	unsigned heapSize_;
	char auto_[autocc];
};

typedef AutoBuffer<> AutoBuf;

#endif