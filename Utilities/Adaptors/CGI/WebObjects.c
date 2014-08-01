/*

Copyright 2000-2007 Apple, Inc. All Rights Reserved.

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
 *  CGI - the 'simplest' case of an adaptor API.
 *
 *  Headers are passed in as environment variables (char **envp).
 *  Form data is available at stdin.
 *
 *  Response is returned via stdout.
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
#include "shmem.h"

#include <stdio.h>
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
#if !defined(MINGW)
#include <winnt-pdo.h>
#endif
#include <io.h>	/* setmode() */
#include <fcntl.h>
#endif

#ifndef MAXPATHLEN
#define MAXPATHLEN 255
#endif

static const char *documentRoot();

#define CGI_SCRIPT_NAME "SCRIPT_NAME"
#define CGI_PATH_INFO "PATH_INFO"
#define CGI_SERVER_PROTOCOL "SERVER_PROTOCOL"
#define CGI_DOCUMENT_ROOT "DOCUMENT_ROOT"
#define WO_CONFIG_URL "WO_CONFIG_URL"
#define WO_ADAPTOR_INFO_USERNAME "WO_ADAPTOR_INFO_USERNAME"
#define WO_ADAPTOR_INFO_PASSWORD "WO_ADAPTOR_INFO_PASSWORD"
#define WO_CONFIG_OPTIONS "WEBOBJECTS_OPTIONS"

char *WA_adaptorName = "CGI";
static unsigned int freeValueNeeded=0;

/*
 *  the CGI1.1 spec says:
 *
 *  "For Unix compatible operating systems, the following are defined:
 *  ...
 *  Character set
 *    The US-ASCII character set is used for the definition of
 *    environment variable names and header field names; the newline
 *    (NL) sequence is LF; servers SHOULD also accept CR LF as a
 *    newline.
 *  ..."
 */
#define CRLF  "\r\n"

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
 *  send response to server
 */
static void sendResponse(HTTPResponse *resp)
{
   String *resphdrs;
#ifndef PROFILE
   fprintf(stdout,"Status: %d %s" CRLF,resp->status,resp->statusMsg);
#endif

   resphdrs = resp_packageHeaders(resp);
#ifndef PROFILE
   fputs(resphdrs->text,stdout);
#endif
   str_free(resphdrs);
#ifndef PROFILE
   fputs(CRLF,stdout);
#endif

#ifndef PROFILE
   /* resp->content_valid will be 0 for HEAD requests and empty responses */
   if (resp->content_valid) {
      int count;

      while (resp->content_read < resp->content_length &&
             (resp->flags & RESP_LENGTH_INVALID) != RESP_LENGTH_INVALID) {
         fwrite(resp->content,sizeof(char),resp->content_valid,stdout);
         count = resp_getResponseContent(resp, 1);
         if(count > 0)
         {
            // 2009/06/09: handle situations where content_length is wrong or
            //             unset. Read as much data as possible from the
            //             WebObjects application and send the data to the
            //             client-side.
            resp->content_read += count;
            resp->content_valid = count;
         }
         if(count != 0)
         {
            // 2009/04/30: error while reading response content (this can happen
            //             if the instance dies during sending the response - e.g.
            //             during a file download - or if the content_length is
            //             wrong/unset). Stop the loop to avoid endless looping!
            WOLog(WO_WARN, "sendResponse(): received an incomplete data package. Please look for a dead instance or adjust content-length value.");
         }
        }
        fwrite(resp->content,sizeof(char),resp->content_valid,stdout);
   }
   fflush(stdout);
#endif
   return;    
}

static void die_resp(HTTPResponse *resp)
{
   sendResponse(resp);
   resp_free(resp);
   exit(0);
}

static void die(const char *msg, int status)
{
   HTTPResponse *resp = resp_errorResponse(msg, status);
   die_resp(resp);
}


/* Read up to dataSize bytes into the buffer at dataBuffer. */
/* Returns the number of bytes read, or -1 on error. */
static int readContentData(HTTPRequest *req, void *buffer, int dataSize, int mustFill)
{
   int n = fread(buffer, 1, dataSize, stdin);
   if (n != dataSize) {
      int err = ferror(stdin);
      if (err)
         WOLog(WO_ERR,"Error getting content data: %s (%d)", strerror(errno), err);
   }
   return n == dataSize ? n : -1;
}

int do_tests()
{
  int res = 0;
  
  if(res == 0)
    res = shmem_do_tests();

  return res;
}

#ifdef  PROFILE
int doit(int argc, char *argv[], char **envp);  /* forward */

int main(int argc, char *argv[], char **envp) {
   int i;
   for (i=0; i < 50000; i++)
      doit(argc, argv, envp);
   return 0;
}
int doit(int argc, char *argv[], char **envp) {
#else
   /*
    * the request handler...
    */
   int main(int argc, char *argv[], char **envp) {
#endif
      HTTPRequest *req;
      HTTPResponse *resp = NULL;
      WOURLComponents wc = WOURLComponents_Initializer;
      const char *qs;
      unsigned int qs_len;
      char *url;
      const char *script_name, *path_info, *config_url, *username, *password, *config_options;
      const char *reqerr;
      WOURLError urlerr;
      strtbl *options = NULL;

#ifdef WIN32
      _setmode(_fileno(stdout), _O_BINARY);
      _setmode(_fileno(stdin), _O_BINARY);
#endif

      if(argc == 2 && strcmp(argv[1], "test") == 0)
      {
	exit(do_tests());
      }

      script_name = getenv(CGI_SCRIPT_NAME);
      path_info = getenv(CGI_PATH_INFO);

      if (script_name == NULL)
         die(INV_SCRIPT, HTTP_NOT_FOUND);
      else if (path_info == NULL) {
         path_info = "/";
      }
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
      WOLog(WO_INFO,"CGI: config url is %s", config_url);
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
          die("The request could not be completed due to a server error.", HTTP_SERVER_ERROR);
      }

      /*
       *  extract WebObjects application name from URI
       */

      url = WOMALLOC(strlen(path_info) + strlen(script_name) + 1);
      strcpy(url, script_name);
      strcat(url, path_info);
      WOLog(WO_INFO,"<CGI> new request: %s",url);
      
      urlerr = WOParseApplicationName(&wc, url);
      if (urlerr != WOURLOK) {
         const char *_urlerr;
         _urlerr = WOURLstrerror(urlerr);
         WOLog(WO_INFO,"URL Parsing Error: %s", _urlerr);

         if (urlerr == WOURLInvalidApplicationName) {
             if (ac_authorizeAppListing(&wc)) {
                 resp = WOAdaptorInfo(NULL, &wc);
                 die_resp(resp);
             } else {
                 die(_urlerr, HTTP_NOT_FOUND);
             }
         }

         die(_urlerr, HTTP_BAD_REQUEST);
      }

      /*
       *  build the request...
       */
      req = req_new( getenv("REQUEST_METHOD"), NULL);

      /*
       *  validate the method
       */
      reqerr = req_validateMethod(req);
      if (reqerr) {
          die(reqerr, HTTP_BAD_REQUEST);
      }

      /*
       *  copy the headers.  This looks wierd... all we're doing is copying
       *  *every* environment variable into our headers.  It may be beyond
       *  the spec, but more information probably won't hurt.
       */
      while (envp && *envp) {
         char *key, *value;
         /* copy env. line. */
         key = WOSTRDUP(*envp);

         for (value = key; *value && !isspace((int)*value) && (*value != '='); value++) {}
         if (*value) {
            *value++ = '\0';  /* null terminate 'key' */
         }
         while (*value && (isspace((int)*value) || (*value == '='))) {
            value++;
         }
         /* BEGIN Support for getting the client's certificate. */
         if (strcmp((const char *)key, "SSL_CLIENT_CERTIFICATE") == 0 || strcmp((const char *)key, "SSL_SERVER_CERTIFICATE") == 0 ) {
             value = 0;
             WOLog(WO_INFO,"<CGI> DROPPING ENV VAR (DUPLICATE) = %s", key);
         }
         if (strcmp((const char *)key, "SSL_CLIENT_CERT") == 0 || strcmp((const char *)key, "SSL_SERVER_CERT") == 0) {
             value = make_cert_one_line(value);
             //WOLog(WO_INFO,"<CGI> PASSING %s = %s", key, value);
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
         envp++;      /* next env variable */
      }

      /*
       *  get form data if any
       *  assume that POSTs with content length will be reformatted to GETs later
       */
      if (req->content_length > 0) {
         req_allocateContent(req, req->content_length, 1);
         req->getMoreContent = (req_getMoreContentCallback)readContentData;
         if (req->content_buffer_size == 0)
            die(ALLOCATION_FAILURE, HTTP_SERVER_ERROR);
         if (readContentData(req, req->content, req->content_buffer_size, 1) == -1) {
            die(WOURLstrerror(WOURLInvalidPostData), HTTP_BAD_REQUEST);
         }
      }

      /* Always get the query string */
      qs = getenv("QUERY_STRING");
      if (qs) {
         qs_len = strlen(qs);
      } else {
         qs_len = 0;
      }

      if (qs_len > 0) {
         wc.queryString.start = qs;
         wc.queryString.length = qs_len;
         WOLog(WO_INFO,"<CGI> new request with Query String: %s", qs);
      }

      /*
       *  message the application & collect the response
       */
      resp = tr_handleRequest(req, url, &wc, getenv(CGI_SERVER_PROTOCOL), documentRoot());

      if (resp != NULL) {
         sendResponse(resp);
         resp_free(resp);   /* dump the response */
      }

      WOFREE(url);
      st_free(options);
      req_free(req);
      
#if defined(FINDLEAKS)
      showleaks();
#endif
      return 0;
   }


   const char *documentRoot() {
      static char path[MAXPATHLEN+1] = "";

      if (path[0] == '\0') {
#ifdef WIN32
         WOReadKeyFromConfiguration(CGI_DOCUMENT_ROOT, path, MAXPATHLEN);
#else
         const char *doc_root;
         /* Apache provides this as an environment variable straight */
         if ((doc_root = getenv(CGI_DOCUMENT_ROOT)) != NULL) {
            strncpy(path, doc_root, MAXPATHLEN);
         } else {
            const char *path_trans, *path_info;
            path_trans = getenv("PATH_TRANSLATED");
            path_info = getenv("PATH_INFO");

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
         WOLog(WO_ERR,"Can't find document root in CGI variables");
         return "/usr/local/apache/htdocs";   /* this is bad.... */
      }
}
