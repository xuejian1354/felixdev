#!/bin/sh

#arm,mips,x86
PLAT=mips

if [ ${PLAT} == "arm" ]; then
  TARCC=arm-develop-linux-gnueabi-gcc
  TARARCH=arm-develop
elif [ ${PLAT} == "mips" ]; then
  TARCC=mips-unknown-linux-uclibc-gcc
  TARARCH=mips-unknown
elif [ ${PLAT} == "x86" ]; then
  TARCC=gcc
  TARARCH=
fi

EXSH=`pwd`/$0
EXDIR=$(dirname ${EXSH})

PRETARGET=/tmp/transite-target
JHOME=/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.41.x86_64

#cfile=com_ymbl_smartgateway_extension_IpTables.c
#hfile=com_ymbl_smartgateway_extension_IpTables.h
#dstdir=iptables/iptables

cd ${EXDIR}

#echo "git iptables..."
#[ ! -d "iptables" ] && \
#    git clone -b v1.4.21 git://git.netfilter.org/iptables && \
#    rm -rf iptables/.git && \
#    patch -p0 < patches/iptables-jni-1.4.21.patch && \
#    cp -v ${cfile} ${dstdir} && \
#    cp -v ${hfile} ${dstdir} && \
#    cp -v dlog.h ${dstdir}

#isnew=`find ${cfile} -newer ${dstdir}/${cfile}`
#[ "$isnew" == "${cfile}" ] && \
#    cp -v ${cfile} ${dstdir}

#isnew=`find ${hfile} -newer ${dstdir}/${hfile}`
#[ "$isnew" == "${hfile}" ] && \
#    cp -v ${hfile} ${dstdir}

#isnew=`find dlog.h -newer ${dstdir}/dlog.h`
#[ "$isnew" == "dlog.h" ] && \
#    cp -v dlog.h ${dstdir}

cfile=com_ymbl_smartgateway_extension_XL2tpd.c
hfile=com_ymbl_smartgateway_extension_XL2tpd.h
dstdir=xl2tpd

echo "git xl2tpd..."
[ ! -d "xl2tpd" ] && \
    git clone -b v1.3.6 https://github.com/xelerance/xl2tpd.git && \
    rm -rf xl2tpd/.git && \
    patch -p0 < patches/xl2tpd-jni-1.3.6.patch && \
    cp -v ${cfile} ${dstdir} && \
    cp -v ${hfile} ${dstdir} && \
    cp -v dlog.h ${dstdir}

isnew=`find ${cfile} -newer ${dstdir}/${cfile}`
[ "$isnew" == "${cfile}" ] && \
    cp -v ${cfile} ${dstdir}

isnew=`find ${hfile} -newer ${dstdir}/${hfile}`
[ "$isnew" == "${hfile}" ] && \
    cp -v ${hfile} ${dstdir}

isnew=`find dlog.h -newer ${dstdir}/dlog.h`
[ "$isnew" == "dlog.h" ] && \
    cp -v dlog.h ${dstdir}

echo "curl ppp..."
[ ! -d "ppp" ] && \
    curl -R -O https://fossies.org/linux/misc/ppp-2.4.7.tar.gz && \
    tar xf ppp-2.4.7.tar.gz && \
    rm -rf ppp-2.4.7.tar.gz && \
    mv ppp-2.4.7 ppp && \
    patch -p0 < patches/ppp-jni-2.4.7.patch

cfile=com_ymbl_smartgateway_extension_Lua.c
hfile=com_ymbl_smartgateway_extension_Lua.h
dstdir=lua/src

echo "curl lua..."
[ ! -d "lua" ] && \
    curl -R -O http://www.lua.org/ftp/lua-5.1.5.tar.gz && \
    tar xf lua-5.1.5.tar.gz && \
    rm -rf lua-5.1.5.tar.gz && \
    mv lua-5.1.5 lua && \
    patch -p0 < patches/lua-jni-5.1.5.patch && \
    cp -v ${cfile} ${dstdir} && \
    cp -v ${hfile} ${dstdir} && \
    cp -v dlog.h ${dstdir}

isnew=`find ${cfile} -newer ${dstdir}/${cfile}`
[ "$isnew" == "${cfile}" ] && \
    cp -v ${cfile} ${dstdir}

isnew=`find ${hfile} -newer ${dstdir}/${hfile}`
[ "$isnew" == "${hfile}" ] && \
    cp -v ${hfile} ${dstdir}

isnew=`find dlog.h -newer ${dstdir}/dlog.h`
[ "$isnew" == "dlog.h" ] && \
    cp -v dlog.h ${dstdir}

echo "compiling..."
#for i in ppp iptables RedirectToSock5Service;
for i in ppp;
do
  cd $i
  [ ! -x "configure" ] && ./autogen.sh
  isnew=`find ../compile.sh -newer configure`
  (([ "$isnew" == "../compile.sh" ] && make clean) || [ ! -f "Makefile" ]) && touch configure && ( \
    if [ ${PLAT} == "arm" ]; then ./configure --prefix=${PRETARGET} --host=arm-develop-linux-gnueabi CXXFLAGS=-I${EXDIR}/extra/libevent/include LDFLAGS=-L${EXDIR}/extra/libevent/lib/arm-develop; \
    elif [ ${PLAT} == "mips" ]; then ./configure --prefix=${PRETARGET} --host=mips-en751221-linux-gnu CXXFLAGS=-I${EXDIR}/extra/libevent/include LDFLAGS=-L${EXDIR}/extra/libevent/lib/mips-en751221; \
    elif [ ${PLAT} == "x86" ]; then ./configure --prefix=${PRETARGET}; fi)

  make CC=${TARCC} && make install
  rm -rf ${EXDIR}/$i/target
  mv ${PRETARGET} ${EXDIR}/$i/target
  cd ../
done


isnew=`find compile.sh -newer xl2tpd/Makefile`
[ "$isnew" == "compile.sh" ] && make -C xl2tpd clean && touch xl2tpd/Makefile
make -C xl2tpd CC=${TARCC} DESTDIR=${EXDIR}/xl2tpd/target CFLAGS="-fPIC -DDEBUG_PPPD -DTRUST_PPPD_TO_DIE -DSANITY -DLINUX -DUSE_KERNEL -DIP_ALLOCATION -D_JNI_IMPLEMENTATION_ -I${JHOME}/include -I${JHOME}/include/linux -I${EXDIR}/extra/libpcap/include" LDFLAGS=-L${EXDIR}/extra/libpcap/lib/${TARARCH}

isnew=`find compile.sh -newer lua/src/Makefile`
[ "$isnew" == "compile.sh" ] && make -C lua/src clean && touch lua/src/Makefile
make -C lua/src linux CC=${TARCC} CFLAGS="-DLUA_USE_LINUX -DNO_GETLOGIN -fPIC -std=gnu99 -D_JNI_IMPLEMENTATION_ -I${JHOME}/include -I${JHOME}/include/linux -I${EXDIR}/extra/readline/include" LDFLAGS="-L${EXDIR}/extra/readline/lib/${TARARCH} -L${EXDIR}/extra/ncurses/lib/${TARARCH}"

echo "copying..."
#for j in com.ymbl.smartgateway.extension com.ymbl.smartgateway.transite;
#do
#  cp -v ${EXDIR}/extra/sudo/bin/arm-develop/sudo ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/iptables/target/lib/libtransite.so.0.1.0 ${EXDIR}/../../$j/src/main/java/IpTables.so
#  cp -v ${EXDIR}/iptables/target/lib/libip4tc.so.0 ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/iptables/target/lib/libip6tc.so.0 ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/iptables/target/lib/libxtables.so.10 ${EXDIR}/../../$j/src/main/java/
#  cp -av ${EXDIR}/iptables/target/lib/xtables ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/RedirectToSock5Service/target/lib/librectsocks5.so.0.1.0 ${EXDIR}/../../$j/src/main/java/RedirectToSocks5Service.so
#  cp -v ${EXDIR}/xl2tpd/XL2tpd.so ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/xl2tpd/xl2tpd ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/xl2tpd.conf ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/options.l2tpd.client ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/ppp.options ${EXDIR}/../../$j/src/main/java/options
#  cp -v ${EXDIR}/ppp/target/sbin/pppd ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/ppp/target/lib/pppd/2.4.7/pppol2tp.so ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/ppp/target/lib/pppd/2.4.7/openl2tp.so ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/lua/src/Lua.so ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/lua/src/lua ${EXDIR}/../../$j/src/main/java/
#  cp -v ${EXDIR}/myplugin.lua ${EXDIR}/../../$j/src/main/java/
#done

mkdir -p ${EXDIR}/transite-target/bin
for x in extra/busybox/bin/${TARARCH}/busybox xl2tpd/xl2tpd ppp/target/sbin/pppd lua/src/lua;
do
  cp -v ${EXDIR}/$x ${EXDIR}/transite-target/bin/
done

mkdir -p ${EXDIR}/transite-target/etc
for x in xl2tpd.conf options.l2tpd.client ppp.options exec.lua myplugin.lua;
do
  cp -v ${EXDIR}/$x ${EXDIR}/transite-target/etc/
done

mkdir -p ${EXDIR}/transite-target/lib
for x in xl2tpd/XL2tpd.so lua/src/Lua.so;
do
  cp -v ${EXDIR}/$x ${EXDIR}/transite-target/lib/
done

mkdir -p ${EXDIR}/transite-target/lib/pppd/2.4.7
for x in ppp/target/lib/pppd/2.4.7/pppol2tp.so ppp/target/lib/pppd/2.4.7/openl2tp.so;
do
  cp -v ${EXDIR}/$x ${EXDIR}/transite-target/lib/pppd/2.4.7/
done

mkdir -p ${EXDIR}/transite-target/var/run

cd ${EXDIR}
zip -r ${EXDIR}/transite-${TARARCH}.zip transite-target/
rm -rf ${EXDIR}/transite-target
