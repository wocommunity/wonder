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
 *	Covers for memory allocation functions so that we can trail the
 *	malloc()s and match them to the free()s.
 *
 */
#include "config.h"
#include "strdict.h"
#include "womalloc.h"
#include "log.h"
#include "Platform.h"

#if	defined(FINDLEAKS)

/* define to log all allocations/frees */
/* #define LOG_ALL_ALLOCATIONS 1 */

#include <stdio.h>

#define GUARD_SIZE 1024
#define LOWSIDE_BYTE 0x77
#define HIGHSIDE_BYTE 0xaa

typedef struct _Allocation {
   const char *file;			/* filename in which the allocation occurred */
   unsigned int line;			/* line # within file */
   unsigned int size;			/* requested allocation size */
   void *realPtr;			/* pointer to the actual allocated buffer (including guard area) */
   void *effectivePtr;			/* pointer returned to the allocator */
   int index;				/* unique allocation index */
   struct _Allocation *next;
   struct _Allocation *last;
} Allocation;

static Allocation *allocations = NULL;
static Allocation *unusedAllocations = NULL;
static WA_recursiveLock *womallocLock = NULL;
static int initialized = 0;
static int malloc_index = 1;
static int leak_mark = 0;

static void freeAllocation(Allocation *a);
static int check_guard(Allocation *a);
static void describeAllocation(Allocation *a, const char *prefix, const char *suffix);

void womallocinit()
{
   womallocLock = WA_createLock("womalloc lock");
   initialized = 1;
}

static void mallocbug()
{
   WOLog(WO_DBG, "break on mallocbug() to debug");
}

static Allocation *newAllocation(unsigned int size, const char *file, unsigned int line)
{
   Allocation *a;

   a = unusedAllocations;
   if (a)
   {
      unusedAllocations = a->next;
   } else {
      a = malloc(sizeof(Allocation));
   }
   a->next = a->last = NULL;

   if (a)
   {
      a->file = file;
      a->line = line;
      a->size = size;
      a->index = malloc_index++;
      a->realPtr = malloc(size + GUARD_SIZE * 2);
      if (a->realPtr)
      {
         memset(a->realPtr, LOWSIDE_BYTE, GUARD_SIZE);
         memset(&(((char *)a->realPtr)[GUARD_SIZE]), 0xff, size);
         memset(&(((char *)a->realPtr)[size+GUARD_SIZE]), HIGHSIDE_BYTE, GUARD_SIZE);
         a->effectivePtr = &(((char *)a->realPtr)[GUARD_SIZE]);

         a->next = allocations;
         if (allocations)
            allocations->last = a;
         allocations = a;
#ifdef LOG_ALL_ALLOCATIONS
         describeAllocation(a, "new allocation: ", "");
#endif
      } else {
         free(a);
         a = NULL;
      }
   }
   if (!a)
   {
      WOLog(WO_DBG, "recordAllocation(): malloc failure");
      mallocbug();
   }
   return a;
}

static void freeAllocation(Allocation *a)
{
#ifdef LOG_ALL_ALLOCATIONS
         describeAllocation(a, "freeing allocation: ", "");
#endif
   if (a->next)
      a->next->last = a->last;
   if (a->last)
      a->last->next = a->next;
   if (a == allocations)
      allocations = a->next;
   a->next = a->last = NULL;

   if (a->realPtr)
   {
      check_guard(a);
      free(a->realPtr);
   }
   a->realPtr = NULL;
   a->next = unusedAllocations;
   unusedAllocations = a;
}

static Allocation *allocationForPtr(void *ptr)
{
   Allocation *a = allocations;

   while (a && a->effectivePtr != ptr)
      a = a->next;
   if (!a)
   {
      WOLog(WO_DBG, "allocationForPtr(): lookup of unknown pointer 0x%x", (int)ptr);
      mallocbug();
   }
   return a;
}

static void describeAllocation(Allocation *a, const char *prefix, const char *suffix)
{
   WOLog(WO_DBG, "%s0x%x, %d bytes, %s:%d%s", prefix, (int)a->effectivePtr, a->size, a->file, a->line, suffix);
}


static int check_guard(Allocation *a)
{
   unsigned char *cptr = (unsigned char *)a->realPtr;
   int i, foundProblem = 0;

   for (i=0; i<GUARD_SIZE && !foundProblem; i++)
   {
      if (cptr[i] != LOWSIDE_BYTE)
         foundProblem = 1;
      if (cptr[i + GUARD_SIZE + a->size] != HIGHSIDE_BYTE)
         foundProblem = 1;
   }
   if (foundProblem)
      describeAllocation(a, "check_guard(): guard area corrupted - ", "\n");
   return foundProblem;
}

void *womalloc(const char *srcfile, int line, size_t sz)
{
   Allocation *a;
   
   if (initialized) WA_lock(womallocLock);
   a = newAllocation(sz, srcfile, line);
   if (initialized) WA_unlock(womallocLock);
   return a ? a->effectivePtr : NULL;
}

void *wocalloc(const char *srcfile, int line, size_t ct, size_t sz)
{
   void *ptr;

   if (initialized) WA_lock(womallocLock);
   ptr = womalloc(srcfile, line, ct * sz);
   if (ptr)
      memset(ptr, 0, ct * sz);
   if (initialized) WA_unlock(womallocLock);
   return ptr;
}

void *worealloc(const char *srcfile, int line, void *ptr, size_t sz)
{
   Allocation *old;
   void *new = NULL;

   if (initialized) WA_lock(womallocLock);
   if (ptr == NULL)
      return womalloc(srcfile, line, sz);
   if (sz == 0)
      wofree(srcfile, line, ptr);
   else {
      old = allocationForPtr(ptr);
      if (old)
      {
         new = womalloc(srcfile, line, sz);
         if (new)
         {
            int copySize = old->size;
            if (copySize > sz)
               copySize = sz;
            memcpy(new, old->effectivePtr, copySize);
            freeAllocation(old);
         }
      } else {
         WOLog(WO_DBG, "worealloc(): could not find ptr 0x%x", (int)ptr);
         mallocbug();
      }
   }
   if (initialized) WA_unlock(womallocLock);
   return new;
}

void wofree(const char *srcfile, int line, void *ptr)
{
   Allocation *a;
   if (initialized) WA_lock(womallocLock);
   a = allocationForPtr(ptr);
   if (a)
      freeAllocation(a);
   else {
      WOLog(WO_DBG, "wofree(): attempt to free unallocated ptr 0x%x, %s:%d", (int)ptr, srcfile, line);
      mallocbug();
   }
   if (initialized) WA_unlock(womallocLock);
}

char *wostrdup(const char *srcfile, int line, const char *s1) {
   char *buf;
   int size;

   if (initialized) WA_lock(womallocLock);
   size = strlen(s1) + 1;
   buf = womalloc(srcfile, line, size);
   if (buf)
      memcpy(buf, s1, size);
   if (initialized) WA_unlock(womallocLock);
   return buf;
}

void showleaks()
{
   Allocation *a;
   char *newStr;
   int max = 0;
   static int iteration = 0;
   char iterStr[16];
   if (initialized) WA_lock(womallocLock);
   a = allocations;
   sprintf(iterStr,"%6d: ", iteration++);
   WOLog(WO_DBG, "showleaks() dumping allocation list:");
   while (a)
   {
      if (a->index > leak_mark)
         newStr = " (new)";
      else
         newStr = "";
      describeAllocation(a, iterStr, newStr);
      if (max < a->index)
         max = a->index;
      a = a->next;
   }
   leak_mark = max;
   WOLog(WO_DBG, "showleaks() complete");
   if (initialized) WA_unlock(womallocLock);
}

#endif
