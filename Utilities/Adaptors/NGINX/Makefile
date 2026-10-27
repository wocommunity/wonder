# Makefile for building Nginx WebObjects module
#
# Usage:
#   make NGINX_SRC=/path/to/nginx-1.24.0
# Or using installed system headers:
#   make NGINX_INCLUDES=/usr/include/nginx

NGINX_SRC ?= /usr/local/src/nginx
NGINX_INCLUDES ?= -I$(NGINX_SRC)/src/core \
                  -I$(NGINX_SRC)/src/event \
                  -I$(NGINX_SRC)/src/event/modules \
                  -I$(NGINX_SRC)/src/os/unix \
                  -I$(NGINX_SRC)/src/http \
                  -I$(NGINX_SRC)/src/http/modules \
                  -I$(NGINX_SRC)/objs

CC ?= gcc
CFLAGS ?= -O2 -g -Wall -fPIC -Wno-deprecated-non-prototype -Wno-unused-parameter \
          -DMULTITHREADED_ADAPTOR -DTHREAD_MODEL=2 -DNGX_HTTP_SSL=1 -DNGX_SSL=1 \
          -I../Adaptor $(NGINX_INCLUDES)

ifdef SUPPORT_REFUSENEWSESSION_ATTR
CFLAGS += -DSUPPORT_REFUSENEWSESSION_ATTR
endif
ADAPTOR_GIT_COMMIT ?= $(shell git rev-parse --short=8 HEAD 2>/dev/null)
ifneq "$(ADAPTOR_GIT_COMMIT)" ""
CFLAGS += -DADAPTOR_GIT_COMMIT=\"$(ADAPTOR_GIT_COMMIT)\"
endif
CFLAGS += $(EXTRA_CFLAGS)

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

all: ngx_http_webobjects_module.so

ngx_http_webobjects_module.so: ngx_http_webobjects_module.c $(COMMON_SRC)
	$(CC) $(CFLAGS) -shared -o $@ $^ -lpthread

clean:
	rm -f ngx_http_webobjects_module.so *.o *.dSYM
