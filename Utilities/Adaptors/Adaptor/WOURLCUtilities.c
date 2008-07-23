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
/*
 * WARNING : The WOF URL format is common to the adaptor and the WebObjects framework.
 * If you modify the following code, your adaptor may not be able to communicate with WebObjects applications !
 *
 * Currently (WebObjects 4.0/4.5), the WOF URL format is:
 *	<PREFIX>/<APPLICATION-NAME>[.woa[/<APPLICATION-NUMBER>][/<REQUEST-HANDLER-KEY>[/<REQUEST-HANDLER-PATH>]]][?<QUERY-STRING>]
 * Where <PREFIX> is:
 *	<*>/WebObjects[-<WEBOBJECTS-VERSION>][.exe|.dll]
 */

#include "config.h"
#include "WOURLCUtilities.h"
#include <stdio.h>
#include <ctype.h>
#include <string.h>

void WOParseSizedURL_40(WOURLComponents *components, const char *string, unsigned length) {

    WOURLComponent *c[7] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName),  &(components->applicationNumber), &(components->requestHandlerKey),&(components->requestHandlerPath), &(components->queryString) };

    const char *start;
    const char *version;
    const char *extension;
    const char *end;
    const char *s;
    int i, j;

    /* Extract prefix and WebObjects version. */
    start = (string ? string : "");
    /* Find start of version. */
    version = start;
    for (s = start; s <= string + length - 11 && *s != '?' && version == start; ++s) {
        if (strncmp(s, "/WebObjects", 11) == 0 || strncmp(s, "/WEBOBJECTS", 11) == 0) {
            version = s + 11;
        } else {
            for (; s < string + length - 11 && *s == '/'; ++s);
        }
    }
    /* Find end of prefix. */
    if (version > start) {
        for (s = version; s < string + length && *s != '?' && *s != '/'; ++s);
        end = s;
    } else {
        end = start;
    }
    /* Find start of extension. */
    {
        const char *extensions[] = { ".exe", ".EXE", ".dll", ".DLL", NULL };
        extension = end;
        for (i = 0; extensions[i] && extension == end; ++i) {
            int n = strlen(extensions[i]);
            if (end - n >= version && strncmp(end - n, extensions[i], n) == 0) {
                extension = end - n;
            }
        }
    }
    /* Treat invalid versions as invalid prefix. */
    if (version < extension && (*version != '-' || (extension - (version + 1)) < 1)) {
        version = start;
        extension = start;
        end = start;
    }
    /* start == pointer to first character of prefix. */
    /* version == pointer to first character after "/WebObjects". */
    /* extension == pointer to first character after WebObjects version. */
    /* end == pointer to first character after prefix. */
    c[0]->start = start;
    c[0]->length = end - start;
    c[1]->start = ((version < extension) ? version + 1 : version);
    c[1]->length = ((version < extension) ? extension - (version + 1) : 0);

    /* Extract application name. */
    {
        const char *extensions[3] = {WOADAPTOR_APP_EXTENSION, WOADAPTOR_APP_EXTENSION_UPPERCASE, NULL };
        start = ((start < end && end < string + length && *end != '?') ? end + 1 : end);
        extension = string + length;
        end = string + length;
        for (j = 0; extensions[j] && extension == end; ++j) {
            int n = strlen(extensions[j]);
            for (s = start; s + n <= string + length && *s != '?' && extension == end; ++s) {
                if (strncmp(s, extensions[j], n) == 0 && (s + n == string + length || *(s + n) == '?' || *(s + n) == '/')) {
                    extension = s;
                    end = s + n;
                }
            }
            if (*s == '?') end = s;
        }
        /* start == pointer to first character of name. */
        /* extension == pointer to first character after name. */
        /* end == pointer to first character after extension. */

        c[2]->start = start;
        c[2]->length = (end < extension) ? end - start : extension - start;

        /* get rid of trailing slashes in case the app name is last */
        /* and followed by trailing slashes */
        while(c[2]->length && c[2]->start[c[2]->length-1] == '/') c[2]->length--;
    }

    /* Extract Instance Number */

    start = ((end < string + length && *end != '?') ? end + 1 : end);

    /* First skip any extra slashes */
    while(*start == '/') start++;

    for (s = start; s < string + length && *s != '?' && *s != '/'; ++s);
    end = s;
    /* start == pointer to first character of component. */
    /* end == pointer to first character after component. */
    c[3]->start = start;
    c[3]->length = (end > start) ? end - start : 0;

    /* Now check if this is really an instance number
     * For that we will check that all characters are digits in c[3], or c[3] == "-1"
     */
    for (s = c[3]->start; s < c[3]->length + c[3]->start && (isdigit((int)*s) || ((s == c[3]->start) && (*s == '-'))); s++);

    if (s != c[3]->length + c[3]->start) {
        /* This field is not just digits. This field has to be the request handler key */
        c[4]->start = c[3]->start;
        c[4]->length = c[3]->length;
        /* let's just leave c[3] as is right now (we'll do more with the query string later) */
        c[3]->length = 0;
    } else {
        /* Let's look for the request handler key now. */

        start = ((end < string + length && *end != '?') ? end + 1 : end);

        /* First skip any extra slashes */
        while(*start == '/') start++;

        for (s = start; s < string + length && *s != '?' && *s != '/'; ++s);
        end = s;
        /* start == pointer to first character of request handler key. */
        /* end == pointer to first character after request handler key. */
        c[4]->start = start;
        c[4]->length = (end > start) ? end - start : 0;
    }

    /* Extract request handler path. */
    start = ((end < string + length && *end != '?') ? end + 1 : end);
    for (s = start; s < string + length && *s != '?'; ++s);
    end = s;
    /* start == pointer to first character of request handler path. */
    /* end == pointer to first character after request handler path. */
    c[5]->start = start;
    c[5]->length = end - start;

    /* Extract query string. */
    /* N.B. aB. Only do this if we haven't already stored the query string in the components */
    if (c[6]->start == NULL) {
        start = ((end < string + length) ? end + 1 : end);
        end = string + length;
        c[6]->start = start;
        c[6]->length = end - start;
    }
}

void WOParseURL_40(WOURLComponents *components, const char *string) {
    WOParseSizedURL_40(components, string, (string ? strlen(string) : 0));
}

WOURLError WOCheckURL_40(WOURLComponents *components) {

    WOURLComponent *c[7] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName), &(components->applicationNumber), &(components->requestHandlerKey), &(components->requestHandlerPath), &(components->queryString) };
    const WOURLError e[7] = { WOURLInvalidPrefix, WOURLInvalidWebObjectsVersion, WOURLInvalidApplicationName,  WOURLInvalidApplicationNumber, WOURLInvalidRequestHandlerKey, WOURLInvalidRequestHandlerPath, WOURLInvalidQueryString };

    WOURLComponents prefixComponents;
    int i, j;

    /* Check component starts. */
    for (i = 0; i <= 6; ++i) {
        if (!c[i]->start) {
            return e[i];
        }
    }

    /* Check component characters. */
    for (i = 0; i <= 6; ++i) {
        for (j = 0 ; j < c[i]->length; ++j) {
            if (c[i]->start[j] == '\0') {
                /* One of the components is just empty ! */
                return e[i];
            } else if (i <= 5 && c[i]->start[j] == '?') {
                /* One of the components contains a ? , which should indicate a query string ! */
                return e[i];
            }
        }
    }

    /* Check component consistency. */
    if (!c[0]->length) {
        /* There is no prefix ! */
        return e[0];
    } else if (!c[2]->length) {
        /* There is no application name ! */
        return e[2];
    } else if (!c[4]->length && ( c[5]->length)) {
        /* There is a no request handler key, but there is a request handler path or a query string ! */
        return e[4];
    }

    /* Check prefix syntax. */
    WOParseSizedURL_40(&prefixComponents, c[0]->start, c[0]->length);
    if (c[0]->length != prefixComponents.prefix.length) {
        return e[0];
    }

    return WOURLOK;
}

unsigned int WOSizeURL_40(WOURLComponents *components) {

    WOURLComponent *c[7] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName), &(components->applicationNumber), &(components->requestHandlerKey), &(components->requestHandlerPath), &(components->queryString) };

    unsigned int length;

    length = 0;

    /* prefix */
    length += c[0]->length;

#if 0 /* webobjects version should not be added as it is in the prefix */
        if (c[1]->length) {
            length += 1 + c[1]->length;
        }
#endif

    /* application name */
    length += 1 + c[2]->length + 4;

    /* instance number */
    if (c[3]->length) {
        length += 1 + c[3]->length;

    }

    /* request handler key */
    if (c[4]->length) {
        length += 1 + c[4]->length;

    }

    /* request handler path */
    if (c[5]->length) {
        length += 1 + c[5]->length;

    }


    /* query string */
    if (c[6]->length) {
        length += 1 + c[6]->length;

    }

    /* finish the string. Do not add 1 to account for the null terminator. */

    return length;
}

void WOComposeURL_40(char *string, WOURLComponents *components) {

    WOURLComponent *c[11] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName),  &(components->applicationNumber), &(components->requestHandlerKey), &(components->requestHandlerPath), &(components->queryString) };

    char *s;

    s = string;

    /* prefix */
    strncpy(s, c[0]->start, c[0]->length);
    s += c[0]->length;

#if 0 /* webobjects version should not be added as it is in the prefix */
        if (c[1]->length) {
            *s++ = '-';
            strncpy(s, c[1]->start, c[1]->length);
            s += c[1]->length;
        }
#endif

    /* application name */
    *s++ = '/';
    strncpy(s, c[2]->start, c[2]->length);
    s += c[2]->length;
    strncpy(s, WOADAPTOR_APP_EXTENSION, 4);
    s += 4;

    /* instance number */
    if (c[3]->length) {
        *s++ = '/';
        strncpy(s, c[3]->start, c[3]->length);
        s += c[3]->length;
    }

    /* request handler key */
    if (c[4]->length) {
        *s++ = '/';
        strncpy(s, c[4]->start, c[4]->length);
        s += c[4]->length;
    }

    /* request handler path */
    if (c[5]->length) {
        *s++ = '/';
        strncpy(s, c[5]->start, c[5]->length);
        s += c[5]->length;
    }


    /* query string */
    if (c[6]->length) {
        *s++ = '?';
        strncpy(s, c[6]->start, c[6]->length);
        s += c[6]->length;
    }

    /* finish the string */
    *s = '\0';
}
