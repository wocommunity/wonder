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
#ifndef	HTTP_RESPONSE_H
#define HTTP_RESPONSE_H

/*
 *	encapsulates the response from the webapp.
 * 
 */
#include "transport.h"
#include "wastring.h"

typedef struct _HTTPResponse {
	unsigned status;
	char *statusMsg;
	struct _strtbl *headers;
	unsigned content_length;
	void *content;
        int flags;
        /* List of Strings that contain data associated with this response */
        /* These Strings get freed along with the response. */
        String *responseStrings;
} HTTPResponse;


/* Flags for use in HTTPResponse flags field */
#define RESP_DONT_FREE_CONTENT 	1	/* don't free the content data (typically if it is owned by a String) */
#define RESP_HTTP10		2	/* set if the response was HTTP/1.0 */
#define RESP_HTTP11		4	/* set if the response was HTTP/1.1 */


HTTPResponse *resp_new(char *status);

void resp_free(HTTPResponse *resp);

#define resp_addStringToResponse(resp, str) { str->next = resp->responseStrings; resp->responseStrings = str; }

/*
 *	Constructor: retrieves the response from the web app
 *      Reads response headers, but not content.
 */
HTTPResponse *resp_getResponseHeaders(net_fd socket);

/*
 *      Reads response content.
 */
HTTPResponse *resp_getResponseContent(HTTPResponse *resp, net_fd socket);

/*
 *	generate an error response 
 */
HTTPResponse *resp_errorResponse(const char *msg, int status);

/*
 *	generate a redirect to the given URL (mostly for fancy HTML messages)
 */
HTTPResponse *resp_redirectedResponse(const char *path);

/*
 * Adds a header to the response. rawhdr contains the new header, and it is
 * freed as a result of this call.
 */
void resp_addHeader(HTTPResponse *resp, String *rawhdr);

/*
 *	convenience (mostly for CGI) to repackage headers into 1 buffer
 */
String *resp_packageHeaders(HTTPResponse *resp);

/*
 *	for debugging, returns a malloc'd string description 
 */
char *resp_description(HTTPResponse *resp);


#endif
