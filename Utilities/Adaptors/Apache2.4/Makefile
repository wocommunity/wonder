include ../make.config

# Set up a bunch of environment settings
SHELL = /bin/sh

INCLUDES = -I${APACHEINCLUDE_DIR} -I../Adaptor

# Common compiler and linker flags
CFLAGS += -O2 ${INCLUDES} ${DEBUG_FLAG} -DSINGLE_THREADED_ADAPTOR -D${OS} -DFORKING_WEBSERVER -DAPACHE ${ENABLE_SSL_SUPPORT}

APXSFLAGS = -c -S CC=${CC} -Wc,"${CFLAGS}" -Wl,"${LDFLAGS}"


ifeq "MACOS" "${OS}"

# These are the default settings for OSX.
CFLAGS += -Wall ${RC_CFLAGS}
LDARCHS = $(patsubst %, -arch %, ${RC_ARCHS})
LDFLAGS += ${OPENSSL_LIB_FLAGS} ${LDARCHS} -macosx_version_min 10.5 -lm -module
STRIP_FLAGS = -S
endif
# End MacOS X Specific Settings


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
#$(COMMON_OBJFILES) : %.o : ../Adaptor/%.c
#	$(CC) $(CFLAGS) -c $<

ifeq "MINGW" "$(OS)"
mod_WebObjects.so : mod_WebObjects.o ${COMMON_OBJFILES}
	${CC} -L${APACHEDIR}/lib -Wl,--kill-at -shared -o mod_WebObjects.so mod_WebObjects.o ${COMMON_OBJFILES} ${LIBS} ${APACHELIBS}
#	strip mod_WebObjects.so
	
mod_WebObjects.o : mod_WebObjects.c
	${CC} ${CFLAGS} -c mod_WebObjects.c

else

mod_WebObjects.so : mod_WebObjects.c ${COMMON_SRCFILES}
	${APXS2.2} ${APXSFLAGS} mod_WebObjects.c ${COMMON_SRCFILES}
#	${APXS2.2} -i -A -n WebObjects mod_WebObjects.la #auto-install module into libexec dir
	cp .libs/mod_WebObjects.so .  #extra copy to put mod_WebObjects.so in traditional location

endif

#
# vstudioe from command line
# http://www.mcs.anl.gov/research/projects/mpi/mpich1-old/docs/mpichntman/node10.htm
# compile with cl
# cl /nologo /MD /W3 /O2 /D WIN32 /D _WINDOWS /D NDEBUG /D SINGLE_THREADED_ADAPTOR -I"C:\Apache24\include" -I"../Adaptor"  /Fomod_WebObjects.lo mod_WebObjects.c

clean:
	rm -f mod_WebObjects.lo mod_WebObjects.la mod_WebObjects.slo mod_WebObjects.so mod_WebObjects.o *.o *.lib *.exp
	rm -f ../Adaptors/*.o ../*.slo ../*.lo
	rm -rf .libs

include ../Adaptor/make.postamble



