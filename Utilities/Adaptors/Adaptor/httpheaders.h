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
/*
 *	Some server api's (actually, just ISAPI) don't just hand up
 *	all the headers & info.  We need to ask for the information.  This
 *	is the list of headers we ask for.  If you find that the specific 
 *	header value isn't available to your app and you're using 
 *	ISAPI, check that it's here...
 *
 */
#ifndef	_HTTP_HEADERS_
#define _HTTP_HEADERS_

/*
 *	ISAPI has one call for all the information.
 */
static 
const char * const http_headers[] = {
		"accept",
		"accept-encoding",
		"accept-language",
		"allow",
		"authorization",
		"auth-user",
		"cookie",
		"content-length",
		"content-type",
		"if-modified-since",
		"last-modified",
		"method",
		"path-info",
		"pragma",
		"protocol",
		"referer",
		"user-agent",
		"x-webobjects-recording",
		"ANNOTATION_SERVER",
		"AUTH_PASS",
		"AUTH_TYPE",
		"CLIENT_CERT",
		"CONTENT_ENCODING",
		"GATEWAY_INTERFACE",
		"HOST",
		"HTTP_ACCEPT_LANGUAGE",
		"HTTP_AUTHORIZATION",
		"HTTP_COOKIE",
		"HTTP_USER_AGENT",
		"HTTPS",
		"HTTPS_KEYSIZE",
		"HTTPS_SECRETKEYSIZE",
		"PATH_INFO",
		"PATH_TRANSLATED",
		"QUERY",
		"QUERY_STRING",
		"REMOTE_ADDR",
		"REMOTE_HOST",
		"REMOTE_USER",
		"SCRIPT_NAME",
		"SERVER_ID",
		"SERVER_NAME",
		"SERVER_PORT",
		"SERVER_PROTOCOL",
		"SERVER_SOFTWARE",
		"HTTP_X_WEBOBJECTS_RECORDING",
		NULL
};

#define	HTTP_HEADER_COUNT	((sizeof(http_headers)/sizeof(char *)) - 1)

#endif	/* _HTTP_HEADERS_ */

