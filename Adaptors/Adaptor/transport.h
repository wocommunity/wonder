/*

Copyright © 2000 Apple Computer, Inc. All Rights Reserved.

The contents of this file constitute Original Code as defined in and are
subject to the Apple Public Source License Version 1.1 (the 'License').
You may not use this file except in compliance with the License. 
Please obtain a copy of the License at http://www.apple.com/publicsource 
and read it before usingthis file.

This Original Code and all software distributed under the License are
distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT. 
Please see the License for the specific language governing rights 
and limitations under the License.


*/
#ifndef TRANSPORT_H_INCLUDED
#define TRANSPORT_H_INCLUDED
/*
 *	This defines the basic primitives that must be provided by any
 *	transport mechanism to an application.  This can be implemented in
 *	any manner. http via socket is the basic, but possibilities include 
 *	SSL, DO/IIOP, rpc, etc...
 *
 *	To add a transport, implement the functions described below and
 *	add the transport to transport.c.  See socket.c for an example.
 *
 *	Any major change in this requires a corresponding change to a WOAdaptor
 *	subclass on the application side.
 *
 */

#include "appcfg.h"
#include "wastring.h"
#include "strtbl.h"

#if defined(WIN32)
#include <winsock.h>
#include <time.h>
#endif

struct iovec;
#ifdef NEED_IOVEC_DEFINITION
#ifdef WIN32
struct iovec {
   void *iov_base;
   size_t iov_len;
};
#endif
#endif

struct in_addr;

/*
 *	this is #defined here so that the implementation may #undef it and
 *	replace with any type that's sizeof(void *) (to reduce the need
 *	to always type cast).
 */
struct _netfd;
typedef struct _netfd *net_fd;

#if 0
#if !defined(net_fd)
#define	net_fd	void *					/* opaque pointer */
#endif
#endif
#define	NULL_FD	((net_fd)-1)

struct _wotransport;

/*
 * Status codes for transport operations.
 * Transport implementations should store a status for each
 * connection. If any transport operation ever returns a result
 * other than TR_OK, then all subsequent operations on that
 * connection should immediately fail with TR_ERROR, without
 * attempting to use the connection.
 * (The one exception is the close, which should close the connection.)
 */
typedef enum {
   TR_OK = 0,  /* connection is ok; no error has occurred */
   TR_RESET = 1,   /* connection was reset by peer, or broken pipe error (instance has shut down or crashed) */
   TR_TIMEOUT = 2, /* a timeout occurred on the connection */
   TR_ERROR = 3,   /* some other error has occurred on the connection */
} TR_STATUS;

/*
 *	an open connection
 */
typedef struct _WOConnection {
   void *fd;			/* this implies that sizeof(net_fd) == sizeof(void *)! */
   unsigned char inUse;		/* = 0 when available */
   unsigned char isPooled;
   int port;					/* the port */
   char host[WA_MAX_HOST_NAME_LENGTH];		/* the host */
#ifdef FORKING_WEBSERVER
   int pid;			/* process id of the process that opened the connection */
#endif
   int generation;
} WOConnection;

typedef struct _wotransport {
   const char * const name;		/* name, as used in conf */
   const char * const description;	/* descriptive name */
   void *rsvd;				/* for use of transport module */

   /* 
    * establish connection to application to host on port
    * cto, sto, rto are connect, send and receive timeout in seconds
    * returns the new connection, or NULL_FD if an error occurrs
    */
   net_fd (*openinst)(const char *host, int port, unsigned short cto, unsigned short sto, unsigned short rto, int sbufsiz, int rbufsiz);

   /*
    * close a connection
    */
   void (*close_connection)(net_fd s);

   /*
    * called before a socket is re-used for another connection
    * cleans out any residual characters or other fixups
    * should also check if the connection is still valid and return error if it was dropped
    * if the return is not TR_OK the connection should be closed
    */
   TR_STATUS (*reset_connection)(net_fd s);

   /*
    * Send a null terminated string through s.
    */
   TR_STATUS (*sendline)(net_fd s, const char *buf);

   /*
    * Receive a [\r]\n terminated string.
    * Returns the received data as a String, which must be freed by the caller.
    * Returns NULL if an error or timeout is encountered.
    */
   String *(*recvline)(net_fd s);

   /*
    * Send len bytes from the buffer at buf through s.
    */
   TR_STATUS (*sendbytes)(net_fd s, const char *buf, int len);

   /*
    * Receive, unconditionaly, exactly 'maxlen' bytes.
    * Returns the number of bytes actually received, which will
    * only be less than maxlen if an error occurrs or timeout occurrs.
    */
   int (*recvbytes)(net_fd s, char *buf, int maxlen);

   /*
    * Called to flush any output buffers and send all data.
    */
   TR_STATUS (*flush_connection)(net_fd s);

   /*
    * Perform a scattered send.
    */
   TR_STATUS (*sendBuffers)(net_fd s, struct iovec *buffers, int bufferCount);

   /*
    * return the status code for the connection
    */
   TR_STATUS (*status)(net_fd s);
   
} wotransport;

typedef wotransport * wotransport_t;
extern wotransport_t transport;



/*
 *	cover functions to enable connection pooling.  these functions will
 *	manage a pool of open connections to the instance.
 */


int tr_init(strtbl *dict);


/* Returns a WOConnection which wraps the given net_fd. */
/* This function exists solely to bridge between appcfg.c which uses net_fd, */
/* and response.c which uses WOConnection *. */
WOConnection *tr_wrap_net_fd(net_fd s);


/*
 * Return a connection to the instance, which may be from the connection pool
 * if pooling is enabled. Returns the new connection or NULL if a connection
 * could not be obtained.
 */
WOConnection *tr_open(WOInstanceHandle instHandle);

/*
 * Called to indicate that a connection is no longer needed. If connection
 * pooling is enabled for the instance and poolConnection is nonzero
 * the connection is retained in the instance's connection pool. Otherwise
 * the connection is actually closed.
 */
void tr_close(WOConnection *c, WOInstanceHandle instHandle, int poolConnection);

/*
 * Returns true if the connection was reset by the instance.
 * This would be the case if the instance dies or gets restarted.
 */
int tr_connectionWasReset(WOConnection *c);

/*
 *	copies available transports to the string buffer
 */
void tr_description(String *str);

int mcast_listensocket(int port);
int mcast_send(int s, struct in_addr *mcast_address, int port, const char *msg);
int mcast_recv(int socket, int timeout, char *buffer, int maxlen);
int mcast_collect_replies(int s, char *buffer, int maxlen);
int mcast_close(int socket);

#endif
