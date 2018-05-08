#!/bin/sh

EXSH=`pwd`/$0
EXDIR=$(dirname ${EXSH})

PRETARGET=/tmp/transite-target

cfile=com_ymbl_smartgateway_extension_IpTables.c
hfile=com_ymbl_smartgateway_extension_IpTables.h

cd ${EXDIR}

[ ! -d "iptables" ] && \
    git clone -b v1.4.21 git://git.netfilter.org/iptables && \
    rm -rf iptables/.git && \
    patch -p0 < patches/iptables-jni-1.4.21.patch && \
    cp -v ${cfile} iptables/iptables/ && \
    cp -v ${hfile} iptables/iptables/ && \
    cp -v dlog.h iptables/iptables/

isnew=`find ${cfile} -newer iptables/iptables/${cfile}`
[ "$isnew" == "${cfile}" ] && \
    cp -v ${cfile} iptables/iptables/

isnew=`find ${hfile} -newer iptables/iptables/${hfile}`
[ "$isnew" == "${hfile}" ] && \
    cp -v ${hfile} iptables/iptables/

isnew=`find dlog.h -newer iptables/iptables/dlog.h`
[ "$isnew" == "dlog.h" ] && \
    cp -v dlog.h iptables/iptables/

cd iptables
rm -rf ${PRETARGET}

[ ! -x "configure" ] && \
    ./autogen.sh && ./configure --prefix=${PRETARGET} --host=arm-develop-linux-gnueabi
#    ./autogen.sh && ./configure --prefix=${PRETARGET}

make && make install
rm -rf ${EXDIR}/iptables/target
mv ${PRETARGET} ${EXDIR}/iptables/target
cp -v ${EXDIR}/iptables/target/lib/libtransite.so.0.1.0 ../../src/main/java/transite.so
cp -v ${EXDIR}/iptables/target/lib/libip4tc.so.0 ../../src/main/java/
cp -v ${EXDIR}/iptables/target/lib/libip6tc.so.0 ../../src/main/java/
cp -v ${EXDIR}/iptables/target/lib/libxtables.so.10 ../../src/main/java/
cp -av ${EXDIR}/iptables/target/lib/xtables ../../src/main/java/
