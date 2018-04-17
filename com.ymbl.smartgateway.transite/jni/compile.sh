#!/bin/sh

#CC=mips-unknown-linux-uclibc-gcc
CC=gcc

EXSH=`pwd`/$0
EXDIR=$(dirname ${EXSH})

cd ${EXDIR}

[ ! -d "iptables" ] && \
    git clone -b v1.6.2 git://git.netfilter.org/iptables && \
    rm -rf iptables/.git && \
    patch -p0 < iptables-jni-1.6.2.patch && \
    cp -v com_ymbl_smartgateway_transite_TransiteActivator.c iptables/iptables/ && \
    cp -v com_ymbl_smartgateway_transite_TransiteActivator.h iptables/iptables/ && \
    cp -v dlog.h iptables/iptables/

isnew=`find com_ymbl_smartgateway_transite_TransiteActivator.c -newer iptables/iptables/com_ymbl_smartgateway_transite_TransiteActivator.c`
[ "$isnew" == "com_ymbl_smartgateway_transite_TransiteActivator.c" ] && \
    cp -v com_ymbl_smartgateway_transite_TransiteActivator.c iptables/iptables/

isnew=`find com_ymbl_smartgateway_transite_TransiteActivator.h -newer iptables/iptables/com_ymbl_smartgateway_transite_TransiteActivator.h`
[ "$isnew" == "com_ymbl_smartgateway_transite_TransiteActivator.h" ] && \
    cp -v com_ymbl_smartgateway_transite_TransiteActivator.h iptables/iptables/

isnew=`find dlog.h -newer iptables/iptables/dlog.h`
[ "$isnew" == "dlog.h" ] && \
    cp -v dlog.h iptables/iptables/

cd iptables

[ ! -x "configure" ] && \
    ./autogen.sh && ./configure --prefix=${EXDIR}/iptables/target

make && make install
cp target/lib/libtransite.so.0.1.0 ../../src/main/java/transite.so
