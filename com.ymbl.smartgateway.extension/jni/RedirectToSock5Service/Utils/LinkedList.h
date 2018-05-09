#ifndef __LINKED_LIST__
#define __LINKED_LIST__


template<typename T>
class LinkedObject
{
public:
	LinkedObject(void)
		: prev_(NULL), next_(NULL) {
	}

	T* getPrev(void) {
		return prev_;
	}
	void setPrev(T *prev) {
		prev_ = prev;
	}

	T* getNext(void) {
		return next_;
	}

	void setNext(T *next) {
		next_ = next;
	}

private:
	T * prev_, *next_;
};


template<typename T>
class LinkedList
{
public:
	LinkedList(void) {
		clear();
	}

	bool empty(void) const {
		return head_ == NULL;
	}

	void clear(void) {
		count_ = 0;
		head_ = tail_ = NULL;
	}

	int count(void) const {
		return count_;
	}

	T* getHead(void) {
		return head_;
	}

	T* getTail(void) {
		return tail_;
	}


	void addHead(T *t) {
		t->setPrev(NULL);
		t->setNext(head_);

		if (head_ == NULL) {
			head_ = tail_ = t;
		}
		else {
			head_->setPrev(t);
			head_ = t;
		}
		++count_;
	}

	void addTail(T *t) {
		t->setPrev(tail_);
		t->setNext(NULL);

		if (head_ == NULL) {
			head_ = tail_ = t;
		}
		else {
			tail_->setNext(t);
			tail_ = t;
		}	
		++count_;
	}

	void insertBefore(T *t, T *ref) {
		if (ref == head_) {
			addHead(t);
		}
		else {
			T *prev = ref->getPrev();
			t->setNext(ref);
			t->setPrev(prev);
			prev->setNext(t);
			ref->setPrev(t);	
			++count_;
		} 
	}

	void insertAfter(T *t, T *ref) {
		if (ref == tail_) {
			addTail(t);
		}
		else {
			T *next = ref->getNext();
			t->setNext(next);
			t->setPrev(ref);
			next->setPrev(t);
			ref->setNext(t);
			++count_;
		}
	}

	T* popHead(void) {
		if (head_ == NULL)
			return NULL;

		T  *h = head_;
		if (head_ == tail_) {
			head_ = tail_ = NULL;
		}
		else {
			head_ = head_->getNext();
			head_->setPrev(NULL);
		}
		--count_;
		return h;
 	}

	T* popTail(void) {
		if (tail_ == NULL)
			return NULL;

		T  *t = tail_;
		if (head_ == tail_) {
			head_ = tail_ = NULL;
		}
		else {
			tail_ = tail_->getPrev();
			tail_->setNext(NULL);
		}	
		--count_;
		return t;
	}


	void remove(T *t) {
		if (t == head_ && t == tail_) {
			head_ = tail_ = NULL;
		}
		else if (head_ == t) {
			head_ = head_->getNext();
			head_->setPrev(NULL);
		}
		else if (tail_ == t) {
			tail_ = tail_->getPrev();
			tail_->setNext(NULL);
		}
		else {
			T *prev = t->getPrev();
			T *next = t->getNext();
			next->setPrev(prev);
			prev->setNext(next);
		}
		--count_;
	}

	void moveToHead(T *t) {
		if (t != head_) {
			remove(t);
			addHead(t);
		}
	}

	void moveToTail(T *t) {
		if (t != tail_) {
			remove(t);
			addTail(t);
		}
	}

	void moveBefore(T *t, T *ref) {
		if (t->getNext() != ref) {
			remove(t);
			insertBefore(t, ref);
		}
	}

	void moveAfter(T *t, T *ref) {
		if (t->getPrev() != ref) {
			remove(t);
			insertAfter(t, ref);
		}
	}

private:
	int count_;
	T * head_, *tail_;
};

#endif
