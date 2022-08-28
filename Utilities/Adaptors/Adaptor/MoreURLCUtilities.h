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
#ifndef	MORE_URL_C_UTILITIES_H
#define	MORE_URL_C_UTILITIES_H
/*
 *
 *	companion to WOURLCUtilities....
 *
 *	things that all adaptors will do, so we may as well not duplicate code
 *
 *	handles URL syntax versioning between V2 - V3 as well
 */
 
#include "WOURLCUtilities.h"

#define	NULL_WOURLComponent		((WOURLComponent){NULL,0})

#define	WOURLComponents_Initializer	((WOURLComponents) { \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent, \
	NULL_WOURLComponent \
})


/*
 *	Cover some functions to support URL versioning
 */
 
unsigned int SizeURL(WOURLComponents *wc);
void ComposeURL(char *string, WOURLComponents *wc, int shouldProcessUrl);

// Initial URL sanitization.
WOURLError WOValidateInitialURL( const char* url );

/*
 *	parses just the application name from the url, returns 0 on 
 *	success & fills in only wc.prefix, wc.webobjectsVersion and
 *	wc.applicationName
 */
WOURLError WOParseApplicationName(WOURLComponents *wc, const char *url);

/*
 *	calls WOParseURL and then WOCheckURL
 */
const char *WOParseAndCheckURL(WOURLComponents *wc, const char *url, int version, int shouldProcessUrl);

/*
 *	err code to string
 */
const char *WOURLstrerror(WOURLError err);




/*
 *	for debugging, I guess
 */
void logComponents(WOURLComponents *wc);

#endif
