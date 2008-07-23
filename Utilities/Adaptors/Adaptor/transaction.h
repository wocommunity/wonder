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
#ifndef	TRANSACTION_H_INCLUDED
#define TRANSACTION_H_INCLUDED
/*
 *	conducts the http server independent 'meat' of the request/response
 *	cycle to the application. This basically boils down to:
 *
 *	1) performing load balancing
 *	2) opening the communications channel to the app instance
 *	3) forwarding the request
 *	4) collecting the response
 *	5) closing/finalizing the connection
 *	
 */
#include "request.h"
#include "response.h"

/* Returns zero on success, nonzero if initialization fails. */
int transaction_init();

/*
 *	Code common to all adaptors.  The server-specific portion is responsible
 *	for collecting the request & related information.  The URL must be
 *	partially parsed - wc contains the application name and the query string.
 */
HTTPResponse *tr_handleRequest(HTTPRequest *req, const char *url, WOURLComponents *wc, const char *server_protocol, const char *documentRoot);

#endif
