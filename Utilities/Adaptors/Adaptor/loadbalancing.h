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
#ifndef LOADBALANCING_H_INCLUDED
#define LOADBALANCING_H_INCLUDED
/*
 *	Load Balancing:  this defines the generic interface to any load
 *	balancing algorithm.  Enough calls are available to give the load
 *	balancer info about the progress of the request & how it turned out.
 *
 *	The goal of load balancing is generally better (shorter) response times
 *	for URL requests; implementations can only guess at ways to achieve
 *	that.  Strategies mostly strive to evenly distribute sequential 
 *	requests to the pool of apps.
 *
 *	Note that for applications which store session state in the app,
 *	load balancing is performed for the 1st request only.
 *
 *	To add a load balancing module, implement the functions defined below
 *	and add the module to 'loadbalancing.c'.  See random.c for an example.
 *
 */

#include "appcfg.h"
#include "WOAppReq.h"
#include "wastring.h"

typedef struct _scheduler * scheduler_t;

/* A convenience macro for checking an instance's timers against the current time */
#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
  #define canScheduleInstance(inst, currentTime) (inst->connectFailedTimer < currentTime && inst->refuseNewSessionsTimer < currentTime && inst->instanceNumber[0] != '-' && (inst->refuseNewSessions == 0))
#else
  #define canScheduleInstance(inst, currentTime) (inst->connectFailedTimer < currentTime && inst->refuseNewSessionsTimer < currentTime && inst->instanceNumber[0] != '-')
#endif

/*
 *	called during init_adaptor().
 *      returns nonzero if initialization fails
 */
int lb_init(struct _strtbl *dict);

/*
 *	default implementations for common functions.  If your scheduler
 *	doesn't implement any these, you can use these in your struct.
 */

/* sets inst->connectionFailedTime to the current time */
void lb_appDidNotRespond(_WOInstance *inst);

/* increments instance->pendingResponses */
void lb_beginTransaction(WOAppReq *req, WOInstanceHandle instHandle);

/* decrements instance->pendingResponses (if > 0) */
int lb_finalizeTransaction(WOAppReq *req, WOInstanceHandle instHandle);
	

typedef	struct	_scheduler	{
	const char * const name;		/* name, as used in conf */
	const char * const description;		/* descriptive name */

        /*
         *      Called once during adaptor initialization to initialize the
         *      load balancer. If the initialization succeeds this function
         *      should return zero. If the function returns nonzero then
         *      the scheduler will be disabled. This may be NULL,
         *      which is taken to mean no initialization is required
         *      and the scheduler is always available.
         */
        int (*initialize)(struct _strtbl *options);
        

	/*
         *     Search app's list of instances for an instance to use.
         *     app is locked for writing while this runs.
         *     The loadbalancer should pick an instance, lock it,
         *     and return the instance's handle. It is the caller's responsibility
         *     to unlock the instance.
	 */
        WOInstanceHandle ((*selectInstance)(WOAppReq *req, _WOApp *app));

	/*
	 *	notify load balancing that instance did not respond
         *      (note that the instance is locked for writing when this is called)
	 */
        void (*instanceDidNotRespond)(_WOInstance *inst);

	/*
	 *	called before the request is sent to the application and after the
	 *	response is received.  Intended to allow load balancing information
	 *	to be gathered about request, and may be used to collect status 
	 *	information from the application (e.g. number of active sessions).
         *      finalizeTransaction should return nonzero if the instance is refusing
         *      new sessions.
	 */
        void (*beginTransaction)(WOAppReq *req, WOInstanceHandle instHandle);
        int (*finalizeTransaction)(WOAppReq *req, WOInstanceHandle instHandle);

        void (*WOAdaptorInfo)(String *text, WOInstance *instance);

} scheduler;


/*
 *	locate a scheduler by name
 *      returns default if not found or name == NULL
 */
scheduler_t lb_schedulerByName(const char *name);

/*
 *	copies available schedulers to the string buffer
 */
void	lb_description(String *str);


#endif
