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
#ifndef	_IIS_HTTP_HEADERS_
#define _IIS_HTTP_HEADERS_

/*
 *	ISAPI has one call for all the information except for these.
 *	This list is meant to be used in conjunction with ALL_RAW to get the full set
 * 	of headers from IIS.
 *	This list was taken from the Jan2001 edition of MSDN Library description of
 *	GetServerVariable.
 */
static const char * const iis_http_headers[] = {
    "APPL_MD_PATH",
    "APPL_PHYSICAL_PATH",
    "AUTH_PASSWORD",
    "AUTH_TYPE",
    "AUTH_USER",
    "CERT_COOKIE",
    "CERT_FLAGS",
    "CERT_ISSUER",
    "CERT_KEYSIZE",
    "CERT_SECRETKEYSIZE",
    "CERT_SERIALNUMBER",
    "CERT_SERVER_ISSUER",
    "CERT_SERVER_SUBJECT",
    "CERT_SUBJECT",
    "CONTENT_LENGTH",
    "CONTENT_TYPE",
    "LOGON_USER",
    "HTTPS",
    "HTTPS_KEYSIZE",
    "HTTPS_SECRETKEYSIZE",
    "HTTPS_SERVER_ISSUER",
    "HTTPS_SERVER_SUBJECT",
    "INSTANCE_ID",
    "INSTANCE_META_PATH",
    "PATH_INFO",
    "PATH_TRANSLATED",
    "QUERY_STRING",
    "REMOTE_ADDR",
    "REMOTE_HOST",
    "REMOTE_USER",
    "REQUEST_METHOD",
    "SCRIPT_NAME",
    "SERVER_NAME",
    "SERVER_PORT",
    "SERVER_PORT_SECURE",
    "SERVER_PROTOCOL",
    "SERVER_SOFTWARE",
    "URL",
    "HTTP_X_WEBOBJECTS_RECORDING",
    NULL
};

#define	IIS_HTTP_HEADER_COUNT	((sizeof(iis_http_headers)/sizeof(char *)) - 1)

#endif	/* _IIS_HTTP_HEADERS_ */

