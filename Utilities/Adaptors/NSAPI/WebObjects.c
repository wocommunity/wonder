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
 *	WebObjects server adaptor Netscape API module.
 *
 *	To install this adaptor, make the following modifications to Netscapes
 *	"obj.conf" file (site specific parameters enclosed in "<>"):
 *
 *	1) tell Netscape to load the shlib/dll
 *
 *		Init fn="load-modules" shlib="<path>/WebObjects.so" \
 *			funcs="WebObjects_init,WebObjectsNameTrans,WebObjectsRequest"
 *
 *	2) allow everything to be properly initialized,
 *
 *		Init fn="WebObjects_init" [root="<docroot>"] [config="webobjects://239.128.14.2:1085"] \
 *                      [username="<WOAdaptorInfo username">] [password="<WOAdaptorInfo password>"]
 *
 *	3) detect WebObjects request during the NameTrans process
 *
 *		NameTrans from="<something>" fn="WebObjectsNameTrans" \
 *			name="<objectname>" [dir="<docroot>"]
 *
 *		e.g. to map requests like "http://myhost/WebObjects/...." to WOF apps,
 *		use:
 *		NameTrans from="/WebObjects" fn="WebObjectsNameTrans" name="WebObjects"
 *
 *	4) define the service function for <objectname>
 *
 *		<Object name="<objectname>"
 *		Service fn="WebObjectsRequest"
 *		</Object>
 *
 */

#include "config.h"
#include "womalloc.h"
#include "request.h"
#include "response.h"
#include "log.h"
#include "transaction.h"
#include "MoreURLCUtilities.h"
#include "errors.h"
#include "httperrors.h"
#include "listing.h"
#include <nsapi.h>

#ifndef	Netscape_3		/* backward compatibility with 2.0... */
#define	server_portnum	port
#endif

/*
 *	development tool: uncomment if you want to log internal httpd stuff
 */
//#define	DEBUG_NETSCAPE_GUTS

/*
 *	keywords used in the obj.conf file:
 */
#define	PATHTRANS	"from"			/* NameTrans */
#define	APPROOT		"dir"			/* NameTrans */
#define	OBJECTNAME	"name"			/* NameTrans, Object */

char *WA_adaptorName = "NSAPI";

static int adaptorEnabled;


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
        //freeValueNeeded=1;

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
 *	this is pretty handy for figuring out where Netscape stashes
 *	all the interesting bits that we want to pass in the headers
 */
#ifdef	DEBUG_NETSCAPE_GUTS
static void dump_pb(pblock *pb, const char *msg)
{
   int i;

   for (i=0; pb  && (i < pb->hsize); i++) {
      struct pb_entry *entry = pb->ht[i];
      while (entry != NULL) {
         pb_param *nv = entry->param;
         if (nv != NULL)
            WOLog(WO_DBG,"%s->\t%s = %s",msg,nv->name,nv->value);
         entry = entry->next;
      }
   }
   return;
}
#else
#define	dump_pb(PB,MSG)		/* nop */
#endif

/*
 * Called during server init -
 *
 *	the following keys can be specified in an Init line in the
 *	obj.conf, like this:
 *
 *	Init fn="WebObjects_init" config="path-to-config" \
 *			root="WebObjects-Doc-Root-Path"
 *
 *	pb contains the "name=value" pairs from the Init line
 *	sn = rq = NULL
 *
 */
NSAPI_PUBLIC
int WebObjects_init(pblock *pb, Session *sn, Request *rq)
{
   strtbl *dict;
   int i;

   WOLog_init(NULL, NULL, WOLogLevel[WO_DBG]);
   dump_pb(pb,"init.pb");

   dict = st_new(10);
   /*
    *	copy all the key/value pairs into our init table
    */
   for (i=0; i < pb->hsize; i++) {
      struct pb_entry *entry = pb->ht[i];
      while (entry != NULL) {
         pb_param *kvpair = entry->param;
         if (kvpair != NULL) {
            st_add(dict, kvpair->name, kvpair->value, STR_COPYKEY|STR_COPYVALUE);
         }
         entry = entry->next;
      }
   }

   adaptorEnabled = init_adaptor(dict)==0;
   if (adaptorEnabled)
      log_error(1,"WebObjects",NULL,NULL,"initialized");
   return REQ_PROCEED;
}


/*
 *	NameTrans:
 *
 *	format of obj.conf is:
 *		NameTrans from="some-dir" fn=WebObjectsNameTrans dir="app-dir" name="obj-name"
 *
 *	If 'some-dir' is found in the URI, then set the request's WebObjects
 *	application root to 'app-dir' and let the WebObjects object process
 *	the request.
 *
 */
NSAPI_PUBLIC
int WebObjectsNameTrans(pblock *pb, Session *sn, Request *rq)
{
   WOURLComponents wc;
   const char *from;
   const char *objName;
   const char *uripath;
   const char *approot;

   if (!adaptorEnabled)
      return REQ_NOACTION;

   dump_pb(pb,"nametrans.pb");					/* spew debug info */
           dump_pb(rq->vars,"nametrans.rq->vars");		/* spew debug info */

                   from = pblock_findval(PATHTRANS,pb);
                   uripath = pblock_findval("ppath",rq->vars);
                   objName = pblock_findval(OBJECTNAME,pb);
                   if ((from == NULL) || (uripath == NULL) || (objName == NULL))
                   return REQ_NOACTION;

                   if (strncmp(from,uripath,strlen(from)) == 0) {
                      /*
                       *	make sure this is a valid WebObjects(tm) URL
                       */
                      wc = WOURLComponents_Initializer;
                      if (WOParseApplicationName(&wc, uripath) != WOURLOK) /* bail now if something wierd */
                         return REQ_NOACTION;
                      pblock_nvinsert(OBJECTNAME,(char *)objName,rq->vars);

                      approot = pblock_findval(APPROOT,pb);
                      if (approot)
                         pblock_nvinsert(APPROOT,(char *)approot,rq->vars);
                      return REQ_PROCEED;
                   } else
                   return REQ_NOACTION;
}

inline
static void cpyhdr(const char *key,pblock *pb,HTTPRequest *req,const char *wokey)
{
    const char *value = pblock_findval((char *)key, pb);
    if (value != NULL)
       req_addHeader(req, (wokey) ? wokey : key, value, 0);
}
/*
 *	Copy Headers into the request.
 *
 *	This is kind of like an easter egg hunt, CGI equivilant headers are
 *	stashed all over in different pblocks.  Do the best we can without
 *	missing any...
 */
static void copyHeaders(pblock *pb, Session *sn, Request *rq, HTTPRequest *req)
{
   int i;
   const char *hdrval;
   char *portstr;
   const char *server;


   /*
    *	the following line will generate a compiler warning. uncomment if
    *	Netscape ever implements it. request_loadheaders(sn,rq);
    */

   /*
    *	first, blindly copy the request headers
    */
   for (i=0; i < rq->headers->hsize; i++) {
      struct pb_entry *entry = rq->headers->ht[i];
      while (entry != NULL) {
         pb_param *hdr = entry->param;
         if (hdr != NULL)
            req_addHeader(req, hdr->name, hdr->value, 0);
         	   entry = entry->next;
      }
   }

   for (i=0; i < rq->vars->hsize; i++) {
      struct pb_entry *entry = rq->vars->ht[i];
      while (entry != NULL) {
         pb_param *hdr = entry->param;
          if (hdr != NULL) {
              /*
               * BEGIN Support for getting the client's certificate as one liner.
               */
              if (strcmp((const char *)hdr->name, "auth-cert") == 0 && hdr->value != NULL) {
                  const char *val = (const char *)make_cert_one_line((char *)hdr->value);
                  if(val != NULL){
                      req_addHeader(req, "SSL_CLIENT_CERT", val, 0);
                      //WOLog(WO_DBG, "Adding server variable %s", hdr->name);
                      //WOLog(WO_DBG, "With value %s", hdr->value);
                  }
                  /*
                   * END Support for getting the client's certificate.
                   */
                  else {
                      req_addHeader(req, hdr->name, hdr->value, 0);
                      //WOLog(WO_DBG, "Adding server variable %s", hdr->name);
                      //WOLog(WO_DBG, "With value %s", hdr->value);
                  }
              }
         }
         entry = entry->next;
      }
   }

   if (req->method == HTTP_POST_METHOD)
      req_addHeader(req,"REQUEST_METHOD","POST", 0);
   else if (req->method == HTTP_HEAD_METHOD)
      req_addHeader(req,"REQUEST_METHOD","HEAD", 0);
   else
      req_addHeader(req,"REQUEST_METHOD","GET", 0);

   /*
    *	collect up the server specific headers
    */
   cpyhdr("ip", sn->client, req, "REMOTE_ADDR");
   cpyhdr("query", rq->reqpb, req, "QUERY_STRING");
   cpyhdr("protocol", rq->reqpb, req, "SERVER_PROTOCOL");

   hdrval = session_maxdns(sn);
   if (!hdrval)	hdrval = session_dns(sn);
   if (hdrval)
      req_addHeader(req, "REMOTE_HOST", hdrval, 0);
   req_addHeader(req, "SERVER_SOFTWARE", system_version(), 0);
   portstr = (char *)WOMALLOC(32);
   if (portstr)
   {
      util_itoa(server_portnum, portstr);
      req_addHeader(req, "SERVER_PORT", portstr, STR_FREEVALUE);
   }

   /*
    *	Netscape claims to have fixed this in 3, if it causes a problem
    *	comment it out
    */
   server = server_hostname;
   if (server != NULL)
      req_addHeader(req, "SERVER_NAME", server, 0);


   return;
}

/*
 *	callback - copy headers into rq
 */
static void gethdr(const char *key, const char *value, void *rq)
{
   pblock_nvinsert((char *)key, (char *)value, ((Request *)rq)->srvhdrs);
}

static int sendResponse(Session *sn, Request *rq, HTTPResponse *resp)
{
   pb_param *pb_entry;

   /*
    *	collect up the headers
    */
   pb_entry = pblock_remove(CONTENT_TYPE,rq->srvhdrs);			/* remove default */
   param_free(pb_entry);			/* aB. Need to free parameters we remove from pblocks !!! */
   st_perform(resp->headers,gethdr,rq);

   /*
    *	ensure a content length
    */
   if (pblock_findval(CONTENT_LENGTH, rq->srvhdrs) == NULL) {
      char length[64];
      util_itoa(resp->content_length,length);
      pblock_nvinsert(CONTENT_LENGTH,length, rq->srvhdrs);
   }

   protocol_status(sn, rq, resp->status, resp->statusMsg);

   if (protocol_start_response(sn, rq) == REQ_NOACTION) {
      WOLog(WO_ERR,"protocol_start_response() returned REQ_NOACTION (!?)");
      return REQ_PROCEED;
   }

   if (resp->content_length)
      if (net_write(sn->csd, resp->content, resp->content_length) == IO_ERROR) {
         WOLog(WO_ERR,"Failed to send content to client");
         return REQ_EXIT;
      }

         return REQ_PROCEED;
}

static int die_resp(Session *sn, Request *rq, HTTPResponse *resp)
{
   sendResponse(sn, rq, resp);
   resp_free(resp);
   return REQ_PROCEED;
}

static int die(Session *sn, Request *rq, const char *msg, int status)
{
   HTTPResponse *resp;

   log_error(0,"WebObjects",NULL,NULL,"Aborting request - %s",msg);
   resp = resp_errorResponse(msg, status);
   return die_resp(sn, rq, resp);
}


/*
 *	the request handler...
 */
NSAPI_PUBLIC
int WebObjectsRequest(pblock *pb, Session *sn, Request *rq)
{
   HTTPRequest *req;
   HTTPResponse *resp = NULL;
   WOURLComponents wc = WOURLComponents_Initializer;
   const char *reqerr;
   const char *qs;
   int retval;
   char *uri;
   WOURLError urlerr;

   if (!adaptorEnabled)
      return REQ_NOACTION;

   /* spew debug info */
   dump_pb(sn->client,"service.sn->client");
   dump_pb(pb,"service.pb");
   dump_pb(rq->vars,"service.rq->vars");
   dump_pb(rq->reqpb,"service.rq->reqpb");
   dump_pb(rq->headers,"service.rq->headers");
   dump_pb(rq->srvhdrs,"service.rq->srvhdrs");

   uri = pblock_findval("uri", rq->reqpb);
   WOLog(WO_INFO,"<WebObjects NSAPI> new request: %s", uri);

   urlerr = WOParseApplicationName(&wc, uri);
   if (urlerr != WOURLOK) {
      const char *_urlerr;
      _urlerr = WOURLstrerror(urlerr);
      WOLog(WO_INFO,"URL Parsing Error: %s", _urlerr);
      if (urlerr == WOURLInvalidApplicationName) {
          if (ac_authorizeAppListing(&wc)) {
              resp = WOAdaptorInfo(NULL, &wc);
              die_resp(sn, rq, resp);
          } else {
              die(sn, rq, _urlerr, HTTP_NOT_FOUND);
          }
      }
          die(sn, rq, _urlerr, HTTP_BAD_REQUEST);
   }

   /*
    *	build the request ....
    */
   req = req_new( pblock_findval("method", rq->reqpb), NULL);

   /*
    *	validate the method
    */
   reqerr = req_validateMethod(req);
   if (reqerr) {
      req_free(req);
       return die(sn,rq,reqerr, HTTP_BAD_REQUEST);
   }

   /*
    *	copy the headers..
    */
   copyHeaders(pb, sn, rq, req);

   /*
    *	get form data if any
    *   assume that POSTs with content length will be reformatted to GETs later
    */
   if (req->content_length > 0)
   {
      int len_remaining = req->content_length;
      char *buffer = WOMALLOC(req->content_length);
      char *data = buffer;
      int c;

      while (len_remaining-- > 0) {
         if ((c = netbuf_getc(sn->inbuf)) == IO_ERROR) {
            log_error(0,"WebObjects",sn,rq,"Error reading form data");
            WOFREE(buffer);
            req_free(req);
            return die(sn,rq,INV_FORM_DATA, HTTP_BAD_REQUEST);
         }
         *data++ = c;
      }
      req->content = buffer;
   }

   /* Always get the query string */
   qs = pblock_findval("query", rq->reqpb);
   wc.queryString.start = qs;
   wc.queryString.length = qs ? strlen(qs) : 0;


   /*
    *	message the application & collect the response
    *
    *	note that handleRequest free()'s the 'req' for us
    */
   if (resp == NULL) {
      /* if no error so far... */
      req->api_handle = rq;				/* stash this in case it's needed */
      resp = tr_handleRequest(req, uri, &wc, pblock_findval("protocol",rq->reqpb), NULL);
   }

   if (resp != NULL) {
      retval = sendResponse(sn, rq, resp);
      resp_free(resp);
   } else {
      retval = REQ_EXIT;		/* no response from app - bail */
   }
   req_free(req);
#if defined(FINDLEAKS)
   showleaks();
#endif

   return retval;
}

