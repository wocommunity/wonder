/*

Copyright © 2000-2007 Apple, Inc. All Rights Reserved.

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
/*
 *	To add a transport, implement the required functions and add
 *	the transport to the list below.
 *
 */

#define NEED_IOVEC_DEFINITION
//#define DEBUG 1


#include "config.h"
#include "transport.h"
#include "string.h"
#include "log.h"
#include "womalloc.h"
#include "list.h"
#include "appcfg.h"
#include "shmem.h"
#include <errno.h>
#include <sys/types.h>
#ifndef WIN32
#include <sys/time.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#else
#include <process.h>
#endif

#define MCAST_RECVBUF_MINSIZ	64	/* To collect a multicast reply we require at least this much buffer space */

/* Old winsock header is missing this - the WO4.5 VC++ headers are for VC++ 4.2 */
#ifndef IN_MULTICAST
#define	IN_CLASSD(i)		(((long)(i) & 0xf0000000) == 0xe0000000)
#define	IN_MULTICAST(i)		IN_CLASSD(i)
#endif

/*
 *	declare the struct here...
 */
extern wotransport tr_nbsocket;

wotransport_t transport = &tr_nbsocket;	/* non-blocking IO */

static const char connectionPoolKey[] = "Connection Pool";

#ifdef WIN32
/*
 * A default implementation of sendBuffers that can be used if the platform does not support a scattered write.
 */
static TR_STATUS tr_sendBuffers_noiovec(net_fd appfd, struct iovec *buffers, int bufferCount)
{
   int i, result = 0;

   for (i=0; i<bufferCount && result == TR_OK; i++)
   {
#ifdef DEBUG
       WOLog(WO_DBG,"(req) %s",buffers[i].iov_base);
#endif
      result = transport->sendbytes(appfd, buffers[i].iov_base, buffers[i].iov_len);
   }
   return result;
}
#endif

int tr_init(strtbl *dict)
{
#ifdef WIN32
   transport->sendBuffers = tr_sendBuffers_noiovec;
#endif
   return 0;
}

static void tr_clearConnectionPoolCallback(ShmemArray *array, unsigned int elementNumber)
{
   list *connectionPool;
   int i;

   connectionPool = sha_localDataForKey(array, elementNumber, connectionPoolKey);
   for (i=wolist_count(connectionPool)-1; i!=-1; i--)
      tr_close(wolist_elementAt(connectionPool, i), elementNumber, 0);
   sha_setLocalDataForKey(array, elementNumber, connectionPoolKey, NULL, NULL);
   wolist_dealloc(connectionPool);
}

WOConnection *tr_wrap_net_fd(net_fd s)
{
   WOConnection *c;
   c = WOCALLOC(sizeof(WOConnection), 1);
   if (c)
   {
      c->fd = s;
   }
   return c;
}

/*
 *	connection pool support:
 */
WOConnection *tr_open(WOInstanceHandle instHandle) {
   _WOInstance *inst;
   WOConnection *c = NULL;
   net_fd fd = ((net_fd)0);
   int usedPooledConnection = 0, addedPooledConnection = 0;
   int i;
   list *connectionPool;
#ifdef FORKING_WEBSERVER
   int my_pid;
#endif

#ifdef FORKING_WEBSERVER
   my_pid = getpid();
#endif
   /*
    *	find an available connection in this list
    */
   inst = ac_lockInstance(instHandle);
   if (inst)
   {
      connectionPool = (list *)ac_localInstanceData(instHandle, connectionPoolKey);
      if (connectionPool != NULL)
      {
         for (i = 0; (i < wolist_count(connectionPool)) && (c == NULL); i++) {
            WOConnection *_c;
            _c = wolist_elementAt(connectionPool,i);
            if (_c->inUse == 0) {
               /* Check to be sure we can reuse this connection. There are several reasons we might not be able to. */
               /*  - generation mismatch if the instance has been restarted */
               /*  - this is a forking webserver and we inherited the connection; we cannot reuse it because there is no global lock */
               /*  - host/port mismatch if the instance was reconfigured */
               /*  - reset_connection() fails if there is leftover junk in the buffers */
               /* If any of these checks fail, dump this connection. */
               if ((inst->generation != _c->generation) ||
#ifdef FORKING_WEBSERVER
                   (_c->pid != my_pid) ||
#endif
                   (_c->port != inst->port) || (strcmp(_c->host, inst->host) != 0) ||
                   (transport->reset_connection(_c->fd) != 0))
               {
                  tr_close(_c, instHandle, 0);
                  i = -1;	/* the close removed the element from the list, so decrement the counter */
               } else {
                  usedPooledConnection++;
                  c = _c;
                  c->inUse = 1;
               }
            }
         }
      }
      /*
       *	do we need to create a new connection?
       */
      if (c == NULL) {
		 /*
			Do not hold the lock while
			we make the connection -- it could block
			for 3 seconds (depends on config) and take
			the lock with it
		  
			read out all the data we need from the instance
			while we still hold the lock
		 */
		 char host[WA_MAX_HOST_NAME_LENGTH];
		 unsigned int port;
		 unsigned int connectTimeout, sendTimeout, recvTimeout;
		 unsigned int sendSize, recvSize;
		 
		 /* 
			keep a copy of this in case it changes out from under
			us -- this is basically a serial number for the 
			instance record.  we use this later when we
			store the connection
		 */
		 int generation;
		 
		 strncpy(host, inst->host, WA_MAX_HOST_NAME_LENGTH);
		 port = inst->port;
		 connectTimeout = inst->connectTimeout;
		 sendTimeout = inst->sendTimeout;
		 recvTimeout = inst->recvTimeout;
		 sendSize = inst->sendSize;
		 recvSize = inst->recvSize;
		 
		 generation = inst->generation;

		 /* release the lock for the connect itself */
		 ac_unlockInstance(instHandle);
		 
		 /* make the call referring to our local items, not the 
		    ones in inst */
         fd = transport->openinst(host, port, connectTimeout, sendTimeout, recvTimeout, sendSize, recvSize);

		 /* retake the lock */
		 inst = ac_lockInstance(instHandle);
		 
		 if (!inst) {
			WOLog(WO_WARN, 
				"unable to retake lock for instance %d", instHandle);
				
			if (fd != NULL_FD) {
				/* clean up */
				transport->close_connection(fd);
			}
			
			/* failed */
			return NULL;
		 }
		 
		 /*
			reget the connection pool pointer now that we have
			the lock again
		 */		 
	     connectionPool = (list *)ac_localInstanceData(instHandle, connectionPoolKey);
		 
         if (fd != NULL_FD) {
			/* copy the params, making sure to use our local
			   copies, in case inst has changed */
            c = WOMALLOC(sizeof(WOConnection));
            c->fd = fd;
            c->inUse = 1;
            c->port = port;
            c->isPooled = 0;
            c->generation = generation;
            memcpy(c->host, host, WA_MAX_HOST_NAME_LENGTH);
#ifdef FORKING_WEBSERVER
            c->pid = my_pid;
#endif
            if (inst->connectionPoolSize > 0)
            {
               if (connectionPool == NULL)
               {
                  connectionPool = wolist_new(inst->connectionPoolSize);
                  ac_setLocalInstanceData(instHandle, connectionPoolKey, connectionPool, tr_clearConnectionPoolCallback);
               }
               if (wolist_count(connectionPool) < inst->connectionPoolSize)
               {
                  c->isPooled = 1;
                  wolist_add(connectionPool,c);
                  addedPooledConnection = wolist_count(connectionPool);
                  /* note: might show multiple logs with same peak count since the count in shared memory is updated later */
                  WOLog(WO_INFO,"Created new pooled connection [%d] to %s:%d", addedPooledConnection, inst->host, inst->port);
               }
            }
            if (c->isPooled)
               WOLog(WO_INFO,"Using pooled connection to %s:%d",inst->host,inst->port);
         }
      }
      if (usedPooledConnection || addedPooledConnection)
      {
         if (usedPooledConnection)
            inst->reusedPoolConnectionCount++;
         if (addedPooledConnection > inst->peakPoolSize)
            inst->peakPoolSize = addedPooledConnection;
      }
      ac_unlockInstance(instHandle);
   }
   return c;
}


void tr_close(WOConnection *c, WOInstanceHandle instHandle, int poolConnection) {
   list *connectionPool;

   if (c->isPooled == 1) {
      /*
       *	allow the transport to clean up the connection.  close if
       *	reset returns != 0 or no "Keep-Alive" indication in response hdrs
       */
      if (!poolConnection || transport->reset_connection(c->fd))
      {
         transport->close_connection(c->fd);
         if (instHandle != AC_INVALID_HANDLE)
         {
            if (ac_lockInstance(instHandle))
            {
               connectionPool = (list *)ac_localInstanceData(instHandle, connectionPoolKey);
               wolist_removeAt(connectionPool, wolist_indexOf(connectionPool,c));
               ac_unlockInstance(instHandle);
            }
            WOLog(WO_INFO,"Dumping pooled connection to %s(%d)",c->host, c->port);
         }
         WOFREE(c);
      } else
         c->inUse = 0;
   } else {
      transport->close_connection(c->fd);
      WOFREE(c);
   }
}

int tr_connectionWasReset(WOConnection *c)
{
   return transport->status(c->fd) == TR_RESET;
}


/*
 *	copies available transports to the string buffer
 */
void	tr_description(String *str) {
   str_append(str, transport->name);
}



/* Multicast code for wotaskd discovery */

/*
 * Scheme: Open a UDP listen socket.
 *         Open a multicast send socket; send out message from listen port. Wait on listen port.
 *         Read datagrams for a short time and assemble a list of wotaskd respondents.
 *         Close socket.
 */

static inline int socket_close(int s) {
#ifdef WIN32
   return closesocket(s);
#else
   return close(s);
#endif
}

/*
 * Return a listener socket - not really multicast - any UDP.
 * port can be zero => system chooses a port
 */
int mcast_listensocket(int port) {
   struct sockaddr_in sock_addr;
   int s;
   int rc;

   s = socket(AF_INET, SOCK_DGRAM, 0);
   if (s != -1) {
      /* specify the port where we intend to receive packets */
      sock_addr.sin_family = AF_INET;
      sock_addr.sin_addr.s_addr = htonl(INADDR_ANY);
      sock_addr.sin_port = htons(port);
      rc = bind(s, (struct sockaddr *) &sock_addr, sizeof(sock_addr));
      if (rc < 0) {
         char *errMsg = WA_errorDescription(WA_error());
         WOLog(WO_ERR, "bind() failed, Error: %s", errMsg);
         WA_freeErrorDescription(errMsg);
         socket_close(s);
         s = -1;
      }

      /* No need to call listen() as this is UDP */
   } else {
      char *errMsg = WA_errorDescription(WA_error());
      WOLog(WO_WARN, "socket() failed, Error: %s", errMsg);
      WA_freeErrorDescription(errMsg);
      socket_close(s);
      s = -1;
   }

   return s;
}

/*
 * Send ASCII (nul-terminated C string) message to the specified Multicast address.
 * N.B. It's not necessary to join a multicast group to send to it.
 */
int mcast_send(int s, struct in_addr *mcast_address, int port, const char *msg) {
   int i, rc=-2, len;
   struct sockaddr_in sockaddr;

   /* IN_MULTICAST macro expects host byte order */
   if (IN_MULTICAST(ntohl(mcast_address->s_addr))) {
      sockaddr.sin_family = AF_INET;
      sockaddr.sin_addr.s_addr = mcast_address->s_addr;
      sockaddr.sin_port = htons(port);
      len = strlen(msg) + 1; /* include nul terminator in sent data */
      /* retry sendto() up to 5 times */
      for (i=0; i<5; i++)
      {
         rc = sendto(s, msg, len, 0, (const struct sockaddr *)&sockaddr, sizeof(sockaddr));
         if (rc == len) {
            WOLog(WO_INFO, "Sent request %s OK (%d bytes)", msg, rc);
            break;
         } else {
            int ec = WA_error();
            char *errMsg = WA_errorDescription(ec);
            WOLog(WO_ERR, "sendto() failed, Error: %s", errMsg);
            WA_freeErrorDescription(errMsg);
         }
      }
   } else {
      WOLog(WO_ERR,"<transport>: non-multicast address given to mcast_send");
      rc = -2;
   }

   return rc;
}

/*
 * Read a packet into the buffer. Give up if it takes > timeout usecs.
 * Returns the number of bytes read, 0 on timeout, or -1 on error.
 */
int mcast_recv(int socket, int timeout, char *buffer, int maxlen) {
   int ret, rc;
   fd_set readfds;
   struct timeval tv;

   if (maxlen < MCAST_RECVBUF_MINSIZ) {
      WOLog(WO_ERR, "mcast_recv: buffer size (%d) too small", maxlen);
      return -1;
   }

   FD_ZERO(&readfds); /* initializes a descriptor set to the null set */
   FD_SET(socket, &readfds); /* includes a particular descriptor in fdset */

   tv.tv_sec  = timeout / 1000000;
   tv.tv_usec = timeout % 1000000;

   rc = select(socket + 1, &readfds, NULL, NULL, &tv);

   if (rc > 0) {
      if (FD_ISSET(socket, &readfds)) {
         /* Read one packet */
         rc = recvfrom(socket, buffer, maxlen, 0, NULL, 0);
         if (rc > 0) {
            //WOLog(WO_INFO, "recvfrom() OK, %s (%d bytes)", buffer, rc);
            ret = rc;
         } else {
            int ec = WA_error();
            char *errMsg = WA_errorDescription(ec);
            WOLog(WO_ERR, "recvfrom() failed, Error: %d", ec);
            WA_freeErrorDescription(errMsg);
            ret = -1;
         }
      } else {
         WOLog(WO_DBG, "mcast_recv(): select() OK but no active FD (rc = %d)", rc);
         /* Treat this as a timeout */
         ret = 0;
      }
   } else if (rc == 0) {
      /* Timeout */
      ret = 0;
   } else {
      int ec = WA_error();
      char *errMsg = WA_errorDescription(ec);
      WOLog(WO_ERR, "select() failed, Error: %d", ec);
      WA_freeErrorDescription(errMsg);
      ret = -1;
   }
   return ret;
}

/*
 * Determine the max length of time we will wait for replies from wotaskd's.
 * This really needs to be a bit more adaptive than this simple algorithm.
 * Something like an exponential backoff with a total max wait limit might work better.
 */
int mcast_collect_replies(int s, char *buffer, int maxlen) {
   int reply_count = 0;
   int rc = 1;
   int timeout = 10000;
   /* nul-terminate string to begin with */
   *buffer = '\0';

   while (rc > 0) {
      rc = mcast_recv(s, timeout, buffer, maxlen);
      if (rc > 0) {
         buffer[rc - 1] = ',';
         buffer += rc;
         maxlen -= rc;
         reply_count++;
      } else if (rc < 0) {
         char *errMsg = WA_errorDescription(WA_error());
         WOLog(WO_ERR, "mcast_collect_replies Error: %s", errMsg);
         WA_freeErrorDescription(errMsg);
         *buffer = '\0';
         break;
      }
   }
   if (rc == 0)
      WOLog(WO_INFO, "mcast_collect_replies() got %d replies, timed out after %d usec", reply_count, timeout);
   *buffer = '\0';
   if (reply_count > 0) {
      /* Back up before the comma */
      buffer--;
      *buffer = '\0';
   }
   return reply_count;
}

int mcast_close(int socket) {
   return socket_close(socket);
}

