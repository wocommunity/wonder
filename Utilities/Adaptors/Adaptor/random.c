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
 *	random load balancing
 *
 */
#include "config.h"
#include "loadbalancing.h"
#include "list.h"
#include "log.h"
#include "strtbl.h"
#include "shmem.h"

#include <string.h>
#include <stdlib.h>
#include <time.h>
#if	defined(WIN32)
#include <windows.h>
/* Windows doesn't have random(). */
#define srandom(x) srand(x)
#define random() rand()
#endif



static int rnd_initialize(strtbl *options)
{
   int ret = 0;
   time_t now;
   time(&now);
   srandom(now);
   return ret;
}

/*
 *	pick an instance at random & return it
 */
static
WOInstanceHandle rnd_selectInstance(WOAppReq *req, _WOApp *app) {
   WOInstanceHandle instanceList[WA_MAX_APP_INSTANCE_COUNT];
   int i, count, candidateInstance, selectedInstance = AC_INVALID_HANDLE;

   /* build a list of available instances */
   for (i=0, count=0; i<WA_MAX_APP_INSTANCE_COUNT; i++)
   {
      if (app->instances[i] != AC_INVALID_HANDLE)
         instanceList[count++] = app->instances[i];
   }
   if (count > 0)
   {
      /* now try to pick an instance */
      time_t currentTime = time(NULL);
      do {
         WOInstance *inst;
         /* pick a candidate */
         candidateInstance = random() % count;
         inst = ac_lockInstance(instanceList[candidateInstance]);
         if (inst)
         {
            /* check that we can use this candidate at this time */
            if (canScheduleInstance(inst, currentTime))
               selectedInstance = instanceList[candidateInstance]; /* ok, use this one */
            else
               ac_unlockInstance(instanceList[candidateInstance]);
         }
         /* if we couldn't use the candidate, remove it from our list of available instances and try again */
         if (selectedInstance == AC_INVALID_HANDLE)
         {
            if (candidateInstance != count-1)
               memmove(&instanceList[candidateInstance], &instanceList[candidateInstance+1], (count-candidateInstance-1)*sizeof(WOInstanceHandle));
            count--;
         }
      } while (selectedInstance == AC_INVALID_HANDLE && count > 0);
   }
   return selectedInstance;
}



/*
 *	define our scheduler
 */
const scheduler lb_random = {
   "random",
   "randomly select application instance",
   rnd_initialize,
   rnd_selectInstance,
   lb_appDidNotRespond,
   lb_beginTransaction,
   lb_finalizeTransaction,
   NULL,
};

