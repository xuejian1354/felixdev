#ifndef __INIT_DAEMON__
#define __INIT_DAEMON__


#ifdef _WIN32
#define initDaemon()
#else 

#include <unistd.h>  
#include <sys/types.h>
#include <sys/prctl.h>
#include <sys/wait.h>


void initDaemon(void)
{
	for (;;) {
		pid_t pid = fork();
		if (pid == -1)
			exit(1);

		if (pid == 0) {
			prctl(PR_SET_PDEATHSIG, SIGHUP);
			break;
		}

		if (pid != 0) {
			int status;
			pid_t wpid = wait(&status);
			LOG_DEBUG(pid << wpid << status);
		}
	}
}
#endif


#endif

