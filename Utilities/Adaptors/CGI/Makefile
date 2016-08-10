
include ../make.config
include ../Adaptor/make.preamble

CFLAGS += -O2 -I../Adaptor $(DEBUG_FLAG) -DSINGLE_THREADED_ADAPTOR -D$(OS) -DCGI


ifeq "MACOS" "$(OS)"
CFLAGS += ${RC_CFLAGS}
endif

ifeq "HPUX" "$(OS)"
CFLAGS += -nopdolib
endif

ifeq "SOLARIS" "$(OS)"
# Since Solaris is SVR4 system, it requires linking with these 2 libraries.
LDFLAGS += -lsocket -lnsl
endif

ifeq "Windows" "$(OS)"
# Suddenly, I need this
LDFLAGS += -lgcc
endif

all: WebObjects${EXESUFFIX}

ifeq "MACOS" "$(OS)"
WebObjects${EXESUFFIX}: WebObjects.o ${COMMON_OBJFILES}
	${CC} ${CFLAGS} ${LDFLAGS} -o WebObjects${EXESUFFIX} WebObjects.o ${COMMON_OBJFILES}
	strip -S WebObjects${EXESUFFIX}
else
WebObjects${EXESUFFIX}: WebObjects.o ${COMMON_OBJFILES}
	${CC} ${CFLAGS} ${LDFLAGS} -o WebObjects${EXESUFFIX} WebObjects.o ${COMMON_OBJFILES} ${LIBS}
endif

clean:
	rm -f WebObjects${EXESUFFIX} *.o

include ../Adaptor/make.postamble
