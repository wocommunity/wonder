# NOTE: if you want to recompile the Apache 1.3 module on Mac OS X 10.5, you're
# going to have to download Apache 1.3 from the foundation and link against those binaries.

include make.config

ifeq "" "${ADAPTOR_BUILD_TARGET}"
ADAPTOR_BUILD_TARGET = adaptors
endif


# By default, only build CGI adaptor.
ifeq "" "$(ADAPTORS)"
ADAPTORS = CGI IIS
endif

COMMON_TARGETS = Adaptor/make.preamble Adaptor/make.postamble

all: ${ADAPTOR_BUILD_TARGET}

adaptors: ${COMMON_TARGETS} $(ADAPTORS)

clean:
	touch ${COMMON_TARGETS}
	for adaptor in $(ADAPTORS) Adaptor ; do \
		echo Cleaning $$adaptor ; \
		( cd $${adaptor} ; ${MAKE} clean ) ; \
	done

OS_NOT_DEFINED:
	@echo OS \"${OS}\" unknown. Check the Makefile.
	exit 1

${COMMON_TARGETS}: Adaptor
	cd Adaptor ; ${MAKE}

CGI::
	cd CGI ; ${MAKE}

FastCGI::
	cd FastCGI ; ${MAKE}

Apache::
	cd Apache ; ${MAKE}

Apache2::
	cd Apache2 ; ${MAKE}

Apache2.2::
	cd Apache2.2 ; ${MAKE}

Apache2.4::
	cd Apache2.4 ; ${MAKE}

IIS::
	cd IIS ; ${MAKE}

NSAPI::
	cd NSAPI ; ${MAKE}

