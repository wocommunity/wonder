
include ../make.config
#include ../Adaptor/make.preamble

# Set up a bunch of environment settings
SHELL = /bin/sh

srcdir = .
top_srcdir = .

prefix = /usr/local
exec_prefix = ${prefix}

bindir = ${exec_prefix}/bin
sbindir = ${exec_prefix}/sbin
libexecdir = ${exec_prefix}/libexec
datadir = ${prefix}/share
sysconfdir = ${prefix}/etc
sharedstatedir = ${prefix}/com
localstatedir = ${prefix}/var
libdir = ${exec_prefix}/lib
infodir = ${prefix}/info
mandir = ${prefix}/man
includedir = ${prefix}/include
oldincludedir = /usr/include
pkgdatadir = $(datadir)/mod_tut2
pkglibdir = $(libdir)/mod_tut2
pkgincludedir = $(includedir)/mod_tut2
top_builddir = .

host_triplet = powerpc-apple-darwin6.6

LIBTOOL = /usr/bin/glibtool

PACKAGE = mod_WebObjects


DEFAULT_INCLUDES =  -I. -I$(srcdir)
INCLUDES = -I${APACHEINCLUDEFLAGS} -I../Adaptor


# build package info
DEFS = -DPACKAGE=\"mod_WebObjects\" -DVERSION=\"2.0\" -DHAVE_DLFCN_H 

CFLAGS = -g -O2 -Wall -DSINGLE_THREADED_ADAPTOR -D$(OS) -DFORKING_WEBSERVER -DAPACHE -fPIC -fno-common

LDFLAGS = -flat_namespace
LIBS =

LTCOMPILE = $(LIBTOOL) --mode=compile $(CC) $(DEFS) $(DEFAULT_INCLUDES) $(INCLUDES) $(CFLAGS)
CCLD = $(CC)
LINK = $(LIBTOOL) --mode=link $(CCLD) $(CFLAGS) $(LDFLAGS) -o $@



# Flags for use when enabling mod_ssl support
# On OSX, apxs does it, and it's on by default
APXSAPACHESSLFLAG = -DAPACHE_SECURITY_ENABLED


# These are the defaults for OSX.
OPENSSLINCLUDEFLAGS = -I/usr/local/include/openssl
OPENSSLLIBFLAGS = -L/usr/lib -lcrypto -lssl
#CFLAGS = -O2 -Wall -I../Adaptor  $(DEBUG_FLAG) -DSINGLE_THREADED_ADAPTOR -D$(OS) -DFORKING_WEBSERVER -DAPACHE 

#APXSFLAGS = -c -I../Adaptor ${OPENSSLLIBFLAGS} -D SINGLE_THREADED_ADAPTOR ${LDAPACHESSLFLAG} -S CC=$(CC) 

#CFLAGS += $(APXSAPACHESSLFLAG)

INCLUDES += $(OPENSSLINCLUDEFLAGS)
LIBS += $(OPENSSL_LIB_FLAGS)

sources = mod_WebObjects.c $(wildcard ../Adaptor/*.c)
objects = mod_WebObjects.o $(patsubst %.c, %.o, $(wildcard ../Adaptor/*.c))
lo_objects = mod_WebObjects.lo $(patsubst %.c, %.lo, $(wildcard ../Adaptor/*.c))

all: compile link

compile: $(sources)
	$(CC) -c $(DEFS) $(DEFAULT_INCLUDES) $(INCLUDES) $(CFLAGS) $^
	    	    
link : $(objects)
	libtool -dynamic -install_name libmod_WebObjects.la -o $(APACHEMODULE_DIR) $^

$(lo_objects): $(sources)
	    $(LTCOMPILE) -c -o $@ `test -f '$<' || echo '$(srcdir)/'`$<
	    
libmod_WebObjects.la : $(lo_objects)
	$(LINK) -rpath $(APACHEMODULE_DIR) $(lo_objects) $(LIBS)

install : libmod_WebObjects.la
	APXS -i -a -n mod_WebObjects libmod_WebObjects.la

clean:
	-rm -f mod_WebObjects.o mod_WebObjects.la *.o *.lo; \
	cd ../Adaptor; rm -f *.o *.lo 

.PHONY: clean


#################################
#include ../Adaptor/make.postamble
