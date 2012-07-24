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
#define NEED_IOVEC_DEFINITION

#include "config.h"
#include "WOAppReq.h"
#include "request.h"
#include "log.h"
#include "errors.h"
#include "strtbl.h"
#include "womalloc.h"
#include "MoreURLCUtilities.h"

#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <stdio.h>
#ifndef WIN32
#include <sys/uio.h>
#endif


#define	REQUEST_METHOD_HEADER	"x-webobjects-request-method"
#define	HEADER_CNT  32			/* hint for number of headers/request */



static RequestMethod get_http_method();
static const char *customHeaderFor(const char *header);


HTTPRequest *req_new(const char *method, char *uri)
{
   HTTPRequest *req;

   req = WOCALLOC(1,sizeof(HTTPRequest));
   //ak: adding the request method string, to be able to support other methods like HEAD, GET and POST
   req->method_str = (method ? method : "");
   req->method = get_http_method(method);
   req->request_str = uri;		/* no strdup(), but we free */
   req->shouldProcessUrl = 1;
   return req;
}

void req_free(HTTPRequest *req)
{
   if (req->headers) {
      st_free(req->headers);
   }
   if (req->request_str)		WOFREE(req->request_str);
   if (req->content)			WOFREE(req->content);
   WOFREE(req);
}

void req_allocateContent(HTTPRequest *req, unsigned content_length, int allowStreaming)
{
   if (req) {
      req->content_buffer_size = content_length;
      if (allowStreaming && req->content_buffer_size > REQUEST_STREAMED_THRESHOLD)
      {
         req->content_buffer_size = REQUEST_STREAMED_THRESHOLD;
         WOLog(WO_DBG, "req_allocateContent(): content will be streamed. content length = %d", content_length);
      }
      req->content = WOMALLOC(req->content_buffer_size);
      if (!req->content)
         req->content_buffer_size = 0;
   }
}
      
const char *req_validateMethod(HTTPRequest *req)
{
   if (req->method == HTTP_NO_METHOD) {
      return "WebObjects is a cgi program that should only be "
      "started by a http server";
   }
   if ((req->method == HTTP_UNKNOWN_METHOD) ||
       (req->method == HTTP_PUT_METHOD)) {
      return NULL;
   }

   return NULL;		/* no error */
}


/*
 *	reformat URL to conform to our norms.  also has side effect of
 *	setting 'REQUEST_METHOD' header
 */
void req_reformatRequest(HTTPRequest *req, WOAppReq *app, WOURLComponents *wc, const char *http_version)
{
   char *default_http_version = "HTTP/1.1";
   int http_version_length = http_version ? strlen(http_version) : strlen(default_http_version);

   wc->applicationName.start = app->name;
   wc->applicationName.length = strlen(app->name);
   wc->applicationNumber.start = app->instance;		/* note that this is by reference */
   wc->applicationNumber.length = strlen(app->instance);
   wc->applicationHost.start = app->host;
   wc->applicationHost.length = strlen(app->host);

   if (req->request_str)
      WOFREE(req->request_str);

   /* METHOD + SizeURL + SPACE + http_version_length + \r\n + NULL) */
   req->request_str = WOMALLOC(strlen(req->method_str) + 1 + SizeURL(wc) + 1 + http_version_length + 2 + 1); 

   strcpy(req->request_str, req->method_str);
   strcat(req->request_str," ");
   req_addHeader(req, REQUEST_METHOD_HEADER, req->method_str, 0);
   
   ComposeURL(req->request_str + strlen(req->request_str), wc, req->shouldProcessUrl);

   strcat(req->request_str," ");
   if (http_version)
   {
       strcat(req->request_str,http_version);
       if (strcasecmp(http_version,"HTTP/1.1") == 0)
       {
           req_addHeader(req, "Host", app->host, 0);
       }
   } else {
       strcat(req->request_str,default_http_version);
       req_addHeader(req, "Host", app->host, 0);
   }
   strcat(req->request_str,"\r\n");

   WOLog(WO_INFO,"New request is %s",req->request_str);

   return;
}

/*
 *	common routing to add header called by both addHeader()
 */
inline
static
const char *req_AddHeader_common(HTTPRequest *req,const char *key,const char *value)
{
   const char *customKey;
   const char *hdrkey;

   customKey = customHeaderFor(key);
   hdrkey = (customKey != NULL) ? customKey : key;

   if (req->headers == NULL)
      req->headers = st_new(HEADER_CNT);

   /*
    *	we inspect the headers in line here to grab the content length
    */
   if ((req->content_length == 0) &&
       ((strcasecmp(key, CONTENT_LENGTH) == 0) || (strcasecmp(key, "content_length") == 0)))	/* get content-length */
       req->content_length = atoi(value);

#ifdef DEBUG
       WOLog(WO_DBG,"(req-hdr) %s: %s",key, value);
#endif

       return hdrkey;
}

void req_addHeader(HTTPRequest *req,const char *key, const char *value, int flags)
{
   const char *hdrkey;

   hdrkey = req_AddHeader_common(req, key, value);

   /*
    *	we know that all interfaces (CGI, NSAPI,...) use statically allocated
    *	header keys, always.  *Most* of the time, the value is static too,
    *	but there are cases where it might not be e.g. derived value, like
    *	"server-port", or are always malloc'd.  
    */
   st_add((strtbl *)req->headers, hdrkey, value, flags);

   return;
}

void req_removeHeader(HTTPRequest *req,const char *key)
{
   st_removeKey((strtbl *)req->headers, key);
}

const char *req_HeaderForKey(HTTPRequest *req, const char *key)
{
   if (req->headers)
      return st_valueFor(req->headers, key);
   else
      return NULL;
}


typedef struct {
   net_fd socket;
   int result;
} SendHeadersInfo;


#ifdef WIN32
// see http://support.apple.com/kb/TA26907:
// WebObjects 5.1: How to Improve Performance of IIS Adaptor on
// Microsoft Windows
static void req_appendHeader(const char *key, const char *val, String *headers) 
{
   int valLength = strlen(val);
   while (val[valLength - 1] == '\r' || val[valLength - 1] == '\n') 
   {
      valLength--;
   }
   str_append(headers, key);
   str_appendLiteral(headers, ": ");
   str_appendLength(headers, val, valLength);
   str_appendLiteral(headers, "\r\n");
}

int req_sendRequest(HTTPRequest *req, net_fd socket) 
{
   struct iovec *buffers;
   int bufferCount, appStatus;
   int browserStatus = 0;
   String *headersString;

   buffers = WOMALLOC(3 * sizeof(struct iovec));

   headersString = str_create(req->request_str, 0);
   if (headersString) 
   {
      st_perform(req->headers, (st_perform_callback)req_appendHeader, headersString);
   }
   buffers[0].iov_base = headersString->text;
   buffers[0].iov_len = headersString->length;
   buffers[1].iov_base = "\r\n";
   buffers[1].iov_len = 2;
   bufferCount = 2;
   if (req->content_length > 0) 
   {
      bufferCount++;
      buffers[2].iov_base = req->content;
      buffers[2].iov_len = req->content_buffer_size;
   }
   appStatus = transport->sendBuffers(socket, buffers, bufferCount);
   str_free(headersString);

   /* If we are streaming the content data, continue until we have sent everything. */
   /* Note that we reuse buffers, and the existing content-data buffer. */
   if (req->content_length > req->content_buffer_size)
   {
      int total_sent = req->content_buffer_size;
      int len_read, amount_to_read;
      req->haveReadStreamedData = 1;
      while (total_sent < req->content_length)
      {
         amount_to_read = req->content_length - total_sent;
         if (amount_to_read > req->content_buffer_size)
            amount_to_read = req->content_buffer_size;
         len_read = req->getMoreContent(req, req->content, amount_to_read, 0);
         if (len_read > 0)
         {
            if(appStatus == 0)
            {
               buffers[0].iov_base = req->content;
               buffers[0].iov_len = len_read;
               appStatus = transport->sendBuffers(socket, buffers, 1);
               // 2009/04/28: in case of a transport error, carry on with reading
               //             incoming input stream (= browser data).  That way,
               //             the browser (hopefully) switch to the receive mode
               //             after sending the complete request and receives/shows
               //             the adaptor error message (old behaviour: endless
               //             sending/uploading view).
               if(appStatus != 0)
               {
                  WOLog(WO_ERR, "Failed to send streamed content.");
               }
            }
            total_sent += len_read;
         } else if (len_read < 0) {
            WOLog(WO_ERR, "Failed to read streamed content.");
            browserStatus = -1;
            break;
         }
      }
   }
   WOFREE(buffers);
   if(browserStatus != 0) WOLog(WO_ERR, "error receiving request");
   if (appStatus == 0)
   {
      // 2009/04/30: as long as we haven't received any error message from
      //             the instance, flush the socket to complete the data
      //             transfer!
      appStatus = transport->flush_connection(socket);
   }
   else
      WOLog(WO_ERR, "error sending request");

   return
       ((appStatus != 0)
          ? appStatus
          : browserStatus);
}

#else

static void setupIOVec(const char *key, const char *value, struct iovec **iov)
{
   (*iov)->iov_base = (void *)key;
   (*iov)->iov_len = strlen(key);
   (*iov)++;
   (*iov)->iov_base = ":";
   (*iov)->iov_len = 1;
   (*iov)++;
   (*iov)->iov_base = (void *)value;
   (*iov)->iov_len = strlen(value);
   while (value[(*iov)->iov_len - 1] == '\r' || value[(*iov)->iov_len - 1] == '\n')
      (*iov)->iov_len--;
   (*iov)++;
   (*iov)->iov_base = "\r\n";
   (*iov)->iov_len = 2;
   (*iov)++;
}

int req_sendRequest(HTTPRequest *req, net_fd socket)
{
   struct iovec *buffers, *bufferIterator;
   int bufferCount, result;

   buffers = WOMALLOC((st_count(req->headers) * 4 + 3) * sizeof(struct iovec));
   buffers[0].iov_base = req->request_str;
   buffers[0].iov_len = strlen(req->request_str);
   bufferIterator = &buffers[1];
   st_perform(req->headers, (st_perform_callback)setupIOVec, &bufferIterator);
   bufferIterator->iov_base = "\r\n";
   bufferIterator->iov_len = 2;
   bufferCount = st_count(req->headers) * 4 + 2;
   if (req->content_length > 0)
   {
      bufferCount++;
      bufferIterator++;
      bufferIterator->iov_base = req->content;
      bufferIterator->iov_len = req->content_buffer_size;
   }
   result = transport->sendBuffers(socket, buffers, bufferCount);

   /* If we are streaming the content data, continue until we have sent everything. */
   /* Note that we reuse buffers, and the existing content-data buffer. */
   if (req->content_length > req->content_buffer_size)
   {
      int total_sent = req->content_buffer_size;
      int len_read, amount_to_read;
      req->haveReadStreamedData = 1;
      while (total_sent < req->content_length && result == 0)
      {
         amount_to_read = req->content_length - total_sent;
         if (amount_to_read > req->content_buffer_size)
            amount_to_read = req->content_buffer_size;
         len_read = req->getMoreContent(req, req->content, amount_to_read, 0);
         if (len_read > 0)
         {
            buffers[0].iov_base = req->content;
            buffers[0].iov_len = len_read;
            result = transport->sendBuffers(socket, buffers, 1);
            total_sent += len_read;
         } else if (len_read < 0) {
            WOLog(WO_ERR, "Failed to read streamed content.");
            result = -1;
         }
      }
   }
   WOFREE(buffers);
   if (result == 0)
      result = transport->flush_connection(socket);
   else
      WOLog(WO_ERR, "error sending request");

   return result;
}

#endif


static const char * const GET_METHOD = "GET";
static const char * const POST_METHOD = "POST";
static const char * const HEAD_METHOD = "HEAD";
static const char * const PUT_METHOD = "PUT";

/*
 * get_http_method() find the HTTP method for this request
 */
static RequestMethod get_http_method(const char *method)
{
   if (method) {
      if (!strcmp(method,GET_METHOD)) {
         return HTTP_GET_METHOD;
      } else if (!strcmp(method, POST_METHOD)) {
         return HTTP_POST_METHOD;
      } else if (!strcmp(method, HEAD_METHOD)) {
         return HTTP_HEAD_METHOD;
      } else if (!strcmp(method, PUT_METHOD)) { /* cern 0.9 */
         return HTTP_PUT_METHOD;
      } else
         return HTTP_UNKNOWN_METHOD;
   } else
      return HTTP_NO_METHOD;
}


/*
 *	for backwards compatibility, we translate server header keys to our
 *	custom keys.  No one knows why we did this in the first place, but now
 *	it's become ingrained in some applications so we keep it.
 *
 *	NOTE: the following table must be kept in ascending order on
 *	'server_header' for the implementation of customHeaderFor() to work.
 *
 *	The purpose is to translate what comes in from the server to our own
 *	string.  A bsearch() is used to look up the incoming key in the xlate
 *	table; a better way exists, but that's a job for another day.
 *
 */
typedef	struct _hdrpair {
   const char * const server_header;
   const char * const custom_header;
} hdrpair;

static const hdrpair headerTable[] = {
   {"ANNOTATION_SERVER", "x-webobjects-annotation-server"},
   {"AUTH_PASS", "x-webobjects-auth-pass"},
   {"AUTH_TYPE", "x-webobjects-auth-type"},
   {"CONTENT_ENCODING", "content-encoding"},
   {"CONTENT_LENGTH", CONTENT_LENGTH},
   {"CONTENT_TYPE", CONTENT_TYPE},
   {"DOCUMENT_ROOT", "x-webobjects-documentroot"},
   {"GATEWAY_INTERFACE", "x-webobjects-gateway-interface"},
   {"HTTP_ACCEPT", "accept"},
   {"HTTP_ACCEPT_ENCODING", "accept-encoding"},
   {"HTTP_ACCEPT_LANGUAGE", "accept-language"},
   {"HTTP_ALLOW", "allow"},
   {"HTTP_AUTHORIZATION", "authorization"},
   {"HTTP_COOKIE", "cookie"},
   {"HTTP_DATE", "date"},
   {"HTTP_EXPIRES", "expires"},
   {"HTTP_FROM", "from"},
   {"HTTP_IF_MODIFIED_SINCE", "if-modified-since"},
   {"HTTP_LAST_MODIFIED", "last-modified"},
   {"HTTP_MIME_VERSION", "mime-version"},
   {"HTTP_PRAGMA", "pragma"},
   {"HTTP_REFERER", "referer"},
   {"HTTP_USER_AGENT", "user-agent"},
   {"HTTP_X_WEBOBJECTS_RECORDING", "x-webobjects-recording"},

   {"QUERY_STRING", "x-webobjects-query-string"},
   {"REMOTE_ADDR", "x-webobjects-remote-addr"},
   {"REMOTE_HOST", "x-webobjects-remote-host"},
   {"REMOTE_IDENT", "x-webobjects-remote-ident"},
   {"REMOTE_USER", "x-webobjects-remote-user"},
   {"REQUEST_METHOD", REQUEST_METHOD_HEADER},
   {"SERVER_NAME", "x-webobjects-server-name"},
   {"SERVER_PORT", "x-webobjects-server-port"},
   {"SERVER_SOFTWARE", "x-webobjects-server-software"},
   {NULL,NULL}
};
#define	CUST_HDR_COUNT	(sizeof(headerTable)/sizeof(hdrpair) - 1)

static int compareKey(const void *k1, const void *k2) {
   const char *svrhdr = k1;
   const hdrpair *h2 = (hdrpair *)k2;
   return strcmp(svrhdr,h2->server_header);
}

extern int x_webobjects_headers;	/* config.c */

static const char *customHeaderFor(const char *svrhdr)
{
   hdrpair *xlate;

   /*
    *	see if this "feature" has been turned off
    */
   if (x_webobjects_headers == 0)
      return NULL;

   /*
    *	major optimization - since all of our keys are uppercase,
    *	only look in the table if the hdr is upper
    */
   if (isupper((int)*svrhdr))
      xlate = bsearch(svrhdr, headerTable, CUST_HDR_COUNT, sizeof(hdrpair), compareKey);
   else
      xlate = NULL;
   return (xlate != NULL) ? xlate->custom_header : NULL;
}

