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
#include "request.h"

/*
 *    The utilities defined in this file help with the manipulation of WOF dynamic URLs.
 *    WOF dynamic URLs are composed of the following components:
 *        <APPLICATION-NUMBER>: The instance number of the URL's application.
 *        <APPLICATION-NAME>: The name of the application the URL refers to.
 *        <REQUEST-HANDLER-KEY>: The key of the request handler that should handle the request
 *        <REQUEST-HANDLER-PATH>: The information specific to this request handler - not parsed.
 *        <QUERY-STRING>: What is after the question mark.
 *        <WEBOBJECTS-VERSION>: A string identifying the version of WebObjects run by the URL's application.
 *        <PREFIX>: A server-specific string.
 *    These utilitie allow parsing of a WOF dynamic URL string for its components and generation of a
 *    WOF dynamic URL string from its components.  Because the format of a WOF dynamic URL may change over
 *    time, these utilities should always be used to access a WOF dynamic URL's components or to generate
 *    a WOF dynamic URL (i.e. no assumptions should be made about a WOF dynamic URL's format). Once inside
 *    the request handler specified by the key, it is up to the developer of the request handler to handle
 *    the decoding of the url and the format generation on the following pages, if needed.
 */

#ifndef _WOF_URL_C_UTILITIES_H
#define _WOF_URL_C_UTILITIES_H


/*
 * Extension definitions
 */

#define WOADAPTOR_APP_EXTENSION ".woa"
#define WOADAPTOR_APP_EXTENSION_UPPERCASE ".WOA"

/*********** WOF dynamic URL types. ***********/

typedef struct _WOURLComponent {
    const char *start;
    unsigned int length;
} WOURLComponent;

typedef struct _WOURLComponents {
    WOURLComponent prefix;
    WOURLComponent webObjectsVersion;
    WOURLComponent applicationName;
    WOURLComponent applicationNumber;
    WOURLComponent applicationHost;
    WOURLComponent sessionID;
    WOURLComponent pageName;
    WOURLComponent contextID;
    WOURLComponent senderID;
    WOURLComponent queryString;
    WOURLComponent suffix;
    WOURLComponent requestHandlerKey;
    WOURLComponent requestHandlerPath;
} WOURLComponents;

typedef enum {
    WOURLOK = 0,
    WOURLInvalidPrefix = 1,
    WOURLInvalidWebObjectsVersion = 2,
    WOURLInvalidApplicationName = 3,
    WOURLInvalidApplicationNumber = 4,
    WOURLInvalidRequestHandlerKey = 5,
    WOURLInvalidRequestHandlerPath = 6,
    WOURLInvalidApplicationHost,
    WOURLInvalidPageName,
    WOURLInvalidSessionID,
    WOURLInvalidContextID,
    WOURLInvalidSenderID,
    WOURLInvalidQueryString,
    WOURLInvalidSuffix,
    WOURLInvalidPostData,
    WOURLNoPostData,
    WOURLForbiddenCharacter
} WOURLError;

/*********** WOF dynamic URL functions. ***********/

void WOParseURL_40(WOURLComponents *components, const char *string);
    /* ... */

WOURLError WOCheckURL_40(WOURLComponents *components);
    /* ... */

unsigned int WOSizeURL_40(WOURLComponents *components);
    /* ... */

void WOComposeURL_40(char *string, WOURLComponents *components, int shouldProcessUrl);
    /* ... */

#endif /* _WOF_URL_C_UTILITIES_H */
