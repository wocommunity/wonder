include ../make.config

# Set up a bunch of environment settings
SHELL = /bin/sh

INCLUDES = -I${APACHEINCLUDE_DIR} -I../Adaptor

# Common compiler and linker flags
CFLAGS = -O2 -Wall ${INCLUDES} ${DEBUG_FLAG} -DSINGLE_THREADED_ADAPTOR -D${OS} -DFORKING_WEBSERVER -DAPACHE ${ENABLE_SSL_SUPPORT}

APXSFLAGS = -c -S CC=${CC} -Wc,${CFLAGS} -Wl,${LDFLAGS}



# These are the default settings for OSX.
ifeq "MACOS" "$(OS)"
# Flags for use when enabling mod_ssl support -- Note that there's a dyld error with using SSL right now.
ENABLE_SSL_SUPPORT =
#ENABLE_SSL_SUPPORT = -DAPACHE_SECURITY_ENABLED
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS = -I/usr/include/openssl
OPENSSL_LIB_FLAGS = -L/usr/lib -lcrypto -lssl

INCLUDES += ${OPENSSL_INCL_FLAGS}
LDFLAGS += ${OPENSSL_LIB_FLAGS}

# We need to use Apache2's libtool as it knows the correct flags to pass to gcc and ld on OS X
LIBTOOL = $(APACHE2_DIR)/build/libtool

# I arrived at the LTFLAGS by observing where apxs failed.  
# apxs is pretty dumb...  at least the current OS X version (2.0.45) is.
# It calls $APACHE_DIR/build/libtool to compile the module, which builds .o files.
# It then calls libtool to link the files, but fails because it won't work with .o files.
# So I experimented and found that if I changed the name of the files to the expected .lo
# the libtool linker would succeed.  Go figure.  :-)
# This is probably not needed on some other platforms.  Maybe there's even a workaround for OS X.
LTFLAGS = -prefer-pic -DAP_HAVE_DESIGNATED_INITIALIZER -DDARWIN -DSIGPROCMASK_SETS_THREAD_MASK -no-cpp-precomp
# STRIP_FLAGS = -S
endif
# End MacOS X Specific Settings



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



ifeq "LINUX" "${OS}"
# Flags for use when enabling mod_ssl support
ENABLE_SSL_SUPPORT =
# These should be set to point at the ssl include and lib directories, respectively.
OPENSSL_INCL_FLAGS =
OPENSSL_LIB_FLAGS =

# If Apache is in a nonstandard location, change this
APACHE_INCL_FLAGS = -I/usr/local/apache2/include

CFLAGS +=-DEAPI ${APACHE_INCL_FLAGS}
LDFLAGS +=-G -L/lib -lc ${OPENSSL_LIB_FLAGS}
APXSFLAGS = -i
endif
# End Linux Config





# Build steps
# Get the list of source files from the ../Adaptor directory and change their extension to .o
objects := $(patsubst ../Adaptor/%.c, %.o, $(wildcard ../Adaptor/*.c))
# The same list as above but with the .lo extension (libtool object, FWIW)
lo_objects = $(patsubst ../Adaptor/%.c, %.lo, $(wildcard ../Adaptor/*.c))


all: mod_WebObjects.so

# The objects target compiles all the .c files, except mod_WebObjects.c, which we want
# libtool (through apxs or otherwise) to take care of.
$(objects) : %.o : ../Adaptor/%.c
	$(CC) $(CFLAGS) -c $<


# MacOS X Build
ifeq "MACOS" "$(OS)"
# As I mentioned above in the MacOS X build settings, these libtool statements were culled from
# observations of where apxs failed.  I just tried to duplicate the commands it was issuing, given
# the input of which files to compile into the module, except giving it the .lo files it insisted upon.
# It's a totally backwards way to do things, but it was the only way I could get things to build.
# If someone can clean this up, I would be grateful.

# The lo_objects target just renames all the .o files to .lo files, so the linker will be happy.
$(lo_objects) : %.lo : %.o
	mv $< $@

# The mod_WebObjects.so target actually assembles the module from the .lo files from the objects target.
mod_WebObjects.so: $(lo_objects)
# Compile mod_WebObjects.c
	${LIBTOOL} --silent --mode=compile $(CC) $(LTFLAGS) ${CFLAGS} -c -o mod_WebObjects.lo mod_WebObjects.c && touch mod_WebObjects.slo

# Link everything into a library
	${LIBTOOL} --silent --mode=link $(CC) -o mod_WebObjects.la $(CFLAGS) $(LDLAGS) -rpath $(APACHEMODULE_DIR) -module -avoid-version mod_WebObjects.lo $(lo_objects)
#	strip ${STRIP_FLAGS} mod_WebObjects.so
endif
#end MacOS



# LINUX Section mostly from Senmeisha, Inc.'s Makefile
# I don't know if it works, but it should.
ifeq "LINUX" "${OS}"

mod_WebObjects.so : mod_WebObjects.o ${objects}
	ld ${LDFLAGS} mod_WebObjects.o ${objects} -o mod_WebObjects.so
	${APXS} ${APXSFLAGS} mod_WebObjects.so

mod_WebObjects.o : mod_WebObjects.c ${objects}
	${CC} -c ${CFLAGS} mod_WebObjects.c ${objects}
endif
#end Linux

# Solaris?????  Anyone?  Anyone?
# End Solaris

# The install target does what you'd think it would: install the module; By default, this is not called.
install: mod_WebObjects.so
	${APXS} -i -a -n WebObjects mod_WebObjects.la

clean:
	-rm -rf mod_WebObjects.la *.o *.lo *.slo .libs

.PHONY: clean install


