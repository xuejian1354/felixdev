
H_FILES = \
       	../Utils/AutoBuffer.h \
        ../Utils/initDaemon.h \
        ../Utils/LinkedList.h \
        ../Utils/Event/SocketUtils.h \
        ../Utils/TinyLog.h \
        ../Utils/Utils.h \
        ../Cfg.h \
        ../RedirectClient.h \
        ../RedirectListener.h \
        ../RedirectToSocks5Service.h \
        ../Stdafx.h \
		./RedirectToSock5Service.h

INL_FILES = \
        ../Utils/Event/Signals.inl

CPP_FILES = \
        ../Utils/Event/SocketUtils.cpp \
		../Utils/Event/TcpAcceptor.cpp \
		../Utils/Event/EventDefer.cpp \
        ../Utils/TinyLog.cpp \
        ../Cfg.cpp \
        ../RedirectListener.cpp \
        ../RedirectToSocks5Service.cpp \
		./RedirectToSocks5Service.cpp

AUTOMAKE_OPTIONS = foreign 

projectlibdir=$(libdir)	
#lib_LTLIBRARIES
projectlib_PROGRAMS = libRedirectToSocks5Service.so

libRedirectToSocks5Service_so_CXXFLAGS = -fvisibility=hidden -fPIC -Wall -Werror -I${THIS_LOCAL}/include \
	-I/usr/lib/jvm/java-1.8.0-openjdk-amd64/include -I/usr/lib/jvm/java-1.8.0-openjdk-amd64/include/linux

libRedirectToSocks5Service_so_LDFLAGS = --shared -static-libgcc -static-libstdc++

libRedirectToSocks5Service_so_LDADD = -lpthread ${THIS_LOCAL}/lib/libevent.a

libRedirectToSocks5Service_so_SOURCES = ${H_FILES} ${INL_FILES} ${CPP_FILES} 




	
	

