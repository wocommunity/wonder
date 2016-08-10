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
 *	Yet another socket based transport.  This one is now the default and is non-blocking.
 *
 *	The good news is that this one is Unix/Win/Netscape3+ compatible
 *	and should supersede all others.
 *
 */

#include "config.h"
#include "hostlookup.h"
#include "transport.h"
#include "log.h"
#include "womalloc.h"

#include <string.h>
#include <errno.h>
#if	defined(WIN32)
#include <winsock.h>
#include <io.h>
#else
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <sys/uio.h>
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <fcntl.h>
#endif

#ifdef WIN32
#define closeSocket(sd) closesocket(sd)
#define WA_intr(error)  (error == WSAEINTR)
#define WA_wouldBlock(error) (error == WSAEWOULDBLOCK)
#define WA_inProgress(error)  (error == WSAEINPROGRESS)
#define WA_connectionReset(error) (error == WSAECONNRESET)
#define WA_brokenPipe(error) (error == WSAECONNABORTED)
#define WA_msgSize(error) (error == WSAEMSGSIZE)
#else
#define closeSocket(sd) close(sd)
#define WA_intr(error)  (error == EINTR)
#define WA_wouldBlock(error) (error == EWOULDBLOCK || error == EAGAIN)
#define WA_inProgress(error)  (error == EINPROGRESS)
#define WA_connectionReset(error) (error == ECONNRESET)
#define WA_brokenPipe(error) (error == EPIPE)
#define WA_msgSize(error) (error == EMSGSIZE)
#endif


/*
 *	our socket/buffer pair
 */
#define	NETBUFSZ	16384	/* we really only want to make one system call if we can */
typedef	struct _netfd {
   int	s;				/* the socket */
   TR_STATUS status;
   char *pos;
   int count;
   int send_to, recv_to;
   char buf[NETBUFSZ];
} netfd;
#define	net_fd	netfd *



static int setBlockingState(int sd, int wantNonBlocking) {
   int rc = 0;
#ifndef WIN32
   int flags;
   int toggle = 0;

#if defined(O_NONBLOCK)
   toggle = O_NONBLOCK;
#elif defined(O_NDELAY)
   toggle = O_NDELAY;
#elif defined(FNDELAY)
   toggle = FNDELAY;
#else
#error Non Blocking flag not available on this platform.
#endif

   flags = fcntl(sd, F_GETFL, 0);
   if (wantNonBlocking)
      flags = flags | toggle;
   else
      flags = flags & ~toggle;
   rc = fcntl(sd, F_SETFL, flags);
#else /* WIN32 */
   rc = ioctlsocket (sd, FIONBIO, (u_long *)&wantNonBlocking);
#endif /* WIN32 */
   return rc;
}


static int nonBlockingConnectHostent(int sd, int connectTimeout, struct hostent *hostentry, unsigned int port) {
   int n, rc;
   fd_set wset;
   struct timeval tval;
   struct sockaddr_in connect_addr;
   int connect_addr_len;

   rc = 0;

   if (hostentry) {
      connect_addr.sin_family = AF_INET;
      connect_addr.sin_port = htons((unsigned short)port);
      memcpy(&connect_addr.sin_addr, hostentry->h_addr_list[0], hostentry->h_length);
      connect_addr_len = sizeof(struct sockaddr_in);

      /* Since we're non-blocking, we expect an EINPROGRESS error. */
      if ((n = connect(sd, (struct sockaddr *)&connect_addr, connect_addr_len)) < 0) {
         int ec = WA_error();
         /* Note that on Windows, connect returns WSAEWOULDBLOCK, not WSAEINPROGRESS. */
         if (!WA_inProgress(ec) && !WA_wouldBlock(ec)) {
            char *errMsg = WA_errorDescription(ec);
            WOLog(WO_ERR, "WOSocket: connect error in NB connect: %s", errMsg);
            WA_freeErrorDescription(errMsg);
            return -1;
         }
      }

      /* If n = 0, we connected immediately (e.g. client = host). */
      if (n) {
         FD_ZERO(&wset);
         FD_SET(sd, &wset);
         tval.tv_sec = connectTimeout;
         tval.tv_usec = 0;

         if ((n = select(sd + 1, NULL, &wset, NULL, &tval)) < 0)
         {
            WOLog(WO_DBG, "WOSocket: select failed");
            return -1;
         }

         if (n == 0)
         {
            /* timed out */
            WOLog(WO_DBG, "WOSocket: connect timeout in NB connect");
            return -2;
         }
      }
   } else {
      WOLog(WO_ERR, "WOSocket: hostent NULL - unknown host?");
      rc = -1;
   }
   return rc;
}


/*
 * Read up to ib_capacity bytes from sd into input_buffer, with a timeout of receiveTimeout seconds.
 * Returns the number of bytes read, -1 on error any error (sets appfd->status)
 */
static int nonBlockingRecv(net_fd appfd, int receiveTimeout, char *input_buffer, int ib_capacity) {
   int rc;
   int retryTimeout;

   retryTimeout = receiveTimeout / 2;
   receiveTimeout -= retryTimeout;
   do {
      rc = recv(appfd->s, input_buffer, ib_capacity, 0);
      if (rc < 0) {
         int ec = WA_error();
         if (WA_intr(ec)) {
            /* Interrupted system call. Try again. */
            continue;
#ifndef WIN32
         } else if (WA_wouldBlock(ec)) {
#else
            /* Don't know why, but Windows sometimes returns ERROR_ALREADY_EXISTS. */
            /* A retry seems to succeed. */
         } else if (WA_wouldBlock(ec) || ec == ERROR_ALREADY_EXISTS) {
#endif
            /* We would block on the socket. Set up a select with a timeout. */
            int result;
            fd_set readfds;
            struct timeval tv;
            
            tv.tv_sec  = receiveTimeout;
            tv.tv_usec = 0;

            FD_ZERO(&readfds);
            FD_SET(appfd->s, &readfds);
            result = select(appfd->s + 1, &readfds, NULL, NULL, &tv);
            if (result > 0 && FD_ISSET(appfd->s, &readfds)) {
               continue;
            } else if (result == 0) {
               if (retryTimeout)
               {
                  receiveTimeout = retryTimeout;
                  retryTimeout = 0;
                  continue;
               }
               
               /* select timeout (don't log if we were just polling) */
               if (receiveTimeout > 0)
                  WOLog(WO_DBG, "nonBlockingRecv(): timed out (%d sec)", receiveTimeout+retryTimeout);
               appfd->status = TR_TIMEOUT;
               return -1;
            } else {
               ec = WA_error();
               if (!WA_intr(ec)) {
                  char *errMsg = WA_errorDescription(ec);
                  WOLog(WO_WARN, "nonBlockingRecv(): select() failed: %s", errMsg);
                  WA_freeErrorDescription(errMsg);
                  appfd->status = TR_ERROR;
                  return -1;
               }
            }

         } else {
            /* We can get here if the peer closes the connect.  This can be expected. */
            char *errMsg = WA_errorDescription(ec);
            WOLog(WO_DBG, "nonBlockingRecv(): recv() failed: %s", errMsg);
            WA_freeErrorDescription(errMsg);
            appfd->status = TR_RESET;
            return -1;
         }
      } else if (rc == 0) {
          /* We can get here if the peer closes the connect.  This can be expected. */
          WOLog(WO_DBG, "nonBlockingRecv(): recv() returned 0 (connection closed)");
          appfd->status = TR_RESET;
          return -1;
      }
   } while (rc < 0 && appfd->status == TR_OK);

   return rc;
}


static net_fd openapp(const char *hostName, int port, unsigned short cto, unsigned short sto, unsigned short rto, int sbufsiz, int rbufsiz)
{
   int s = 0;
   struct in_addr;
   net_fd appfd;
   struct hostent *host;

   host = hl_find(hostName);
   if (host == NULL)
   {
      WOLog(WO_ERR, "openapp(): host lookup failed for %s", hostName);
      return NULL_FD;
   }
   
   WOLog(WO_INFO, "attempting to connect to %s on port %d",host->h_name,port);

   s = socket(AF_INET, SOCK_STREAM, 0);
   if (s < 0) {
      char *errMsg = WA_errorDescription(WA_error());
      WOLog(WO_ERR,"couldn't create socket to %s (%d): %s", host->h_name, port, errMsg);
      WA_freeErrorDescription(errMsg);
      return NULL_FD;
   }

   /* set send buffer size */
   if (sbufsiz != 0) {
      if (setsockopt(s, SOL_SOCKET, SO_SNDBUF, (void *)&sbufsiz, sizeof(sbufsiz)) < 0) {
         char *errMsg = WA_errorDescription(WA_error());
         WOLog(WO_WARN, "openapp(): error setting send buffer size to %d: %s", sbufsiz, errMsg);
         WA_freeErrorDescription(errMsg);
      }
   }

   /* set receive buffer size */
   if (rbufsiz != 0) {
      if (setsockopt(s, SOL_SOCKET, SO_RCVBUF, (void *)&rbufsiz, sizeof(rbufsiz)) < 0) {
         char *errMsg = WA_errorDescription(WA_error());
         WOLog(WO_WARN, "openapp(): error setting receive buffer size to %d: %s", rbufsiz, errMsg);
         WA_freeErrorDescription(errMsg);
      }
   }

   /* Set the socket to be non-blocking. */
   if (setBlockingState(s, 1) == -1) {
      char *errMsg = WA_errorDescription(WA_error());
      WOLog(WO_ERR,"openapp(): couldn't set socket to nonblocking");
      WA_freeErrorDescription(errMsg);
      closeSocket(s);
      return NULL_FD;
   }

   /* attempt to connect */
   if (nonBlockingConnectHostent(s, cto, host, port) < 0) {
      char *errMsg = WA_errorDescription(WA_error());
      WOLog(WO_ERR,"couldn't connect to %s (%d): %s", host->h_name, port, errMsg);
      WA_freeErrorDescription(errMsg);
      closeSocket(s);
      return NULL_FD;
   }

   /* set up the buffer */
   appfd = WOMALLOC(sizeof(netfd));
   appfd->s = s;
   appfd->status = TR_OK;
   appfd->pos = 0;
   appfd->send_to = sto;
   appfd->recv_to = rto;
   appfd->count = 0;
   return appfd;
}


static void	closeapp(net_fd s)
{
   shutdown(s->s, 2);
   closeSocket(s->s);
   WOFREE(s);		/* release the buffer */
}


static TR_STATUS reset(net_fd appfd)
{
   int result, warned = 0, n;
   struct timeval tv;
   fd_set wset;

   if (appfd->status != TR_OK)
      return TR_ERROR;
   
   /* drain any data from the socket */
   do {
      result = nonBlockingRecv(appfd, 0, appfd->buf, NETBUFSZ);
      if (result > 0 && warned == 0)
      {
         WOLog(WO_WARN, "reset(): leftover data in socket buffer");
         warned = 1;
      }
      if (result == -1 && appfd->status == TR_TIMEOUT)
         appfd->status = TR_OK;
   } while (result > 0);
   /* clear our buffer */
   if (appfd->status == TR_OK)
   {
      if (appfd->count != 0)
         WOLog(WO_WARN, "reset(): leftover data in buffer");
      appfd->count = 0;
      appfd->pos = appfd->buf;

      FD_ZERO(&wset);
      FD_SET(appfd->s, &wset);
      tv.tv_sec = 0;
      tv.tv_usec = 0;

      n = select(appfd->s + 1, NULL, &wset, NULL, &tv);
      if (n != 1)
      {
         WOLog(WO_WARN, "reset(): connection dropped");
         appfd->status = TR_RESET;
      }
   }
   return appfd->status;
}

static TR_STATUS sendbytes(net_fd appfd, const char *buf, int len)
{
   int remaining, sent, n;
   int s = appfd->s;
   struct timeval tv;
   fd_set wset;
   TR_STATUS ret = TR_OK;

   if (appfd->status != TR_OK)
      return TR_ERROR;
   
   remaining = len;
   while (remaining > 0 && ret == TR_OK) {
      while((sent = send(s, (void *)buf, remaining, 0)) < 0 && ret == TR_OK) {
         int ec = WA_error();
         if (WA_wouldBlock(ec)) {
            FD_ZERO(&wset);
            FD_SET(s, &wset);
            tv.tv_sec = appfd->send_to;
            tv.tv_usec = 0;

            n = select(s + 1, NULL, &wset, NULL, &tv);
            if (n == 0) {
               /* Timed out */
               WOLog(WO_DBG, "sendbytes(): timed out");
               ret = TR_TIMEOUT;
               break;
            } else if (n < 0) {
               char *errMsg = WA_errorDescription(WA_error());
               WOLog(WO_ERR, "sendbytes(): select error: %s", errMsg);
               WA_freeErrorDescription(errMsg);
               ret = TR_ERROR;
               break;
            }
            /* Drop back into the while loop and retry the send() */
         } else if (WA_intr(ec)) {
            /* Try send again */
         } else if (WA_brokenPipe(ec)) {
            ret = TR_RESET;
         } else {
            /* If we get EPIPE or any other error, bail out. */
            char *errMsg = WA_errorDescription(WA_error());
            WOLog(WO_ERR, "sendbytes(): send error: %s", errMsg);
            WA_freeErrorDescription(errMsg);
            ret = TR_ERROR;
            break;
         }
      }

      /* if we get here with sent < 0, we have a failure */
      if (ret != TR_OK && ret != TR_RESET) {
         WOLog(WO_ERR,"sendbytes(): failed to send data to app");
      } else {
         remaining -= sent;
         buf += sent;
      }
   }
   return ret;
}

static TR_STATUS sendline(net_fd s, const char *buf)
{
   return sendbytes(s, buf, strlen(buf));
}

#ifdef WIN32
/* On windows this is implemented as a sequence of calls to sendbytes. See tr_sendBuffers(). */
static TR_STATUS sendBuffers(net_fd appfd, struct iovec *buffers, int bufferCount)
{
   WOLog(WO_ERR, "sendBuffers() called on Windows.");
   return TR_ERROR;
}
#else
static TR_STATUS sendBuffers(net_fd appfd, struct iovec *buffers, int bufferCount)
{
   struct msghdr msg;
   int sent, n, s = appfd->s;
   struct timeval tv;
   fd_set wset;
   TR_STATUS ret = TR_OK;

   if (appfd->status != TR_OK)
      return TR_ERROR;
   
   while (bufferCount > 0 && ret == TR_OK)
   {
      memset(&msg, 0, sizeof(msg));
      msg.msg_iov = buffers;
      msg.msg_iovlen = bufferCount;
      sent = sendmsg(s, &msg, 0);
      if (sent < 0) {
         int ec = WA_error();
         if (WA_msgSize(ec)) {
            if (bufferCount > 1)
            {
               ret = sendBuffers(appfd, buffers, bufferCount/2);
               if (ret == TR_OK)
                  ret = sendBuffers(appfd, &buffers[bufferCount/2], bufferCount - bufferCount/2);
               bufferCount = 0;
            } else
               ret = TR_ERROR;
         } else if (WA_wouldBlock(ec)) {
            FD_ZERO(&wset);
            FD_SET(s, &wset);
            tv.tv_sec = appfd->send_to;
            tv.tv_usec = 0;

            n = select(s + 1, NULL, &wset, NULL, &tv);
            if (n == 0) {
               /* Timed out */
               WOLog(WO_DBG, "sendBuffers(): timed out");
               ret = TR_TIMEOUT;
            } else if (n < 0) {
               char *errMsg = WA_errorDescription(WA_error());
               WOLog(WO_ERR, "sendbytes(): select error: %s", errMsg);
               WA_freeErrorDescription(errMsg);
               ret = TR_ERROR;
            }
            /* Drop back into the while loop and retry the send() */
         } else if (WA_intr(ec)) {
            /* Try send again */
         } else if (WA_brokenPipe(ec)) {
            ret = TR_RESET;
         } else {
            /* If we get any other error, bail out. */
            char *errMsg = WA_errorDescription(WA_error());
            WOLog(WO_ERR, "sendBuffers(): send error: %s", errMsg);
            WA_freeErrorDescription(errMsg);
            ret = TR_ERROR;
            break;
         }
      } else {
         /* be sure all data was sent */
         while (sent > 0)
         {
            if (sent >= buffers->iov_len)
            {
               sent -= buffers->iov_len;
               buffers++;
               bufferCount--;
            } else {
               buffers->iov_base = (caddr_t)buffers->iov_base + sent;
               buffers->iov_len -= sent;
               sent = 0;
            }
         }
         while (bufferCount > 0 && buffers->iov_len == 0)
         {
            buffers++;
            bufferCount--;
         }
      }
   }
   appfd->status = ret;
   return ret;
}
#endif /* WIN32 */

static void fillbuf(net_fd appfd)
{
   int n;
   char *buf = appfd->buf;

   n = nonBlockingRecv(appfd, appfd->recv_to, buf, NETBUFSZ);

   if (n > 0) {
      appfd->count = n;
      appfd->pos = appfd->buf;
   }
}

static String *recvline(net_fd s)
{
   int len;
   String *result;
   int gotCRLF = 0;
   
   if (s->status != TR_OK) {
       WOLog(WO_ERR, "Request failed with status %d", s->status);
       return NULL;
   }
   
   result = str_create(NULL, 0);
   if (!result)
   {
       WOLog(WO_ERR, "Error creating String");
       return NULL;
   }
   
   do
   {       
       if (s->count == 0)
       {
           fillbuf(s);
           if (s->status != TR_OK || s->count == 0)
           {
               WOLog(WO_ERR, "Request failed with status %d (fillbuf)", s->status);
               str_free(result);
               return NULL;
           }
       }

       for (len = 0; len < s->count; len++)
       {
           if (s->pos[len] == '\r' || s->pos[len] == '\n')
           {
               gotCRLF = 1;
               break;
           }
       }

       // if header length > 10240 drop header (see below)
       if (result->length < 10240)
       {
           if(str_appendLength(result, s->pos, len) != 0)
               WOLog(WO_ERR, "str_appendLength failed (%s, %s, %d)", result, s->pos, len);
       }

       s->count -= len;
       s->pos += len;

   } while (!gotCRLF);

   // consume CR/LF
   s->count -= 1;
   s->pos += 1;

   len = 0;
   
   if (s->pos[-1] == '\r')
   {
      /* saw a \r - need to look ahead 1 char for a \n */
      if (s->count == 0)
      {
         fillbuf(s);
         if (s->status != TR_OK || s->count == 0)
         {
             WOLog(WO_ERR, "Request failed with status %d (fillbuf lookahead)", s->status);
             str_free(result);
             return NULL;
         }
      }

       if (s->pos[len] == '\n')
         len++;
   }
   
   s->count -= len;
   s->pos += len;

   /* place an arbitrary limit on the length of a header line */
   if (result->length > 10240)
   {
       WOLog(WO_ERR, "Header length > 10240");
       str_free(result);
       return NULL;
   }

   return result;
}


static int recvbytes(net_fd s, char *buf, int maxlen)
{
   int n, count;

   if (s->status != TR_OK)
      return 0;
   
   /*
    *	we may have some residual in the buffer to clear out
    */
   n = (s->count > maxlen) ? maxlen : s->count;
   if (n != 0) {
      memcpy(buf, s->pos, n);
      s->pos += n;
      s->count -= n;
      maxlen -= n;
   }
   /*
    *	get the rest (if any)
    */
   while ( maxlen > 0 ) {
      count = nonBlockingRecv(s, s->recv_to, buf+n, maxlen);
      if ( count < 1 )
         return n;
      n += count;
      maxlen -= count;
   }
   return n;
}

static TR_STATUS flush(net_fd s)
{
   return TR_OK;		/* nothing to flush */
}

static TR_STATUS connectionStatus(net_fd s)
{
   return s->status;
}


/*
 *	the transport definition
 */
wotransport tr_nbsocket = {
   "nbsocket",				/* name */
   "non-blocking socket",	 	/* description */
   NULL,
   openapp,
   closeapp,
   reset,
   sendline,
   recvline,
   sendbytes,
   recvbytes,
   flush,
   sendBuffers,
   connectionStatus
};
