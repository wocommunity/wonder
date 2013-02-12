include ../make.config
include ../Adaptor/make.preamble

# Common compiler and linker flags
CFLAGS += -O2 -I../Adaptor ${DEBUG_FLAG} -DSINGLE_THREADED_ADAPTOR -D${OS} -DFORKING_WEBSERVER -DAPACHE ${ENABLE_SSL_SUPPORT}

# MacOS X
ifeq "MACOS" "${OS}"
# Flags for use when enabling mod_ssl support
ENABLE_SSL_SUPPORT = -DAPACHE_SECURITY_ENABLED
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS = -I/usr/local/include/openssl
OPENSSL_LIB_FLAGS = -L/usr/lib -lcrypto -lssl
CFLAGS += -Wall ${RC_CFLAGS}
LDARCHS = $(patsubst %, -arch %, ${RC_ARCHS})
LDFLAGS +=${OPENSSL_LIB_FLAGS} ${LDARCHS}
STRIP_FLAGS = -S

# Include the Apache headers - Adjust as required.
CFLAGS += -I/usr/include/httpd

endif

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

ifeq "LINUX" "${OS}"
# Flags for use when enabling mod_ssl support
ENABLE_SSL_SUPPORT = -DAPACHE_SECURITY_ENABLED
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS =
OPENSSL_LIB_FLAGS =

# If Apache is in a nonstandard location, change this
APACHE_INCL_FLAGS = -I/usr/apache/include

CFLAGS +=-DEAPI ${APACHE_INCL_FLAGS}
LDFLAGS +=-G -L/lib -lc ${OPENSSL_LIB_FLAGS}
endif

ifeq "FreeBSD" "${OS}"
#CFLAGS +=-fPIC `${APXS} -q CFLAGS`
OPENSSL_INCL_FLAGS =
OPENSSL_LIB_FLAGS =
ifdef USE_SSL
ifdef WITH_MODSSL
  ENABLE_SSL_SUPPORT = -DAPACHE_SECURITY_ENABLED
  OPENSSL_INC_FLAGS = -I/usr/local/include/openssl
  OPENSSL_LIB_FLAGS = -L/usr/lib -lcrypto -lssl
endif
endif
endif

CFLAGS += ${OPENSSL_INCL_FLAGS}

APXSFLAGS = -c -S CC=${CC} -Wc,"${CFLAGS}" -Wl,"${LDFLAGS}"

all: mod_WebObjects.so

mod_WebObjects.so : mod_WebObjects.c ${COMMON_OBJFILES}
	${APXS1.3} ${APXSFLAGS} mod_WebObjects.c ${COMMON_OBJFILES}
	strip ${STRIP_FLAGS} mod_WebObjects.so

clean:
	rm -f mod_WebObjects.so mod_WebObjects.o *.o

include ../Adaptor/make.postamble
