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
 *	FastCGI WebObjects adaptor
 *
 *	Refer to FastCGI site for more informaion:
 *	http://www.fastcgi.com
 *	
 *	Credits / blames (in order of appearance):
 *	- Apple <webobjects@apple.com>
 *	- Wojtek Narczynski <wojtek@power.com.pl>
 *	- Pawel Piaskowy <pawel@ifirma.pl>
 *	- Marek Janukowicz <marek@power.com.pl>
 *
 */
#include "config.h"
#include "womalloc.h"
#include "MoreURLCUtilities.h"
#include "request.h"
#include "response.h"
#include "errors.h"
#include "httperrors.h"
#include "log.h"
#include "transaction.h"
#include "listing.h"
#include "wastring.h"

#include <stdlib.h>
#include <sys/types.h>
#include <errno.h>
#include <ctype.h>
#include <string.h>

#if !defined(WIN32)
#include <sys/param.h>
#include <signal.h>
#else
#include <winsock.h>
#include <winnt-pdo.h>
#include <io.h>	/* setmode() */
#include <fcntl.h>
#endif

#include <fcgiapp.h>

#ifndef MAXPATHLEN
#define MAXPATHLEN 255
#endif

static const char *documentRoot();

#define PATH_TRANSLATED "PATH_TRANSLATED"
#define CGI_SCRIPT_NAME	"SCRIPT_NAME"
#define	CGI_PATH_INFO	"PATH_INFO"
#define	CGI_SERVER_PROTOCOL	"SERVER_PROTOCOL"
#define	CGI_DOCUMENT_ROOT "DOCUMENT_ROOT"
#define WO_CONFIG_URL "WO_CONFIG_URL"
#define WO_ADAPTOR_INFO_USERNAME "WO_ADAPTOR_INFO_USERNAME"
#define WO_ADAPTOR_INFO_PASSWORD "WO_ADAPTOR_INFO_PASSWORD"
#define WO_CONFIG_OPTIONS "WEBOBJECTS_OPTIONS"

char *WA_adaptorName = "FastCGI";
static unsigned int freeValueNeeded=0;

FCGX_Stream *in, *out, *err;
FCGX_ParamArray hdrp;
static int should_terminate=0;

/*
 *	the CGI1.1 spec says:
 *
 *	"For Unix compatible operating systems, the following are defined:
 *	...
 *	Character set
 *		The US-ASCII character set is used for the definition of
 *		environment variable names and header field names; the newline
 *		(NL) sequence is LF; servers SHOULD also accept CR LF as a
 *		newline.
 *	..."
 */
#define	CRLF	"\r\n"

/*
 * BEGIN Support for getting the client's certificate.
 */
char *make_cert_one_line(char *value) {
    char *returnValue = (char *)NULL;
    int i;
    int j;

    if (value) {
        //
        // Copy everything except newlines.
        // Really, we should copy everything that is in the base64
        // encoding alphabet except for newlines, spaces, and tabs.
        // But, this will do for now...
        //
        returnValue = strdup(value);
        freeValueNeeded=1;

        for (j=0, i = 0; i < strlen(value);i++) {
            if (value[i] != '\n') {
                returnValue[j]= value[i];
                j++;
            }
        }
        returnValue[j] = '\0';  // and NULL-terminate
    }

    return returnValue;
}
/*
 * END Support for getting the client's certificate.
 */

/*
 *	send response to server
 */
static void sendResponse(HTTPResponse *resp)
{
   String *resphdrs;
#ifndef PROFILE
   FCGX_FPrintF(out,"Status: %d %s" CRLF,resp->status,resp->statusMsg);
#endif

   resphdrs = resp_packageHeaders(resp);
#ifndef PROFILE
   FCGX_PutS(resphdrs->text,out);
#endif
   str_free(resphdrs);
#ifndef PROFILE
   FCGX_PutS(CRLF, out);
#endif

#ifndef PROFILE

   /* resp->content_valid will be 0 for HEAD requests and empty responses */
   if (resp->content_valid) {
      while (resp->content_read < resp->content_length) {
         //fwrite(resp->content,sizeof(char),resp->content_valid,stdout);
	      FCGX_PutStr(resp->content, resp->content_valid, out);
		 if (resp_getResponseContent(resp, 1) == -1)
         {
         	break;
         }
      }
      //fwrite(resp->content,sizeof(char),resp->content_valid,stdout);
      FCGX_PutStr(resp->content, resp->content_valid, out);
   }
   FCGX_FFlush(out);
#endif
   return;		
}

static void sendErrorResponse(HTTPResponse *resp)
{
   sendResponse(resp);
   resp_free(resp);
}

static void prepareAndSendErrorResponse(const char *msg, int status)
{
   HTTPResponse *resp = resp_errorResponse(msg, status);
   sendErrorResponse(resp);
}


/* Read up to dataSize bytes into the buffer at dataBuffer. */
/* Returns the number of bytes read, or -1 on error. */
static int readContentData(HTTPRequest *req, void *buffer, int dataSize, int mustFill)
{
   WOLog (WO_INFO, "data size: %d", dataSize );
   //int n = fread(buffer, 1, dataSize, stdin);
   int n = FCGX_GetStr(buffer, dataSize, in);
   if (n != dataSize) {
      //int err = ferror(stdin);
	  int err = FCGX_GetError(in);
      if (err)
         WOLog(WO_ERR,"Error getting content data: %s (%d)", strerror(errno), err);
   }
/*	*((char*)buffer+dataSize) = '\0';
	 WOLog ( WO_INFO, "buffer: %s", (char *)buffer );*/
   return n == dataSize ? n : -1;
}


static void sig_handler(int signum) {
    switch (signum) {
	case SIGUSR1 : should_terminate = 1;
		       WOLog(WO_INFO,"<FastCGI> Signal %s caught", strsignal(signum));
		       break;
	case SIGPIPE : WOLog(WO_WARN,"<FastCGI> Signal %s caught", strsignal(signum));
		       break;
	case SIGTERM : WOLog(WO_ERR,"<FastCGI> Signal %s caught", strsignal(signum));
		       exit(-1);
	default:       WOLog( WO_INFO,"<FastCGI> Signal %s caught", strsignal(signum));
    }
}

int main() {

#ifdef	PROFILE
    int i;
#endif
    const char *config_url, *username, *password, *config_options;
    strtbl *options = NULL;
    int exit_status = 0;

    // install SIGUSR1, SIGPIPE, SIGTERM handler
    signal(SIGTERM, sig_handler);
    signal(SIGUSR1, sig_handler);
    signal(SIGPIPE, sig_handler);

      /* Provide a hook via an environment variable to define the config URL */
      config_url = getenv(WO_CONFIG_URL);
      if (!config_url) {
         /* Flat file URL */
         /* config_url = "file:///Local/Library/WebObjects/Configuration/WOConfig.xml"; */
         /* Local wotaskd */
         /* config_url = "http://localhost:1085"; */
         /* Multicast URL */
         config_url = CONFIG_URL; /* Actually "webobjects://239.128.14.2:1085"; */
      }
      WOLog(WO_INFO,"<FastCGI> config url is %s", config_url);
      options = st_new(8);
      st_add(options, WOCONFIG, config_url, 0);

      /*
         * If your webserver is configured to pass these environment variables, we use them to
       * protect WOAdaptorInfo output.
       */
      username = getenv(WO_ADAPTOR_INFO_USERNAME);
      if (username && strlen(username) != 0) {
         st_add(options, WOUSERNAME, username, 0);
         password = getenv(WO_ADAPTOR_INFO_PASSWORD);
         if(password && strlen(password) != 0) {
            st_add(options, WOPASSWORD, password, 0);
         }
      }

      config_options = getenv(WO_CONFIG_OPTIONS);
      if (config_options)
         st_add(options, WOOPTIONS, config_options, 0);
      /*
       * SECURITY ALERT
       *
       * To disable WOAdaptorInfo, uncomment the next line.
       * st_add(options, WOUSERNAME, "disabled", 0);
       *
       * To specify an WOAdaptorInfo username and password, uncomment the next two lines.
       * st_add(options, WOUSERNAME, "joe", 0);
       * st_add(options, WOPASSWORD, "secret", 0);
       *
       */

      if (init_adaptor(options)) {
          WOLog( WO_ERR, "<FastCGI> Adaptor initialization failed.");
    	    exit(-1);
      }

    WOLog( WO_INFO,"<FastCGI> process started" );
   
    while (!should_terminate) {

      HTTPRequest *req;
      HTTPResponse *resp = NULL;
      WOURLComponents wc = WOURLComponents_Initializer;
      const char *qs;
      unsigned int qs_len;
      char *url;
      const char *script_name, *path_info;
      const char *reqerr;
      WOURLError urlerr;
      FCGX_ParamArray hdrp_org;
     
      exit_status = FCGX_Accept(&in, &out, &err, &hdrp );
      if ( exit_status < 0 ) {
	    break;
      }

#ifdef	PROFILE
    for (i=0; i < 50000; i++) {
#endif

      WOLog( WO_INFO,"<FastCGI> request accepted" );

#ifdef WIN32
      _setmode(_fileno(stdout), _O_BINARY);
      _setmode(_fileno(stdin), _O_BINAR1Y);
#endif

      script_name = FCGX_GetParam( CGI_SCRIPT_NAME, hdrp);
      path_info = FCGX_GetParam( CGI_PATH_INFO, hdrp);

      WOLog( WO_INFO,"<FastCGI> CGI_SCRIPT_NAME = %s", script_name );
      WOLog( WO_INFO,"<FastCGI> CGI_PATH_INFO = %s", path_info );

      if (script_name == NULL) {
         prepareAndSendErrorResponse(INV_SCRIPT, HTTP_NOT_FOUND);
         break;
      } else if (path_info == NULL) {
         path_info = "/";
      }

      /*
       *	extract WebObjects application name from URI
       */

      url = WOMALLOC(strlen(path_info) + strlen(script_name) + 1);
      strcpy(url, script_name);
      strcat(url, path_info);
      WOLog(WO_INFO,"<FastCGI> new request: %s",url);
      
      urlerr = WOParseApplicationName(&wc, url);
      if (urlerr != WOURLOK) {
         const char *_urlerr;
         _urlerr = WOURLstrerror(urlerr);
         WOLog(WO_INFO,"<FastCGI> URL Parsing Error: %s", _urlerr);

         if (urlerr == WOURLInvalidApplicationName) {
             if (ac_authorizeAppListing(&wc)) {
                 resp = WOAdaptorInfo(NULL, &wc);
                 sendErrorResponse(resp);
             } else {
                 prepareAndSendErrorResponse(_urlerr, HTTP_NOT_FOUND);
             }
             WOFREE(url);
             break;
         }

         prepareAndSendErrorResponse(_urlerr, HTTP_BAD_REQUEST);
         WOFREE(url);
         break;
      }


      /*
       *	build the request...
       */
      req = req_new( FCGX_GetParam("REQUEST_METHOD", hdrp), NULL);


      /*
       *	validate the method
       */
      reqerr = req_validateMethod(req);
      if (reqerr) {
          prepareAndSendErrorResponse(reqerr, HTTP_BAD_REQUEST);
          WOFREE(url);
          break;
      }

      /*
       *	copy the headers.  This looks wierd... all we're doing is copying
       *	*every* environment variable into our headers.  It may be beyond
       *	the spec, but more information probably won't hurt.
       */
      hdrp_org=hdrp;
      while (hdrp && *hdrp) {
         char *key, *value;
         /* copy env. line. */
         key = WOSTRDUP(*hdrp);

         for (value = key; *value && !isspace((int)*value) && (*value != '='); value++) {}
         if (*value) {
            *value++ = '\0';	/* null terminate 'key' */
         }
         while (*value && (isspace((int)*value) || (*value == '='))) {
            value++;
         }
         /* BEGIN Support for getting the client's certificate. */
         if (strcmp((const char *)key, "SSL_CLIENT_CERTIFICATE") == 0 || strcmp((const char *)key, "SSL_SERVER_CERTIFICATE") == 0 ) {
             value = 0;
             WOLog(WO_INFO,"<FastCGI> DROPPING ENV VAR (DUPLICATE) = %s", key);
         }
         if (strcmp((const char *)key, "SSL_CLIENT_CERT") == 0 || strcmp((const char *)key, "SSL_SERVER_CERT") == 0) {
             value = make_cert_one_line(value);
             //WOLog(WO_INFO,"<FastCGI> PASSING %s = %s", key, value);
         }
         /*  END Support for getting the client's certificate  */

         if (key && *key && value && *value) {
            /* must specify copy key and value because key translation might replace this key, and value lives in the same buffer */
            req_addHeader(req, key, value, STR_COPYKEY|STR_COPYVALUE);
         }

         /*  BEGIN Support for getting the client's certificate  */
         if (freeValueNeeded ) {
             free(value);
             freeValueNeeded=0;
         }
         /*  END Support for getting the client's certificate  */

         WOFREE(key);
         hdrp++;			/* next env variable */
      }
      hdrp=hdrp_org;

      /*
       *	get form data if any
       *	assume that POSTs with content length will be reformatted to GETs later
       */
	
      WOLog ( WO_INFO, "Getting request data, length: %d",req->content_length );
      if (req->content_length > 0) {
         req_allocateContent(req, req->content_length, 1);
         req->getMoreContent = (req_getMoreContentCallback)readContentData;
         WOLog ( WO_INFO, "content_buffer_size: %d",req->content_buffer_size );
         if (req->content_buffer_size == 0) {
            prepareAndSendErrorResponse(ALLOCATION_FAILURE, HTTP_SERVER_ERROR);
            WOFREE(url);
            break;
         }
         if (readContentData(req, req->content, req->content_buffer_size, 1) == -1) {
            prepareAndSendErrorResponse(WOURLstrerror(WOURLInvalidPostData), HTTP_BAD_REQUEST);
            WOFREE(url);
            break;
         }
      }

      /* Always get the query string */
      qs = FCGX_GetParam("QUERY_STRING", hdrp);
      if (qs) {
         qs_len = strlen(qs);
      } else {
         qs_len = 0;
      }

      if (qs_len > 0) {
         wc.queryString.start = qs;
         wc.queryString.length = qs_len;
         WOLog(WO_INFO,"<FastCGI> new request with Query String: %s", qs);
      }

      /*
       *	message the application & collect the response
       */
      resp = tr_handleRequest(req, url, &wc, FCGX_GetParam(CGI_SERVER_PROTOCOL, hdrp), documentRoot());

      if (resp != NULL) {
         sendResponse(resp);
         resp_free(resp);		/* dump the response */
      }

      WOFREE(url);
      req_free(req);

#if defined(FINDLEAKS)
      showleaks();
#endif
    }

#ifdef	PROFILE
    }
#endif


    st_free(options);
    WOLog( WO_INFO,"<FastCGI> process exiting" );

    return exit_status;
}


   const char *documentRoot() {
      static char path[MAXPATHLEN+1] = "";

      if (path[0] == '\0') {
#ifdef WIN32
         WOReadKeyFromConfiguration(CGI_DOCUMENT_ROOT, path, MAXPATHLEN);
#else
         const char *doc_root;
         /* Apache provides this as an environment variable straight */
         if ((doc_root = FCGX_GetParam(CGI_DOCUMENT_ROOT, hdrp)) != NULL) {
            strncpy(path, doc_root, MAXPATHLEN);
         } else {
            const char *path_trans, *path_info;
            path_trans = FCGX_GetParam(PATH_TRANSLATED, hdrp);
            path_info = FCGX_GetParam(CGI_PATH_INFO, hdrp);

            if (path_trans && path_info) {
               char *e = strstr(path_trans,path_info);
               if (e) {
                  strncpy(path,path_trans,e-path_trans);
               }
            }
         }
#endif
      }
      if (path[0] != '\0')
         return path;
      else {
         WOLog(WO_ERR,"<FastCGI> Can't find document root in CGI variables");
         return "/usr/local/apache/htdocs";		/* this is bad.... */
      }
}
