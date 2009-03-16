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
#ifndef	WOF_ERRS
#define	WOF_ERRS
/*
 *	error returns for WebObjects requests gone awry
 */
#define	INVALID_URI	\
	"No application name and other arguments in URL, or path_info too long"
#define	INV_PROTOCOL \
	"Cannot determine server protocol from CGI"
#define	INV_SCRIPT	\
	"Cannot determine script name from CGI"
#define	INV_PREFIX \
	"Invalid prefix in URL"
#define	INV_SUFFIX \
	"Invalid suffix in URL"
#define	INV_APPNAME	\
	"Invalid application name"
#define	INV_APPNUM	\
	"Invalid application number in URL"
#define	INV_HOST	\
	"Invalid application host name in URL"
#define	INV_RHKEY	\
	"Invalid request handler key in URL"
#define	INV_RHPATH	\
	"Invalid request handler path in URL"
#define	INV_PAGE	\
	"Invalid page name in URL"
#define	INV_SESSID	\
	"Invalid session ID in URL"
#define	INV_CTXID	\
	"Invalid context ID in URL"
#define	INV_SENDER	\
	"Invalid sender ID in URL"
#define	INV_QUERY	\
	"Invalid query string in URL"
#define	INV_URL	\
	"Invalid URL"
#define	NO_APPNAME	\
	"Cannot obtain the name of the application being requested, or the application name is too long"
#define	NO_RESPONSE	\
	"Did not receive any response from application. It is possible that the application does not exist, or that the requested url is incorrect."
#define	INV_RESPONSE \
	"Invalid response received from application."
#define	INV_METHOD	\
	"Unknown HTTP method, or HTTP PUT method not supported."
#define	NOT_FOUND_APP	\
	"The requested application was not found on this server."
#define	INV_FORM_DATA	\
    "The request contained fewer content data than specified by the content-length header"
#define	NO_FORM_DATA	\
    "Error whilst reading content data for request"
#define ALLOCATION_FAILURE   \
    "Memory allocation failure."

#endif
