
include ../make.config
include ../Adaptor/make.preamble
# Flags for use when enabling mod_ssl support
# On OSX, apxs does it, and it's on by default
APXSAPACHESSLFLAG = -D APACHE_SECURITY_ENABLED
# On Solaris, cc does it, and it isn't
#LDAPACHESSLFLAG = -DAPACHE_SECURITY_ENABLED
# More flags for use when enabling mod_ssl support
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSLINCLUDEFLAGS = 
OPENSSLLIBFLAGS = 

# MacOS X
ifeq "MACOS" "$(OS)"

# These are the defaults for OSX. You may have to adjust them on Solaris.
OPENSSLINCLUDEFLAGS = -I/usr/local/include/openssl
OPENSSLLIBFLAGS = -L/usr/lib -lcrypto -lssl
CFLAGS = -O2 -Wall -I../Adaptor  $(DEBUG_FLAG) -DSINGLE_THREADED_ADAPTOR -D$(OS) -DFORKING_WEBSERVER -DAPACHE 

APXSFLAGS = -c -I../Adaptor ${OPENSSLLIBFLAGS} -D SINGLE_THREADED_ADAPTOR ${LDAPACHESSLFLAG} -S CC=$(CC) 

all: mod_WebObjects.so

mod_WebObjects.so : mod_WebObjects.c ${COMMON_OBJFILES}
	${APXS} ${APXSFLAGS} mod_WebObjects.c ${COMMON_OBJFILES}

clean:
	rm -f mod_WebObjects.so mod_WebObjects.o *.o

endif 


ifeq "SOLARIS" "$(OS)"

# If Apache is in a nonstandard location, change this
APACHEINCLUDEFLAGS = -I/usr/apache/include



CFLAGS = -O2 -Wall -I../Adaptor ${LDAPACHESSLFLAG} $(DEBUG_FLAG) -DSINGLE_THREADED_ADAPTOR -D$(OS) -DEAPI -DFORKING_WEBSERVER -DAPACHE ${OPENSSLINCLUDEFLAGS} ${APACHEINCLUDEFLAGS} ${OPENSSLLIBFLAGS} -L/lib -lsocket -lnsl
APXSFLAGS = -i
LDFLAGS +=  -G -L/lib -lsocket -lnsl -lc ${OPENSSLLIBFLAGS}

all: adaptor 

adaptor: mod_WebObjects.so
	${APXS} ${APXSFLAGS} mod_WebObjects.so 

mod_WebObjects.so : mod_WebObjects.o ${COMMON_OBJFILES}
#       ${APXS} ${APXSFLAGS} -c mod_WebObjects.c ${COMMON_OBJFILES}
#       ${APXS} ${APXSFLAGS} mod_WebObjects.o ${COMMON_OBJFILES}
	ld ${LDFLAGS} mod_WebObjects.o ${COMMON_OBJFILES} -o mod_WebObjects.so

clean:
	rm -f mod_WebObjects.so mod_WebObjects.o *.o

#mod_WebObjects.o: mod_WebObjects.c
#       ${APXS} ${APXSFLAGS} -c mod_WebObjects.c

mod_WebObjects.o: mod_WebObjects.c ${COMMON_OBJFILES}
	${CC} -c ${CFLAGS} mod_WebObjects.c ${COMMON_OBJFILES}
endif

ifeq "LINUX" "$(OS)"
CFLAGS = -O2 -Wall -I../Adaptor ${LDAPACHESSLFLAG} $(DEBUG_FLAG) -DSINGLE_THREADED_ADAPTOR -D$(OS) -DEAPI -DFORKING_WEBSERVER -DAPACHE ${OPENSSLINCLUDEFLAGS} ${APACHEINCLUDEFLAGS} ${OPENSSLLIBFLAGS} -L/lib 
APXSFLAGS = -i
LDFLAGS += -G -L/lib -lc ${OPENSSLLIBFLAGS}

all: adaptor

adaptor: mod_WebObjects.so
	${APXS} ${APXSFLAGS} mod_WebObjects.so 

mod_WebObjects.so : mod_WebObjects.o ${COMMON_OBJFILES}
# ${APXS} ${APXSFLAGS} -c mod_WebObjects.c ${COMMON_OBJFILES}
# ${APXS} ${APXSFLAGS} mod_WebObjects.o ${COMMON_OBJFILES}
	ld ${LDFLAGS} mod_WebObjects.o ${COMMON_OBJFILES} -o mod_WebObjects.so

clean:
	rm -f mod_WebObjects.so mod_WebObjects.o *.o

#mod_WebObjects.o: mod_WebObjects.c
# ${APXS} ${APXSFLAGS} -c mod_WebObjects.c

mod_WebObjects.o: mod_WebObjects.c ${COMMON_OBJFILES}
	${CC} -c ${CFLAGS} mod_WebObjects.c ${COMMON_OBJFILES}
endif

#################################
include ../Adaptor/make.postamble
