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
 * This file provides a template implementation for all the platform
 * specific adaptor requirements.
 */

#include "log.h"
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#ifdef WIN32
#include <windows.h>
#include <winsock.h>
#endif
#include "Platform.h"
#include "womalloc.h"

#ifndef SINGLE_THREADED_ADAPTOR
static char *unnamedLock = "(unnamed)";
#endif

/*
 * Error functions. This template is based on the unix errno facilities.
 * A more sophisticated implementation may be required for multithreaded
 * environments. Some sample source applicable to Windows platforms is also
 * shown.
 */

int WA_error()
{
#ifdef WIN32
   return WSAGetLastError();
#else
   return errno;
#endif
}

#ifndef WIN32
char *WA_errorDescription(int error)
{
   return strerror(error);
}

extern void WA_freeErrorDescription(char *msg)
{
   /* This is a no-op because WA_errorDescription doesn't copy the string. */
}

#else
/*
 * implementation for the Windows platform.
 */
char *WA_errorDescription(int error)
{
   LPVOID lpMsgBuf = 0;
   FormatMessage(
                 FORMAT_MESSAGE_ALLOCATE_BUFFER |
                 FORMAT_MESSAGE_FROM_SYSTEM |
                 FORMAT_MESSAGE_IGNORE_INSERTS,
                 NULL,
                 error,
                 MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), /* Default language */
                 (LPTSTR) &lpMsgBuf,
                 0,
                 NULL
                 );
   return lpMsgBuf;
}

extern void WA_freeErrorDescription(char *msg)
{
   LocalFree((LPVOID)msg);
}


#if (THREAD_MODEL == WIN32_THREADS)
typedef struct {
   HANDLE lock;
   const char *name;
} WinRecursiveLock;

WA_recursiveLock WA_createLock(const char *name)
{
   WinRecursiveLock *lock;

   lock = WOMALLOC(sizeof(WinRecursiveLock));
   if (lock)
   {
      lock->lock = CreateMutex(NULL, FALSE, NULL);
      if (name)
         lock->name = name;
      else
         lock->name = unnamedLock;
   } else
      WOLog(WO_ERR, "WA_createLock(): could not malloc");
   return (WA_recursiveLock)lock;
}


#ifdef EXTRA_DEBUGGING_LOGS
void _WA_lock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_lock(WA_recursiveLock _lock)
#endif
{
   WinRecursiveLock *lock = (WinRecursiveLock *)_lock;
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "locking %s from %s:%d", lock->name, file, line);
#endif
   WaitForSingleObject(lock->lock, INFINITE);
}

#ifdef EXTRA_DEBUGGING_LOGS
void _WA_unlock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_unlock(WA_recursiveLock _lock)
#endif
{
   WinRecursiveLock *lock = (WinRecursiveLock *)_lock;
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "unlocking %s from %s:%d", lock->name, file, line);
#endif
   ReleaseMutex(lock->lock);
}

void WA_yield()
{
   SleepEx(0,0);
}

#endif /* THREAD_MODEL */

#endif /* WIN32 */


#if (THREAD_MODEL == CTHREADS)
#include <mach/cthreads.h>
typedef struct {
   cthread_t lockingThread;
   unsigned int lockCount;
   struct mutex m;
   struct condition c;
   const char *name;
} CThreadRecursiveLock;

WA_recursiveLock WA_createLock(const char *name)
{
   CThreadRecursiveLock *lock;

   lock = WOMALLOC(sizeof(CThreadRecursiveLock));
   if (lock)
   {
      mutex_init(&lock->m);
      condition_init(&lock->c);
      lock->lockCount = 0;
      lock->lockingThread = NULL;
      if (name)
         lock->name = name;
      else
         lock->name = unnamedLock;
   }
   return lock;
}


/*
 * Lock the mutex.
 */
#ifdef EXTRA_DEBUGGING_LOGS
void _WA_lock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_lock(WA_recursiveLock _lock)
#endif
{
   CThreadRecursiveLock *lock = (CThreadRecursiveLock *)_lock;
   cthread_t self = cthread_self();
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "thread %x locking %s from %s:%d", self, lock->name, file, line);
#endif
   mutex_lock(&lock->m);
   while (lock->lockingThread != self && lock->lockingThread != NULL)
      condition_wait(&lock->c, &lock->m);
   lock->lockingThread = self;
   lock->lockCount++;
   mutex_unlock(&lock->m);
}

/*
 * Unlock the mutex.
 */
#ifdef EXTRA_DEBUGGING_LOGS
void _WA_unlock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_unlock(WA_recursiveLock _lock)
#endif
{
   CThreadRecursiveLock *lock = (CThreadRecursiveLock *)_lock;
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "thread unlocking %s from %s:%d", cthread_self(), lock->name, file, line);
#endif
   mutex_lock(&lock->m);
   lock->lockCount--;
   if (lock->lockCount == 0)
   {
      lock->lockingThread = NULL;
      condition_signal(&lock->c);
   }
   mutex_unlock(&lock->m);
}

void WA_yield()
{
   cthread_yield(cthread_self());
}
#endif /* cthreads */

#if (THREAD_MODEL == PTHREADS)
#include <pthread.h>
#include <sched.h>
typedef struct {
   pthread_t lockingThread;
   unsigned int lockCount;
   pthread_mutex_t m;
   pthread_cond_t c;
   const char *name;
} PThreadRecursiveLock;

WA_recursiveLock WA_createLock(const char *name)
{
   PThreadRecursiveLock *lock;

   lock = WOMALLOC(sizeof(PThreadRecursiveLock));
   if (lock)
   {
      pthread_mutex_init(&lock->m, NULL);
      pthread_cond_init(&lock->c, NULL);
      lock->lockCount = 0;
      lock->lockingThread = 0;
      if (name)
         lock->name = name;
      else
         lock->name = unnamedLock;
   }
   return lock;
}


/*
 * Lock the mutex.
 */
#ifdef EXTRA_DEBUGGING_LOGS
void _WA_lock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_lock(WA_recursiveLock _lock)
#endif
{
   PThreadRecursiveLock *lock = (PThreadRecursiveLock *)_lock;
   pthread_t self = pthread_self();
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "thread %x locking %s from %s:%d", self, lock->name, file, line);
#endif
   pthread_mutex_lock(&lock->m);
   while (!pthread_equal(lock->lockingThread,self) && lock->lockCount != 0)
      pthread_cond_wait(&lock->c, &lock->m);
   lock->lockingThread = self;
   lock->lockCount++;
   pthread_mutex_unlock(&lock->m);
}

/*
 * Unlock the mutex.
 */
#ifdef EXTRA_DEBUGGING_LOGS
void _WA_unlock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_unlock(WA_recursiveLock _lock)
#endif
{
   PThreadRecursiveLock *lock = (PThreadRecursiveLock *)_lock;
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "thread %x unlocking %s from %s:%d", pthread_self(), lock->name, file, line);
#endif
   pthread_mutex_lock(&lock->m);
   lock->lockCount--;
   if (lock->lockCount == 0)
   {
      lock->lockingThread = 0;
      pthread_cond_signal(&lock->c);
   }
   pthread_mutex_unlock(&lock->m);
}

void WA_yield()
{
   sched_yield();
}

#endif /* pthreads */


#if (THREAD_MODEL == NSAPI_THREADS)
#include <nsapi.h>

typedef struct {
   SYS_THREAD lockingThread;
   unsigned int lockCount;
   CONDVAR condvar;
   CRITICAL crit;
   const char *name;
} NSAPIThreadRecursiveLock;

WA_recursiveLock WA_createLock(const char *name)
{
   NSAPIThreadRecursiveLock *lock;

   lock = WOMALLOC(sizeof(NSAPIThreadRecursiveLock));
   if (lock)
   {
      lock->crit = crit_init();
      lock->condvar = condvar_init(lock->crit);
      lock->lockCount = 0;
      lock->lockingThread = NULL;
      if (name)
         lock->name = name;
      else
         lock->name = unnamedLock;
   }
   if (!lock)
      WOLog(WO_ERR, "WA_createLock() failed for lock %s", name ? name : unnamedLock);
   return lock;
}

/*
 * Lock the mutex.
 */
#ifdef EXTRA_DEBUGGING_LOGS
void _WA_lock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_lock(WA_recursiveLock _lock)
#endif
{
   NSAPIThreadRecursiveLock *lock = (NSAPIThreadRecursiveLock *)_lock;
   SYS_THREAD self = systhread_current();
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, "  locking %s from %s:%d", lock->name, file, line);
#endif
   crit_enter(lock->crit);
   while (lock->lockingThread != self && lock->lockCount != 0)
      condvar_wait(lock->condvar);
   lock->lockingThread = self;
   lock->lockCount++;
   crit_exit(lock->crit);
}


/*
 * Unlock the mutex.
 */
#ifdef EXTRA_DEBUGGING_LOGS
void _WA_unlock(WA_recursiveLock _lock, const char *file, int line)
#else
void WA_unlock(WA_recursiveLock _lock)
#endif
{
   NSAPIThreadRecursiveLock *lock = (NSAPIThreadRecursiveLock *)_lock;
#ifdef EXTRA_DEBUGGING_LOGS
   if (_lock != logMutex)
      WOLog(WO_DBG, " unlocking %s from %s:%d",  lock->name, file, line);
#endif
   crit_enter(lock->crit);
   lock->lockCount--;
   if (lock->lockCount == 0)
   {
      lock->lockingThread = NULL;
      condvar_notify(lock->condvar);
   }
   crit_exit(lock->crit);
}

void WA_yield()
{
   /* Couldn't find a 'yield' in NSAPI. A 1 ms sleep may be overkill. */
   systhread_sleep(1);
}

#endif /* NSAPI_THREADS */
