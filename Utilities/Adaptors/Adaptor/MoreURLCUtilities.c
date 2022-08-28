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
#include "config.h"
#include "MoreURLCUtilities.h"
#ifdef	SUPPORT_V3_URLS
#include "WOURLCUtilities_3.h"
#endif
#include "errors.h"
#include "log.h"

#include <string.h>
#include <ctype.h>


/*
 *	fudge the version indicator here
 */
#define	WebObjects_STR	"/"WEBOBJECTS
#define	WEBOBJECTS_STR	"/WEBOBJECTS"
#define	WebObjects_LEN	strlen(WebObjects_STR)	/* assume both same length */



const char * const cgi_extensions[] = { ".exe", ".EXE", ".dll", ".DLL", NULL };
const char * const app_extensions[] = {WOADAPTOR_APP_EXTENSION, WOADAPTOR_APP_EXTENSION_UPPERCASE, NULL };

/*
 *	to better deal with URL syntax changes, we parse just the application
 *	name from the URL ...
 *
 *  We assume the struct is correctly initialized on entry to the function, via WOURLComponents_Initializer.
 */
WOURLError WOParseApplicationName(WOURLComponents *wc, const char *url) {
    int len;
    const char *s;
    const char *webobjects, *extension, *version, *start, *end;
    int i;

    len = strlen(url);

    webobjects = NULL;

    /*
     *	spot our marker in the URL.  It'll look like "/WebObjects-n.ext/"
     */
    s = (url != NULL) ? url : "";
    while ( (s <= url + (len - WebObjects_LEN)) && (webobjects == NULL) && (*s != '?') ) {
        while ((*s != '/') && (s <= url + (len - WebObjects_LEN)))
            s++;
        if ((strncmp(s, WebObjects_STR, WebObjects_LEN) == 0) ||
            (strncmp(s, WEBOBJECTS_STR, WebObjects_LEN) == 0) )
            webobjects = s;
        else
            s++;
    }
    if (webobjects == NULL)
        return WOURLInvalidPrefix;	/* bail if "WebObjects" not in URL */

    s = webobjects + WebObjects_LEN;	/* just beyond "WebObjects" */
    for (end = s; (end < url + len) && (*end != '?') && (*end != '/'); end++)
        /* find end of CGI moniker */;

    version = (*s == '-') ? s : NULL;	/* do we have a version? */
    extension = NULL;

    for (i=0; (extension == NULL) && (cgi_extensions[i] != NULL); i++) {
        int n = strlen(cgi_extensions[i]);
        if ((end - n >= version) && (strncmp(end-n, cgi_extensions[i], n) == 0))
            extension = end - n;
    }

    /*
    *   just validate the prefix gunk....
    *   -- Added fix for invalid WO version info
    */
    if (extension != NULL) {
       if (version && ((extension - (version+1) < 1) || (extension - (version+1) > 5) || ( !isdigit((int)*(extension-1)) )))
            return WOURLInvalidWebObjectsVersion;
    } else if (version != NULL) {
       if ((end - (version+1) < 1) || (end - (version+1) > 5) || ( !isdigit((int)*(end-1)) ))
            return WOURLInvalidWebObjectsVersion;
    } else if ((end - s) > 1 )
        return WOURLInvalidPrefix;

    // Iterate the version string and match it to the regex: [a-z0-9\.\-_]+
    //   Its length is already constrained by the above conditional statements.
    if ( version != NULL ) {
        int versionLen = ((extension) ? extension : end)-version;
        for ( const char* v = (version+1); (*v) && v < version+versionLen; v++ ) {
            if ( !isalnum( (int)*v ) && (*v != '.') && (*v != '-') && (*v != '_') )
                return WOURLInvalidWebObjectsVersion;
        }
    }

    wc->prefix.start = url;
    wc->prefix.length = end - url;
    if (version != NULL) {
        wc->webObjectsVersion.start = version + 1;
        wc->webObjectsVersion.length = ((extension) ? extension : end)-version;
    }

    /*
    *	find the application name
    *	  - we're not supporting WO version 2 URLs here....
    *	  - this is probably more convoluted than it need be
    */
    start = ((end < url + len) && (*end != '?')) ? end + 1 : end;
    end = url + len;
    extension = end;
    for (i=0; (extension == end) && (app_extensions[i] != NULL); i++) {
        int n = strlen(app_extensions[i]);
        for (s=start; (s+n <= url+len) && (*s != '?') && (extension == end); ++s) {
            if ( (strncmp(s, app_extensions[i], n) == 0) &&
                    ((s + n == url + len) || (*(s+n) == '?') || (*(s+n) == '/')) )
            {
                extension = s;
                end = s + n;
            }
        }
        if (*s == '?')
            end = s;
    }

    /* start == pointer to first character of name. */
    /* extension == pointer to first character after name. */
    /* end == pointer to first character after extension. */
    wc->applicationName.length = (end < extension) ? end - start : extension - start;
    if (wc->applicationName.length != 0) {
        wc->applicationName.start = start;

        /* get rid of trailing slashes in case the app name is last */
        /* and followed by trailing slashes */
        while(wc->applicationName.length && wc->applicationName.start[wc->applicationName.length-1] == '/')
            wc->applicationName.length--;
        return WOURLOK;
    } else
        return WOURLInvalidApplicationName;
}

/*
 *	cover functions for the different versions...
 */
void ComposeURL(char *string, WOURLComponents *wc, int shouldProcessUrl) {
    if (wc->webObjectsVersion.length) {
        switch (*(wc->webObjectsVersion.start)) {
            case '4':
#if defined(SUPPORT_V4_URLS)
                WOComposeURL_40(string, wc, shouldProcessUrl);
#else /* SUPPORT_V4_URLS */
                WOLog(WO_ERR,"Unknown URL version");
#endif /* SUPPORT_V4_URLS */
                break;
            case '3':
#if defined(SUPPORT_V3_URLS)
                WOComposeURL(string, wc);
#else /* SUPPORT_V3_URLS */
                WOLog(WO_ERR,"Unknown URL version");
#endif /* SUPPORT_V3_URLS */
                break;
            default:
                WOLog(WO_ERR,"Unknown URL version");
                break;
        }
    }

#if !defined(SUPPORT_V3_URLS) && !defined(SUPPORT_V4_URLS)
#error	"URL Version support is mucked up!"
#endif

    /* logComponents(wc); */
    WOLog(WO_DBG,"Composed URL to '%s'",string);
}

unsigned int SizeURL(WOURLComponents *wc) {
    if (wc->webObjectsVersion.length) {
        switch (*(wc->webObjectsVersion.start)) {
            case '4':
#if defined(SUPPORT_V4_URLS)
                return WOSizeURL_40(wc);
#endif /* SUPPORT_V4_URLS */
                break;
            case '3':
#if defined(SUPPORT_V3_URLS)
                return WOSizeURL(wc);
#endif /* SUPPORT_V3_URLS */
                break;
            default:
                WOLog(WO_ERR,"SizeURL: Unknown URL version");
                break;
        }
    }

    return 4096;	/* .. and hope it's enough */
}


/*
 * Filter for illegal URL characters in the passed adaptor request URL.
 */
WOURLError WOValidateInitialURL( const char* url ) {
    const char* i;
    int j;

    char illegal_vals[] = { 0x0A, 0x0D };
    int len = strlen( url );

    i = (url != NULL) ? url : "";
    WOLog(WO_DBG, "WOValidateInitialURL(): Inspecting URL: %s (%d)", url, len);

    for( ; (*i) && i < (url+len); i++ ) {
        for ( j = 0; j < (sizeof(illegal_vals)/sizeof(char)); j++ ) {
            if ( *i == illegal_vals[j] )  return WOURLForbiddenCharacter;
        }
    }

    return WOURLOK;
}


/*
 *	Dealing with URLs.  Most code is provided with WebObjects and must
 *	be used without modification to insure forward compatibility.
 *	Here, though, we want to provide a little more functionality to
 *	allow cleaner interface and support different URL syntax's.
 */
const char *WOParseAndCheckURL(WOURLComponents *wc, const char *url, int version, int shouldProcessUrl) {
    WOURLError result = WOURLOK;

    switch (version) {
        case 4:
#ifdef	SUPPORT_V4_URLS
            if (shouldProcessUrl) {
                WOParseURL_40(wc, url);		/* parse a V4 syntax URL */
                result = WOCheckURL_40(wc);
            }
            if (result == WOURLOK) {
                wc->webObjectsVersion.start = v4_url;
                wc->webObjectsVersion.length = URLVersionLen;
                WOLog(WO_INFO,"V4 URL: %s",url);
                return NULL;
            }
#endif	/* SUPPORT_V4_URLS */
            break;
        case 3:
#ifdef	SUPPORT_V3_URLS
            if (shouldProcessUrl) {
                WOParseURL(wc, url);		/* parse a V3 syntax URL */
                result = WOCheckURL(wc);
            }
            if (result == WOURLOK) {
                wc->webObjectsVersion.start = v3_url;
                wc->webObjectsVersion.length = 	URLVersionLen;
                WOLog(WO_INFO,"V3 URL: %s",url);
                return NULL;		/* no error */
            }
#endif	/* SUPPORT_V3_URLS */
            break;
        default:
            break;
    }

    if (result == WOURLOK)		/* still in initialized state? */
        return "Unsupported URL version";
    else
        return WOURLstrerror(result);
}


const char *WOURLstrerror(WOURLError err) {
    const char *errStr;

    switch (err) {
        case WOURLOK:
            errStr = NULL;
            break;
        case WOURLInvalidPrefix:
            errStr = INV_PREFIX;
            break;
        case WOURLInvalidSuffix:
            errStr = INV_SUFFIX;
            break;
        case WOURLInvalidApplicationName:
            errStr = INV_APPNAME;
            break;
        case WOURLInvalidApplicationNumber:
            errStr = INV_APPNUM;
            break;
        case WOURLInvalidApplicationHost:
            errStr = INV_HOST;
            break;
        case WOURLInvalidRequestHandlerKey:
            errStr = INV_RHKEY;
            break;
        case WOURLInvalidRequestHandlerPath:
            errStr = INV_RHPATH;
            break;
        case WOURLInvalidPageName:
            errStr = INV_PAGE;
            break;
        case WOURLInvalidSessionID:
            errStr = INV_SESSID;
            break;
        case WOURLInvalidContextID:
            errStr = INV_CTXID;
            break;
        case WOURLInvalidSenderID:
            errStr = INV_SENDER;
            break;
        case WOURLInvalidQueryString:
            errStr = INV_QUERY;
            break;
        case WOURLInvalidPostData:
            errStr = INV_FORM_DATA;
            break;
        case WOURLNoPostData:
            errStr = NO_FORM_DATA;
            break;
        default:
            errStr = INV_URL;
            break;
    }
    return errStr;
}

/*
 *	debugging aid - no need to inline
 */

static char *_doappnd(const char *d,char *s, WOURLComponent *c) {
    int dl = strlen(d);
    memcpy(s,d,dl);	s += dl;
    if (c->start != NULL) {
        memcpy(s, c->start, c->length);
        s += c->length;
    } else {
        memcpy(s, "(null)", 6);
        s += 6;
    }
    return s;
}


void logComponents(WOURLComponents *wc) {
    char msg[4096], *c;		/* clearly for debugging only .. */

    strcpy(msg,"URL Components:");
    c = _doappnd("\n\tPrefix:\t",msg+strlen(msg), &(wc->prefix));
    c = _doappnd("\n\tWOVersion:\t",c, &(wc->webObjectsVersion));
    c = _doappnd("\n\tAppName:\t",c, &(wc->applicationName));
    c = _doappnd("\n\tAppNumber:\t",c, &(wc->applicationNumber));
    c = _doappnd("\n\tAppHost:\t",c, &(wc->applicationHost));
    c = _doappnd("\n\tSessionID:\t",c, &(wc->sessionID));
    c = _doappnd("\n\tPageName:\t",c, &(wc->pageName));
    c = _doappnd("\n\tContextId:\t",c, &(wc->contextID));
    c = _doappnd("\n\tSenderId:\t",c, &(wc->senderID));
    c = _doappnd("\n\tQueryString:\t",c, &(wc->queryString));
    c = _doappnd("\n\tSuffix:\t",c, &(wc->suffix));
    *c = '\0';
    WOLog(WO_INFO,msg);
}
