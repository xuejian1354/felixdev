#!/bin/sh
#CC=mips-unknown-linux-uclibc-gcc
CC=gcc

JHOME=/usr/lib/jvm/java-1.6.0-openjdk-1.6.0.41.x86_64
EXSH=`pwd`/$0
EXDIR=$(dirname ${EXSH})

#javah -cp ${EXDIR}/../build/classes -jni com.ymbl.smartgateway.transite.TransiteActivator
#mv com_ymbl_smartgateway_transite_TransiteActivator.h ${EXDIR}/
${CC} -Wall -fPIC -D_JNI_IMPLEMENTATION_  -I${EXDIR} -I${JHOME}/include -I${JHOME}/include/linux -shared -o ${EXDIR}/../src/main/java/transite.so ${EXDIR}/com_ymbl_smartgateway_transite_TransiteActivator.cpp
chmod -x ${EXDIR}/../src/main/java/transite.so
