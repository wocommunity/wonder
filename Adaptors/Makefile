
include make.config

ifeq "" "${ADAPTOR_BUILD_TARGET}"
ADAPTOR_BUILD_TARGET = adaptors
endif


# By default, only build CGI adaptor.
ifeq "" "$(ADAPTORS)"
ADAPTORS = CGI
endif

COMMON_TARGETS = Adaptor/make.preamble Adaptor/make.postamble

all: ${ADAPTOR_BUILD_TARGET}

adaptors: ${COMMON_TARGETS} $(ADAPTORS)

clean:
	touch ${COMMON_TARGETS}
	for adaptor in $(ADAPTORS) Adaptor ; do \
		echo Cleaning $$adaptor ; \
		( cd $${adaptor} ; make clean ) ; \
	done

OS_NOT_DEFINED:
	@echo OS \"${OS}\" unknown. Check the Makefile.
	exit 1

${COMMON_TARGETS}: Adaptor
	cd Adaptor ; make

CGI::
	cd CGI ; make

Apache::
	cd Apache ; make

Apache2::
	cd Apache2 ; make

Apache2.2::
	cd Apache2.2 ; make

IIS::
	cd IIS ; make

NSAPI::
	cd NSAPI ; make
