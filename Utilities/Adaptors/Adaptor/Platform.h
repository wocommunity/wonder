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

#ifndef _PLATFORM_H_
#define _PLATFORM_H_

/* Define this to log each lock/unlock */
/* #define EXTRA_DEBUGGING_LOGS */

#ifdef EXTRA_DEBUGGING_LOGS
#warning Building with EXTRA_DEBUGGING_LOGS on.
#endif

#ifndef SINGLE_THREADED_ADAPTOR
#ifndef MULTITHREADED_ADAPTOR
#error Must define either SINGLE_THREADED_ADAPTOR or MULTITHREADED_ADAPTOR. Fix the makefile.
#endif
#endif

#define SINGLE 		0
#define CTHREADS	1
#define PTHREADS	2
#define WIN32_THREADS	3
#define NSAPI_THREADS   4

#ifdef SINGLE_THREADED_ADAPTOR
#define THREAD_MODEL SINGLE
#else
#ifdef WIN32
#define THREAD_MODEL WIN32_THREADS
#endif
#ifdef SOLARIS
#define THREAD_MODEL PTHREADS
#endif
#ifdef HPUX
#define THREAD_MODEL PTHREADS
#endif

/* Override the previous definition if we are building NSAPI. */
#ifdef NSAPI
#undef THREAD_MODEL
#define THREAD_MODEL NSAPI_THREADS
#endif
#endif

/*
 * A short text string which identifies the adaptor version.
 * This is passed to instances as the x-webobjects-adaptor-version header.
 * For example: "CGI/4.5", "Apache/4.5", etc.
 */
extern char *WA_adaptorName;


/**********************************************
 * Functions relating to error codes/messages.*
 **********************************************/

/*
 * Return the error code associated with the last system call.
 */
extern int WA_error();

/*
 * Returns a human readable description of the error code.
 */
extern char *WA_errorDescription(int error);

/*
 * Free the error string previouly returned by WA_errorDescription().
 */
extern void WA_freeErrorDescription(char *msg);



/*********************
 * Locking functions *
 *********************/
/*
 * If adaptor will be running in a multithreaded environment, these primitives are used for locking.
 * They are meant to encapsulate a system dependent implementation of a recursive lock. A thread
 * calls WA_lock to aquire the lock. It should block if any other thread is holding the lock
 * but proceed if the lock is held by the calling thread. WA_unlock is called when a thread wants to
 * release the lock. It should only allow other threads to aquire the lock after WA_unlock has been
 * called by the thread the same number of times as WA_lock.
 */

/*
 * An opaque typedef for a system dependent lock.
 */
typedef void *WA_recursiveLock;

/*
 * Allocates, initializes, and returns a mutex.
 * Depending on the locking implementation, name may be associated with the lock
 * to simplify debugging. Name should be unique, or may be NULL.
 */
WA_recursiveLock WA_createLock(const char *name);

#ifndef EXTRA_DEBUGGING_LOGS
/*
 * Lock the mutex.
 */
void WA_lock(WA_recursiveLock);

/*
 * Unlock the mutex.
 */
void WA_unlock(WA_recursiveLock);
#else
/* For debugging, pass the location of the lock call */
void _WA_lock(WA_recursiveLock, const char *file, int line);
void _WA_unlock(WA_recursiveLock, const char *file, int line);
#define WA_lock(lock) _WA_lock(lock, __FILE__, __LINE__)
#define WA_unlock(lock) _WA_unlock(lock, __FILE__, __LINE__)
#endif

/*
 * yield the current thread
 */
void WA_yield();

/*
 * If the adaptor is single threaded we don't need any of these.
 */
#ifdef SINGLE_THREADED_ADAPTOR
#define WA_createLock(name) ((WA_recursiveLock)1)
#undef WA_lock
#define WA_lock(lock)
#undef WA_unlock
#define WA_unlock(lock)
#define WA_yield()
#endif

#endif /* _PLATFORM_H_ */
