/*

Copyright � 2000-2007 Apple, Inc. All Rights Reserved.

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
#ifndef CONFIG_H_INCLUDED
#define CONFIG_H_INCLUDED
/*
 * This is where we define some host dependent stuff as well as defaults 
 * for some things.  
 *
 * This file is the first include in all source.
 */

#include "Platform.h"

/* #define	inline	*/	/* ... if compiler doesn't support 'inline' */
#ifdef _MSC_VER // SWK used for Visual Studio
#define	inline _inline
#define alloca _alloca
#endif
/* Possibly useful if you want to wait at a particular point for the debugger to attach. */
#ifndef WIN32
#define DEBUGWAIT \
{ \
   volatile int wait = 1; \
   WOLog(WO_ERR, "my pid is: %d", getpid()); \
   while (wait) \
      sleep(1); \
}
#else
#define DEBUGWAIT

#ifndef _INTPTR_T_DEFINED
typedef int  intptr_t;
#define _INTPTR_T_DEFINED
#endif

#endif


#define CURRENT_WOF_VERSION_MAJOR	4
#define CURRENT_WOF_VERSION_MINOR	6

#define ADAPTOR_VERSION			"4.6"

/* Used to turn the value of a macro into a string literal */
#define _Str(x) #x
#define Str(x) _Str(x)

/* maximum number of wotaskds the adaptor can query for config info */
#define WA_MAX_CONFIG_SERVERS		16

#define WA_MAX_APP_NAME_LENGTH 		64	/* maximum length of a WOApplication's name, including a terminating null */
#define WA_MAX_APP_COUNT		256	/* maximum number of applications the adaptor can keep track of */
#define WA_MAX_APP_INSTANCE_COUNT	128	/* maximum number of instances of a single application the adaptor can keep track of */
#define WA_MAX_URL_LENGTH		256	/* maximum length of a redirect url in the config, including the null */
#define WA_MAX_ADDITIONAL_ARGS_LENGTH	0	/* maximum length of the additional args, including the null */
#define WA_LB_MAX_NAME_LENGTH		16	/* maximum length of a load balancing routine's name, including the null */
#define WA_APP_LB_INFO_SIZE		4	/* size in bytes to reserve for load balancing info in WOApp */
#if defined(_MSC_VER) || defined(MINGW)  // SWK changed from 8 to 16 cause VS2005 uses _time_t_64
#define WA_INST_LB_INFO_SIZE		16	/* size in bytes to reserve for load balancing info in WOInstance */
#else
#define WA_INST_LB_INFO_SIZE		16	/* size in bytes to reserve for load balancing info in WOInstance */
#endif
#define WA_MAX_HOST_NAME_LENGTH		64	/* maximum length of a host name, including the null */
#define WA_MAX_INSTANCE_NUMBER_LENGTH	8	/* maximum length of an instance number, including the null */

   
/*
 *	default values for some feature settings
 */
/* Instance defaults */
#define	CONN_TIMEOUT		 3		/* timeout for connect to webapp instance */
#define	SEND_TIMEOUT		 5		/* timeout for send to webapp instance */
#define	RECV_TIMEOUT		30		/* timeout for receive from webapp instance */
#define	CONF_CONN_TIMEOUT	 1		/* timeout for connect to configuration server */
#define	CONF_SEND_TIMEOUT	 2		/* timeout for send to configuration server */
#define	CONF_RECV_TIMEOUT	 3		/* timeout for receive from configuration server */
#define	SEND_BUF_SIZE		32768		/* standard send buffer size for tcp sockets */
#define	RECV_BUF_SIZE		32768		/* standard receive buffer size for tcp sockets */
/* Application defaults */
#define	DEADAPPINTERVAL		30		/* if connect() fails, ignore the instance for this many seconds */
#define	RETRIES			10		/* if your app can failover between instances, this is how many times to retry */
#ifdef FORKING_WEBSERVER
#define	CONNECTION_POOL_SZ	0		/* set to 0 for Apache 1.x, because of failure to scale */
#else
#define CONNECTION_POOL_SZ	8		/* a pretty low number is 4; 32 is probably big */
#endif

/* Threshold for streaming request data. Requests which specify content length greater than these values will be streamed. */
/* REQUEST_STREAMED_THRESHOLD is relatively large so we can collect data without bothering the instance, to preserve historic */
/* behavior for most requests, and to give some chance of redirecting the request if there is a problem. (Once we read some streamed */
/* data we can no longer redirect the request to a new instance.) */
#define REQUEST_STREAMED_THRESHOLD	(1024 * 1024)

/* Buffer size for streaming response data back to the client. */
/* RESPONSE_STREAMED_SIZE is relatively small, as there is no advantage in buffering large chunks of response content data. */
/* Choose a value compatible with the network socket buffer sizes. */
#define RESPONSE_STREAMED_SIZE	(SEND_BUF_SIZE < RECV_BUF_SIZE ? SEND_BUF_SIZE : RECV_BUF_SIZE)

/*
 *	to retain x-webobjects- style header keys, enable this
 */
/* #define X_WEBOBJECTS_HEADERS	1 */

/*
 *	some of the above defaults can be changed by setting the values for
 *	these key strings in the apache.conf/srm.conf/obj.conf/registry for 
 *	Apache/Netscape/IIS respectively
 *
 *	Note that for Apache, we use the more descriptive configuration
 *	directive shown in the comment.  Internally it gets mapped to the 
 *	short version.
 */
typedef struct _strtbl *strtbl_p;

/*
 * Initialize the adaptor. If this returns nonzero then the initialization
 * has failed and the adaptor is unusable.
 */
int init_adaptor(strtbl_p options);
const char *adaptor_valueForKey(const char *option);

#define	WOCONFIG	"config"		/* WebObjectsConfig   */
#define	WOUSERNAME 	"username"	   	/* WebObjectsAdminUsername */
#define	WOPASSWORD 	"password"	   	/* WebObjectsAdminPassword */
#define	WOCNFINTVL	"confinterval"		/* WebObjectsConfig	  */
#define	WOLOGPATH	"logPath"	 	/* WebObjectsLog */
#define	WOLOGLEVEL	"logLevel"		/* WebObjectsLog */
#define	WOLOGFLAG	"logFlag"		/* WebObjectsLog */
#define	WOSTATEFILE	"stateFile"		/* WebObjectsStateFile */
#define WOOPTIONS       "options"               /* additional adaptor options string */
#define WODOMAIN        "domainname"            /* domain name to use in links to the adaptor */

/* Application settings keys */
#define	WOSCHEDULER 		"scheduler"		/* scheduler to use */
#define	WOERRREDIR		"redir"			/* redirect url */
#define WOADDITIONALARGS	"additionalArgs"	/* Additional args */
#define WOAPPNAME		"name"			/* WOApp name */
#define	WORETRIES		"retries"		/* number of times to retry a request after communications failure  */
#define	WODEADAPP		"dormant"		/* delay after failed connect() before retrying instance  */
#define	WOPOOLSZ		"poolsize"		/* size of per instance connection pool */
#define	WOURLVERSION 		"urlVersion"		/* url version number */

/* Instance settings keys */
#define WOINSTANCENUMBER	"id"			/* the instance number */
#define WOHOST			"host"			/* host the instance is running on */
#define WOPORT			"port"			/* port the instance is listening on */
#define WOSENDBUFSIZE		"sendBufSize"		/* size of the send buffer */
#define WORECVBUFSIZE		"recvBufSize"		/* size of the receive buffer */
#define WOSENDTIMEOUT		"sendTimeout"		/* send timeout */
#define WORECVTIMEOUT		"recvTimeout"		/* receive timeout */
#define WOCNCTTIMEOUT		"cnctTimeout"		/* connect timeout */

#if defined(SUPPORT_REFUSENEWSESSION_ATTR)
  #define WOREFUSENEWSESSIONS     "refuseNewSessions"
#endif

/* This one is defined above, but included again here to be complete. */
/*#define WOADDITIONALARGS	"additionalArgs"*/	/* Additional args */

#ifdef CGI
#define X_WEBOBJECTS_HEADERS 1
#endif

/*
 *	support for older version URLs.
 */
/* #define	SUPPORT_V3_URLS	*/	/* comment out to disable */
#define	SUPPORT_V4_URLS		/* comment out to disable */
    
#define	STATINTERVAL	5	/* wait this many seconds between checks of /tmp/logWebObjects */
/* #define	ALWAYS_LOG */	/* of course, you might want to just always log.. */

/* The configured interval is bracketed by these. */
#define	MIN_CONF_INTERVAL 10			/* minimum interval between re-reading config */
#define	MAX_CONF_INTERVAL 3600			/* maximum interval between re-reading config */
#define DEFAULT_CONF_INTERVAL 10		/* the default value, if no value is supplied elsewhere */
#define CONF_SEARCH_INTERVAL (DEFAULT_CONF_INTERVAL*10)	/* minimum time between broadcast searches for config servers */
/*
 *	timestamping log calls is kinda expensive, use this if you *really* need it
 */
/* #define	TIMESTAMP_LOG_MESSAGES	*/	/* time stamping of log messages */

/*
 *	some configuration string constants
 */
#define CONFIG_FILE "WebObjects.xml"
#define CONFIG_FILE_PATH "WebObjects/Configuration/" CONFIG_FILE

/* For local wotaskd */
/* #define CONFIG_URL "http://localhost:1085" */

/* Obtain configuration information via multicast */
#define CONFIG_URL "http://localhost:1085/"

#define	LOG_FILE "WebObjects.log"
#define	LOG_FLAG "logWebObjects"

/*
 *	some important path constants you might want to tweak
 */
#if	defined(WIN32)
    /* if not found in registry, use this...  */
#define	APPLE_ROOT 	"C:/Apple/Local/"	
#define	TEMPDIR		"C:/TEMP/"
int WOReadKeyFromConfiguration(const char *keyName, char *buf, int buflen);
#elif	defined(__APPLE__)
	/*	Mac OS  */
#define APPLE_ROOT	"/Local/"
#define TEMPDIR	"/tmp"
#else
	/*	other Unix	*/
#define	APPLE_ROOT	"/opt/Apple/Local/"
#define	TEMPDIR	"/tmp"
#endif
#define EXECROOT "Library"

/* Where to keep persistent adaptor state */
#define DEFAULT_STATE_FILE TEMPDIR "/WOAdaptorState"
#define TEMPDIRWITHSLASH TEMPDIR "/"

/*
 *	and operating system independent functions for obvious constants
 */
const char *root();
const char *tmp();

/*
 *	operating specific things regarding gethostbyname(), gethostent()
 */
#if	defined(SOLARIS) || defined(IRIX)
#define	HAS_REENTRANT_GETHOSTENT
#if defined(NSAPI)
#define	NEEDS_HSTRERR
#endif
#endif
#if defined(WIN32)
#define	NEEDS_HSTRERR
#ifndef strcasecmp
int strcasecmp(const char *, const char *);
#endif
#endif

/*
 *	some string constants that are best collected into one place
 */
#define	CONTENT_LENGTH	"content-length"	/* http header */
#define CONTENT_TYPE    "content-type"		/* ditto */
#define	CONNECTION	"connection"
#define	HTTP_KEEP_ALIVE	"keep-alive"
#define	HTTP_CLOSE	"close"
#define WEBOBJECTS	"WebObjects"		/* the ubiquitous magic moniker */
#define	INST_COOKIE	"woinst="		/* WO 4.0 instance number cookie */
#define	COOKIE		"cookie"
#define	LOCATION	"location"

/*
 *	private headers that shouldn't get leaked back to the browser
 */
#define	LOAD_AVERAGE_HEADER	 "x-webobjects-loadaverage"
#define	REFUSING_SESSIONS_HEADER "x-webobjects-refusenewsessions"
#define REQUEST_ID_HEADER        "x-webobjects-request-id"

static const char * const wo_versions = "34";
#define v4_url  wo_versions+1
#define v3_url  wo_versions
#define URLVersionLen   1

#endif
