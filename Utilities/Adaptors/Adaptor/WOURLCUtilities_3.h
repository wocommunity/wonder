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
 *    The utilities defined in this file help with the manipulation of WOF dynamic URLs.
 *    WOF dynamic URLs are composed of the following components:
 *	<APPLICATION-NAME>: The name of the application the URL refers to.
 *	<PAGE-NAME>: The name of the page in which the URL was generated.
 *	<SESSION-ID>: The ID of the session in which the URL was generated.
 *	<CONTEXT-ID>: The ID of the context in which the URL was generated.
 *	<SENDER-ID>: The ID of the element associated with the URL.
 *	<APPLICATION-NUMBER>: The instance number of the URL's application.
 *	<APPLICATION-HOST>: The name of the host running the URL's application.
 *	<WEBOBJECTS-VERSION>: A string identifying the version of WebObjects run by the URL's application.
 *	<PREFIX>: A server-specific string.
 *   These utilities allow parsing of a WOF dynamic URL string for its components and generation
 *   of a WOF dynamic URL string from its components.  Because the format of a WOF dynamic URL
 *   may change over time, these utilities should always be used to access a WOF dynamic URL's
 *   components or to generate a WOF dynamic URL (i.e. no assumptions should be made about a WOF
 *   dynamic URL's format).
 */

#ifndef _WOF_URL_C_UTILITIES_H
#define _WOF_URL_C_UTILITIES_H

#define WOADAPTOR_APP_EXTENSION ".woa"
#define WOADAPTOR_APP_EXTENSION_UPPERCASE ".WOA"

/*********** WOF dynamic URL functions. ***********/

void WOParseURL(WOURLComponents *components, const char *string);
    /* ... */

WOURLError WOCheckURL(WOURLComponents *components);
    /* ... */

unsigned int WOSizeURL(WOURLComponents *components);
    /* ... */

void WOComposeURL(char *string, WOURLComponents *components);
    /* ... */

#endif /* _WOF_URL_C_UTILITIES_H */
