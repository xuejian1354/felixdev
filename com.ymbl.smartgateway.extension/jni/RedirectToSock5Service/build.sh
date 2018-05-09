
./autogen.sh


if [ "$1"x == "FengHuo"x ]; then
  CXX=/home/sgj/crosstools-mips/usr/bin/mips-linux-g++
  THIS_LOCAL=/home/sgj/projects/opensource/libevent-2.1.8-stable/mips/output
elif [ "$1"x == "ChinaMobile"x ]; then
  CXX=/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-g++
  THIS_LOCAL=/home/sgj/projects/opensource/libevent-2.1.8-stable/mips-cm/output
else
  THIS_LOCAL=/usr/local
  CXX=
fi


if [ "$1"x == "FengHuo"x ]; then
	./configure CXXFLAGS=-DMANUFACTURE=\\\"FengHuo\\\"  --host=mips CXX=${CXX} THIS_LOCAL=${THIS_LOCAL}
elif [ "$1"x == "ChinaMobile"x ]; then
	./configure CXXFLAGS=-DMANUFACTURE=\\\"ChinaMobile\\\"  --host=mips CXX=${CXX} THIS_LOCAL=${THIS_LOCAL}
else
	./configure --enable-debug CXXFLAGS=-DMANUFACTURE=\\\"$(uname -m)\\\"  THIS_LOCAL=${THIS_LOCAL}
fi

make clean
make


if [ "$1"x = "FengHuo"x ]; then
	echo "FengHuo mips strip..."
	/home/sgj/crosstools-mips/usr/bin/mips-linux-strip ./RedirectToSocks5Service
elif [ "$1"x == "ChinaMobile"x ]; then
	echo "ChinaMobile mips strip..."
	/home/sgj/crosstools-mips-cm/usr/bin/mips-linux-strip ./RedirectToSocks5Service
fi
