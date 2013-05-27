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
 *	This is where the http server independent portion of the request
 *	processing is done.
 *
 *	We can fail in a number of ways here:
 *	 - app not found
 *	 - can't connect to application
 *	 - error while transmitting request
 *	 - invalid response received from app (error while receiving)
 *
 *	Creates a response page from the configuration when app not found (and feature is enabled).
 *
 */

/* #define DEBUG 1 */

#include "config.h"
#include "WOAppReq.h"
#include "transaction.h"
#include "log.h"
#include "errors.h"
#include "httperrors.h"
#include "loadbalancing.h"
#include "appcfg.h"
#include "transport.h"
#include "listing.h"
#include "womalloc.h"
#include "MoreURLCUtilities.h"
#include "shmem.h"

#include <sys/types.h>
#ifndef WIN32
#include <unistd.h>
#include <sys/param.h>		/* MAX name lengths */
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>


/*
 *	this creates a response which documents the request.  Good for
 *	debugging things with weird undebuggable environments, like NSAPI.
 */
HTTPResponse *WOAdaptorInfo(HTTPRequest *req, WOURLComponents *wc);

static HTTPResponse *_collectRequestInformation(WOAppReq *app, WOURLComponents *wc, const char *url, int urlVersion, HTTPRequest *req);
static HTTPResponse *_runRequest(WOAppReq *app, WOAppHandle woappHandle, WOInstanceHandle instHandle, HTTPRequest *req);
static HTTPResponse *_errorResponse(WOAppReq *app, WOURLComponents *wc, HTTPRequest *req);

static char uniqueID_str[17];
static int uniqueID_counter;
static WA_recursiveLock tr_lock = NULL;

static String *tr_uniqueID()
{
   String *uniqueID;
   char counter_str[9];
   int count;

   uniqueID = str_create(uniqueID_str, 25);

   WA_lock(tr_lock);
   count = uniqueID_counter++;
   WA_unlock(tr_lock);
   sprintf(counter_str, "%8.8x", count);
   str_appendLength(uniqueID, counter_str, 8);
   return uniqueID;
}

int transaction_init()
{
   int pid, currentTime;
   tr_lock = WA_createLock("transaction lock");
   currentTime = time(NULL);
#ifndef WIN32
   pid = getpid();
#else
   pid = GetCurrentProcessId();
#endif
   sprintf(uniqueID_str, "%8.8x%8.8x", currentTime, pid);
   return tr_lock == NULL;
}

#define haveTriedInstance(appreq, handle) (appreq->attemptedInstances[(handle)>>8] & (1<<((handle)&7)))
#define markTriedInstance(appreq, handle) (appreq->attemptedInstances[(handle)>>8] |= (1<<((handle)&7)))
/*
 * This function wraps the call to the load balancer's implementation of selectInstance.
 * If the load balancer returns AC_INVALID_HANDLE (could not find an instance), this
 * function takes over and tries to send the request to each configured instance in turn.
 */
static WOInstanceHandle tr_selectInstance(WOAppReq *appreq, _WOApp *app)
{
   WOInstanceHandle instHandle = AC_INVALID_HANDLE;
   
   /* Call the selectInstance with the app write locked so the scheduler can write to the loadBalancingInfo area. */
   /* Also, holding the lock prevents the instance from being deleted from the config before we update pendingResponses. */
   if (!appreq->schedulerFailed)
   {
      if (appreq->scheduler->selectInstance)
         instHandle = appreq->scheduler->selectInstance(appreq, app);
      else {
         WOLog(WO_ERR, "tr_selectInstance(): scheduler does not define selectInstance()!");
         instHandle = AC_INVALID_HANDLE;
      }
      if (instHandle == AC_INVALID_HANDLE)
         appreq->schedulerFailed = 1;
      else {
         if (haveTriedInstance(appreq, instHandle))
         {
            /* if the scheduler picks the same instance twice for the same request, consider this a failure */
            ac_unlockInstance(instHandle);
            instHandle = AC_INVALID_HANDLE;
            appreq->schedulerFailed = 1;
         }
      }
   }
   if (appreq->schedulerFailed)
   {
      int i;
      WOInstance *inst;
      time_t currentTime;

      currentTime = time(NULL);
      /* If the scheduler failed we will get in here. Just run through the configured instances in order. */
      WOLog(WO_INFO, "tr_selectInstance(): scheduler failed to select instance.");
      for (i=0; i<WA_MAX_APP_INSTANCE_COUNT && instHandle == AC_INVALID_HANDLE; i++)
      {
         if (app->instances[i] != AC_INVALID_HANDLE && !(haveTriedInstance(appreq, app->instances[i])))
         {
            inst = ac_lockInstance(app->instances[i]);
            if (inst)
            {
               if (inst->instanceNumber[0] != '-' && inst->connectFailedTimer < currentTime)
               {
                  instHandle = app->instances[i];
                  WOLog(WO_INFO, "tr_selectInstance(): Trying instance number %s", inst->instanceNumber);
               } else {
                  ac_unlockInstance(app->instances[i]);
               }
            }
            if (instHandle == AC_INVALID_HANDLE)
               markTriedInstance(appreq, app->instances[i]);
         }
      }
   }
   if (instHandle != AC_INVALID_HANDLE)
      markTriedInstance(appreq, instHandle);
   else
      appreq->error = err_noInstance;
   return instHandle;
}

/*
 * Prepare appreq to use the given instance. The instance must be locked.
 */
static void tr_prepareToUseInstance(WOAppReq *appreq, WOInstanceHandle instHandle)
{
   _WOInstance *instance;
   
   instance = ac_lockedInstance(instHandle);
   if (instance)
   {
      /* this will prevent the instance from being freed */
      /* note that we are holding a write lock on the app */
      instance->pendingResponses++;
      /* This number is used in req_reformatRequest when updating the request URL */
      memcpy(appreq->instance, instance->instanceNumber, WA_MAX_INSTANCE_NUMBER_LENGTH);
      appreq->port = instance->port;
   }
}

      
/*
 *	Handle the meat of the request:
 *	   - check for a load balanced instance
 *	   - (or) autostart an (or find an autostarted) instance
 *	   - open a socket to the application
 *	   - message the application with the request using HTTP
 *	   - wait for and receive the response
 *	   - free the request
 *	   - return the response
 *
 *	If we can't message the application, retry the request up to
 *	RETRIES times (see config.h).
 *
 */
HTTPResponse *tr_handleRequest(HTTPRequest *req, const char *url, WOURLComponents *wc, const char *server_protocol, const char *docroot)
{
   WOAppReq app;
   int connectionAttempts = 0, searchAttempts = 0;
   HTTPResponse *resp = NULL;
   WOAppHandle appHandle = AC_INVALID_HANDLE;
   _WOApp *woapp = NULL;
   WOInstanceHandle instHandle = AC_INVALID_HANDLE, retryInstHandle;
   _WOInstance *inst;
   int urlVersion, retries = RETRIES;

   memset(&app, 0, sizeof(WOAppReq));
   app.urlVersion = CURRENT_WOF_VERSION_MAJOR;
   app.docroot = docroot;
   app.request = req;				/* valid for request forwarding phase */

   /*
    * copy the application name
    */
   if (wc->applicationName.length != 0 && wc->applicationName.length < WA_MAX_APP_NAME_LENGTH) {
      strncpy(app.name, wc->applicationName.start, wc->applicationName.length);
      app.name[wc->applicationName.length] = '\0';
   }

   /*
    *	find the application
    */
   for (searchAttempts = 0; searchAttempts < 2 && instHandle == AC_INVALID_HANDLE; searchAttempts++)
   {
      /* If the find app succeeds, have to be very careful about the possibility */
      /* that someone else can delete it out from under us. */
      appHandle = ac_findApplication(app.name);
      if (appHandle != AC_INVALID_HANDLE)
      {
         /* cache some info so we don't have to keep the app locked */
         woapp = ac_lockedApp(appHandle);
         urlVersion = woapp->urlVersion;
         app.scheduler = lb_schedulerByName(woapp->loadbalance);
         strncpy(app.redirect_url, woapp->redirect_url, WA_MAX_URL_LENGTH);
         retries = woapp->retries;
         /* extract the request & application information from the URI */
         resp = _collectRequestInformation(&app, wc, url, urlVersion, req);
         if (resp != NULL) {
            ac_unlockApp(appHandle);
            return resp;		/* some kind of error in URI */
         }

         /* note: if we found the app, it is still locked at this point */

         /*
          *	select an app, the request may be requesting a specific instance
          *	or it may be indifferent.  an instance neutral request may be a new
          *	request or a returning one for which the state is not stored in the
          *	app.
          * 	if specified instance does not respond, we'll failover to another
          * 	instance
          */
         if (app.instance[0] == 0) {
            instHandle = tr_selectInstance(&app, woapp);
            if (instHandle != AC_INVALID_HANDLE)
            {
               WOLog(WO_INFO,"Selected new app instance at index %d", instHandle);
               tr_prepareToUseInstance(&app, instHandle);
               ac_unlockInstance(instHandle);
            }
         } else {
            WOLog(WO_INFO,"Selecting specific app instance %s.", app.instance);
            instHandle = ac_findInstance(woapp, app.instance);
            if (instHandle == AC_INVALID_HANDLE)
            {
               WOLog(WO_WARN, "Unable to find instance %s. Attempting to select another.", app.instance);
               instHandle = tr_selectInstance(&app, woapp);
            }
            if (instHandle != AC_INVALID_HANDLE)
            {
               tr_prepareToUseInstance(&app, instHandle);
               ac_unlockInstance(instHandle);
            }
         }

         /* At this point, pendingResponses has been incremented in the instance. */
         /* An instance will not be deleted with a nonzero pendingResponses coung, and */
         /* an app will not be deleted if it has any instances. Now it is ok to unlock the app. */
         ac_unlockApp(appHandle);
      }
      /* If we didn't find the app or if we didn't find a specific requested instance, reload the config */
      if (searchAttempts == 0 && (appHandle == AC_INVALID_HANDLE || (instHandle == AC_INVALID_HANDLE && app.instance[0] == '-')))
      {
         if (app.instance[0] != 0)
            WOLog(WO_INFO, "Requested application '%s' not found. Reloading config.", app.name);
         else
            WOLog(WO_INFO, "Specific instance %s:%s not found. Reloading config.", app.name, app.instance);
         ac_resetConfigTimers();
         ac_readConfiguration();
      } else {
         /* didn't reload the config, so no point in retrying */
         break;
      }
   }

   /*
    *	run the request...
    */
   resp = NULL;
   if (instHandle != AC_INVALID_HANDLE) {
      /* Attempt to send request and read response */
      do {
         /* Fix up URL so app knows it's being load balanced ...  */
         /* We need to do this in the loop to get correct instance number into the request URL */
         req_reformatRequest(req, &app, wc, server_protocol);
         WOLog(WO_INFO,"Sending request to instance number %s, port %d", app.instance, app.port);
         resp = _runRequest(&app, appHandle, instHandle, req);

         /* Cannot retry if we have read some streamed content data because we cannot unwind the byte stream. */
         if (req->haveReadStreamedData)
            retries = 0;
         
         if (resp) {
            if (resp->status == 302) /* redirected */
            {
               if (!req->haveReadStreamedData && st_valueFor(resp->headers, "x-webobjects-refusing-redirection"))
               {
                  /* redirected because instance is refusing new sessions */
                  resp_free(resp);
                  resp = NULL;
                  WOLog(WO_INFO, "Request redirected because instance refusing new sessions.");
               }
            }
         } else {
            if (app.error != err_read)
            {
               connectionAttempts++;
               /* Mark this instance as unresponsive */
               WOLog(WO_INFO,"Marking instance %s dead", app.instance);
               if (app.scheduler->instanceDidNotRespond)
               {
                  inst = ac_lockInstance(instHandle);
                  if (inst)
                  {
                     app.scheduler->instanceDidNotRespond(inst);
                     ac_unlockInstance(instHandle);
                  }
               }
               WOLog(WO_DBG, "connectionAttempts = %d, retries = %d", connectionAttempts, retries);
               ac_readConfiguration();
            }
         }
         if (resp == NULL && connectionAttempts <= retries) {
            /* appHandle is still valid because we have not decremented pendingResponses on the instance */
            woapp = ac_lockApp(appHandle);
            if (woapp)
            {
               retryInstHandle = tr_selectInstance(&app, woapp);
               if (retryInstHandle != AC_INVALID_HANDLE)
               {
                  tr_prepareToUseInstance(&app, retryInstHandle);
                  ac_unlockInstance(retryInstHandle);
               }
               ac_unlockApp(appHandle);
            } else
               retryInstHandle = AC_INVALID_HANDLE;
            /* Decrement pendingResponses on the original instance. It is safe to do now. */
            /* The retry instance has had pendingResponses incremented, which will prevent the */
            /* app from being removed. (Or, there was not other instance to retry, and we are done.) */
            inst = ac_lockInstance(instHandle);
            if (inst)
            {
               if (inst->pendingResponses > 0)
                  inst->pendingResponses--;
               ac_unlockInstance(instHandle);
               instHandle = retryInstHandle;
            }
            if (retryInstHandle != AC_INVALID_HANDLE) {
               WOLog(WO_INFO,"Attempting failover to new instance at index %d", retryInstHandle);
            } else {
               WOLog(WO_INFO,"No new instance located for failover");
            }
            instHandle = retryInstHandle;
         }
      } while (resp == NULL && instHandle != AC_INVALID_HANDLE && connectionAttempts < retries);
      if (instHandle != AC_INVALID_HANDLE)
      {
         /* still have to decrement pendingResponses */
         inst = ac_lockInstance(instHandle);
         if (inst)
         {
            if (inst->pendingResponses > 0)
               inst->pendingResponses--;
            ac_unlockInstance(instHandle);
            /* At this point, the app could be removed from the config at any time. */
            instHandle = retryInstHandle;
         }
      }
   } else {
      if (ac_authorizeAppListing(wc))
         resp = WOAdaptorInfo(req, wc);
      else
         app.error = err_notFound;
   }

   /*
    *	how'd we do?
    */
   if (resp == NULL) {
      /*
       *	we may be able to send a palatable response...
       */
      resp = _errorResponse(&app, wc, req);
   }
   return resp;
}

/*
 *	finish the job that the api stubs started..
 */
static HTTPResponse *_collectRequestInformation(WOAppReq *app, WOURLComponents *wc, const char *url, int urlVersion, HTTPRequest *req)
{
   const char *urlerr;
   /*
    *	we need to complete the URL parsing...
    */
   if ((urlVersion == 4) || (urlVersion == 3)) {
      urlerr = WOParseAndCheckURL(wc, url, urlVersion, req->shouldProcessUrl);
   } else {
      urlerr = "Unsupported URL version";
      WOLog(WO_WARN, "Unsupported URL version (%d) on %s", urlVersion, app->name);
   }

   if (urlerr != NULL)
      return resp_errorResponse(urlerr, HTTP_BAD_REQUEST);

   /*
    *	pick up the app name & host (if any) & instance
    */
   if (wc->applicationName.length == 0)
       return resp_errorResponse(NO_APPNAME, HTTP_BAD_REQUEST);

   strncpy(app->name, wc->applicationName.start, wc->applicationName.length);
   app->name[wc->applicationName.length] = '\0';
   if (app->name[wc->applicationName.length - 1] == '/')	/* garbage? */
       app->name[wc->applicationName.length - 1] = '\0';

       if (wc->applicationHost.length > 0) {
          strncpy(app->host, wc->applicationHost.start, wc->applicationHost.length);
          app->host[wc->applicationHost.length] = '\0';
       } else
       app->host[0] = '\0';

       /*
        *	is the session/application information in the URL?
        *	if not, look in the cookies for the application instance number
        *	in the cookies.
        */
       app->instance[0] = 0; /* default to any instance */
       if (wc->applicationNumber.length > 0) {
          if (wc->applicationNumber.length < WA_MAX_INSTANCE_NUMBER_LENGTH)
          {
             memcpy(app->instance, wc->applicationNumber.start, wc->applicationNumber.length);
             app->instance[wc->applicationNumber.length] = 0;
          }
       } else {
          const char *cookie, *woinst;

          cookie = req_HeaderForKey(req, COOKIE);
          if (cookie && ((woinst = strstr(cookie, INST_COOKIE)) != NULL)) {
             const char *instid = &woinst[sizeof(INST_COOKIE)-1];
             int len = 0;
             while (len < WA_MAX_INSTANCE_NUMBER_LENGTH && instid[len] != ';' && instid[len])
                len++;
             if (len < WA_MAX_INSTANCE_NUMBER_LENGTH)
             {
                memcpy(app->instance, instid, len);
                app->instance[len] = 0;
             }

			 // remove any quotes from the instance number
#ifdef _MSC_VER // SWK Start VC can't define attributes here using '{' fixed it
			 {
#endif
			 char *before, *after;
			 before = after = app->instance;
			 while(*before){
			 	if((*before == '\'') || (*before == '"')){
					before++;
				}
				*after++ = *before++;
			 }
			 *after = 0;
#ifdef _MSC_VER // SWK End
			 }
#endif
             WOLog(WO_INFO,"Cookie instance %s from %s",app->instance,cookie);
          }
       }

       app->urlVersion = (wc->webObjectsVersion.start) ?
       atoi(wc->webObjectsVersion.start) : CURRENT_WOF_VERSION_MAJOR;

       /*
        *	Add the adaptor identifier header.
        *  WOFramework checks for the presence of this header, so the name should not change.
        *  (The value of the header is ignored by WOFramework.)
        */
       req_addHeader(req, "x-webobjects-adaptor-version", WA_adaptorName, 0);

       return NULL;  /* no errors */
}

/*
 *	run the request
 */
static HTTPResponse *_runRequest(WOAppReq *app, WOAppHandle woappHandle, WOInstanceHandle instHandle, HTTPRequest *req) {
   WOConnection *c;
   HTTPResponse *resp = NULL;
   int send_status, keepConnection, retryRequest = 0;
   const char *idString = NULL;

   WOLog(WO_INFO,"Trying to contact %s:%s on %s(%d)", app->name,app->instance,app->host,app->port);

   /* Tag the request with a unique identifier (if it hasn't been already) */
   /* (It might already have been tagged if we are retrying the request due to refusing new sessions, for example) */
   idString = req_HeaderForKey(req, REQUEST_ID_HEADER);
   if (idString == NULL)
   {
      String *requestID = tr_uniqueID();
      if (requestID)
      {
         req_addHeader(req, REQUEST_ID_HEADER, requestID->text, STR_COPYVALUE);
         idString = req_HeaderForKey(req, REQUEST_ID_HEADER);
         str_free(requestID);
      }
   }
   
   do {
      c = tr_open(instHandle);				/* connect */

      if (c == NULL) {
         WOLog(WO_INFO,"%s:%s NOT LISTENING on %s(%d)", app->name,app->instance,app->host,app->port);
         app->error = err_connect;
         return NULL;
      }

      /*
       *	app found and is listening on port
       */
      if (app->scheduler->beginTransaction)
         app->scheduler->beginTransaction(app, instHandle);

      /* Make sure that we're the only connection header, and we're explicit about the setting */
      req_removeHeader(req,CONNECTION);
      if (c->isPooled) {
         req_addHeader(req,CONNECTION,HTTP_KEEP_ALIVE,0);
      } else {
	 req_addHeader(req,CONNECTION,HTTP_CLOSE,0);
      }

      WOLog(WO_INFO,"%s:%s on %s(%d) connected [pooled: %s]", app->name, app->instance, app->host, app->port, c->isPooled ? "Yes" : "No");

      /*
       *	send the request....
       */
      send_status = req_sendRequest(req, c->fd);
      if (send_status != 0) {
         if ((send_status == TR_RESET) && (retryRequest == 0) && !req->haveReadStreamedData)  {
              /* If we get here the connection was reset. This means the instance has either quit or crashed. */
              /* Bump the generation number so all pooled connections to this instance will be invalidated. */
              /* Then retry the request with a new connection. If the instance is not running the retry will */
              /* fail with a different error and the instance will be marked dead. */
              _WOInstance *inst = ac_lockInstance(instHandle);
              /* note: if we get here, keepConnection == 0 --> this connection will be closed */
              if (inst)
              {
                  ac_cycleInstance(inst, c->generation);
                  ac_unlockInstance(instHandle);
              }
              retryRequest++;
              WOLog(WO_INFO, "retrying request due to connection reset");

              /* Must close connection before continuing */
              tr_close(c, instHandle, 0);
              continue;
          } else {
              WOLog(WO_ERR,"Failed to send request");
              tr_close(c, instHandle, 0);          /* close app connection */
              if (send_status == -1)
                 app->error = err_read;
              else
                 app->error = err_send;
              return NULL;
          }
      }

      /* Note that we have a request queued */
      WOLog(WO_INFO,"Request %s sent, awaiting response", req->request_str);

      /* While the app is processing the request, take the opportunity to check/update the config. */
      ac_readConfiguration();

      /*
       *	now wait for the response...
       */
      resp = resp_getResponseHeaders(c, instHandle);
      /* go ahead and read the first chunk of response data */
      if (resp && req->method != HTTP_HEAD_METHOD)
      {
         int count = resp_getResponseContent(resp, 1);
         if (count == -1)
         {
            resp_free(resp);
            resp = NULL;
         }
         else
         {
            // 2009/06/09: handle situations where content_length is wrong or
            //             unset.  Read as much data as possible from the
            //             WebObjects application and send the data to the
            //             client-side.
            if(count > 0)
            {
               resp->content_read += count;
               resp->content_valid = count;
            }
         }
      }

      /* Validate the ID */
      if (idString && resp)
      {
         const char *respID = st_valueFor(resp->headers, REQUEST_ID_HEADER);
         if (respID != NULL)
         {
            if (strcmp(respID, idString) != 0)
            {
               WOLog(WO_ERR, "Got response with wrong ID! Dumping response. request ID = %s, response ID = %s", idString, respID);
               /* note this will cause the connection to be closed below */
               resp_free(resp);
               resp = NULL;
            } else
               st_removeKey(resp->headers, REQUEST_ID_HEADER);
         } else
            WOLog(WO_WARN, "Got response with no ID.");
      }
               
      app->response = resp;

      /*
       *	check if this connection can be kept open
       */
      keepConnection = 0;

#ifndef CGI /* doesn't make sense to keep the connection for CGI */
      if (resp && resp->headers)
      {
         const char *keepAlive;
         keepAlive = st_valueFor(resp->headers, CONNECTION);
         if (keepAlive)
         {
            /* if the keep alive header is set, honor the value */
            if (strcasecmp(keepAlive, HTTP_KEEP_ALIVE) == 0)
               keepConnection = 1;
         } else {
            /* no keep alive header - keep alive by default for HTTP/1.1 only */
            if (resp->flags & RESP_HTTP11)
               keepConnection = 1;
         }
      }
#endif

      if (resp != NULL) {
         if (app->scheduler->finalizeTransaction)
            if (app->scheduler->finalizeTransaction(app, instHandle))
               keepConnection = 0;

         st_removeKey(resp->headers, REFUSING_SESSIONS_HEADER);
         st_removeKey(resp->headers, LOAD_AVERAGE_HEADER);
         st_removeKey(resp->headers, CONNECTION);

         WOLog(WO_INFO,"received ->%d %s",resp->status,resp->statusMsg);
         retryRequest = 0;
      } else {
         if (c != NULL && tr_connectionWasReset(c))
         {
            /* If we get here the connection was reset. This means the instance has either quit or crashed. */
            /* Bump the generation number so all pooled connections to this instance will be invalidated. */
            /* Then retry the request with a new connection. If the instance is not running the retry will */
            /* fail with a different error and the instance will be marked dead. */
            /* Note that only one retry due to a connection reset error is allowed. This is to prevent an */
            /* infinite loop if the instance dies while processing the request and restarts quickly enough */
            /* to process the retry. */
            _WOInstance *inst = ac_lockInstance(instHandle);
            /* note: if we get here, keepConnection == 0 --> this connection will be closed */
            if (inst)
            {
               ac_cycleInstance(inst, c->generation);
               ac_unlockInstance(instHandle);
            }
            retryRequest++;
            if(req->haveReadStreamedData)
            {
                // 2009/04/28: in case of streaming operation, the request
                //             isn't fully available after a connection reset! 
                app->error = err_response;
                retryRequest = 0;
            }
            if (retryRequest == 1)
               WOLog(WO_INFO, "retrying request due to connection reset");
         } else
         app->error = err_response;
      }
      if (resp && resp->content_read < resp->content_length)
      {
         resp->keepConnection = keepConnection;
         resp->instHandle = instHandle;
         resp->flags |= RESP_CLOSE_CONNECTION;
      } else {
         tr_close(c, instHandle, keepConnection);
      }
   } while (retryRequest == 1);

   return resp;
}

static const char * const _errors[] = {
   "No error",
   NOT_FOUND_APP,
   "No instance available",
   "Can't connect to application instance",
   NO_RESPONSE,
   INV_RESPONSE,
   "Failed to read content data (timed out or broken connection)",
   NULL
};

static HTTPResponse *_errorResponse(WOAppReq *app, WOURLComponents *wc, HTTPRequest *req)
{
   HTTPResponse *resp;
   const char *redirect_url = &app->redirect_url[0];

   WOLog(WO_ERR,"Request handling error: %s",_errors[app->error]);

   if (app->redirect_url[0] == 0)
      redirect_url = adaptor_valueForKey(WOERRREDIR);

   /*
    *	try to do the right thing...
    */
   if (redirect_url != NULL) {
      resp = resp_redirectedResponse(redirect_url);
   } else if (app->error == err_notFound) {
      if (ac_authorizeAppListing(wc))
         resp = WOAdaptorInfo(NULL, wc);
      else {
         resp = resp_errorResponse(NOT_FOUND_APP, HTTP_NOT_FOUND);
         if (resp->statusMsg)	WOFREE(resp->statusMsg);
         resp->statusMsg = WOSTRDUP("File Not Found");
      }
   } else
       resp = resp_errorResponse(_errors[app->error], HTTP_SERVER_ERROR);

   if (resp)
   {
      st_add(resp->headers, "Cache-Control", "no-cache, private, no-store, must-revalidate, max-age=0", 0);
      st_add(resp->headers, "Expires", "Thu, 01 Jan 1970 00:00:00 GMT", 0);
      st_add(resp->headers, "date", "Thu, 01 Jan 1970 00:00:00 GMT", 0);
      st_add(resp->headers, "pragma", "no-cache", 0);
   }
   return resp;
}


