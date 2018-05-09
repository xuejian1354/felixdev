#ifndef __EVENT_DEFER__
#define __EVENT_DEFER__

class EventDefer
{
public:
	typedef void (*Func)(void *);

public:
	int init(event_base *evbase);
	void uninit(void);

	int call(Func func,void *arg);
	
	template <typename F> 
	int call(F f, void *arg) 
		{ return call((Func)f,arg); }

private:
	void onCallComes(void);

	int fdread_;
	int fdwrite_;
	event *event_;
};





#endif
