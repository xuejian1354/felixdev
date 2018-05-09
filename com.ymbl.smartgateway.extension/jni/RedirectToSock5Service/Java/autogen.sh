#!/bin/bash

if [ x"$1" = xclean ]; then
	echo autogen.sh clean
	make clean
	rm -rf .libs .deps libtool Makefile Makefile.in  aclocal.m4 \
	 autom4te.cache config.log config.status \
	 configure 	ctserv depcomp install-sh missing
	exit
fi
 



aclocal
autoconf
libtoolize --automake
automake --add-missing


#./configure 



