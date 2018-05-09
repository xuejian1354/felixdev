#ifndef ___OBJECT_POOL____
#define ___OBJECT_POOL____

#include <new>
#include "LinkedList.h"

template<typename T,typename LinkT=T>
class ObjectPool
{   
public:
	ObjectPool(void) :
		mem_(NULL)  {
	}

	ObjectPool(unsigned cnt) :
		mem_(NULL)  {
		mmalloc(cnt);
	}

	~ObjectPool(void) {
		mmfree();
	} 

	unsigned count(void) const {
		return count_;
	}

	void mmalloc(unsigned cnt) {
		assert(mem_ == NULL);
		count_ = cnt;
		mem_ = (T*)malloc(count_ * sizeof(T));
		for (unsigned i = 0; i < count_; ++i)
			pool_.addTail(&mem_[i]);
	}

	void mmfree(void) {
		if (mem_ != NULL) {
			//assert(pool_.count() == count_);
			::free(mem_);
			mem_ = NULL;
			pool_.clear();
		}
	}
#if __cplusplus >= 201103L
	template <typename ...Args>
	T* create(Args... args) {
		T *obj = alloc();
		return obj == NULL ? NULL : ::new(obj) T(args...);
	}
#else
#define OBJECT_POOL_CREATE T *obj = alloc(); return obj == NULL ? NULL : ::new(obj) T

	T* create(void) { OBJECT_POOL_CREATE(); }
	template <typename A1> T* create(A1 a1)
	{ OBJECT_POOL_CREATE(a1); }
	template <typename A1,typename A2> T* create(A1 a1,A2 a2)
	{ OBJECT_POOL_CREATE(a1,a2); }
	template <typename A1, typename A2,typename A3> T* create(A1 a1, A2 a2,A3 a3)
	{ OBJECT_POOL_CREATE(a1, a2,a3); }
	template <typename A1, typename A2,typename A3,typename A4> T* create(A1 a1, A2 a2,A3 a3,A4 a4)
	{ OBJECT_POOL_CREATE(a1, a2,a3,a4); }
	template <typename A1, typename A2,typename A3,typename A4, typename A5> T* create(A1 a1, A2 a2,A3 a3,A4 a4,A5 a5)
	{ OBJECT_POOL_CREATE(a1, a2,a3,a4,a5); }
	template <typename A1, typename A2,typename A3,typename A4, typename A5, typename A6> T* create(A1 a1, A2 a2,A3 a3,A4 a4,A5 a5,A6 a6)
	{ OBJECT_POOL_CREATE(a1, a2,a3,a4,a5,a6); }
	template <typename A1, typename A2,typename A3,typename A4, typename A5, typename A6,typename A7> T* create(A1 a1, A2 a2,A3 a3,A4 a4,A5 a5,A6 a6,A7 a7)
	{ OBJECT_POOL_CREATE(a1, a2,a3,a4,a5,a6,a7); }
	template <typename A1, typename A2,typename A3,typename A4, typename A5, typename A6,typename A7, typename A8> T* create(A1 a1, A2 a2,A3 a3,A4 a4,A5 a5,A6 a6,A7 a7,A8 a8)
	{ OBJECT_POOL_CREATE(a1, a2,a3,a4,a5,a6,a7,a8); }

#undef OBJECT_POOL_CREATE

#endif

	void destroy(T *t)  {
		t->~T();
		dealloc(t);
	}
	 

private: 	
	T* alloc(void) {
		T *t = (T*)pool_.popHead(); 
		if (t == NULL) {
			LOG_DEBUG(count_);
			t = (T*)::malloc(sizeof(T));
		}
		assert(t != NULL);
		return t;
	}

	void dealloc(T *t) {
		if (t >= mem_ && t < mem_ + count_)
			pool_.addHead((LinkT*)t);
		else 
			::free(t);
	} 

	T *mem_;
	unsigned count_;
	LinkedList<LinkT> pool_;
};


#endif
