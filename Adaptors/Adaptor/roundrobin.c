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
 *
 *	Round Robin scheduling is the simplest (beyond random) of the load
 *	balancing techniques.  It has better expected response time than
 *	random (see e.g. Kleinrock for details).
 *
 *	Overall, we can expect a better distribution of requests and session
 *	load than the 'random' distributor.
 *
 *	There can be a problem with R/R when multiple http servers (with WebObjects
 *	adaptors) are forwarding requests to instances: each server/adaptor
 *	maintains its own index for round robin.  This use of adaptor local data
 * 	can cause undesired r/r behaviour - including the worst case: each
 *	server/adaptor sending requests to the same instance.
 *
 */

#include "config.h"
#include "loadbalancing.h"
#include "appcfg.h"
#include "list.h"
#include "strtbl.h"
#include "log.h"
#include "shmem.h"

#include <stdlib.h>

typedef struct {
   int index;
} RoundRobinInfo;

/* Note: this macro defeats the const typedef of WOApp. Be sure to only write if the app is locked. */
#define appRoundRobinInfo(app) ((RoundRobinInfo *)&app->loadBalancingInfo)

static int rr_initialize(strtbl *options)
{
   int ret = 0;
   if (WA_APP_LB_INFO_SIZE < sizeof(RoundRobinInfo))
   {
      WOLog(WO_WARN, "rr_initialize(): WA_APP_LB_INFO_SIZE too small to use round robin");
      ret = 1;
   }
   return ret;
}

static WOInstanceHandle rr_selectInstance(WOAppReq *req, _WOApp *app) {
   int i;
   WOInstanceHandle selectedInstance = AC_INVALID_HANDLE;
   time_t currentTime;

   if (app)
   {
      RoundRobinInfo *info = appRoundRobinInfo(app);
      currentTime = time(NULL);
      /*
       *	index to the next in turn.  if the adaptor is not stateful (CGI)
       *	this degrades into just picking an instance at random
       */
      for (i=0; i<WA_MAX_APP_INSTANCE_COUNT && selectedInstance == AC_INVALID_HANDLE; i++)
      {
         info->index = (info->index + 1) % WA_MAX_APP_INSTANCE_COUNT;
         if (app->instances[info->index] != -1)
         {
            WOInstance *inst;
            inst = ac_lockInstance(app->instances[info->index]);
            if (inst)
            {
               if (canScheduleInstance(inst, currentTime))
                  selectedInstance = app->instances[info->index];
               else
                  ac_unlockInstance(app->instances[info->index]);
            }
         }
      }
   }
   return selectedInstance;
}


/*
 *	define our scheduler
 */
const scheduler lb_roundrobin = {
   "roundrobin",
   "cycle new requests through available instances",
   rr_initialize,
   rr_selectInstance,
   lb_appDidNotRespond,	/* no did-not-respond routine */
   lb_beginTransaction,	/* no begin x-action routine */
   lb_finalizeTransaction,	/* no finalize routine */
   NULL,
};
