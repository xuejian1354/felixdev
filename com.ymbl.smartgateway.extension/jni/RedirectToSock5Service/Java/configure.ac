#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ(2.69)
AC_INIT(libRedirectToSock5Service, 1.0.0)
#AC_CONFIG_SRCDIR([../RedirectToSocks5Service.cpp])

AM_INIT_AUTOMAKE([subdir-objects])


AC_PROG_CXX
AC_PROG_LIBTOOL




AC_CONFIG_FILES([Makefile])

AC_SUBST(THIS_LOCAL)


AC_ARG_ENABLE(debug, AS_HELP_STRING([--enable-debug], [enable DEBUG mode(default=no)]),[],[enable_debug=no])                                              
AS_IF([test "x$enable_debug" = "xyes"], [CXXFLAGS+=" -g2 -O0 -D_DEBUG -DDEBUG -Wall"],        
    [test "x$enable_debug" = "xno"], [CXXFLAGS+=" -DNDEBUG -O2 -Wall"],                  
    [])       
    
AC_OUTPUT
