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
 *	Log stuff to a log file.  During normal operation, we only log messages
 *	at the WO_ERR level to the server's error log file.
 *
 *	If the administrator creates a file (usually "/tmp/logWebObjects"), then
 *	we log all messages into the file specified as "log" at init or
 *	/tmp/WebObjects.log.  /tmp/logWebObjects must be owned by root.
 *
 *	Only Netscape permits logging to the error log without passing in
 *	some pointer to a server or request struct.  Since this routine has
 *	no such pointer (for Apache & IIS), we have to hack around it. We
 *	assume the server stub creates an adaptor global to cache a valid
 *	pointer.
 *
 */
#include "config.h"
#include "log.h"
/* #include "womalloc.h" - to avoid infinite recursion, this module should not use these allocation routines */
#include "wastring.h"

#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>
#include <sys/types.h>		/* mode_t */
#include <sys/stat.h>		/* umask() */
#include <time.h>
#if defined(WIN32)
#ifndef _MSC_VER // SWK old // SWK old WO4.5 headerfile
#if !defined(MINGW)
#include <winnt-pdo.h>
#endif
#endif
#include <windows.h>
#include <io.h>
#else
#include <sys/param.h>
#include <fcntl.h>
#include <unistd.h>
#endif

#if defined(Netscape)
#include <frame/log.h>
#elif defined(APACHE)
#if	defined(mach)
#include <hsregex.h>
#endif	/* mach-regex */
#include <httpd.h>
#include <http_log.h>
/*
 *	to log properly into the Apache error.log
 */
extern server_rec *_webobjects_server;
#elif defined(IIS)
// #include <httpext.h> // deactivated anyway
/*
 *	to log properly into IIS's error.lgo
 */
/* extern foo_type *_bar; */
#endif

#ifndef MAXPATHLEN
#define MAXPATHLEN 255
#endif

//#define	ALWAYS_LOG	*/	/* of course, you might want to just always.. */

#define	STRSIZE	2000

static int initialized = 0;
static char logPath[MAXPATHLEN];	/* "/tmp/WebObjects.log" */

#ifndef	ALWAYS_LOG
static char logFlag[MAXPATHLEN];	/* "/tmp/logWebObjects" */
#endif

WA_recursiveLock	logMutex;

const char * const WOLogLevel[] = {"Debug", "Info", "Warn",  "Error", "User", "" };

#define	MAXLEVEL WO_ERR

static int baselevel = WO_DBG;

void WOLog_init(const char *logfile, const char *logflag, const char *level)
{
   int i;
   int fd;

   logMutex = WA_createLock("logMutex");

   /*
    *	the file we stat() to see if we should log
    */
#ifndef	ALWAYS_LOG
   if (logflag != NULL) {
       strcpy(logFlag, logflag);
   } else {
       sprintf(logFlag,"%s/%s",tmp(),LOG_FLAG);
   }
#endif

   /*
    *	log to file.  we need to make sure it's world writable since
    *	we're likely to fork/exec from root to httpd or something...
    */
   if (logfile != NULL) {
      strcpy(logPath, logfile);
   } else {
      sprintf(logPath,"%s/%s",tmp(),LOG_FILE);
   }
#ifdef	WIN32
   fd = _lopen(logPath, OF_WRITE| OF_SHARE_COMPAT);
   _lclose(fd);
#else
   fd = open(logPath, O_WRONLY, 0644);
   close(fd);		/* create the file if needed */
#endif
   chmod(logPath, 0644);

   if (level) {
      for (i = WO_DBG; i <= WO_USER; i++) {
         if (strcasecmp(WOLogLevel[i], level) == 0) {
            baselevel = i;
            break;
         }
      }
   }

   initialized = 1;
}


/*
 *	Only consult the flag (existence of /tmp/logWebObjects) once every
 *	so many log attempts.  This reduces the number of stats() to
 *	(hopefully) once per request on average.
 */
inline
static int shouldLog()
{
#ifdef	ALWAYS_LOG
   return 1;
#else
   static int _shouldLog = 0;
   static time_t statTime = 0;
   time_t now;

   now = time(NULL);
   WA_lock(logMutex);
   if (statTime < now) {
      struct stat statbuf;
      statTime = now + STATINTERVAL;		/* reset timer */
      _shouldLog = (stat(logFlag,&statbuf) == 0);
      #ifndef WIN32
      _shouldLog = _shouldLog && (statbuf.st_uid == 0);  // requesting root ownership does not make sense under Win32
      #endif
   }
   WA_unlock(logMutex);
   return _shouldLog;
#endif
}

void WOLog(int level, const char *format, ...)
{
   FILE *log;
   va_list ap;
   int do_it;
#if defined(TIMESTAMP_LOG_MESSAGES)
   struct tm *t;
   time_t now;
   char timestamp[64];
#endif

   if (level < baselevel)
      return;

   if (! initialized )
	   return;

   do_it = shouldLog();
   if ( do_it ) {
      /*
       * plenty of people have complained that we need to timestamp
       * the log entries.  the problem is that mktime & friends aren't
       * reentrant.
       */
#if defined(TIMESTAMP_LOG_MESSAGES)
      WA_lock(logMutex);
      time(&now);
      t = localtime(&now);
      strftime(timestamp, sizeof(timestamp), "%d-%b-%Y %T - ", t);
      WA_unlock(logMutex);
#endif
      log = fopen(logPath, "a+");
      if (log != NULL) {
#if defined(TIMESTAMP_LOG_MESSAGES)
         fprintf(log, timestamp);
#endif
         fprintf(log,"%s: ", WOLogLevel[level]);
         va_start(ap, format);
         vfprintf(log, format, ap);
         va_end(ap);
         fprintf(log,"\n");
         fclose(log);
      }else{
// TODO - figure out how to report this for other web servers
#if defined(APACHE)
         ap_log_error(APLOG_MARK, APLOG_ERR, 0, _webobjects_server, "Failed to append to log file '%s'.  This can occur when the file is not writable by the child httpd process.  A workaround is to change the ownership of the file to match the child httpd process.", logPath);
#endif
      }
   }


   /*
    *	if the error is serious, include it into the server's log
    */
#if	defined(Netscape) || defined(APACHE) || defined(IIS)
   if (level == WO_ERR) {
      String *str;
      str = str_create(NULL, 128);
      va_start(ap,format);
      str_vappendf(str, format, ap);
      va_end(ap);
#if defined(Netscape)
      log_error(0,"WebObjects",NULL,NULL,str->text);
#elif defined(APACHE)
      ap_log_error(APLOG_MARK, APLOG_ERR, 0, _webobjects_server, "%s", str->text);
#elif defined(IIS)
      /*
       *	again, we're stymied because we don't have a ptr to the
       *	server struct
       * /
       {
          LPDWORD len = strlen(logstr);
          ServerSupportFunction(p->ConnID, HSE_APPEND_LOG_PARAMETER,
                                str->text, &len, (LPDWORD)NULL);
       }
       */
#endif
      str_free(str);
   }
#endif
}
