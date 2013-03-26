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
 * Here we manage all application instances.  Information regarding instances
 * is obtained from 3 possible sources (in order of preference): wotaskd,
 * the public WebObjects.xml, the private WebObjects.xml.  All sources
 * contain application instance information in XML format.
 *
 */
#include "config.h"
#include "appcfg.h"
#include "loadbalancing.h"
#include "strdict.h"
#include "log.h"
#include "womalloc.h"
#include "hostlookup.h"
#include "transport.h"
#include "request.h"
#include "response.h"
#include "shmem.h"
#include "strtbl.h"
#include "list.h"
#include "wastring.h"
#include "httperrors.h"

#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>

#ifndef MAXPATHLEN
#define MAXPATHLEN 255
#endif

#ifndef F_OK
#define F_OK 0
#endif

#if	defined(SOLARIS)
int gethostname(char *name, int namelen);
#endif

#if	defined(WIN32)
#ifndef _MSC_VER // SWK old WO4.5 headerfile
#if !defined(MINGW)
#include <winnt-pdo.h>
#endif
#endif
#include <io.h>
#else
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>		/* inet_addr() */
#include <sys/param.h>
#endif

#define	HTTP_NOTMODIFIED	304
#define	HTTP_IFMODIFIEDSINCE	"If-Modified-Since"
#define	HTTP_LASTMODIFIED	"Last-Modified"
#define	FILECOLONSLASHSLASH	"file://"
#define	HTTPCOLONSLASHSLASH	"http://"
#define	MCASTCOLONSLASHSLASH	"webobjects://"
#define	FCSSSZ			7
#define	HCSSSZ			7
#define	MCSSSZ			13
#define	CONFIG_URI		"/WebObjects/wotaskd.woa/wa/woconfig"
#define	CONFIG_PORT		1085
#define	CONFIG_TRANSPORT	"nbsocket"
#define MCAST_BUFSIZ		4096			/* Size of multicast receive buffer */
#define NOT_MODIFIED_CONFIG	((char *)-1)

#ifdef USE_WO_CONF_PARSER
extern WebObjects_config_handler WebObjects_conf_parse;
#endif
extern WebObjects_config_handler WebObjects_xml_parse;
extern char *WOAdaptorInfo_username;
extern char *WOAdaptorInfo_password;

static WebObjects_config_handler *parsers[] = {
   &WebObjects_xml_parse,		/* the default */
#ifdef USE_WO_CONF_PARSER
   &WebObjects_conf_parse,
#endif
   NULL,				/* list terminator */
};

void *apps;
void *instances;

typedef enum {
   CM_UNKNOWN, CM_FILE, CM_LIST, CM_MCAST
} ConfigMethod;

ConfigMethod configMethod = CM_UNKNOWN;

#define WA_MAX_LAST_MODIFIED_LENGTH 64
typedef struct _ConfigServer {
   char host[WA_MAX_HOST_NAME_LENGTH];
   int port;
   char path[WA_MAX_URL_LENGTH];	/* path to WebObjects.xml */
   char lastModifiedTime[WA_MAX_LAST_MODIFIED_LENGTH];	/* for conditional GET HTTP header */
} ConfigServer;

static _WOApp *appsBase = NULL;
unsigned int appListSize = WA_MAX_APP_COUNT;

static _WOInstance *instancesBase = NULL;
unsigned int instanceListSize = WA_MAX_APP_COUNT * WA_MAX_APP_INSTANCE_COUNT;

static int config_interval = DEFAULT_CONF_INTERVAL;

static int updateNumericSetting(const char *settingName, int *dest, const char *value);

/*
 * Config variables.
 */
ConfigServer *configServers;
//static strdict *config_servers = NULL;		/* list of servers from which configuration is read */
typedef struct {
   time_t config_read_time;
   time_t config_servers_read_time;
   time_t public_mtime;
   time_t private_mtime;
   time_t configChangeTime;			/* can't add a new app or instance if current time - configChangeTime < 10 */
} ConfigTimes;

static ConfigTimes *configTimes;
static int *configuredInstances = NULL;

static char *_public_config = NULL;		/*  .../WebObjects.xml */
static char *_private_config = NULL; 		/* /tmp/WebObjects.xml */

static const char *public_config();		/* returns path ... */
static const char *private_config();		/* .. to config files */
static void setConfigServers(const char *);
static void setConfigServersViaMulticast(const char *);

/*
 *      ac_prepareToModifyConfig() should be called before the app/instance
 *      configuration will be changed. If the return value is nonzero, the
 *      configuration cannot be updated at this time. If the return value is
 *      zero then the configuration may be updated by calling ac_updateApplication().
 *      There is a timeout associated with the configuration update, so the
 *      update should proceed as quickly as possible.
 *      When all updates have occurred, ac_finishedModifyingConfig() should be called.
 *      ac_finishedModifyingConfig() will *DELETE* any instances which do not
 *      have pending requests and were not configured since the previous call to
 *      ac_prepareToModifyConfig().
 */
static int ac_prepareToModifyConfig();
static void ac_finishedModifyingConfig();


/*
 *	reading configurations from various sources.  malloc'd buffer is returned
 *	and must be free'd
 */
#define	CONTENT_TYPE_LENGTH_MAX	256
static char *file_config(const char *path, time_t *mtime, int *len);
static WebObjects_config_handler *parserForType(const char *filetype);

static void readServerConfig();
static net_fd _contactServer(ConfigServer *);
static char *_retrieveServerInfo(ConfigServer *, net_fd, int *, char *);


/*
 *	set up some stuff...
 *	extract values from dict for:
 *		"config", "daemon", ....
 *
 */
int ac_init(strtbl *dict)
{
   unsigned int count;	
   unsigned int ret = 0;
   const char *config_uri, *config_interval_setting;

   appsBase = WOShmem_alloc("application list", sizeof(WOApp), &appListSize);
   if (appsBase == NULL)
      ret = 1;

   instancesBase = WOShmem_alloc("instance list", sizeof(WOInstance), &instanceListSize);
   if (instancesBase == NULL)
      ret = 1;

   count = 1;
   configTimes = WOShmem_alloc("configTimes", sizeof(ConfigTimes), &count);
   if (configTimes == NULL)
      ret = 1;

   count = 1;
   configServers = WOShmem_alloc("configServers", sizeof(ConfigServer) * WA_MAX_CONFIG_SERVERS, &count);
   if (configServers == NULL)
      ret = 1;
   
   if (ret == 0)
   {
      apps = sha_alloc("WOApp array", appsBase, sizeof(WOApp), appListSize);
      instances = sha_alloc("WOInstance array", instancesBase, sizeof(WOInstance), instanceListSize);
      if (!apps || !instances)
         ret = 1;
   }

   if (dict != NULL) {
      config_uri = st_valueFor(dict, WOCONFIG);
      if (config_uri != NULL) {
         WOLog(WO_INFO, "ac_init(): reading configuration from: %s", config_uri);
         if (strncmp(config_uri,FILECOLONSLASHSLASH, FCSSSZ) == 0) {
            _public_config = WOSTRDUP(config_uri+FCSSSZ);
            configMethod = CM_FILE;
         } else if (strncmp(config_uri,HTTPCOLONSLASHSLASH, HCSSSZ) == 0) {
            configMethod = CM_LIST;
            setConfigServers(config_uri);
         } else if (strncmp(config_uri,MCASTCOLONSLASHSLASH, MCSSSZ) == 0) {
            configMethod = CM_MCAST;
         }
      }
      config_interval_setting = st_valueFor(dict, WOCNFINTVL);
      if (config_interval_setting)
      {
         updateNumericSetting(WOCNFINTVL, (int*)&config_interval, config_interval_setting);
         if (config_interval > MAX_CONF_INTERVAL)
         {
            WOLog(WO_WARN, "ac_init(): config interval reduced to %d (was %d).", MAX_CONF_INTERVAL, config_interval);
            config_interval = MAX_CONF_INTERVAL;
         }
         if (config_interval < MIN_CONF_INTERVAL)
         {
            WOLog(WO_WARN, "ac_init(): config interval increased to %d (was %d).", MIN_CONF_INTERVAL, config_interval);
            config_interval = MIN_CONF_INTERVAL;
         }
      }
   }
   return ret;
}




/* reset all config times; the next config read will not be skipped */
void ac_resetConfigTimers()
{
   void *lockHandle;
   lockHandle = WOShmem_lock(configTimes, sizeof(ConfigTimes), 1);
   configTimes->public_mtime = (time_t)0;
   configTimes->private_mtime = (time_t)0;
   configTimes->config_read_time = 0;
   configTimes->config_servers_read_time = 0;
   WOShmem_unlock(lockHandle);
}


/*
 * Searches for an app with the given name in the app list.
 * If the app is found, locks the app and returns the handle of the found app.
 * If the app is not found or the lock fails, returns AC_INVALID_HANDLE.
 */
WOAppHandle ac_findApplication(const char *name)
{
   int i;
   _WOApp *app = NULL;
   for (i=0; i<appListSize; i++)
   {
#ifndef DISABLE_SHARED_MEMORY
      /* do the comparison without locking for speed */
      if (strcmp(appsBase[i].name, name) == 0)
      {
#endif
         /* do the lock, and double check the name */
         app = ac_lockApp(i);
         if (app)
         {
            if (strcmp(app->name, name) == 0)
               break;
            /* very unusual - somebody else must have just changed the name */
            ac_unlockApp(i);
            app = NULL;
         }
#ifndef DISABLE_SHARED_MEMORY
      }
#endif
   }
   return i < appListSize && app != NULL ? i : AC_INVALID_HANDLE;
}

/*
 *	find a specific instance
 */
WOInstanceHandle ac_findInstance(WOApp *app, char *number)
{
   WOInstanceHandle index = AC_INVALID_HANDLE;
   if (app != NULL) {
      int i;

      for (i = 0; i < WA_MAX_APP_INSTANCE_COUNT && index == AC_INVALID_HANDLE; i++)
      {
         if (app->instances[i] != AC_INVALID_HANDLE)
         {
            WOInstance *_inst;

            _inst = ac_lockInstance(app->instances[i]);
            if (_inst)
            {
               if (strcmp(_inst->instanceNumber, number) == 0)
                  index = app->instances[i];
               else
                  ac_unlockInstance(app->instances[i]);
            }
         }
      }
	  if((index == AC_INVALID_HANDLE) && (i == WA_MAX_APP_INSTANCE_COUNT)){
         WOLog(WO_ERR, "ac_findInstance(): Error: An instance with number '%s' could not be found after searching %d instances.  In a properly operating deployment environment, this is an unlikely senario and should be investigated!", number, WA_MAX_APP_INSTANCE_COUNT);
	  }
   }
   return index;
}



/*
 *  Check if the request is authorized to view the full app listing.
 */
int ac_authorizeAppListing(WOURLComponents *wc) {
    int result = 0;

    if ((wc->applicationName.start != NULL) && (strcmp(wc->applicationName.start, "WOAdaptorInfo") == 0)) {
        if (WOAdaptorInfo_username != NULL && strcmp(WOAdaptorInfo_username, "public") == 0) {
           /* if user is "public", return success */
           result = 1;
        } else if (WOAdaptorInfo_username || WOAdaptorInfo_password) {
	   /* fail if both user and password are null */
           /* looking for a string of form foo+bar */
           if ((wc->queryString.start != NULL) && (wc->queryString.length != 0)) {
              int userLen = 0, passwdLen = 0;

              if (WOAdaptorInfo_username)
                 userLen = strlen(WOAdaptorInfo_username);
              if (WOAdaptorInfo_password)
                 passwdLen = strlen(WOAdaptorInfo_password);
              if (wc->queryString.length == userLen+passwdLen+1) {
                 if ((strncmp(wc->queryString.start, WOAdaptorInfo_username, userLen) == 0) &&
                     (strncmp(&(wc->queryString.start[userLen+1]), WOAdaptorInfo_password, passwdLen) == 0) &&
                     (wc->queryString.start[userLen] == '+'))
                    result = 1;
              }
           }
        }
        if (result == 0)
           WOLog(WO_WARN, "authorization failure for instance listing page");
    }
    return result;
}

/*
 *	static path cruft ...
 *      config lease must be held when this is called
 */
static const char *public_config()
{
   if (_public_config == NULL) {
      char path[MAXPATHLEN];
      sprintf(path,"%s%s/%s",root(), EXECROOT, CONFIG_FILE_PATH);
      if ( access( path, F_OK ) == 0)		/* file exist ? */
         _public_config = WOSTRDUP(path);
      else if (access( "./" CONFIG_FILE, F_OK) == 0)
         _public_config = "./" CONFIG_FILE;		/* try current directory */
   }
   return _public_config;
}

/*
 *      config lease must be held when this is called
 */
static const char *private_config()
{
   if (_private_config == NULL) {
      char path[MAXPATHLEN];
      sprintf(path,"%s%s",tmp(), "/" CONFIG_FILE);
      _private_config = WOSTRDUP(path);
   }
   return _private_config;
}

/*
 *	return the proper parser for the config file type
 */
static WebObjects_config_handler *parserForType(const char *filetype)
{
   WebObjects_config_handler **handler;

   for (handler=parsers; (filetype != NULL) && (*handler != NULL); handler++) {
      const char **ct;
      for (ct = (const char **)(*handler)->content_types; *ct; ct++)
         if (strcmp(*ct, filetype) == 0)
            return *handler;
   }
   return NULL;
}

/*
 * Prepare to modify the contents of the app/instance arrays.
 * A lease time is stored in shared memory. Other processes will
 * not attempt to modify the shared config until this time expires.
 * The lease time is cleared in ac_finishedModifyingConfig(). It
 * is implemented as a timeout to protect against an adaptor crash.
 *
 * configTimes must be locked when this is called
 */
/* This is how long the config lease lasts (seconds). */
#define CONFIG_LEASE_TIME 30
static int ac_prepareToModifyConfig()
{
   time_t currentTime;

   currentTime = time(NULL);
   if (currentTime - configTimes->configChangeTime < CONFIG_LEASE_TIME)
   {
      WOLog(WO_INFO, "ac_prepareToModifyConfig(): modify config - someone else holds the lease");
      //WOShmem_unlock(lockHandle);
      return 1;
   }
   /* this will block others out from changing the config for CONFIG_LEASE_TIME seconds */
   configTimes->configChangeTime = currentTime;
   configuredInstances = (int *)WOCALLOC(instanceListSize, sizeof(int));
	
   return 0;
}

/*
 * Called when finished modifying the app/instance arrays. This
 * clears the lease time to 0.
 * configTimes must be locked when this is called
 */
static void ac_finishedModifyingConfig()
{
   int i, j, instanceCount, foundInstance;
   int appHandle;
   _WOApp *app;
   _WOInstance *inst;
   
   /* check if there have been instances removed from the configuration */
   if (configuredInstances)
   {
      for (i=0; i<instanceListSize; i++)
      {
         inst = ac_lockedInstance(i); /* Note: it isn't really locked, we just need to look at it. We hold the config lease, so this is ok. */
         /* we are taking a shortcut by not locking the instance here, but we double check later with the instance locked */
         if (!configuredInstances[i] && inst->instanceNumber[0] != 0 && inst->pendingResponses == 0)
         {
            /*
             * must hold write lock on app and instance to do the remove
             * instance->pendingResponses must be zero to do the remove
             */
            appHandle = inst->app;
            app = ac_lockApp(appHandle);
            if (app)
            {
               inst = ac_lockInstance(i);
               if (inst)
               {
                  if (inst->pendingResponses == 0)
                  {
                     int generation;
                     WOLog(WO_INFO, "ac_finishedModifyingConfig(): removing %s:%s (%d)", app->name, inst->instanceNumber, inst->port);
                     sha_clearLocalData(instances, i);
                     generation = inst->generation;
                     memset(inst, 0, sizeof(_WOInstance));
                     inst->generation = generation + 1;
                     instanceCount = 0;
                     foundInstance = 0;
                     for (j=0; j<WA_MAX_APP_INSTANCE_COUNT && (!foundInstance || !instanceCount); j++)
                     {
                        if (app->instances[j] == i)
                        {
                           app->instances[j] = AC_INVALID_HANDLE;
                           foundInstance = 1;
                        }
                        if (app->instances[j] != AC_INVALID_HANDLE)
                           instanceCount++;
                     }
                     if (instanceCount == 0)
                     {
                        WOLog(WO_INFO, "ac_finishedModifyingConfig(): %s has no instances. Removing from config.", app->name);
                        sha_clearLocalData(apps, appHandle);
                        memset(app, 0, sizeof(_WOApp));
                     }
                  }
                  ac_unlockInstance(i);
               }
               ac_unlockApp(appHandle);
            }
         }
      }
      WOFREE(configuredInstances);
      configuredInstances = NULL;
   }
   configTimes->configChangeTime = 0; /* release our lease and let others add now */
}

/*
 * Copies newApp to the shared app array.
 * Returns the handle of the new app.
 * Returns AC_INVALID_HANDLE if the app could not be added.
 * If the return is not AC_INVALID_HANDLE, the returned app handle is already locked for writing.
 * (Presumably we will be adding instances.)
 */
static WOAppHandle ac_newApplication(WOApp *newApp)
{
   int newIndex = AC_INVALID_HANDLE, index;
   _WOApp *app = NULL;

   /* search for an available slot in the app table; we hold the config lease so don't need to lock */
   for (index=0; index < appListSize; index++)
      if (appsBase[index].name[0] == 0)
         break;

   if (index < appListSize)
   {
      /* probably don't really need to lock here since we hold the config lease */
      app = ac_lockApp(index);
      if (app != NULL)
      {
         /* Found an available slot for the new app, and now it is locked. Initialize it. */
         memcpy(app, newApp, sizeof(WOApp));
         newIndex = index;
      }
   } else {
      WOLog(WO_ERR, "ac_newApplication(): no room to create application: %s", newApp->name);
   }
   return newIndex;
}

/*
 * Copies newInstance to the shared instance array.
 * Returns the handle of the new instance, or AC_INVALID_HANDLE if it could not be added.
 * Note that in contrast to ac_newApplication(), the new instance is not locked upon return.
 */
static WOInstanceHandle ac_newInstance(WOInstance *newInstance)
{
   int newIndex = AC_INVALID_HANDLE, index;
   _WOInstance *instance = NULL;

   /* search for an available slot in the app table; we hold the config lease so don't need to lock */
   for (index=0; index < instanceListSize; index++)
      if (instancesBase[index].instanceNumber[0] == 0)
         break;

   if (index < instanceListSize)
   {
      /* probably don't really need to lock here since we hold the config lease */
      instance = ac_lockInstance(index);
      if (instance != NULL)
      {
         int generation;
         /* This is only called from ac_updateApplication(), so we know the app is locked */
         _WOApp *app = ac_lockedApp(newInstance->app);
         /* Found an available slot for the new app, and now it is locked. Initialize it. */
         generation = instance->generation;
         memcpy(instance, newInstance, sizeof(WOInstance));
         instance->generation = generation + 1; /* don't reset the generation number */
         WOLog(WO_INFO, "ac_newInstance(): added %s:%s (%d)", app->name, instance->instanceNumber, instance->port);
         ac_unlockInstance(index);
         newIndex = index;
      }
   } else {
      WOLog(WO_ERR, "ac_newInstance(): no room to create instance: %s (%s)", appsBase[instance->app].name, instance->instanceNumber);
   }
   return newIndex;
}



/*
 * Update a string setting in either a WOApp or WOInstance. settingName is a human readable
 * name of the setting. dest is the location at which the new value should be stored. newValue
 * is the new value for the setting. maxValueLen is the maximum permitted length of the
 * setting's value including the terminating null.
 * This function validates the length of the new value, and copies it to dest if the new value
 * is different than the old value. Returns nonzero if the setting was changed.
 */
static int updateStringSetting(const char *settingName, char *dest, const char *newValue, int maxValueLen)
{
   int len, changed = 0;
   len = strlen(newValue);
   if (len >= maxValueLen)
   {
      if (len > 0)
         WOLog(WO_WARN, "%s value too long: %s (%d chars max)", settingName, newValue, maxValueLen);
   } else {
      if (strcmp(dest, newValue) != 0)
      {
         strcpy(dest, newValue);
         changed = 1;
      }
   }
   return changed;
}

/*
 * Update a numeric setting in either a WOApp or WOInstance. settingName is a human readable
 * name of the setting. dest is the location at which the new value should be stored. newValue
 * is the new value for the setting as a string.
 * This function converts the newValue string to a number, checks that the number is non-negative,
 * and stores it at *dest if it has changed. Returns nonzero if the value was changed.
 */
static int updateNumericSetting(const char *settingName, int *dest, const char *value)
{
   int intval, changed = 0;
   char *end;
   intval = strtol(value, &end, 0);
   if (*value != 0 && *end == 0 && intval >= 0)
   {
      if (*dest != intval)
      {
         *dest = intval;
         changed = 1;
      }
   } else
      WOLog(WO_ERR, "Bad numeric value for %s: %s", settingName, value);
   return changed;
}

/*
 * Callback to update a particular setting in a WOApp.
 */
static void updateAppKey(const char *key, const char *value, _WOApp *app)
{
   int changed = 0;
   if (strcmp(key, WOSCHEDULER) == 0)
      changed = updateStringSetting(key, app->loadbalance, value, WA_LB_MAX_NAME_LENGTH);
   else if (strcmp(key, WOERRREDIR) == 0)
      changed = updateStringSetting(key, app->redirect_url, value, WA_MAX_URL_LENGTH);
   else if (strcmp(key, WOADDITIONALARGS) == 0)
      changed = updateStringSetting(key, app->additionalArgs, value, WA_MAX_ADDITIONAL_ARGS_LENGTH);
   else if (strcmp(key, WOAPPNAME) == 0)
      changed = updateStringSetting(key, (char *)app->name, value, WA_MAX_APP_NAME_LENGTH);
   else if (strcmp(key, WORETRIES) == 0)
      changed = updateNumericSetting(key, &app->retries, value);
   else if (strcmp(key, WODEADAPP) == 0)
      changed = updateNumericSetting(key, &app->deadInterval, value);
   else if (strcmp(key, WOPOOLSZ) == 0)
      changed = updateNumericSetting(key, &app->connectionPoolSize, value);
   else if (strcmp(key, WOURLVERSION) == 0)
      changed = updateNumericSetting(key, &app->urlVersion, value);
   else if (strcmp(key, "protocol") == 0)
   {
      /* not currently used */
   } else {
      /* The setting was not recognized. Log and ignore it. */
      WOLog(WO_INFO, "Unknown attribute in application config: \"%s\", value = \"%s\"", key, value);
   }
}

/*
 * Callback to update a particular setting in a WOApp.
 */
static void updateInstanceKey(const char *key, const char *value, _WOInstance *instance)
{
   int changed = 0;

#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
   instance->refuseNewSessions = 0;
#endif

   if (strcmp(key, WOINSTANCENUMBER) == 0)
      changed = updateStringSetting(key, instance->instanceNumber, value, WA_MAX_INSTANCE_NUMBER_LENGTH);
   else if (strcmp(key, WOHOST) == 0)
      changed = updateStringSetting(key, instance->host, value, WA_MAX_HOST_NAME_LENGTH);
   else if (strcmp(key, WOADDITIONALARGS) == 0)
      changed = updateStringSetting(key, instance->additionalArgs, value, WA_MAX_ADDITIONAL_ARGS_LENGTH);
   else if (strcmp(key, WOPORT) == 0)
      changed = updateNumericSetting(key, &instance->port, value);
   else if (strcmp(key, WOSENDBUFSIZE) == 0)
      changed = updateNumericSetting(key, (int*)&instance->sendSize, value);
   else if (strcmp(key, WORECVBUFSIZE) == 0)
      changed = updateNumericSetting(key, (int*)&instance->recvSize, value);
   else if (strcmp(key, WOSENDTIMEOUT) == 0)
      changed = updateNumericSetting(key, (int*)&instance->sendTimeout, value);
   else if (strcmp(key, WORECVTIMEOUT) == 0)
      changed = updateNumericSetting(key, (int*)&instance->recvTimeout, value);
   else if (strcmp(key, WOCNCTTIMEOUT) == 0)
      changed = updateNumericSetting(key, (int*)&instance->connectTimeout, value);
#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
   else if (strcmp(key, WOREFUSENEWSESSIONS) == 0)
   {
      if(strcmp(value, "YES") == 0)
         instance->refuseNewSessions = 1;
      else
         instance->refuseNewSessions = 0;
   }
#endif
   else
   {
      /* The setting was not recognized. Log and ignore it. */
      WOLog(WO_INFO, "Unknown attribute in instance config: \"%s\", value = \"%s\"", key, value);
   }
}

/*
 * Add or update settings on an app instance.
 * app must be locked for writing, appIndex is the app's handle, and instanceSettings holds
 * the values to configure for the instance.
 * Returns the index in the app's instance array of the configured (possibly new) instance.
 * Returns AC_INVALID_HANDLE if the instance could not be added.
 */
static int ac_updateInstance(_WOApp *app, int appIndex, strtbl *instanceSettings)
{
   const char *number;
   _WOInstance *instance = NULL, newInstance;
   int appInstanceIndex = AC_INVALID_HANDLE, availableIndex = AC_INVALID_HANDLE, newIndex;

   number = st_valueFor(instanceSettings, WOINSTANCENUMBER);
   if (number)
   {
      /* search for this instance in the app's instance array */
      for (appInstanceIndex=0; appInstanceIndex<WA_MAX_APP_INSTANCE_COUNT; appInstanceIndex++)
      {
         /* remember the first available position in the app's instance array */
         if (app->instances[appInstanceIndex] == AC_INVALID_HANDLE)
         {
            if (availableIndex == AC_INVALID_HANDLE)
               availableIndex = appInstanceIndex;
         } else
            if (strcmp(instancesBase[app->instances[appInstanceIndex]].instanceNumber, number) == 0)
               break;
      }
      if (appInstanceIndex < WA_MAX_APP_INSTANCE_COUNT)
         /* found the instance; lock it */
         instance = ac_lockInstance(app->instances[appInstanceIndex]);
      else {
         /* didn't find the instance; use a new instance struct */
         memset(&newInstance, 0, sizeof(newInstance));
         instance = &newInstance;
         instance->app = appIndex;
         instance->sendSize = SEND_BUF_SIZE;
         instance->recvSize = RECV_BUF_SIZE;
         instance->connectTimeout = CONN_TIMEOUT;
         instance->sendTimeout = SEND_TIMEOUT;
         instance->recvTimeout = RECV_TIMEOUT;
#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
         instance->refuseNewSessions = 0;
#endif
      }
      if (instance != NULL)
      {
         /* copy app settings to instance struct */
         instance->connectionPoolSize = app->connectionPoolSize;
         instance->deadInterval = app->deadInterval;

         /* process instance settings dict */
         st_perform(instanceSettings, (st_perform_callback)updateInstanceKey, instance);
         if (instance != &newInstance)
         {
            ac_unlockInstance(app->instances[appInstanceIndex]);
            if (configuredInstances)
               configuredInstances[app->instances[appInstanceIndex]] = 1;
         } else {
            /* new instance; add it to the config and update the app's instance list */
            newIndex = ac_newInstance(instance);
            if (newIndex != AC_INVALID_HANDLE)
            {
               appInstanceIndex = availableIndex;
               app->instances[appInstanceIndex] = newIndex;
               if (configuredInstances)
                  configuredInstances[app->instances[appInstanceIndex]] = 1;
            }
         }
      }
   } else {
      WOLog(WO_WARN, "ac_updateInstance(): instance settings do not contain id (ignored)");
   }
   /* return the index in the app's instance list of the instance that was updated */
   return appInstanceIndex;
}

/*
 *	called by the parsing function to add an app
 */
void ac_updateApplication(strtbl *appSettingsDict, list *instancesSettings)
{
   _WOApp *app, localApp;
   const char *name;
   char configuredInstances[WA_MAX_APP_INSTANCE_COUNT];
   int i, appInstanceIndex, count;
   WOAppHandle appHandle;

   name = st_valueFor(appSettingsDict, WOAPPNAME);
   if (name)
   {
      appHandle = ac_findApplication(name);
      if (appHandle == AC_INVALID_HANDLE)
      {
         /* Must be a new app. Initialize settings on the stack. */
         app = &localApp;
         memset(app, 0, sizeof(WOApp));
         for (i=0; i<WA_MAX_APP_INSTANCE_COUNT; i++)
            app->instances[i] = AC_INVALID_HANDLE;
         app->connectionPoolSize = CONNECTION_POOL_SZ;
         app->deadInterval = DEADAPPINTERVAL;
         app->retries = RETRIES;
         app->urlVersion = CURRENT_WOF_VERSION_MAJOR;
      } else
         app = ac_lockedApp(appHandle);
      if (app != NULL)
      {
         st_perform(appSettingsDict, (st_perform_callback)updateAppKey, app);
         /* if this is a new application, need to add it to the public list */
         if (app == &localApp)
         {
            appHandle = ac_newApplication(app); /* note: returns with the app handle locked */
            if (appHandle != AC_INVALID_HANDLE)
               app = &appsBase[appHandle];
            else
               app = NULL;
         }
      }
      if (app != NULL)
      {
         memset(configuredInstances, 0, sizeof(configuredInstances));
         for (i=0; i<wolist_count(instancesSettings); i++)
         {
            strtbl *instanceDict = wolist_elementAt(instancesSettings, i);
            appInstanceIndex = ac_updateInstance(app, appHandle, instanceDict);
            if (appInstanceIndex != AC_INVALID_HANDLE)
               configuredInstances[appInstanceIndex] = 1;
         }
         
         count = 0;
         for (i=0; i<WA_MAX_APP_INSTANCE_COUNT; i++)
         {
            if (app->instances[i] != AC_INVALID_HANDLE)
               count++;
         }
         if (count == 0)
         {
            WOLog(WO_INFO, "ac_updateApplication(): no configured instances; removing %s", app->name);
            *(char *)&app->name[0] = 0; /* no instances, so remove the app */
         }
         ac_unlockApp(appHandle);
      }
   } else {
      WOLog(WO_WARN, "ac_updateApplication(): application settings with no name (ignored)");
   }
}

void ac_cycleInstance(_WOInstance *instance, int oldGeneration)
{
   if ((oldGeneration == -1) || (oldGeneration == instance->generation))
   {
      instance->generation++;
      instance->requests = 0;
      instance->peakPoolSize = 0;
      instance->reusedPoolConnectionCount = 0;
   }
}


void ac_description(String *str)
{

   switch (configMethod)
   {
      case CM_FILE:
      {
         const char *file_info = public_config();
         if (!file_info) file_info = private_config();
         if (file_info != NULL)
            str_appendf(str, "%s<br>",file_info);
         break;
      }
      case CM_LIST:
      case CM_MCAST:
      {
         int i;
         void *lockHandle = WOShmem_lock(configServers, sizeof(ConfigServer) * WA_MAX_CONFIG_SERVERS, 1);
         if (lockHandle)
         {
            for (i=0; i<WA_MAX_CONFIG_SERVERS; i++)
               if (configServers[i].host[0] != 0)
                  break;
            if (i < WA_MAX_CONFIG_SERVERS)
            {
               str_appendLiteral(str, "<br><table align=center>");
               str_appendLiteral(str, "<th>URL</th><th>last modified</th>");
               for (; i < WA_MAX_CONFIG_SERVERS; i++)
               {
                  if (configServers[i].host[0] != 0)
                  {
                     char *lmt;
                     /* append "server:port  lastModifiedTime " */
                     lmt = configServers[i].lastModifiedTime[0] ? configServers[i].lastModifiedTime : "unknown";
                     str_appendf(str, "<tr><td>%s:%d%s</td><td>%s</td></tr>",configServers[i].host,configServers[i].port,configServers[i].path,lmt);
                  }
               }
               str_appendLiteral(str, "</table>");
            } else {
               str_appendLiteral(str, "no config servers<br>");
            }
            WOShmem_unlock(lockHandle);
         } else {
            WOLog(WO_ERR, "ac_description(): WOShmem_lock() failed.");
            str_appendLiteral(str, "config server list not available due to server error");
         }
         break;
      }
      case CM_UNKNOWN:
      {
         str_appendLiteral(str, "bad or missing config url in config");
         break;
      }
   }
}


/*
 *	re-read in the config if necessary
 *      returns nonzero if the config was re-read, zero if it was not
 *      (does *not* indicate whether there were any changes in the config)
 */

int ac_readConfiguration()
{
   char *buffer;
   int len = 0;
   time_t now;
   void *lockHandle;

   now = time(NULL);
   lockHandle = WOShmem_lock(configTimes, sizeof(ConfigTimes), 1);
   if (!lockHandle)
   {
      WOLog(WO_ERR, "ac_readConfiguration: WOShmem_lock() failed. Skipping reading config.");
      return 0;
   }
   if ((now - configTimes->config_read_time) < config_interval) {
      WOShmem_unlock(lockHandle);
      WOLog(WO_DBG, "ac_readConfiguration(): skipped reading config");
      return 0;
   }

   if (ac_prepareToModifyConfig())
   {
      WOShmem_unlock(lockHandle);
      WOLog(WO_INFO, "ac_readConfiguration(): can't update at this time");
      return 0;
   }

   configTimes->config_read_time = now;
   switch (configMethod)
   {
      case CM_MCAST:
         /* check if it is time to search for new servers */
         if ((now - configTimes->config_servers_read_time ) > CONF_SEARCH_INTERVAL)
         {
            WOLog(WO_DBG, "ac_readConfiguration(): searching for config servers");
            configTimes->config_servers_read_time = now;
         }
         setConfigServersViaMulticast(adaptor_valueForKey(WOCONFIG));
         /* fall through to CM_LIST to query the servers */
      case CM_LIST:
         readServerConfig();
         break;
      case CM_FILE:
      {
         const char *path;
         path = public_config();

         WOLog(WO_DBG,"Checking config file %s", (path) ? path : "-");
         buffer = file_config(path, &configTimes->public_mtime, &len);

         /* Only check private conf file if public one doesn't exist */
         if ((buffer == NULL) && (configTimes->public_mtime == (time_t)0)) {
            path = private_config();
            buffer = file_config(path, &configTimes->private_mtime, &len);
         }
         if (buffer != NULL) {
            const char *filetype;
            WebObjects_config_handler *parser;
            /* figure out the file type and parse appropriately */
            filetype = strrchr(path, '.');
            if (filetype != NULL)	filetype++;
            parser = parserForType(filetype);
            if (parser)
            {
               if (parser->parseConfiguration(buffer, len))
                  WOLog(WO_ERR, "Failed parsing configuration.");
            } else {
               WOLog(WO_ERR, "No parser for file type %s", filetype);
            }
            WOFREE(buffer);
         } else {
            /* didn't actually read the config, so don't delete any instances */
            WOFREE(configuredInstances);
            configuredInstances = NULL;
         }
         break;
      }
      case CM_UNKNOWN:
         WOLog(WO_ERR, "ac_readConfiguration(): bad config method, check config url");
         break;
   }

   ac_finishedModifyingConfig();
   WOShmem_unlock(lockHandle);
   return 1;
}


/*
 *	read a configuration from a file
 *      adds a null terminator character after the buffer, which is not reported in len
 */
static char *file_config(const char *path, time_t *mtime, int *len)
{
   struct stat s;
   char *buffer = NULL;
   int file, n, total;
   int bytes;

   /*
    *	see if the file exists and has changed
    */
   if (path) {
      WOLog(WO_DBG,"Checking config %s", path);
      if (stat(path, &s) == 0) {
         WOLog(WO_DBG,"Checking config %s mod time", path);
         if (s.st_mtime > *mtime) {
            buffer = WOMALLOC(((size_t)s.st_size) + 1);
            buffer[s.st_size-1] = 0;	/* add a null terminator */
            WOLog(WO_INFO,"Reading configuration from %s",path);
            if ((file = open(path, O_RDONLY, 0)) < 0) {
               WOLog(WO_ERR,"Error opening config %s: %s",path, strerror(errno));
               return NULL;
            }
            bytes = 0;			/* bytes read in */
            total = (int)s.st_size;
            while (bytes < total) {
               n = read(file, buffer+bytes, total);
               if (n < 0) {
                  WOLog(WO_ERR,"Error reading %s: %s",path, strerror(errno));
                  close(file);
                  WOFREE(buffer);
                  return NULL;
               }
               bytes += n;
               total -= n;
            }
            close(file);
            *mtime = s.st_mtime;		/* update read time */
            *len = bytes;
         } else {
            WOLog(WO_DBG,"%s not modified (s.st_mtime=%d, *mtime=%d)", path, s.st_mtime, *mtime);
         }
      } else {
         WOLog(WO_DBG,"stat call failed on %s (errno=%d)", path, errno);
      }
   } else {
      WOLog(WO_DBG,"Config file: path not supplied");
   }
   return buffer;
}

/*
 * Send all of the requests in one loop and then read the replies in another.
 */
static void readServerConfig() {
	int i;
	net_fd s;
	int len[WA_MAX_CONFIG_SERVERS];
	char *buffer[WA_MAX_CONFIG_SERVERS];
	char content_type[WA_MAX_CONFIG_SERVERS][CONTENT_TYPE_LENGTH_MAX];
	WebObjects_config_handler *parser;
	int oneOrMoreModified = 0;
	int oneOrMoreUnModified = 0;
   
	/* Send the requests for configuration info and hang onto the net_fd's being used. */
	for (i=0; i < WA_MAX_CONFIG_SERVERS; i++) {
		if (configServers[i].host[0]) {
			s = _contactServer(&configServers[i]);
			if(s)  {
				WOLog(WO_INFO, "Preparing to read config for host: %s", configServers[i].host);
				buffer[i] = _retrieveServerInfo(&configServers[i], s, &len[i], content_type[i]);
				if(buffer[i] == NOT_MODIFIED_CONFIG)
					oneOrMoreUnModified = 1;
				else // No response has to be treated as modification, too
					oneOrMoreModified  = 1;
			}
		}
	}
   
	if(oneOrMoreModified && oneOrMoreUnModified) {
		for (i=0; i < WA_MAX_CONFIG_SERVERS; i++) {
			if (configServers[i].host[0] && buffer[i] == NOT_MODIFIED_CONFIG) {
				configServers[i].lastModifiedTime[0] = 0;
				s = _contactServer(&configServers[i]);
				if(s)  {
					WOLog(WO_INFO, "Preparing to read config again for host (unmodified content): %s", configServers[i].host);
					buffer[i] = _retrieveServerInfo(&configServers[i], s, &len[i], content_type[i]);
					oneOrMoreModified  = 1;
				}
			}
		}
	}
   
	if(!oneOrMoreModified) {
		int i;
		for(i=0;i<instanceListSize;i++)
			configuredInstances[i] = 1;
		WOLog(WO_INFO, "All settings are unmodified");
		return;
	}
   
	
	/* Read the configuration information from each server. */
	for (i=0; i < WA_MAX_CONFIG_SERVERS; i++) {
		if (configServers[i].host[0]) {
			int deleteServer = 0;
			if (buffer[i] == NULL)
			{
				deleteServer = 1;
			} else {
				parser = parserForType(content_type[i]);
				if (parser)
				{
					if (parser->parseConfiguration(buffer[i], len[i]))
						WOLog(WO_ERR, "Failed parsing configuration");
				} else {
					WOLog(WO_ERR, "No parser for file type %s", content_type[i]);
				}
				WOFREE(buffer[i]);
				buffer[i] = NULL;
			}
			if (deleteServer)
			{
				if (configMethod == CM_MCAST)
				{
					WOLog(WO_INFO, "Deleting config server %s:%d (couldn't read config).", configServers[i].host, configServers[i].port);
					memset(&configServers[i], 0, sizeof(ConfigServer));
				} else {
					WOLog(WO_INFO, "Config server %s:%d didn't respond.", configServers[i].host, configServers[i].port);
				}
			}
		}
	}
}

/* Send a GET request to a server requesting its configuration info. */
static net_fd _contactServer(ConfigServer *server) {
   HTTPRequest req;
   net_fd s;
   char request_str[MAXPATHLEN+20];

   WOLog(WO_INFO,"Reading configuration from http://%s:%d%s",server->host,server->port,server->path);

   /*
    *	open a connection
    */
   if ((s = transport->openinst(server->host, server->port, CONF_CONN_TIMEOUT, CONF_SEND_TIMEOUT, CONF_RECV_TIMEOUT, SEND_BUF_SIZE, RECV_BUF_SIZE)) == NULL_FD) {
      WOLog(WO_ERR, "Error connecting to server %s",server->host);
      return NULL;
   }
   /*
    *	create & send the request
    */
   memset(&req, 0, sizeof(HTTPRequest));
   req.method = HTTP_GET_METHOD;
   strcpy(request_str, "GET ");
   strcat(request_str, server->path);
   strcat(request_str, " HTTP/1.0\n");
   req.request_str = request_str;
   req.headers = st_new(2);
   if (server->lastModifiedTime[0]) {
      req_addHeader(&req,HTTP_IFMODIFIEDSINCE,server->lastModifiedTime, STR_COPYVALUE|STR_FREEVALUE);
   }
   if (req_sendRequest(&req, s) != 0) {
      transport->close_connection(s);
      s = NULL;
   }

   st_free(req.headers);

   /* Close the connection in the caller of this function. */
   return s;
}

/* Retrieve the response from a request for the server's config (generated by _contactServer) */
static char *_retrieveServerInfo(ConfigServer *server, net_fd s, int *len, char *content_type) {
   HTTPResponse *resp = NULL;
   char *config = NULL;
   WOConnection *c;

   c = tr_wrap_net_fd(s);
   if (c)
      resp = resp_getResponseHeaders(c, AC_INVALID_HANDLE);
   if (resp && (resp_getResponseContent(resp, 0)!=0))
   {
      resp_free(resp);
      resp = NULL;
   }
   tr_close(c, AC_INVALID_HANDLE, 0);
   if (resp != NULL) {
      *len = resp->content_length;
      *content_type = '\0';
      if (resp->status == HTTP_OK) {
         /* may be NOT_MODIFIED (or worse!) */
         const char *mtime;
         const char *c;
         mtime = st_valueFor(resp->headers, HTTP_LASTMODIFIED);
         if (mtime != NULL) {
            strncpy(server->lastModifiedTime, mtime, WA_MAX_LAST_MODIFIED_LENGTH);
         }
         c = st_valueFor(resp->headers, CONTENT_TYPE);
         if ((c != NULL) && (strlen(c) < CONTENT_TYPE_LENGTH_MAX)) {
            strcpy(content_type, c);
            config = resp->content;
            resp->content = NULL;	/* prevent content from being freed along with resp */
            /* WOLog(WO_INFO,"Received config: %s\n%s", content_type, config); */
         } else {
            WOLog(WO_ERR,"Unknown content-type returned from config server: %s",(c != NULL) ? c : "(null)");
         }
      } else if (resp->status == HTTP_NOTMODIFIED) {
         WOLog(WO_INFO,"Not modified response returned from config server: %d",resp->status);
         config = NOT_MODIFIED_CONFIG;
      } else {
         WOLog(WO_ERR,"Error response returned from config server: %d",resp->status);
      }

      /*
       *	we should also get the content-type so we can parse different
       *	types of configuration descriptions....
       */
      resp_free(resp);
   }

   return config;
}

/*
 * Produce the instance list table for inclusion in the WOAdaptorInfo page. As a side effect, set hasRegisteredInstances
 * to a nonzero value if the app has any instances whose instance number does not start with '-'.
 */
void ac_buildInstanceList(String *content, WOApp *app, scheduler_t scheduler, const char *adaptor_url, time_t currentTime, int *hasRegisteredInstances)
{
   int j, newSessionsTimeout, deadTimeout;
   WOInstance *inst;
   int hasAdditionalArgs = 0, additionalArgsLocation;
   const char additionalArgs[] = "<th>args</th>";

   *hasRegisteredInstances = 0;
   /* set up the table header */
   str_appendLiteral(content, "<table cellspacing=10><tr align=center>"
                     "<th>inst</th><th>host</th><th>port</th><th>active<br>reqs</th><th>served</th><th>conn&nbsp;pool<br>&nbsp;peak/reused</th><th>cto&nbsp;/ sto&nbsp;/ rto</th><th>send/rcv buf</th><th>refusing<br>timeout</th><th>dead<br>timeout</th>");
#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
  str_appendLiteral(content, "<th>refuse new<br>sessions</th>");
#endif

   /* We may need an additional column in here, but we won't know until after we walk the instances. */
   /* Insert the header now, and if we don't need it we will overwrite it later with whitespace. */
   additionalArgsLocation = content->length;
   str_appendLength(content, additionalArgs, sizeof(additionalArgs));

   /* let the scheduler append columns */
   if (scheduler && scheduler->WOAdaptorInfo)
      scheduler->WOAdaptorInfo(content, NULL);
   str_appendLiteral(content, "</tr>");
   
   for (j=0; j < WA_MAX_APP_INSTANCE_COUNT; j++) {
      if (app->instances[j] != AC_INVALID_HANDLE)
      {
         inst = ac_checkoutInstance(app->instances[j]);
         if (inst)
         {
            if (inst->instanceNumber[0] != '-')
               *hasRegisteredInstances = 1;
            str_appendLiteral(content, "<tr align=center><td>");
            if (adaptor_url && adaptor_url[0] != 0 && strcmp(inst->instanceNumber, "-1"))
               str_appendf(content, "<a href=\"%s/%s.woa/%s\" TARGET=\"_blank\">%s</a>", adaptor_url, app->name, inst->instanceNumber, inst->instanceNumber);
            else
               str_append(content, inst->instanceNumber);
            str_appendf(content, "</td><td>%s</td>", inst->host);

            if (inst->connectFailedTimer > currentTime)
               deadTimeout = inst->connectFailedTimer - currentTime;
            else
               deadTimeout = 0;
            if (inst->refuseNewSessionsTimer > currentTime)
               newSessionsTimeout = inst->refuseNewSessionsTimer - currentTime;
            else
               newSessionsTimeout = 0;
            if (adaptor_url && adaptor_url[0])
               str_appendf(content, "<td><a href=\"http://%s:%d%s/%s.woa\" TARGET=\"_blank\">%d</a></td>", inst->host, inst->port, adaptor_url, app->name, inst->port);
            else
               str_appendf(content, "<td>%d</td>", inst->port);
            str_appendf(content, "<td>%d</td><td>%d</td><td>%d/%d</td><td>%d/%d/%d</td><td>%d/%d</td><td>%d</td><td>%d</td>",
                        inst->pendingResponses, inst->requests, inst->peakPoolSize, inst->reusedPoolConnectionCount, inst->connectTimeout,inst->sendTimeout,inst->recvTimeout, inst->sendSize, inst->recvSize, newSessionsTimeout, deadTimeout);
#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
            str_appendf(content, "<td>%s</td>", (inst->refuseNewSessions == 1) ? "YES" : "NO");
#endif
            if (WA_MAX_ADDITIONAL_ARGS_LENGTH > 0 && inst->additionalArgs[0] != 0)
            {
               hasAdditionalArgs = 1;
               str_appendf(content, "<td>%s</td>", inst->additionalArgs);
            }
            if (scheduler && scheduler->WOAdaptorInfo)
               scheduler->WOAdaptorInfo(content, inst);
            str_appendLiteral(content, "</tr>");
            ac_checkinInstance(app->instances[j]);
         }
      }
   }
   str_appendLiteral(content, "</table>");
   /* if we didn't find any instances with additional args, "white out" the additional args header */
   if (!hasAdditionalArgs)
      memset(&content->text[additionalArgsLocation], ' ', sizeof(additionalArgs));
}

/* Produce html text describing the app list - for use in the WOAdaptorInfo page. */
/* The text is appended to the given String. */
void ac_listApps(String *content, const char *adaptor_url) {
   WOApp *app;
   int i, count=0, hasRegisteredInstances, appnameUrlStart, appnameUrlEnd;
   time_t currentTime;
   scheduler_t scheduler;

   /* Build the application/instance table for the WOAdaptorInfo page. */
   str_appendLiteral(content, "<br><strong>Available applications:</strong><br><table border=1>");
   currentTime = time(NULL);
   for (i=0; i < WA_MAX_APP_COUNT; i++) {
      app = ac_checkoutApp(i);
      if (app)
      {
         if (app->name[0] != 0)
         {
            count++;
            if (app->loadbalance[0])
               scheduler = lb_schedulerByName(app->loadbalance);
            else
               scheduler = NULL;
            
            /* The left column contains application level settings, the right contains the instance table. */
            str_appendLiteral(content, "<tr valign=top><td>");

            /* The appliction level settings are also a table. */
            /* The first row contains the application name, which encompasses both columns. */
            str_appendLiteral(content, "<table><tr><th colspan=2>");
            appnameUrlStart = content->length;
            if (adaptor_url && adaptor_url[0] != 0)
               str_appendf(content, "<a href=\"%s/%s\" TARGET=\"_blank\">%s</a>", adaptor_url, app->name, app->name);
            else
               str_append(content, app->name);
            appnameUrlEnd = content->length;
            str_appendLiteral(content, "</th></tr>");

            /* Subsequent rows contain the various settings, with the name in column 1 and the value in column 2 */

            /* load balancing */
            str_appendf(content, "<tr><td>L/B:&nbsp;%s</td></tr>", app->loadbalance[0] ? app->loadbalance : "not&nbsp;set");

            /* redirect url */
            str_appendf(content, "<tr><td>redir:&nbsp;%s</td></tr>", app->redirect_url[0]?app->redirect_url:"not&nbsp;set");

            /* dead interval */
            str_appendf(content, "<tr><td>dead&nbsp;time:&nbsp;%d</td></tr>", app->deadInterval);

            /* connection pool size */
            str_appendf(content, "<tr><td>max&nbsp;pool&nbsp;sz:&nbsp;%d</td></tr>", app->connectionPoolSize);

            /* retry count */
            str_appendf(content, "<tr><td>retries:&nbsp;%d</td></tr>", app->retries);

            /* url version - only show if it is not the default */
            if (app->urlVersion != CURRENT_WOF_VERSION_MAJOR)
               str_appendf(content, "<tr><td>URL&nbsp;ver:&nbsp;%d", app->urlVersion);

            /* additional args - only show if there is anything there */
            if (WA_MAX_ADDITIONAL_ARGS_LENGTH > 0 && app->additionalArgs[0] != 0)
               str_appendf(content, "<tr><td>args:&nbsp;%s</td></tr>", app->additionalArgs);
            
            /* end of application settings; close the table */
            str_appendLiteral(content, "</table></td><td>");

            /* Now fill in the cell containing all the instances' info.*/
            /* Also determines whether the app has registered instances. */
            ac_buildInstanceList(content, app, scheduler, adaptor_url, currentTime, &hasRegisteredInstances);

            /* if we don't have registered instances, "white out" the appname link */
            if (!hasRegisteredInstances && adaptor_url && adaptor_url[0] != 0)
            {
               memset(&content->text[appnameUrlStart], ' ', appnameUrlEnd - appnameUrlStart);
               strcpy(&content->text[appnameUrlStart], app->name);
            }
            str_appendLiteral(content, "</td></tr>");
         }
         ac_checkinApp(i);
      }
   }
   if (count == 0)
      str_appendLiteral(content,"<tr><td>-NONE-</td></tr>");
   str_appendLiteral(content, "</table>");
}


/*
 *	rip out the wotaskd servers from a list of uri's like this:
 *		http://host0:port0,http://host1:port1,...
 */
static void setConfigServers(const char *s)
{
   int port, i, badEntry;
   char host[WA_MAX_HOST_NAME_LENGTH];
   char path[MAXPATHLEN+1];
   hostent_t h=NULL;

   while (s && *s) {
      badEntry = 0;
      port = CONFIG_PORT;
      
      if (strncmp(s,HTTPCOLONSLASHSLASH,HCSSSZ) != 0) {
         WOLog(WO_ERR, "Unknown protocol in server URL: %s",s);
         badEntry = 1;
      } else {
         s += HCSSSZ;
         for (i=0; *s && (*s != ':') && (*s != '/') && (*s != ',') && !isspace((int)*s); i++, s++)
            if (i < WA_MAX_HOST_NAME_LENGTH)
               host[i] = *s;
            else
               badEntry = 1;
         if (i < WA_MAX_HOST_NAME_LENGTH)
            host[i] = '\0';
         else
            badEntry = 1;

         if (*s == ':') {
            port = atoi(++s);
            while (isdigit((int)*s)) {
               s++;
            }
         } else {
            WOLog(WO_WARN, "Missing port number in server URL: %s. Using %d.", s, port);
         }

         /* check for optional path */
         path[0] = 0;
         if (*s == '/') {
            for (i=0; *s && (*s != ',') && !isspace((int)*s); i++, s++)
               if (i < MAXPATHLEN)
                  path[i] = *s;
               else
                  badEntry = 1;
            if (i < MAXPATHLEN)
               path[i] = '\0';
            else
               badEntry = 1;
         }
         if (path[0] == 0 || (path[0] == '/' && path[1] == 0))
            strcpy(path, CONFIG_URI);	/* use the default */
      }

      /* Skip to the end of the entry */
      while (*s && *s != ',')
         s++;
      while (*s && (isspace((int)*s) || (*s == ',')))
         s++;

      /*
       *	we now have a server & a port check for conflicts & add if new.
       */
      if (!badEntry)
      {
         h = hl_find(host);
         if (h == NULL) {
            WOLog(WO_WARN, "Can't find server host %s",host);
            badEntry = 1;
         }
      }
      if (!badEntry)
      {
         int freeServer = -1;
         int foundServer = -1;
         for (i=0; i<WA_MAX_CONFIG_SERVERS && foundServer == -1; i++)
         {
            if (configServers[i].host[0])
            {
               if (strncmp(configServers[i].host, h->h_name, WA_MAX_HOST_NAME_LENGTH) == 0 && configServers[i].port == port)
                  foundServer = i;
            } else {
               if (freeServer == -1)
                  freeServer = i;
            }
         }
         if (foundServer != -1)
         {
            WOLog(WO_DBG, "known server %s:%d.", configServers[foundServer].host, configServers[foundServer].port);
         } else {
            if (freeServer != -1)
            {
               strncpy(configServers[freeServer].host, h->h_name, WA_MAX_HOST_NAME_LENGTH);
               configServers[freeServer].port = port;
               strncpy(configServers[freeServer].path, path, WA_MAX_URL_LENGTH);
               WOLog(WO_INFO, "Added new config server %s:%d.", configServers[freeServer].host, configServers[freeServer].port);
            } else {
               WOLog(WO_ERR, "No room to add config server %s:%d. Increase WA_MAX_CONFIG_SERVERS.", host, port);
            }
         }
      }
   }
   return;
}

/*
 *	Go and locate the wotaskd servers from a multicast broadcast like this:
 *		webobjects://239.128.14.2:1085
 */
static void setConfigServersViaMulticast(const char *s)
{
   int port, i, rc;
   int socket;
   struct in_addr mcast_addr;
   char host[WA_MAX_HOST_NAME_LENGTH + 1]; // extra byte so we don't write in unallocated memory

   /* Only do this once - no while loop unlike the http: version */
   if (s && *s) {
      if (strncmp(s,MCASTCOLONSLASHSLASH,MCSSSZ) != 0) {
         WOLog(WO_ERR, "Unknown protocol in server URL: %s",s);
         return;
      }
      s += MCSSSZ;
      for (i=0; *s && (*s != ':') && (i < WA_MAX_HOST_NAME_LENGTH); i++)
         host[i] = *s++;
      host[i] = '\0';
      if (*s != ':') {
         WOLog(WO_ERR, "Missing port number in multicast URL: %s",s);
         return;
      }
      port = atoi(++s);

      /*
       *	we now have a multicast address & a port.
       */
      mcast_addr.s_addr = inet_addr(host);

      if (mcast_addr.s_addr == 0) {
         WOLog(WO_ERR, "Can't convert multicast address %s",host);
         return;
      }

      /* Grab a socket to listen on */
      socket = mcast_listensocket(0);
      /* Send our request */
      rc = mcast_send(socket, &mcast_addr, port, "GET CONFIG-URL");
      if (rc > 0) {
         char hostlist_buffer[MCAST_BUFSIZ];
         rc = mcast_collect_replies(socket, hostlist_buffer, MCAST_BUFSIZ - 1);
         if (rc > 0) {
            WOLog(WO_INFO, "Received hostlist: %s", hostlist_buffer);
            setConfigServers(hostlist_buffer);
         } else {
            WOLog(WO_ERR, "No hostlist received.");
            setConfigServers(NULL);
         }
      }
      mcast_close(socket);
   }
   return;
}

