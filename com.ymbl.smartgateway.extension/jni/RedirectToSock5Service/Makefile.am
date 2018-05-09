
H_FILES = \
       	Utils/AutoBuffer.h \
        Utils/initDaemon.h \
        Utils/LinkedList.h \
        Utils/Event/SocketUtils.h \
        Utils/TinyLog.h \
        Utils/Utils.h \
        Cfg.h \
        RedirectClient.h \
        RedirectListener.h \
        RedirectToSocks5Service.h \
        Stdafx.h

INL_FILES = \
        ./Utils/Event/Signals.inl

CPP_FILES = \
        Utils/Event/SocketUtils.cpp \
				Utils/Event/TcpAcceptor.cpp \
				Utils/Event/EventDefer.cpp \
        ./Utils/TinyLog.cpp \
        Cfg.cpp \
        RedirectListener.cpp \
        RedirectToSocks5Service.cpp


	
AUTOMAKE_OPTIONS = foreign 
#noinst_PROGRAMS = RedirectToSocks5Service

#RedirectToSocks5Service_CXXFLAGS = -Wall -Werror -I${THIS_LOCAL}/include
#RedirectToSocks5Service_LDFLAGS = -lpthread -levent
#RedirectToSocks5Service_SOURCES = ${H_FILES} ${INL_FILES} ${CPP_FILES} main.cpp

JHOME=/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.41.x86_64
lib_LTLIBRARIES = librectsocks5.la
librectsocks5_la_LIBADD = -lpthread -levent
librectsocks5_la_CXXFLAGS = -Wall -fPIC -D_JNI_IMPLEMENTATION_ -I${JHOME}/include -I${JHOME}/include/linux
librectsocks5_la_SOURCES = ${CPP_FILES} com_ymbl_smartgateway_extension_RedirectToSocks5Service.cpp
librectsocks5_la_LDFLAGS = -version-info 1:0:1

