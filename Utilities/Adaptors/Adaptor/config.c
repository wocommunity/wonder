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
 *	Here's where we manage some runtime configuration options.
 *
 */
#include "config.h"
#include "womalloc.h"
#include "appcfg.h"
#include "log.h"
#include "strtbl.h"
#include "loadbalancing.h"
#include "hostlookup.h"
#include "transport.h"
#include "transaction.h"
#include "shmem.h"

#include <string.h>
#ifndef WIN32
#include <signal.h>
#else
#include <winsock.h>
#endif


/*
 *	some behavioral stuff
 */
#ifdef X_WEBOBJECTS_HEADERS
int x_webobjects_headers = X_WEBOBJECTS_HEADERS;
#else
int x_webobjects_headers = 0;
#endif
char *WOAdaptorInfo_username = NULL;
char *WOAdaptorInfo_password = NULL;
static strtbl *adaptor_options = NULL;

/*
 * Parse a string of config info into a dictionary. The string contains
 * zero or more key/value pairs:
 *    key1 = value1, key2 = value2, key3 = value3
 * The string may contain newlines so long as the newline is either before
 * a key or after a value.
 */
void set_adaptor_options(struct _strtbl *options, const char *optionsString)
{
   char *key, *value;
   int optionsLen, keyStart, keyEnd, valueStart, valueEnd, keyLen, valueLen;

   optionsLen = strlen(optionsString);
   keyStart = 0;
   do {
      while (keyStart < optionsLen && (optionsString[keyStart] == ' ' || optionsString[keyStart] == ',' || optionsString[keyStart] == '\r' || optionsString[keyStart] == '\n'))
         keyStart++;
      keyEnd = keyStart;
      while (keyEnd < optionsLen && (optionsString[keyEnd] != ' ' && optionsString[keyEnd] != '='))
         keyEnd++;
      valueStart = keyEnd;
      while (valueStart < optionsLen && (optionsString[valueStart] == ' ' || optionsString[valueStart] == '='))
         valueStart++;
      valueEnd = valueStart;
      while (valueEnd < optionsLen && (optionsString[valueEnd] != ' ' && optionsString[valueEnd] != ','))
         valueEnd++;
      keyLen = keyEnd - keyStart;
      valueLen = valueEnd - valueStart;
      if (keyLen > 0 && valueLen > 0)
      {
         key = WOMALLOC(keyLen + 1);
         strncpy(key, &optionsString[keyStart], keyLen);
         key[keyLen] = 0;
         value = WOMALLOC(valueLen + 1);
         strncpy(value, &optionsString[valueStart], valueLen);
         value[valueLen] = 0;
         /* WOLog(WO_INFO, "set_adaptor_options(): adding key = \"%s\", value = \"%s\"", key, value); */
         st_add(options, key, value, STR_FREEKEY|STR_FREEVALUE);
      }
      keyStart = valueEnd + 1;
   } while (keyStart < optionsLen);
}


/*
 *	moved all adaptor init stuff to here
 */
int init_adaptor(struct _strtbl *options)
{
    const char *logPath = NULL;
    const char *logLevel = NULL;
    const char *logFlag = NULL;
    const char *stateFile = DEFAULT_STATE_FILE;
    const char *s;
    char *sharedS;
    int ret = 0;
    const char *configOptions;

    WOMALLOCINIT();
    configOptions = st_valueFor(options, WOOPTIONS);
    if (configOptions)
       set_adaptor_options(options, configOptions);
    
    adaptor_options = options;
    if (options) {
        logPath = st_valueFor(options, WOLOGPATH);
        logLevel = st_valueFor(options, WOLOGLEVEL);
        logFlag = st_valueFor(options, WOLOGFLAG);
    }

    /* We initialize the logging subsystem early on, in order to get the log messages ... */
    WOLog_init(logPath, logFlag, logLevel);

    /* Initialize the string stuff early too, so we can log the config dictionary. */
    if (ret == 0)
    {
       if ((ret = str_init()))
          WOLog(WO_ERR, "init_adaptor(): str_init failed");
    }

#ifdef _MSC_VER // SWK adding more information to log string
    WOLog(WO_DBG, "init_adaptor(): beginning initialization. Adaptor version " ADAPTOR_VERSION " (Windows Apache Module).");
#else
    WOLog(WO_DBG, "init_adaptor(): beginning initialization. Adaptor version " ADAPTOR_VERSION ".");
#endif
    if (options) {
        char *optionsDescription = st_description(options);
        WOLog(WO_INFO, "init_adaptor(): config options are: %s", optionsDescription);
        WOFREE(optionsDescription);
        /* Need to set some defaults here. 1. A default config_uri. */
        if ( (s = st_valueFor(options, WOCONFIG)) == NULL) {
            st_add(options, WOCONFIG, CONFIG_URL, 0);
            WOLog(WO_INFO,"Config URI defaults to %s", CONFIG_URL);
        }
        if ((s = st_valueFor(options, WOUSERNAME)) != NULL) {
           WOAdaptorInfo_username = WOSTRDUP(s);
            if ((s = st_valueFor(options, WOPASSWORD)) != NULL) {
               WOAdaptorInfo_password = WOSTRDUP(s);
            }
        }

        /* Initialize the choice for a WOAdaptorState file */
        if ((s = st_valueFor(options, WOSTATEFILE)) != NULL) {
            sharedS = WOMALLOC(strlen(TEMPDIRWITHSLASH) + strlen(s) + 1);
            stateFile = sharedS;
            strncpy(sharedS, TEMPDIRWITHSLASH, strlen(TEMPDIRWITHSLASH));
            strcpy(sharedS+strlen(TEMPDIRWITHSLASH), s);
        }
        WOLog(WO_INFO,"Adaptor shared state file: %s",stateFile);

        WOLog(WO_INFO,"Adaptor info user: %s, password: %s", WOAdaptorInfo_username ? WOAdaptorInfo_username : "<no user set>",  WOAdaptorInfo_password ? WOAdaptorInfo_password : "<no password set>");
    }

#ifdef WIN32
    {
       WSADATA socketInitData;
       const WORD wVersionRequested = MAKEWORD( 2, 0 );
       if ((ret = WSAStartup(wVersionRequested, &socketInitData)))
       {
          char *errMsg = WA_errorDescription(ret); /* note: use ret here rather than WO_error() because we haven't initialized */
          WOLog(WO_ERR, "init_adaptor(): WSAStartup() failed: %s", errMsg);
          WA_freeErrorDescription(errMsg);
       }
    }
#endif

    /* Ignore SIGPIPE. */
#ifndef WIN32
    {
       void (*oldHandler)(int) = signal(SIGPIPE, SIG_IGN);
       if (oldHandler != SIG_DFL)
       {
          WOLog(WO_DBG, "init_adaptor(): someone installed a SIGPIPE handler");
          signal(SIGPIPE, oldHandler);
       }
    }
#endif

    if (ret == 0)
       if ((ret = WOShmem_init(stateFile, sizeof(WOApp) * WA_MAX_APP_COUNT + sizeof(WOInstance) * WA_MAX_APP_INSTANCE_COUNT * WA_MAX_APP_COUNT + 1024 * 10)))
          WOLog(WO_ERR,"init_adaptor(): WOShmem_init() failed");

    if (ret == 0)
    {
       WOLog(WO_DBG, "init_adaptor(): WOShmem_init succeeded");
       if ((ret = tr_init(options)))
          WOLog(WO_ERR,"init_adaptor(): tr_init() failed");
    }
    if (ret == 0)
    {
       WOLog(WO_DBG, "init_adaptor(): tr_init succeeded");
       if ((ret = hl_init(options)))
          WOLog(WO_ERR,"init_adaptor(): hl_init() failed");
    }
    if (ret == 0)
    {
       WOLog(WO_DBG, "init_adaptor(): hl_init succeeded");
       if ((ret = lb_init(options)))
          WOLog(WO_ERR,"init_adaptor(): lb_init() failed");
    }
    if (ret == 0)
    {
       WOLog(WO_DBG, "init_adaptor(): lb_init succeeded");
       if ((ret = ac_init(options)))
          WOLog(WO_ERR,"init_adaptor(): ac_init() failed");
    }
    if (ret == 0)
    {
       WOLog(WO_DBG, "init_adaptor(): ac_init succeeded");
       if ((ret = transaction_init()))
          WOLog(WO_ERR,"init_adaptor(): transaction_init() failed");
    }

    if (ret == 0)
       WOLog(WO_DBG, "init_adaptor(): transaction_init() succeeded");

    if (ret == 0)
       ac_readConfiguration();
    return ret;
}

const char *adaptor_valueForKey(const char *option)
{
    return (adaptor_options) ? st_valueFor(adaptor_options,option) : NULL;
}



/*
 *	hide some of the differences between Unix & NT
 */
#if !defined(WIN32)

const char *tmp()
{
    return TEMPDIR;
}
const char *root()
{
    return APPLE_ROOT;
}

#else
#include <windows.h>

static const char *subkey = "SOFTWARE\\Apple\\WebObjects\\Configuration";

int WOReadKeyFromConfiguration(const char *keyName, char *buffer, int bufferSize)
{
    HKEY newHandle;
    LONG result;
    REGSAM security = KEY_READ;
    DWORD subKeys;
    DWORD maxSubKeyLength;
    DWORD maxClassLength;
    DWORD values;
    DWORD maxValueNameLength;
    DWORD maxValueDataLength;
    DWORD securityDescriptor;
    FILETIME fileTime;

    *buffer = '\0';		/* null string */

    result = RegOpenKeyEx( HKEY_LOCAL_MACHINE, subkey, 0, security, &newHandle );

    if (result == ERROR_SUCCESS) {
        DWORD classSize = MAX_PATH+1;
        char  className[MAX_PATH];
        DWORD type;

        RegQueryInfoKey( newHandle, className, &classSize, NULL,
                         &subKeys, &maxSubKeyLength, &maxClassLength, &values,
                         &maxValueNameLength, &maxValueDataLength,
                         &securityDescriptor, &fileTime );
        memset(buffer,0,bufferSize);
        result = RegQueryValueEx(newHandle, keyName, 0, &type, (LPBYTE)buffer, (LPDWORD)&bufferSize);
        RegCloseKey( HKEY_LOCAL_MACHINE );	/* close the registry */
    }

    return (result == ERROR_SUCCESS);
}

const char *tmp()
{
    static char *_tmp = NULL;
    if (_tmp == NULL) {
        char path[MAX_PATH];
        if (! WOReadKeyFromConfiguration("TEMP", path, MAX_PATH) )
            strcpy(path, TEMPDIR);
        _tmp = WOSTRDUP(path);
    }
    return _tmp;
}

/*
 *	insure that root always ends with a slash
 */
const char *root()
{
    static char *_root = NULL;
    if (_root == NULL) {
        char path[MAX_PATH];
        if (! WOReadKeyFromConfiguration("NEXT_ROOT", path, MAX_PATH) )
            strcpy(path, APPLE_ROOT);
        if (path[(*path) ? strlen(path)-1 : 0] != '/')
            strcat(path, "/");
        _root = WOSTRDUP(path);
    }
    return _root;
}

#if !defined(NSAPI_PUBLIC) && !defined(MINGW)
/*
 * Provide our own strcasecmp unless we are building NSAPI.
 */
int
strcasecmp (const char *str1, const char *str2)
{
    char val, left, right;

    if (!str1) return -1;
    if (!str2) return 1;

    while (*str1 && *str2) {
        left = *str1++;
        if (isupper(left)) left = tolower(left);

        right = *str2++;
        if (isupper(right)) right = tolower(right);

        if (0 != (val = left - right))
            return (int) val;
    }

    return *str1 - *str2;
}
#endif

#endif
