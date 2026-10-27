CC = clang
GIT_COMMIT ?= $(shell git rev-parse --short=8 HEAD 2>/dev/null)
ifeq "$(GIT_COMMIT)" ""
ifneq ("$(wildcard ../../../.git-commit)","")
GIT_COMMIT := $(shell cut -c1-8 ../../../.git-commit 2>/dev/null)
endif
endif
ifeq "$(GIT_COMMIT)" ""
GIT_COMMIT := unknown
endif
COMMIT_FLAG = -DADAPTOR_GIT_COMMIT=\"$(GIT_COMMIT)\"
CFLAGS = -O2 -g -Wall -Wsign-compare -I../Adaptor -DMACOS -DMULTITHREADED_ADAPTOR -DTHREAD_MODEL=2 -DALWAYS_LOG $(COMMIT_FLAG)
CFLAGS_SINGLE = -O2 -g -Wall -Wsign-compare -I../Adaptor -DMACOS -DSINGLE_THREADED_ADAPTOR -DALWAYS_LOG $(COMMIT_FLAG)
LDFLAGS = -lpthread

COMMON_SRC = \
	../Adaptor/MoreURLCUtilities.c \
	../Adaptor/Platform.c \
	../Adaptor/WOURLCUtilities.c \
	../Adaptor/WOURLCUtilities_3.c \
	../Adaptor/appcfg.c \
	../Adaptor/cfgparse.c \
	../Adaptor/config.c \
	../Adaptor/hostlookup.c \
	../Adaptor/list.c \
	../Adaptor/listing.c \
	../Adaptor/loadaverage.c \
	../Adaptor/loadbalancing.c \
	../Adaptor/log.c \
	../Adaptor/nbsocket.c \
	../Adaptor/random.c \
	../Adaptor/request.c \
	../Adaptor/response.c \
	../Adaptor/roundrobin.c \
	../Adaptor/shmem.c \
	../Adaptor/strdict.c \
	../Adaptor/strtbl.c \
	../Adaptor/transaction.c \
	../Adaptor/transport.c \
	../Adaptor/wastring.c \
	../Adaptor/womalloc.c \
	../Adaptor/xmlcparser.c \
	../Adaptor/xmlctokenizer.c \
	../Adaptor/xmlparse.c

all: test_wastring test_wastring_single test_adaptor_runner test_adaptor_runner_single

test_wastring: test_wastring.c ../Adaptor/wastring.c ../Adaptor/Platform.c ../Adaptor/womalloc.c ../Adaptor/log.c
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

test_wastring_single: test_wastring.c ../Adaptor/wastring.c ../Adaptor/Platform.c ../Adaptor/womalloc.c ../Adaptor/log.c
	$(CC) $(CFLAGS_SINGLE) -o $@ $^ $(LDFLAGS)

test_adaptor_runner: test_adaptor_runner.c $(COMMON_SRC)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS)

test_adaptor_runner_single: test_adaptor_runner.c $(COMMON_SRC)
	$(CC) $(CFLAGS_SINGLE) -o $@ $^ $(LDFLAGS)

clean:
	rm -rf test_wastring test_wastring_single test_adaptor_runner test_adaptor_runner_single *.o *.dSYM test_shmem_state test_adaptor.log

test: all
	./test_wastring
	./test_adaptor_runner
