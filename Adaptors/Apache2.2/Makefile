include ../make.config

# Set up a bunch of environment settings
SHELL = /bin/sh

INCLUDES = -I${APACHEINCLUDE_DIR} -I../Adaptor

# Common compiler and linker flags
CFLAGS = -O2 -Wall ${INCLUDES} ${DEBUG_FLAG} -DSINGLE_THREADED_ADAPTOR -D${OS} -DFORKING_WEBSERVER -DAPACHE ${ENABLE_SSL_SUPPORT}

APXSFLAGS = -c -S CC=${CC} -Wc,"${CFLAGS}" -Wl,"${LDFLAGS}"



# These are the default settings for OSX.
ifeq "MACOS" "$(OS)"
# Flags for use when enabling mod_ssl support (on by default)
#ENABLE_SSL_SUPPORT =
ENABLE_SSL_SUPPORT = -DAPACHE_SECURITY_ENABLED
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS = -I/usr/include/openssl
OPENSSL_LIB_FLAGS = -L/usr/lib -lcrypto -lssl

INCLUDES += ${OPENSSL_INCL_FLAGS}
LDFLAGS += ${OPENSSL_LIB_FLAGS} -lm -module

# STRIP_FLAGS = -S
endif
# End MacOS X Specific Settings




ifeq "LINUX" "${OS}"
# Flags for use when enabling mod_ssl support
ENABLE_SSL_SUPPORT =
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS =
OPENSSL_LIB_FLAGS =

# If Apache is in a nonstandard location, change this
#APACHE_INCL_FLAGS = -I/usr/local/apache2/include

CFLAGS +=-DEAPI -fPIC ${APACHE_INCL_FLAGS}
LDFLAGS +=-G -L/lib -lc ${OPENSSL_LIB_FLAGS}
APXSFLAGS = -c
endif
# End Linux Config



ifeq "SOLARIS" "${OS}"
# Flags for use when enabling mod_ssl support
ENABLE_SSL_SUPPORT =
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS =
OPENSSL_LIB_FLAGS =

# If Apache is in a nonstandard location, change this
APACHE_INCL_FLAGS = -I/usr/apache/include

CFLAGS +=-DEAPI ${APACHE_INCL_FLAGS}
LDFLAGS +=-G -L/lib -lsocket -lnsl ${OPENSSL_LIB_FLAGS}
endif
# End Solaris Config


ifeq "HPUX" "${OS}"
# Flags for use when enabling mod_ssl support
ENABLE_SSL_SUPPORT =
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS =
OPENSSL_LIB_FLAGS =

# If Apache is in a nonstandard location, change this
APACHE_INCL_FLAGS = -I/opt/hpws/apache/include

CFLAGS = -O2 -Wall -I../Adaptor ${DEBUG_FLAG} -DMULTITHREADED_ADAPTOR -D${OS} -DFORKING_WEBSERVER -DAPACHE ${ENABLE_SSL_SUPPORT}
CFLAGS +=-DEAPI -fPIC -D_HPUX_SOURCE -D_POSIX_C_SOURCE=199506L -D_REENTRANT ${APACHE_INCL_FLAGS}
LDFLAGS +=-G -L/lib -lc -lpthread ${OPENSSL_LIB_FLAGS}
APXSFLAGS = -i
endif
# End HPUX config




# Build steps
# Get the list of source files from the ../Adaptor directory and change their extension to .o
COMMON_SRCFILES := $(wildcard ../Adaptor/*.c)
#COMMON_OBJFILES := $(patsubst %.c, %.o, $(wildcard ../Adaptor/*.c))
COMMON_OBJFILES := $(patsubst ../Adaptor/%.c, %.o, $(wildcard ../Adaptor/*.c))
# The same list as above but with the .lo extension (libtool object, FWIW)
lo_objects = $(patsubst ../Adaptor/%.c, %.lo, $(wildcard ../Adaptor/*.c))


all: mod_WebObjects.so

# The objects target compiles all the .c files, except mod_WebObjects.c, which we want
# libtool (through apxs or otherwise) to take care of.
$(COMMON_OBJFILES) : %.o : ../Adaptor/%.c
	$(CC) $(CFLAGS) -c $<


# MacOS X Build
ifeq "MACOS" "$(OS)"

mod_WebObjects.so : mod_WebObjects.c ${COMMON_SRCFILES}
	${APXS} ${APXSFLAGS} mod_WebObjects.c ${COMMON_SRCFILES}

endif
#end MacOS



# LINUX Section mostly from Senmeisha, Inc.'s Makefile. Thanks.
ifeq "LINUX" "${OS}"

mod_WebObjects.so : mod_WebObjects.o ${COMMON_OBJFILES}
	ld ${LDFLAGS} mod_WebObjects.o ${COMMON_OBJFILES} -o mod_WebObjects.so
	${APXS} ${APXSFLAGS} mod_WebObjects.so

mod_WebObjects.o : mod_WebObjects.c ${COMMON_OBJFILES}
	${CC} -c ${CFLAGS} mod_WebObjects.c ${COMMON_OBJFILES}
endif
#end Linux

# Solaris?????  Anyone?  Anyone?
# End Solaris

# HPUX Section courtesy Carlos Gonzalez
ifeq "HPUX" "${OS}"
mod_WebObjects.so : mod_WebObjects.o ${COMMON_OBJFILES}
        ld -b ${LDFLAGS} mod_WebObjects.o ${COMMON_OBJFILES} -o mod_WebObjects.so
        ${APXS} ${APXSFLAGS} mod_WebObjects.so

mod_WebObjects.o : mod_WebObjects.c ${COMMON_OBJFILES}
        ${CC} -c ${CFLAGS} mod_WebObjects.c ${COMMON_OBJFILES}
endif

# The install target does what you'd think it would: install the module; By default, this is not called.
install: mod_WebObjects.la
ifeq "MACOS" "${OS}"
	${APXS} -i -a -n WebObjects mod_WebObjects.la
else
	# This works on Linux at least, while the above leads to:
	# "Can't locate API module structure `WebObjects_module' in file .../modules/mod_WebObjects.so: 
	# .../modules/mod_WebObjects.so: undefined symbol: WebObjects_module"
	${APXS} -i -a -n WebObjects mod_WebObjects.so
endif

clean:
	-rm -rf mod_WebObjects.la *.o *.lo *.so *.slo .libs

.PHONY: clean install


