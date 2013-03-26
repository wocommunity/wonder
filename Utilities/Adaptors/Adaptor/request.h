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
#ifndef	REQUEST_H_INCLUDED
#define	REQUEST_H_INCLUDED
/*
 *	Adaptor's abstration for a WebObjects request.
 *
 *	Includes all the relevant info.
 *
 */
#include "transport.h"
#include "WOAppReq.h"
#include "WOURLCUtilities.h"


typedef	enum {
	HTTP_NO_METHOD = -2,
	HTTP_UNKNOWN_METHOD,
	HTTP_GET_METHOD,
	HTTP_POST_METHOD,
	HTTP_HEAD_METHOD,
	HTTP_PUT_METHOD,
} RequestMethod;

typedef int (*req_getMoreContentCallback)(void *handle, void *buffer, int bufferSize, int mustFill);

/*
 *	an API independent definition of a request
 */
typedef struct _HTTPRequest {
	RequestMethod	method;		/* get/put/post/... */
	const char *method_str;			/* the http request (includes CRLF) */
	char *request_str;			/* the http request (includes CRLF) */
	void *headers;				/* (strtbl *) but you don't need to know */
	void *api_handle;			/* api specific pointer */
	unsigned content_length;
	void *content;
        unsigned content_buffer_size;
        req_getMoreContentCallback getMoreContent;
        int haveReadStreamedData;
        int shouldProcessUrl;
#ifdef IIS
        /* for IIS we have to keep track of how much we have read */
        // 2009/04/27: IIS is using DWORD in the API layer and a DWORD
        //             is an "unsigned long" value:
        unsigned long total_len_read;
#endif
} HTTPRequest;


HTTPRequest *req_new(const char *method, char *uri);
void req_free(HTTPRequest *req);

/* Allocates the buffer holding content data (HTTPReauest.content). */
/* If allowStreaming is 0, the buffer will be the size specified by content_length. */
/* If allowStreaming is 1, the buffer may be smaller than content_length. */
/* Sets content, and content_buffer_size. Either of these should be checked in case the allocation fails. */
void req_allocateContent(HTTPRequest *req, unsigned content_length, int allowStreaming);

/*
 *	convenience for all adaptors, returns error string or null
 */
const char *req_validateMethod(HTTPRequest *req);

/*
 *	reformat request into a standard WebObjects request 
 */
void req_reformatRequest(HTTPRequest *req, WOAppReq *app, WOURLComponents *wc, const char *http_version);

/*
 *	addHeader will customize the headers so that a WebObjects app
 * 	will see the same set regardless of the server & server adaptor.
 *      flags is passed to st_add(), and determines whether the key and
 *      value are copied or freed.
 */
void req_addHeader(HTTPRequest *req,const char *key, const char *value, int flags);


/*
 *	case is ignored for keys
 */
const char *req_HeaderForKey(HTTPRequest *req, const char *key);

/*
 *      remove a header
 */
void req_removeHeader(HTTPRequest *req,const char *key);

/*
 *	request handling:
 *	sends the request to the application via the open'd socket
 *	returns:
 *         0 if successful
 *         a TR_* error code if there was an error sending data to the instance
 *         -1 if there was a failure reading streamed content data for the request
 */
int req_sendRequest(HTTPRequest *req, net_fd socket);


#endif
