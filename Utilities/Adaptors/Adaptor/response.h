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
        int flags;
        /* List of Strings that contain data associated with this response */
        /* These Strings get freed along with the response. */
        String *responseStrings;
        void *content;
        unsigned content_length;
        unsigned content_buffer_size;
        unsigned content_read; /* total amount of data read from the instance */
        unsigned content_valid; /* amount of valid data in content buffer */
        int (*getMoreContent)(struct _HTTPResponse *resp, void *buffer, int bufferSize);
        
        WOConnection *instanceConnection;
        WOInstanceHandle instHandle;
        int keepConnection;
} HTTPResponse;


/* Flags for use in HTTPResponse flags field */
#define RESP_DONT_FREE_CONTENT 	1	/* don't free the content data (typically if it is owned by a String) */
#define RESP_HTTP10		2	/* set if the response was HTTP/1.0 */
#define RESP_HTTP11		4	/* set if the response was HTTP/1.1 */
#define RESP_CLOSE_CONNECTION	8	/* set if instanceConnection should be closed */
#define RESP_LENGTH_INVALID    16       /* 2009/06/09: set if the content length of the response is invalid */
#define RESP_LENGTH_EXPLICIT   32       /* 2009/06/10: set if the WebObjects instance provides a content-length value */
#define RESP_CONTENT_TYPE_SET  64       /* 2011/11/16: set if the WebObjects instance provides a content-type value */

HTTPResponse *resp_new(char *status, WOInstanceHandle instHandle, WOConnection *instanceConnection);

void resp_free(HTTPResponse *resp);

#define resp_addStringToResponse(resp, str) { str->next = resp->responseStrings; resp->responseStrings = str; }

/*
 *	Constructor: retrieves the response from the web app
 *      Reads response headers, but not content.
 */
HTTPResponse *resp_getResponseHeaders(WOConnection *instanceConnection, WOInstanceHandle instHandle);


/*
 *      Reads response content.
 *      This function sets up the following response fields: content, content_buffer_size, content_read, content_valid
 *      It should be called repeatedly to read response content data from the instance. The data is read into content
 *      (which is allocated the first time this function is called). The amount of valid data in content is returned in
 *      content_valid. This data should be sent back to the client. This function should be called in a loop to sent the
 *      entire response content back to the client.
 *	However, if allowStreaming is 0, then the entire content is read in one go.
 *	Returns 0 on success, -1 if no data was received from the WebObjects application and > 0 if an incomplete data
 *      package was received (the return value describes the number of received bytes in such a situation).
 *
 *      2009/06/09: Description update to reflect return value changes.
 */
int resp_getResponseContent(HTTPResponse *resp, int allowStreaming);

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
