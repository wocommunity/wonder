
include ../make.config
include ../Adaptor/make.preamble

NETSCAPE_VERSION = Netscape_3
SERVERAPI = NSAPI

ifeq "WINDOWS" "$(OS)"
ADAPTOR = WebObjects.dll
DEFFILE = WebObjects.def
OTHER_CLEAN_FILES = $(DEFFILE) WebObjects.lib WebObjects.exp
ARCH_CFLAGS = -DNetscape -D$(NETSCAPE_VERSION) -DREENTRANT -DNET_SSL -DSW_THREADS -DXP_WIN32 -DWIN32 -D_WINDOWS
endif

ifeq "SOLARIS" "$(OS)"
ADAPTOR = libWebObjects.so
ARCH_CFLAGS = -DNetscape -D$(NETSCAPE_VERSION) -DREENTRANT -DNET_SSL -DSW_THREADS -DXP_UNIX -D_REENTRANT -DSOLARIS
endif

ifeq "HPUX" "$(OS)"
ADAPTOR = libWebObjects.so
ARCH_CFLAGS = -DNetscape -D$(NETSCAPE_VERSION) -DREENTRANT -DNET_SSL -DSW_THREADS -DXP_UNIX -DNEEDS_HSTRERR -D_REENTRANT -DHPUX -fPIC
endif

ifeq "" "$(NS_ROOT)"
ERROR = NS_ROOT not defined. You must specify the path to the Netscape installation in make.config.
endif

CFLAGS += $(ARCH_CFLAGS) -I../Adaptor -I$(NS_ROOT)/include $(DEBUG_FLAG) -DMULTITHREADED_ADAPTOR -D$(SERVERAPI) -D$(OS) -DDISABLE_SHARED_MEMORY

all: $(ERROR) $(ADAPTOR)

ifeq "SOLARIS" "$(OS)"
$(ADAPTOR): WebObjects.o ${COMMON_OBJFILES}
	ld -G WebObjects.o ${COMMON_OBJFILES} -lnsl -lsocket -lsched -lc -lrt -o $(ADAPTOR)
endif

ifeq "HPUX" "$(OS)"
$(ADAPTOR): WebObjects.o ${COMMON_OBJFILES}
	ld -b WebObjects.o ${COMMON_OBJFILES} -l:libc.1 -o $(ADAPTOR)
endif
ifeq "WINDOWS" "$(OS)"
$(ADAPTOR): WebObjects.o ${COMMON_OBJFILES}
	$(CC) -dll -L$(NS_ROOT)/lib -lns-httpd36 -o $(ADAPTOR) WebObjects.o ${COMMON_OBJFILES}
endif

clean:
	rm -f $(ADAPTOR) *.o $(OTHER_CLEAN_FILES)

$(ERROR):
	@echo $(ERROR)
	exit 1

include ../Adaptor/make.postamble
