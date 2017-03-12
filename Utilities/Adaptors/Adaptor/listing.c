/*

Copyright � 2000-2007 Apple, Inc. All Rights Reserved.

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
 *	An optional module that generates a response containing information
 *	about the adaptor.  This includes adaptor compilation options,
 *	available applications and request/response information as well
 *	as an echo of the current request.
 */

#include "config.h"
#include "request.h"
#include "response.h"
#include "appcfg.h"
#include "loadbalancing.h"
#include "transport.h"
#include "womalloc.h"
#include "log.h"
#include "wastring.h"

#include <string.h>
#include <stdio.h>	/* sprintf() */
#include <stdlib.h>

#ifdef SOLARIS || defined(AIX)
#include <alloca.h>
#endif

#define	AUTH_CONTENT	\
"<HTML><HEAD><TITLE>404 Resource not found.</TITLE></HEAD><BODY><H1>Resource not found.</H1> The server could not find the resource you requested.<P></BODY></HTML>"

static HTTPResponse *createAuthorizationFailedResponse() {
   HTTPResponse *resp;
   resp = resp_new("HTTP/1.0 403 Authorization Required", AC_INVALID_HANDLE, NULL);
   st_add(resp->headers, "Content-Type", "text/html", 0);
   resp->content_length = resp->content_valid = resp->content_read = sizeof(AUTH_CONTENT);
   resp->content = AUTH_CONTENT;
   resp->flags |= RESP_DONT_FREE_CONTENT;
   return resp;
}

static void dohdr(const char *key, const char *value, String *data) {
   str_append(data,key);
   str_appendLiteral(data,": ");
   str_append(data,value);
   str_appendLiteral(data,"<br>");
}


HTTPResponse *WOAdaptorInfo(HTTPRequest *req, WOURLComponents *wc) {
   HTTPResponse *resp;
   strtbl *rqhdrs;
   String *content;
   char *adaptor_url, contentLength[32];

   if (!ac_authorizeAppListing(wc))
      return createAuthorizationFailedResponse();

   /* force an immediate reload of the config */
   ac_resetConfigTimers();
   ac_readConfiguration();
   /* The WOAdaptorInfo page can get pretty big. 32k is probably enough for 100 instances or so. */
   content = str_create("<HTML><HEAD><TITLE>WebObjects Adaptor Information</TITLE></HEAD><BODY>", 32768);
   if (!content)
   {
      WOLog(WO_ERR, "WOAdaptorInfo(): failed to allocate string buffer");
      resp = createAuthorizationFailedResponse();
      return resp;
   }

   resp = resp_new("HTTP/1.0 200 OK Apple", AC_INVALID_HANDLE, NULL);
   st_add(resp->headers, "Content-Type", "text/html", 0);

   adaptor_url = (char *)alloca(wc->prefix.length + 1);
   strncpy(adaptor_url, wc->prefix.start, wc->prefix.length);
   adaptor_url[wc->prefix.length] = 0;
   ac_listApps(content, adaptor_url);

   str_appendLiteral(content, "<br><strong>Server Adaptor:</strong><br>");
   str_appendLiteral(content, "<p>Server = ");
   str_append(content, WA_adaptorName);
   str_appendLiteral(content, "<br>WebObjects Server Adaptor version = " ADAPTOR_VERSION);
   str_append(content, "<br>");
   str_appendLiteral(content, "WebObjects Configuration URI(s) = ");
   ac_description(content);
   str_appendLiteral(content, "<br>Load balancing algorithms = ");
   lb_description(content);
   str_appendLiteral(content, "<br>Transport = ");
   tr_description(content);
   str_appendLiteral(content, "</p>");

   if (req != NULL)
   {
      str_appendLiteral(content, "<br><strong>Request headers:</strong><br>");
      rqhdrs = (strtbl *)req->headers;
      st_perform(rqhdrs, (st_perform_callback)dohdr, content);
   }
   str_appendLiteral(content, "</BODY></HTML>");

   resp->content_length = content->length;
   resp->content_valid = content->length;
   resp->content_read = content->length;
   resp->content = content->text;
   resp->flags |= RESP_DONT_FREE_CONTENT;
   resp_addStringToResponse(resp, content);

   sprintf(contentLength, "%d", content->length);
   st_add(resp->headers, "Content-Length", contentLength, STR_COPYVALUE);
   return resp;
}

