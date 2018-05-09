
./autogen.sh


if [ "$1"x == "FengHuo"x ]; then
  CC=/home/sgj/crosstools-mips/usr/bin/mips-linux-gcc
  CXX=/home/sgj/crosstools-mips/usr/bin/mips-linux-g++
  LD=/home/sgj/crosstools-mips/usr/bin/mips-linux-ld
  AR=/home/sgj/crosstools-mips/usr/bin/mips-linux-ar
  RANLIB=/home/sgj/crosstools-mips/usr/bin/mips-linux-ranlib
  THIS_LOCAL=/home/sgj/projects/opensource/libevent-2.1.8-stable/mips-pic/output
elif [ "$1"x == "ChinaMobile"x ]; then
  CC=/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-gcc
  CXX=/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-g++
  LD=/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-ld
  AR=/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-ar
  RANLIB=/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-ranlib
  THIS_LOCAL=/home/sgj/projects/opensource/libevent-2.1.8-stable/mips-cm-pic/output
else
  THIS_LOCAL=/home/sgj/projects/opensource/libevent-2.1.8-stable/pic
  CXX=
fi


if [ "$1"x == "FengHuo"x ]; then
 ./configure CXXFLAGS="-D_DLL -DMANUFACTURE=\\\"FengHuo\\\""  --disable-static --enable-shared=yes --host=mips CC=${CC} CXX=${CXX} LD=${LD} AR=${AR} RANLIB=${RANLIB} THIS_LOCAL=${THIS_LOCAL}
elif [ "$1"x == "ChinaMobile"x ]; then
	./configure CXXFLAGS="-D_DLL -DMANUFACTURE=\\\"ChinaMobile\\\"" --disable-static --enable-shared=yes  --host=mips CC=${CC} CXX=${CXX} LD=${LD} AR=${AR} RANLIB=${RANLIB} THIS_LOCAL=${THIS_LOCAL}
else
 ./configure --enable-debug CXXFLAGS="-D_DLL -DMANUFACTURE=\\\"$(uname -m)\\\""  THIS_LOCAL=${THIS_LOCAL}
fi

make clean
make


if [ "$1"x = "FengHuo"x ]; then
	echo "FengHuo mips strip..."
	/home/sgj/crosstools-mips/usr/bin/mips-linux-strip ./libRedirectToSocks5Service.so
elif [ "$1"x == "ChinaMobile"x ]; then
	echo "ChinaMobile mips strip..."
	/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-strip ./libRedirectToSocks5Service.so
fi
