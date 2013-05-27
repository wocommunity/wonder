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
#ifndef	WO_H_INCLUDED
#define WO_H_INCLUDED

/*
 * How the adaptor does logging
 */

#define	WO_DBG	0
#define	WO_INFO 1
#define	WO_WARN 2
#define	WO_ERR	3
#define	WO_USER 4    

/*
 * WOLogLevel[WO_INFO] = "Info"; etc.
 *
 */
extern const char * const WOLogLevel[];


void WOLog_init(const char *logfile, const char *logflag, const char *level);

void WOLog(int level, const char *format, ...);

/*
 * This mutex is used internally to synchronize log requests.
 * It is global only so the locking functions know not to try
 * and log messages associated with this particular lock.
 * (Doing so would be an infinite recursion.)
 */
extern void *logMutex;

#endif
