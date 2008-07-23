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
 *	Framework code for load balancing implementations which implements
 *	some of the generic code and provides hooks for the real implementations.
 *
 *	Here we define the skeleton support for any application request
 *	scheduler.  This includes pointers to all load balancers and management
 *	functions for searching, etc.  This includes the entry point for
 *	application instance searching.
 *
 *	Normally, requests are unbound (not directed to any particular instance)
 *	only for the first request.  Thereafter requests are sent to a numbered
 *	instance.  Two entry points exist for this: lb_selectInstance() and
 *	lb_findInstance().  There, the named application will be found in the
 *	configuration and the scheduler will be asked to select/find the
 *	correct instance.
 *
 *	Other support functions exist for cleaning up after the request is
 *	sent, e.g. to record/report information recieved from the application
 *	about the request.  They're called by tr_handleRequest() in transaction.c
 *
 */
#include "config.h"
#include "loadbalancing.h"
#include "list.h"
#include "log.h"
#include "womalloc.h"
#include "response.h"
#include "shmem.h"


#include <string.h>
#include <stdlib.h>
#include <time.h>

#if	defined(WIN32)
#include <io.h>
#else
#include <netdb.h>
#include <sys/param.h>
#include <unistd.h>
#endif

/*
 *	add new schedulers here
 */
extern scheduler lb_random;
extern scheduler lb_roundrobin;
extern scheduler lb_loadaverage;

/*
 *	the default scheduler is the first enabled one on the list
 *      If there is no persistent state, use only random.
 */
#if defined(CGI) && defined(DISABLE_SHARED_MEMORY)
static scheduler_t loaded_modules[] = {
   &lb_random,
   NULL
};
#else
static scheduler_t loaded_modules[] = {
   &lb_roundrobin,
   &lb_loadaverage,
   &lb_random,
   NULL
};
#endif

/*
 *	locate a scheduler by name....
 */
scheduler_t lb_schedulerByName(const char *name) {
   const scheduler_t *s;
   for (s = loaded_modules; *s && name; s++) {
      if (strcasecmp(name, (*s)->name) == 0)
         return *s;
   }
   return (scheduler_t)loaded_modules[0];
}

/*
 *	copies available schedulers to the string buffer
 */
void lb_description(String *str) {
   int i;

   str_appendLiteral(str, "(");

   for (i=0; loaded_modules[i] != NULL; i++)
   {
      if (i != 0)
         str_appendLiteral(str, ", ");
      str_append(str, loaded_modules[i]->name);
   }
   str_appendLiteral(str, ")");
}

/*
 *	called during startup
 *      calls each module's initializer, and those which fail to initialize are
 *      removed from the list (note the list is collapsed if any fail)
 */
int lb_init(strtbl *dict) {
   int lastEnabled, current, isEnabled;

   for (current = lastEnabled = 0; loaded_modules[current] != NULL; current++)
   {
      if (loaded_modules[current]->initialize != NULL)
         isEnabled = loaded_modules[current]->initialize(dict) == 0;
      else
         isEnabled = 1;
      if (isEnabled)
      {
         loaded_modules[lastEnabled] = loaded_modules[current];
         lastEnabled++;
      } else {
         WOLog(WO_INFO, "lb_init(): %s scheduler not available - initialization failed", loaded_modules[current]->name);
      }
   }
   loaded_modules[lastEnabled] = NULL;
   return 0;
}


/*
 *	we failed doing I/O to the app.  Mark this app as 'dead'/dormant
 *	by setting a timer.  This app will not be used as a candidate for load
 *	balancing requests until this timer expires.  It's up to the scheduler
 *	implementations to heed the timer, though. See the instanceTimersOk() macro.
 */
void lb_appDidNotRespond(_WOInstance *inst) {
   if (inst)
   {
      time_t currentTime = time(NULL);
      WOLog(WO_WARN,"Marking %s:%s unresponsive", inst->host, inst->instanceNumber);
      /* set the deadInterval timer */
      inst->connectFailedTimer = currentTime + inst->deadInterval;
   }
   return;
}

/*
 */
void lb_beginTransaction(WOAppReq *req, WOInstanceHandle instHandle) {
   return;
}

int lb_finalizeTransaction(WOAppReq *req, WOInstanceHandle instHandle) {
   _WOInstance *inst;
   HTTPResponse *resp;
   const char *refuseTime = NULL;
   inst = ac_lockInstance(instHandle);
   if (inst)
   {
      if ( (resp = req->response) != NULL)
      {
         /* since we got a response, we know the app is up */
         inst->connectFailedTimer = 0;

         refuseTime = st_valueFor(resp->headers, REFUSING_SESSIONS_HEADER);
         if (refuseTime)
         {
            char *end;
            inst->refuseNewSessionsTimer = strtol(refuseTime, &end, 0);
            if (*end == 0)
               inst->refuseNewSessionsTimer += time(NULL);
            else
               /* if we see an app which does not report a time to refuse, refuse for 5 minutes (arbitrary) */
               inst->refuseNewSessionsTimer = strcasecmp(refuseTime, "yes") == 0 ? time(NULL) + 300 : 0;
         } else
            inst->refuseNewSessionsTimer = 0;
      }
      inst->requests++;
      ac_unlockInstance(instHandle);
   }
   return refuseTime != NULL ? 1 : 0;
}
