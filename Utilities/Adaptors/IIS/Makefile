
include ../make.config
include ../Adaptor/make.preamble

CFLAGS += -O2 -Wall -I../Adaptor -DMULTITHREADED_ADAPTOR -D$(OS) -DIIS

all: WebObjects.dll

ifeq "MINGW" "$(OS)"
WebObjects.dll: WebObjects.o ${COMMON_OBJFILES}
	${CC} -Wl,--kill-at -shared -o WebObjects.dll WebObjects.o ${COMMON_OBJFILES} ${LIBS}
# Alternative linking for mingw32
#	dllwrap  --add-stdcall-alias -def=WebObjects.def --dllname=$@ --driver-name=$(CC) WebObjects.o ${COMMON_OBJFILES} ${LIBS}
else
WebObjects.dll: WebObjects.o ${COMMON_OBJFILES}
	${NEXT_ROOT}/Developer/Libraries/gcc-lib/i386-nextpdo-winnt3.5/2.7.2.1/ld.exe -nodefaultlib -align:0x1000 -dll -o WebObjects.dll WebObjects.o ${COMMON_OBJFILES}  msvcrt.lib kernel32.lib wsock32.lib advapi32.lib
endif

WebObjects.o: WebObjects.c
	${CC} ${CFLAGS} -c WebObjects.c

clean:
	rm -f WebObjects WebObjects.o WebObjects.dll WebObjects.lib WebObjects.exp *.o

include ../Adaptor/make.postamble
