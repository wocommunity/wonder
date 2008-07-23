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
 *	LoadAverage: a slightly more sophisticated load balancing
 *	method that tries to even out the load by levelling the number of
 *	sessions each app handles at any given time.
 *
 *	This makes sense especially because for most WebObjects apps
 *	store state in the app, requiring each request for a session to always
 *	return to the same app.  In this case, load balancing only really
 *	occurs on the first request - we try to pick the best app at this
 *	time.
 *
 *	We use custom headers to get the session information returned to
 *	the adaptor; the header is, of course, stripped before sending to
 *	the client. The app developer can substitute their own header for
 *	default one (session count) if they want.
 *
 */

#include "config.h"
#include "loadbalancing.h"
#include "appcfg.h"
#include "list.h"
#include "log.h"
#include "request.h"
#include "response.h"
#include "shmem.h"

#include <string.h>
#include <stdlib.h>
#include <time.h>

typedef struct {
   int instanceLoad;
   time_t lastUsedTime;
} LoadAverageInfo;

/* Note: this macro defeats the const typedef of WOInstance. Be sure to only write if the instance is locked. */
#define instLoadAverageInfo(inst) ((LoadAverageInfo *)(&inst->loadBalancingInfo[0]))

/*
 * Compute the effective load for an instance.
 */
#define LOAD_AGE_FACTOR  (60)
inline static int effectiveLoad(LoadAverageInfo *info, time_t currentTime)
{
   int load;

   load = info->instanceLoad - (currentTime - info->lastUsedTime) / LOAD_AGE_FACTOR;
   if (load < 0)
      load = 0;
   return load;
}

static int la_initialize(strtbl *options)
{
   int ret = 0;
   if (WA_INST_LB_INFO_SIZE < sizeof(LoadAverageInfo))
   {
      WOLog(WO_WARN, "la_initialize(): WA_INST_LB_INFO_SIZE too small to use loadaverage");
      ret = 1;
   }
   return ret;
}



static
WOInstanceHandle la_selectInstance(WOAppReq *req, _WOApp *app) {
   WOInstanceHandle instHandle = AC_INVALID_HANDLE;
   int i;
   int minLoad = 0x7fffffff;
   int instLoad;
   time_t currentTime;

   currentTime = time(NULL);
   /* Select the instance with the lowest load */
   for (i=0; i < WA_MAX_APP_INSTANCE_COUNT; i++) {
      if (app->instances[i] != AC_INVALID_HANDLE)
      {
         WOInstance *inst;
         inst = ac_checkoutInstance(app->instances[i]);
         if (inst != NULL)
         {
            LoadAverageInfo *info = instLoadAverageInfo(inst);
            /* Compute a load value for the instance. The "load" includes a positive component */
            /* which was returned by the instance in its last reponse, and a negative component */
            /* based on how long ago the instance has been hit by any request. This is to */
            /* attempt to prevent the situation where an instance whose sessions go idle all */
            /* at once never gets picked by the scheduler. */
            instLoad = effectiveLoad(info, currentTime);
            /* check the instance timers and compare load value */
            if (canScheduleInstance(inst, currentTime) && (instHandle == AC_INVALID_HANDLE || instLoad < minLoad))
            {
               instHandle = app->instances[i];
               minLoad = instLoad;
            }
            ac_checkinInstance(app->instances[i]);
         }
      }
   }
   if (instHandle != AC_INVALID_HANDLE)
   {
      _WOInstance *inst = ac_lockInstance(instHandle);
      if (inst)
      {
         LoadAverageInfo *info = instLoadAverageInfo(inst);
         info->instanceLoad++;
         info->lastUsedTime = currentTime;
         WOLog(WO_INFO, "loadaverage: selected instance at index %d", instHandle);
      } else
         instHandle = AC_INVALID_HANDLE;
   }
   if (instHandle == AC_INVALID_HANDLE)
      WOLog(WO_INFO, "loadaverage: could not select instance");
   return instHandle;
}

/*
 *	find out how many clients the app is dealing with....
 *	strip the header so browser doesn't see it....
 */
static
int la_finalize(WOAppReq *req, WOInstanceHandle instHandle) {
   strtbl *headers = NULL;
   const char *value;
   const char *refuseTime = NULL;
   _WOInstance *instance;
   int reportedLoad;

   instance = ac_lockInstance(instHandle);
   if (instance)
   {
      LoadAverageInfo *info = instLoadAverageInfo(instance);
      info->lastUsedTime = time(NULL);
      if (req->response != NULL) {
         headers = ((HTTPResponse *)req->response)->headers;
         value = st_valueFor(headers, LOAD_AVERAGE_HEADER);
         refuseTime = st_valueFor(headers, REFUSING_SESSIONS_HEADER);
      } else {
         value = NULL;
         refuseTime = NULL;
      }

      if (refuseTime == NULL)
      {
         if (value != NULL) {
            reportedLoad = atoi(value);
            /* Only allow the load to decrease if there are no other pending requests. */
            /* Otherwise we might be in a race with other responses which would cause a load spike to the instance. */
            if (reportedLoad > info->instanceLoad || instance->pendingResponses == 1)
               info->instanceLoad = reportedLoad;
            WOLog(WO_INFO,"%s %s load avg = %s",req->name, instance->instanceNumber, value);
            st_removeKey(headers, LOAD_AVERAGE_HEADER);
         } else {
            WOLog(WO_WARN,"%s %s:%d doesn't report " LOAD_AVERAGE_HEADER, req->name, req->instance);
            /* bump the load count so loadaverage degrades into roundrobin */
            info->instanceLoad++;
         }
      } else {
         /* if instance is refusing new sessions, set load to zero */
         /* this is so it will start being selected immediately after it restarts */
         info->instanceLoad = 0;
      }
      ac_unlockInstance(instHandle);
   }
   /*
    *	call default to get other cleanup stuff
    */
   lb_finalizeTransaction(req, instHandle);
   return refuseTime != NULL ? 1 : 0;
}

static void la_appDidNotRespond(_WOInstance *inst) {
   LoadAverageInfo *info = instLoadAverageInfo(inst);
   lb_appDidNotRespond(inst);
   /* presumably the instance is about to restart, so reset the load factor */
   info->instanceLoad = 0;
}


static void la_WOAdaptorInfo(String *text, WOInstance *instance)
{
   if (instance)
   {
      time_t currentTime = time(NULL);
      LoadAverageInfo *info = instLoadAverageInfo(instance);
      int load = effectiveLoad(info, currentTime);

      if (load != info->instanceLoad)
         str_appendf(text, "<td>%d(%d)</td>", load, info->instanceLoad);
      else
         str_appendf(text, "<td>%d</td>", info->instanceLoad);

      if (info->lastUsedTime)
         str_appendf(text, "<td>%d</td>", time(NULL) - info->lastUsedTime);
      else
         str_appendLiteral(text, "<td>N/A</td>");
   } else {
      str_appendLiteral(text, "<th>Load</th><th>Load Age (sec)</th>");
   }
}

/*
 *	define our scheduler
 */
const scheduler lb_loadaverage = {
   "loadaverage",
   "send new requests to instance with fewest current users",
   la_initialize,
   la_selectInstance,
   la_appDidNotRespond,
   lb_beginTransaction,
   la_finalize,
   la_WOAdaptorInfo,
};
