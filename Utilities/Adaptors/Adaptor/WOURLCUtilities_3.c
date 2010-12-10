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
 * WARNING : The WOF URL format is common to the adaptor and the WebObjects framework.
 * If you modify the following code, your adaptor may not be able to communicate with WebObjects applications !
 *
 * Currently, the WOF URL format is:
 *	<PREFIX>/[<APPLICATION-NAME>][.woa[/<SESSION-ID>/[<PAGE-NAME>][.wo[/<CONTEXT-ID>/<SENDER-ID>[/<APPLICATION-NUMBER>[/<HOST-NAME>]]]]]][?QUERY-STRING]
 * Where <PREFIX> is:
 *	<*>/WebObjects[-<WEBOBJECTS-VERSION>][.exe|.dll]
 */

#include "config.h"
#include "WOURLCUtilities.h"

#include <stdio.h>
#include <ctype.h>
#include <string.h>

void WOParseSizedURL(WOURLComponents *components, const char *string, unsigned length) {

    WOURLComponent *c[11] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName),  &(components->sessionID), &(components->pageName),&(components->contextID), &(components->senderID), &(components->applicationNumber), &(components->applicationHost), &(components->suffix), &(components->queryString) };

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
        const char *extensions[3] = {".woa", ".WOA", NULL };
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
        }
        /* start == pointer to first character of name. */
        /* extension == pointer to first character after name. */
        /* end == pointer to first character after extension. */
        c[2]->start = start;
        c[2]->length = extension - start;
    }

    /* Extract session id */

    start = ((end < string + length && *end != '?') ? end + 1 : end);
    for (s = start; s < string + length && *s != '?' && *s != '/'; ++s);
    end = s;
    /* start == pointer to first character of component. */
    /* end == pointer to first character after component. */
    c[3]->start = start;
    c[3]->length = (end > start + 1 || *start != '-') ? end - start : 0;

    /* Extract page name. */
    {

        const char *extensions[3] =  { ".wo", ".WO", NULL};
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
        }
        /* start == pointer to first character of name. */
        /* extension == pointer to first character after name. */
        /* end == pointer to first character after extension. */
        c[4]->start = start;
        c[4]->length = extension - start;
    }

    /* Extract session ID, context ID, sender ID, application number, and application host. */
    for (i = 5; i <= 8; ++i) {
        start = ((end < string + length && *end != '?') ? end + 1 : end);
        for (s = start; s < string + length && *s != '?' && *s != '/'; ++s);
        end = s;
        /* start == pointer to first character of component. */
        /* end == pointer to first character after component. */
        c[i]->start = start;
        c[i]->length = (end > start + 1 || *start != '-') ? end - start : 0;
    }

    /* Extract suffix. */
    start = ((end < string + length && *end != '?') ? end + 1 : end);
    for (s = start; s < string + length && *s != '?'; ++s);
    end = s;
    /* start == pointer to first character of suffix. */
    /* end == pointer to first character after suffix. */
    c[9]->start = start;
    c[9]->length = end - start;

    /* Extract query string. */
    start = ((end < string + length) ? end + 1 : end);
    end = string + length;
    c[10]->start = start;
    c[10]->length = end - start;
}

void WOParseURL(WOURLComponents *components, const char *string) {
    WOParseSizedURL(components, string, (string ? strlen(string) : 0));
}

WOURLError WOCheckURL(WOURLComponents *components) {

    WOURLComponent *c[11] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName), &(components->sessionID), &(components->pageName), &(components->contextID), &(components->senderID), &(components->applicationNumber), &(components->applicationHost), &(components->suffix), &(components->queryString) };
    const WOURLError e[11] = { WOURLInvalidPrefix, WOURLInvalidWebObjectsVersion, WOURLInvalidApplicationName,  WOURLInvalidSessionID, WOURLInvalidPageName, WOURLInvalidContextID, WOURLInvalidSenderID, WOURLInvalidApplicationNumber, WOURLInvalidApplicationHost, WOURLInvalidSuffix, WOURLInvalidQueryString };

    WOURLComponents prefixComponents;
    int i, j;

    /* Check component starts. */
    for (i = 0; i <= 10; ++i) {
        if (!c[i]->start) {
            return e[i];
        }
    }

    /* Check component characters. */
    for (i = 0; i <= 10; ++i) {
        for (j = 0 ; j < c[i]->length; ++j) {
            if (c[i]->start[j] == '\0') {
                return e[i];
            } else if (i <= 8 && c[i]->start[j] == '?') {
                return e[i];
            } else if (i >= 5 && i <= 8 && c[i]->start[j] == '/') {
                return e[i];
            } else if (i == 7 && !isdigit((int)(c[i]->start[j]))) {
                return e[i];
            }
        }
    }

    /* Check component consistency. */
    if (!c[0]->length) {
        return e[0];
    } else if (!c[2]->length) {
        return e[2];
    } else if (!c[3]->length && (c[5]->length || c[6]->length)) {
        return e[3];
    } else if (!c[5]->length && c[3]->length) {
        return e[5];
    } else if (c[9]->length) {
        return e[9];
    }

    /* Check prefix syntax. */
    WOParseSizedURL(&prefixComponents, c[0]->start, c[0]->length);
    if (c[0]->length != prefixComponents.prefix.length) {
        return e[0];
    }

    return WOURLOK;
}

unsigned int WOSizeURL(WOURLComponents *components) {

    WOURLComponent *c[11] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName), &(components->sessionID), &(components->pageName), &(components->contextID), &(components->senderID), &(components->applicationNumber), &(components->applicationHost), &(components->suffix), &(components->queryString) };

    unsigned int length;
    int n;
    int i;

    length = 0;
    for (n = 10; n >= 0 && !c[n]->length; --n);
    for (i = 0; i <= 10; ++i) {
        if (i >= 2 && i <= n) {
            length += 1;
        } else if (i == 10 && c[i]->length) {
            length += 1;
        }
        if (c[i]->length && i != 1) {
            length += c[i]->length;
        } else if (i == 0) {
            length += 11;
        } else if ((i == 3 || i >= 5) && i < n) {
            length += 1;
        }
        if (i == 2) {
            length += 4;
        } else if (i == 4) {
            length += 3;
        }
    }

    return length;
}

void WOComposeURL(char *string, WOURLComponents *components) {

    WOURLComponent *c[11] = { &(components->prefix), &(components->webObjectsVersion), &(components->applicationName),  &(components->sessionID), &(components->pageName), &(components->contextID), &(components->senderID), &(components->applicationNumber), &(components->applicationHost), &(components->suffix), &(components->queryString) };

    char *s;
    int n;
    int i;

    s = string;
    for (n = 9; n >= 0 && !c[n]->length; --n);
    for (i = 0; i <= 10; ++i) {
        if (i >= 2 && i <= n) {
            *s++ = '/';
        } else if (i == 10 && c[i]->length) {
            *s++ = '?';
        }
        if (c[i]->length && i != 1) {
            strncpy(s, c[i]->start, c[i]->length);
            s += c[i]->length;
        } else if (i == 0) {
            strncpy(s, "/WebObjects", 11);
            s += 11;
        } else if ((i == 3 || i >= 5) && i < n) {
            *s++ = '-';
        }
        if (i == 2) {
            strncpy(s, ".woa", 4);
            s += 4;
        } else if (i == 4 && i < n) {
            strncpy(s, ".wo", 3);
            s += 3;
        }
    }
    *s = '\0';
}
