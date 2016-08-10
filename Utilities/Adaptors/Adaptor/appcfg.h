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
 * Management of application instances has all been moved to here.
 *
 */
#ifndef	APPCFG_H_INCLUDED
#define	APPCFG_H_INCLUDED

#include <time.h>
   
/*
 * This file describes the structures and functions used for managing application
 * and instance definitions. Each WebObjects application known to the adaptor has
 * a WOApp structure to describe it. This structure contains application level
 * settings such as the url version and the load balancer to use. It also contains
 * a list of instances. Each instance of every application has a WOInstance structure
 * to describe it. The WOInstance structure contains instance specific settings
 * such as the instance number and port number.
 */

struct _list;
struct _String;
struct _strtbl;

typedef int WOAppHandle;
typedef int WOInstanceHandle;
#define AC_INVALID_HANDLE (-1)

/* These are actually ShmemArray * */
extern void *apps;
extern void *instances;

/*
 * Macros to check out/in or lock/unlock app or instance array elements.
 * Checking out an instance or app provides shared, read only access to the structure,
 * and prevents other threads from making changes. Locking an instance or app provides
 * exclusive access to the structure, allowing changes.
 * The following conventions are observed to avoid deadlocks:
 * 1) No thread may check out or lock more than one instance at a time.
 * 2) No thread may check out or lock more than one app at a time.
 * 3) A thread with an instance checked out or locked may not check out or lock an app.
 * (Some app settings are copied to the instance struct for this reason.)
 * 4) An instance or app which is checked out must be checked in before it can be locked.
 * 5) An instance or app which is locked must be unlocked before it can be checked out.
 * Note that a thread witn an app checked out or locked is allowed to check out or lock an instance.
 *
 * The checkout operations return WOApp * and WOInstance *, whereas the lock
 * operations return _WOApp * and _WOInstance *. The former are typedef-ed const so
 * the compiler can help keep track of whether access is read only or read/write.
 *
 * A successful checkout or lock returns a pointer to the structure, and must be followed
 * by a corresponding checkin or unlock. It is expected that there will be a fair amount
 * of contention for these resources on a heavily loaded site, so they should not be held
 * longer than necessary. A lock or checkout can theoretically fail, in which case they
 * return NULL. In this case there should be no corresponding call to checkin or unlock.
 * A typical usage might be something like:
 * inst = ac_checkoutInstance(instHandle);
 * if (inst) {
 *    ... do something with inst ...
 *    ac_checkinInstance(instHandle);
 * }
 */
 
#define ac_checkoutApp(appHandle) 		((WOApp *)sha_checkout(apps, appHandle))
#define ac_checkinApp(appHandle) 		(sha_checkin(apps, appHandle))
#define ac_lockApp(appHandle) 			((_WOApp *)sha_lock(apps, appHandle))
#define ac_unlockApp(appHandle) 		(sha_unlock(apps, appHandle))
#define ac_checkoutInstance(instanceHandle) 	((WOInstance *)sha_checkout(instances, instanceHandle))
#define ac_checkinInstance(instanceHandle) 	(sha_checkin(instances, instanceHandle))
#define ac_lockInstance(instanceHandle) 	((_WOInstance *)sha_lock(instances, instanceHandle))
#define ac_unlockInstance(instanceHandle) 	(sha_unlock(instances, instanceHandle))
#define ac_localInstanceData(instanceHandle, key) ((void *)sha_localDataForKey(instances, instanceHandle, key))
#define ac_setLocalInstanceData(instanceHandle, key, value, cb) (sha_setLocalDataForKey(instances, instanceHandle, key, value, cb))

/*
 * These macros are used to translate an app or instance handle into a pointer to the app/instance struct.
 * They do not actually check out or lock the app/instance, so they should be used when it is known that
 * the app/instance has already been checked out or locked.
 */
#define ac_checkedOutApp(appHandle) 		((WOApp *)elementPointer(((ShmemArray *)apps), appHandle))
#define ac_lockedApp(appHandle) 		((_WOApp *)elementPointer(((ShmemArray *)apps), appHandle))
#define ac_checkedOutInstance(instanceHandle) 	((WOInstance *)elementPointer(((ShmemArray *)instances), instanceHandle))
#define ac_lockedInstance(instanceHandle) 	((_WOInstance *)elementPointer(((ShmemArray *)instances), instanceHandle))

/*
 * The application definition.
 */
typedef struct _WOAppStruct {
   /* name must be the first field of this structure. */
   const char name[WA_MAX_APP_NAME_LENGTH];	/* name of the app; this never changes */

   WOInstanceHandle instances[WA_MAX_APP_INSTANCE_COUNT];	/* list of instances of this app; these are indices into the instance table */
   
   /* defaults for application */
   int connectionPoolSize;			/* size of connection pool */
   char loadbalance[WA_LB_MAX_NAME_LENGTH];	/* which load balancing/scheduling algorithm is used */
   int deadInterval;				/* how long to wait after a failed connect() before trying that instance again */
   int retries;					/* how many times to retry a request before returning the redirect_url */
   int urlVersion;				/* Use 4.0/4.5 URLs format */
   char redirect_url[WA_MAX_URL_LENGTH];	/* in case of error */

   char additionalArgs[WA_MAX_ADDITIONAL_ARGS_LENGTH+1];	/* config info specified with "additionalArgs" attribute in xml config, if any */

   char loadBalancingInfo[WA_APP_LB_INFO_SIZE];	/* reserved for use by load balancing routine */
} _WOApp;

typedef const _WOApp WOApp;

/*
 *	The instance definition.
 */
typedef struct _WOInstanceStruct {
   /* instanceNumber must be the first field of this structure. */
   char instanceNumber[WA_MAX_INSTANCE_NUMBER_LENGTH];	/* the instance number, stored as a string */
   /* Used to identify the instance. */
   WOAppHandle app;				/* Index in the app table of this instance's app structure */
   int port;					/* the port */
   char host[WA_MAX_HOST_NAME_LENGTH];		/* the host */

   /* Settings for the instance */
   int sendSize;			/* tcp send/recv buf sizes */
   int recvSize;			/* tcp send/recv buf sizes */
   int connectTimeout;			/* timeouts (seconds) */
   int sendTimeout;
   int recvTimeout;

#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
   unsigned int refuseNewSessions;
#endif

   /* these are copied from the WOApp during config so they can be accessed without locking the app struct */
   int connectionPoolSize;
   int deadInterval;
   
   /* Statistics kept for the instance */
   unsigned int requests;			/* the requests handled */
   unsigned int pendingResponses;		/* how many responses awaiting */

   /* Either of these timers can be set after a request. */
   /* Schedulers should not select the instance unless the current time is */
   /* greater than the value of both of these timers. */
   time_t connectFailedTimer;			/* set if a connect() fails */
   time_t refuseNewSessionsTimer;		/* set if the instance reports refusingNewSessions */

   /* Generation number. Each time the adaptor detects that an instance has died, this number gets */
   /* incremented. Resources which are tied to a particular instance process (ex pooled connections) */
   /* should store the generation number when the resource is created, and check that it still matches */
   /* when the resource is reused. */
   int generation;

   /* Connection pooling statistics */
   unsigned int peakPoolSize;			/* peak persistent open connections in any single process running the adaptor */
   unsigned int reusedPoolConnectionCount;	/* number of times a pooled connection was reused */

   char loadBalancingInfo[WA_INST_LB_INFO_SIZE];	/* reserved for use by load balancing routine */

   char additionalArgs[WA_MAX_ADDITIONAL_ARGS_LENGTH+1];	/* config info specified with "additionalArgs" attribute in xml config, if any */

} _WOInstance;

typedef const _WOInstance WOInstance;

/*
 *	initializer for this bit.  The strtbl may include key value pairs for
 *	the config file location, daemon(s), ...
 *      If the return value is nonzero, the initialization fails and the adaptor
 *      cannot run.
 */
int ac_init(struct _strtbl *dict);


/*
 *      Called by a config parser to update the adaptor's configuration.
 *      appSettingsDict is a dictionary of applicatoin settings, and
 *      instancesSettings is a list of dictionaries containing instance settings.
 *      The application and instance settings are updated from the dictionaries.
 *      If the application does not exist yet it is created. If instances
 *      are specified which do not exist yet they are created also.
 */
void ac_updateApplication(struct _strtbl *appSettingsDict, struct _list *instancesSettings);


/*
 * Mainly for debugging/info. Returns the string used to locate the configuration info.
 */
void ac_description(struct _String *str);

/*
 *	reads the configuration only if a sufficient amount of time has passed
 *      returns nonzero if the config was re-read, zero if it was not
 *      (does *not* indicate whether the configuration changed)
 */
int ac_readConfiguration();

/* reset all config times; the next config read will not be skipped */
void ac_resetConfigTimers();

/*
 *	Find an application (presumably for load balancing).
 *      name specifies the name of the app to find.
 *      If the app is found:
 *         The app is locked, the handle is returned. The caller can use ac_lockedApp()
 *         to obtain a pointer to the app structure. The caller must call ac_unlockApp().
 *      If the app is not found, AC_INVALID_HANDLE is returned.
 */
WOAppHandle ac_findApplication(const char *name);

/*
 *	Find a specific instance. app must be checked out or locked.
 *      Searches for an instance with the given instance number. 
 *      If an instance is found, it is locked and it's handle is
 *      returned. The caller may use ac_lockedInstance() to obtain a
 *      pointer to the instance, and must call ac_unlockInstance()
 *      when finished with the instance structure.
 *      If no matching instance is found, returns AC_INVALID_HANDLE.
 */
WOInstanceHandle ac_findInstance(WOApp *app, char *instanceNumber);

/*
 *      This function increments the instance generation number
 *      and resets per-instance statistics. It should be called
 *      when it is determined that an instance has shut down.
 *      The instance is cycled if oldGeneration matches the
 *      current instance generation number, or oldGeneration == -1.
 */
void ac_cycleInstance(_WOInstance *instance, int oldGeneration);


struct _WOURLComponents;
/*
 *	returns nonzero if the request is authorized to view the entire list of instances
 */
int ac_authorizeAppListing(struct _WOURLComponents *wc);


/*
 *	Produce html text describing the app list - for use in the WOAdaptorInfo page.
 * 	The text is appended to the given String. If adaptor_url is non_null,
 *      then the apps and instances in the WOAdaptorInfo page will be links.
 */
void ac_listApps(struct _String *content, const char *adaptor_url);

typedef struct _WebObjects_config_handler {
   const char * const *content_types;
   int (*parseConfiguration)(char *config, int len);
} WebObjects_config_handler;

#endif
