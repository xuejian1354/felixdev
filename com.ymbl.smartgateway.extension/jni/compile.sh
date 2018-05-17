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

for i in iptables RedirectToSock5Service;
do
  cd $i
  [ ! -x "configure" ] && ./autogen.sh && \
   ./configure --prefix=${PRETARGET} --host=arm-develop-linux-gnueabi CXXFLAGS=-I${EXDIR}/$i/extra/include LDFLAGS=-L${EXDIR}/$i/extra/lib/arm-develop
#   ./configure --prefix=${PRETARGET} --host=mips-en751221-linux-gnu CXXFLAGS=-I${EXDIR}/$i/extra/include LDFLAGS=-L${EXDIR}/$i/extra/lib/mips-en751221
#   ./configure --prefix=${PRETARGET}

  make && make install
  rm -rf ${EXDIR}/$i/target
  mv ${PRETARGET} ${EXDIR}/$i/target
  cd ../
done

cp -v ${EXDIR}/iptables/target/lib/libtransite.so.0.1.0 ../src/main/java/transite.so
cp -v ${EXDIR}/iptables/target/lib/libip4tc.so.0 ../src/main/java/
cp -v ${EXDIR}/iptables/target/lib/libip6tc.so.0 ../src/main/java/
cp -v ${EXDIR}/iptables/target/lib/libxtables.so.10 ../src/main/java/
cp -av ${EXDIR}/iptables/target/lib/xtables ../src/main/java/
cp -v ${EXDIR}/RedirectToSock5Service/target/lib/librectsocks5.so.0.1.0 ../src/main/java/rectsocks5.so
