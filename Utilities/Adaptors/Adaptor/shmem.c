/*

Copyright ? 2000-2007 Apple, Inc. All Rights Reserved.

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

#include "config.h"
#include "log.h"
#include "shmem.h"
#include "womalloc.h"
#include "strdict.h"
#include "list.h"

#include <errno.h>
#include <sys/types.h>
#include <stdio.h>

#ifndef DISABLE_SHARED_MEMORY

#include <fcntl.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/file.h>
#include <sys/mman.h>
#include <sys/uio.h>
#include <unistd.h>
#include <stddef.h>

/* Define this to log each lock/unlock */
/* #define EXTRA_DEBUGGING_LOGS */


/*
 * This structure is the definition of a region.
 */
typedef struct {
   off_t offset;					/* The offset of the beginning of the region in the file */
   size_t elementSize;				/* The size of one element in the region. */
   unsigned int elementCount;		/* The number of elements in the region. */
   off_t nextRegion;				/* The file offset of the next region definition. nextRegion == 0 for the last region */
   char name[1];					/* The null terminated region name. */
} Region;



/*
 * The file descriptor for the file backing the shared memory.
 * Note that WOShmem_fd != -1 indicates that shared memory is enabled.
 */
static int WOShmem_fd = -1;

#ifndef MAP_FAILED
#define MAP_FAILED ((void *)-1)
#endif

/*
 * The address in memory at which the file has been mapped.
 */
static void * WOShmem_base_address = MAP_FAILED;

/*
 * The total size of the mapped memory.
 */
static unsigned int WOShmem_size = 0;

static WA_recursiveLock WOShmem_mutex;

#define offset_to_addr(offset) ((void *)(WOShmem_base_address + offset))
#define addr_to_offset(addr) ((void *)addr - WOShmem_base_address)

#ifndef MAP_FILE
#define MAP_FILE 0
#endif

#ifndef MAP_INHERIT
#define MAP_INHERIT 0
#endif

typedef union _LockInfo {
   struct flock flockInfo;
   union _LockInfo *cache;
} LockInfo;

static LockInfo *WOShmem_lockInfoCache = NULL;
   
/*
 * Writes len bytes of zeros at the end of the open file fd.
 * Returns the number of bytes actually written. If this is
 * less than len, an error occurred (which was logged).
 * In general the size written may be larger than what was requested.
 */
static int append_zeros(int fd, int len)
{
   /* write zeros to initialize */
   int buff[1024], result, ret = 0;
   char *errMsg = NULL;

   memset(buff, 0, sizeof(buff));
   while (len > 0 && !errMsg)
   {
      if (lseek(WOShmem_fd, 0, SEEK_END) == -1)
      {
         errMsg = WA_errorDescription(WA_error());
         WOLog(WO_ERR, "append_zeros: lseek() failed: %s", errMsg);
      }
      if (!errMsg)
      {
         result = write(WOShmem_fd, buff, sizeof(buff));
         if (result == -1)
         {
            errMsg = WA_errorDescription(WA_error());
            WOLog(WO_ERR, "append_zeros: write() failed: %s", errMsg);
         } else {
            len -= result;
            ret += result;
         }
      }
   }
   if (errMsg)
      WA_freeErrorDescription(errMsg);
   return ret;
}

/*
 * This function is used to lock a section of a file using fcntl file locking.
 * Returns zero if the lock was aquired, nonzero if it could not be aquired.
 * The provided lockInfo struct is initialized and passed to fcntl().
 * fcntl() can fail with EAGAIN, so this function retries as needed.
 * The lock will be quickly retried RETRY_SLEEP_COUNT times. If it has still not been
 * aquired then the retry loop will then pause between attempts. If we have not aquired the
 * lock after RETRY_FAIL_COUNT attempts, give up.
 */
#define LOCK_RETRY_SLEEP_COUNT 10
#define LOCK_RETRY_FAIL_COUNT 50
inline static int lock_file_section(int fd, off_t start, off_t len, struct flock *lockInfo, int exclusive)
{
   int err, errCount = 0;
   do {
      lockInfo->l_start = start;
      lockInfo->l_len = len;
      lockInfo->l_type = exclusive ? F_WRLCK : F_RDLCK;
      lockInfo->l_whence = SEEK_SET;
      if (fcntl(WOShmem_fd, F_SETLKW, lockInfo) == -1)
      {
         err = WA_error();
         errCount++;
         if (err != EAGAIN || errCount % 10 == 0)
         {
            char *errMsg = WA_errorDescription(err);
            WOLog(WO_ERR, "lock_file_section(): failed to lock (%d attempts): %s", errCount, errMsg);
            WA_freeErrorDescription(errMsg);
         }
         if (err == EAGAIN)
         {
            WA_yield();
            if (errCount > LOCK_RETRY_SLEEP_COUNT)
               sleep(1);
            if (errCount > LOCK_RETRY_FAIL_COUNT)
            {
               WOLog(WO_ERR, "lock_file_section(): could not aquire lock after %d attempts. Giving up.", LOCK_RETRY_FAIL_COUNT);
            }
         }
      } else
         err = 0;
   } while (err == EAGAIN && errCount < LOCK_RETRY_FAIL_COUNT);
   return err;
}

/*
 * Ensure that the open file fd is at least size bytes long.
 * Expands the file as needed using append_zeros(). File locking
 * is used to ensure no data is overwritten.
 * Returns the actual size of the file on success, -1 on error.
 */
static int ensure_file_size(int fd, size_t size)
{
   struct stat st;
   struct flock lockInfo;
   char *errMsg = NULL;
   int error = 0;

   if (fstat(WOShmem_fd, &st))
   {
      errMsg = WA_errorDescription(WA_error());
      WOLog(WO_ERR, "ensure_file_size(): initial fstat() failed: %s", errMsg);
      error = 1;
   }
   if (!error && st.st_size < size)
   {
      if (lock_file_section(WOShmem_fd, st.st_size, size - st.st_size, &lockInfo, 1))
      {
         error = 1;
      } else {
         /* someone else may have just changed the size, so repeat the stat */
         if (fstat(WOShmem_fd, &st))
         {
            errMsg = WA_errorDescription(WA_error());
            WOLog(WO_ERR, "ensure_file_size(): second fstat() failed: %s", errMsg);
            WA_freeErrorDescription(errMsg);
            error = 1;
         }
         if (!error && st.st_size < size)
         {
            st.st_size += append_zeros(WOShmem_fd, size - st.st_size);
            if (st.st_size < size)
               error = 1;
         }
         lockInfo.l_type = F_UNLCK;
         if (fcntl(WOShmem_fd, F_SETLK, &lockInfo) == -1)
         {
            errMsg = WA_errorDescription(WA_error());
            WOLog(WO_ERR, "ensure_file_size(): failed to unlock: %s", errMsg);
            WA_freeErrorDescription(errMsg);
            error = 1;
         }
      }
   }
   return error ? -1 : st.st_size;
}


int WOShmem_init(const char *file, size_t memsize)
{
   int error = 0;
   char *errMsg = NULL;

   /* open up the file */
   WOShmem_fd = open(file, O_RDWR|O_CREAT, 0600);
   if (WOShmem_fd > 0)
   {
#ifdef APACHE
      /* unlink the file so when Apache exits the file is removed */
      /* The mmap should still be in effect */
      unlink(file);
#endif
      WOShmem_size = ensure_file_size(WOShmem_fd, memsize);
      if (WOShmem_size != -1)
      {
         WOShmem_base_address = (void *)mmap(0, WOShmem_size, PROT_READ|PROT_WRITE, MAP_FILE|MAP_SHARED|MAP_INHERIT, WOShmem_fd, 0);
         if (WOShmem_base_address == MAP_FAILED)
         {
            errMsg = WA_errorDescription(WA_error());
            WOLog(WO_ERR, "WOShmem_init(): couldn't map file: %s", errMsg);
            error = 1;
         }
      } else
         error = 1;
   } else {
      errMsg = WA_errorDescription(WA_error());
      WOLog(WO_ERR,"WOShmem_init(): Couldn't open %s: %s", file, errMsg);
      error = 1;
   }
   if (error)
      WOShmem_fd = -1;
   if (errMsg)
      WA_freeErrorDescription(errMsg);

   WOShmem_mutex = WA_createLock("WOShmem_lock");
   return WOShmem_fd == -1;
}



void *WOShmem_alloc(const char *regionName, size_t elementSize, unsigned int *elementCount)
{
   Region *r, *newRegion;
   int found, nameLen;
   void *lockHandle, *ret = NULL;

   if (WOShmem_fd == -1)
   {
      /* There was some problem mapping the shared file. Fall back to using private storage so the adaptor still works. */
      WOLog(WO_ERR, "WOShmem_alloc(): shared memory disabled - mallocing instead (%s)", regionName);
      return WOCALLOC(*elementCount, elementSize);
   }

   /* Scan the region list looking for regionName. */
   /* Note that since the file was initialized with zeros, the first lookup will not */
   /* find any region, and will create the first region at offset 0. */
   r = (Region *)offset_to_addr(0);
   found = 0;
   do {
      lockHandle = WOShmem_lock(r, sizeof(Region), 0);
      if (strcmp(regionName, r->name) == 0)
      {
         found = 1;
         /* validate the element size */
         if (r->elementSize == elementSize)
         {
            *elementCount = r->elementCount;
            ret = offset_to_addr(r->offset);
            WOLog(WO_INFO, "WOShmem_alloc(): found region \"%s\" (%d x %d)", regionName, elementSize, *elementCount);
         } else {
            WOLog(WO_ERR, "WOShmem_alloc(): size mismatch in region %s: %d vs %d", regionName, r->elementSize, elementSize);
         }
      } else {
         if (r->nextRegion == 0)
         {
            /* r points to the last defined region, which is still locked shared. Need an exclusive lock. */
            WOShmem_unlock(lockHandle);
            lockHandle = WOShmem_lock(r, sizeof(Region), 1);
            /* Be sure that nobody else got in ahead of our exclusive lock */
            /* If they did, just resume the loop to check the region that somebody else just created. */
            if (r->nextRegion == 0)
            {
               /* now we can allocate a new region after the last existing one */
               /* first check that there is enough room for the desired size */
               nameLen = strlen(regionName);
               /* keep things aligned to 16 byte boundary by padding out the name */
               if ((sizeof(Region)+nameLen) % 16)
                  nameLen += 16 - (sizeof(Region)+nameLen) % 16;
               if (r->offset + r->elementSize * r->elementCount + sizeof(Region) + nameLen + elementSize * *elementCount < WOShmem_size)
               {
                  /* enough room; create the new region */
                  r->nextRegion = r->offset + r->elementSize * r->elementCount;
                  newRegion = (Region *)offset_to_addr(r->nextRegion);
                  newRegion->offset = r->nextRegion + sizeof(Region) + nameLen;
                  newRegion->elementSize = elementSize;
                  newRegion->elementCount = *elementCount;
                  newRegion->nextRegion = 0;
                  strcpy(&newRegion->name[0], regionName);
                  ret = offset_to_addr(newRegion->offset);
                  WOLog(WO_INFO, "WOShmem_alloc(): allocated region \"%s\" (%d x %d)", regionName, elementSize, *elementCount);
               } else {
                  /* not enough room for new region */
                  WOLog(WO_ERR, "WOShmem_alloc(): not enough shared memory to allocate region \"%s\" (%d x %d)", regionName, elementSize, *elementCount);
               }
               found = 1;
            }
         }
         r = (Region *)offset_to_addr(r->nextRegion);
      }
      WOShmem_unlock(lockHandle);
   } while (!found);
   return ret;
}

/*
 * Obtain a lock on a chunk of shared memory. The range of memory
 * begins add address addr, and is size bytes in length. If exclusive
 * is zero the lock is a shared lock for read only access to the region.
 * If exclusive is nonzero the lock is an exclusive lock for write
 * access to the region.
 * The return value is a handle which must be supplied to the WOShmem_unlock
 * function when the lock is released. If some error occurrs and
 * a lock could not be obtained, NULL is returned.
 */
void *WOShmem_lock(const void *addr, size_t size, int exclusive)
{
   struct flock *lockInfo;
   ptrdiff_t offset;
   LockInfo *info = NULL;

   if (addr && WOShmem_fd != -1)
   {
      offset = addr_to_offset(addr);
      if (offset >= 0 && offset + size < WOShmem_size)
      {
         /* This gets called a lot, so as an optimization to avoid the malloc() overhead */
         /* we keep a cache of LockInfo's. */
         WA_lock(WOShmem_mutex);
         info = WOShmem_lockInfoCache;
         if (info)
            WOShmem_lockInfoCache = info->cache;
         WA_unlock(WOShmem_mutex);

         /* if there wasn't one in the cache, malloc a new one */
         if (!info)
            info = WOMALLOC(sizeof(LockInfo));
         
         if (info)
         {
            lockInfo = &info->flockInfo;
            if (lock_file_section(WOShmem_fd, offset, size, lockInfo, exclusive))
            {
               /* failed; put the info struct back on the cache */
               WA_lock(WOShmem_mutex);
               info->cache = WOShmem_lockInfoCache;
               WOShmem_lockInfoCache = info;
               WA_unlock(WOShmem_mutex);
               info = NULL;
            }
         }
      }
   }
   return info;
}
   

/*
 * Release a lock obtained by WOShmem_lock. handle is the value returned
 * by WOShmem_lock.
 */
void WOShmem_unlock(void *handle)
{
   if (handle)
   {
      LockInfo *info = (LockInfo *)handle;
      struct flock *lockInfo = &info->flockInfo;
      lockInfo->l_type = F_UNLCK;
      if (fcntl(WOShmem_fd, F_SETLK, lockInfo) == -1)
      {
         char *errMsg = WA_errorDescription(WA_error());
         WOLog(WO_ERR,"WOShmem_unlock(): failed to unlock %d bytes at 0x%x: %s", lockInfo->l_len, lockInfo->l_start, errMsg);
         WA_freeErrorDescription(errMsg);
         /* how should we recover? */
      }
      /* put the info struct back on the cache */
      WA_lock(WOShmem_mutex);
      info->cache = WOShmem_lockInfoCache;
      WOShmem_lockInfoCache = info;
      WA_unlock(WOShmem_mutex);
   }
}

#endif


/* Shared array functions */

void *sha_localDataForKey(ShmemArray *array, unsigned int elementNumber, const char *key)
{
   void *data = NULL;

   if (elementNumber < array->elementCount)
   {
      if (array->elements[elementNumber].localData)
         data = sd_valueFor(array->elements[elementNumber].localData, key);
   }
   return data;
}

void *sha_setLocalDataForKey(ShmemArray *array, unsigned int elementNumber, const char *key, void *data, sha_clearLocalDataCallback clearCallback)
{
   void *oldValue = NULL;
   if (elementNumber < array->elementCount)
   {
      if (!array->elements[elementNumber].localData)
         array->elements[elementNumber].localData = sd_new(1);
      oldValue = sd_add(array->elements[elementNumber].localData, key, data);
      if (clearCallback != NULL)
      {
         if (!array->elements[elementNumber].localDataCleanupCallbacks)
            array->elements[elementNumber].localDataCleanupCallbacks = wolist_new(1);
         if (wolist_indexOf(array->elements[elementNumber].localDataCleanupCallbacks, (void *)clearCallback) == wolist_elementNotFound)
            wolist_add(array->elements[elementNumber].localDataCleanupCallbacks, (void *)clearCallback);
      }
   }
   return oldValue;
}

static void sha_warnAboutLeftoverLocalData(const char *key, void *value, void *userdata)
{
   WOLog(WO_ERR, "sha_clearLocalData(): value for key \"%s\" not cleared - just leaking.", key);
}

void sha_clearLocalData(ShmemArray *array, unsigned int elementNumber)
{
   int i;
   if (elementNumber < array->elementCount)
   {
      if (array->elements[elementNumber].localData)
      {
         if (array->elements[elementNumber].localDataCleanupCallbacks != NULL)
         {
            for (i=wolist_count(array->elements[elementNumber].localDataCleanupCallbacks)-1; i>=0; i--)
            {
               sha_clearLocalDataCallback clear_func = (sha_clearLocalDataCallback) wolist_elementAt(array->elements[elementNumber].localDataCleanupCallbacks, i);
               if (clear_func)
                  clear_func(array, elementNumber);
               wolist_removeAt(array->elements[elementNumber].localDataCleanupCallbacks, i);
            }
            wolist_dealloc(array->elements[elementNumber].localDataCleanupCallbacks);
            array->elements[elementNumber].localDataCleanupCallbacks = NULL;
         }
         sd_perform(array->elements[elementNumber].localData, sha_warnAboutLeftoverLocalData, 0);
         sd_free(array->elements[elementNumber].localData);
         array->elements[elementNumber].localData = NULL;
      }
   }
}

ShmemArray *sha_alloc(const char *name, void *arrayBase, size_t elementSize, unsigned int elementCount)
{
   ShmemArray *array;
   int i;

   array = WOMALLOC(sizeof(ShmemArray) + sizeof(ShmemArrayElement) * (elementCount-1));
   if (array)
   {
      array->name = WOSTRDUP(name);
      array->elementSize = elementSize;
      array->elementCount = elementCount;
      for (i=0; i<array->elementCount; i++)
      {
         array->elements[i].element = (void *)(arrayBase + elementSize * i);
         array->elements[i].lock = WA_createLock("array element lock");
         array->elements[i].writeLock = WA_createLock("array element write lock");
         array->elements[i].lockCount = 0;
         array->elements[i].lockHandle = NULL;
         array->elements[i].localData = NULL;
         array->elements[i].localDataCleanupCallbacks = NULL;
      }
   }
   return array;
}

void *sha_checkout(ShmemArray *array, unsigned int elementNumber)
{
   void *element;
   if (elementNumber < array->elementCount)
   {
      /* read locks block if a write is pending */
#ifdef EXTRA_DEBUGGING_LOGS
       WOLog(WO_DBG, "sha_checkout(): about to check out %s element %d", array->name, elementNumber);
#endif
      WA_lock(array->elements[elementNumber].writeLock);
      WA_lock(array->elements[elementNumber].lock);
      WA_unlock(array->elements[elementNumber].writeLock);
      element = array->elements[elementNumber].element;
      /* if this was the first read lock by this process, obtain a file lock on the data as well */
      if (array->elements[elementNumber].lockCount == 0)
         array->elements[elementNumber].lockHandle = WOShmem_lock(element, array->elementSize, 0);
      array->elements[elementNumber].lockCount++;
      WA_unlock(array->elements[elementNumber].lock);
#ifdef EXTRA_DEBUGGING_LOGS
       WOLog(WO_DBG, "sha_checkout(): checked out %s element %d", array->name, elementNumber);
#endif
   } else {
      element = NULL;
      WOLog(WO_ERR, "sha_checkout(): failed to check out %s element %d", array->name, elementNumber);
   }

   return element;
}

void sha_checkin(ShmemArray *array, unsigned int elementNumber)
{
   if (elementNumber < array->elementCount)
   {
#ifdef EXTRA_DEBUGGING_LOGS
      WOLog(WO_DBG, "sha_checkin(): about to check in %s element %d", array->name, elementNumber);
#endif
      WA_lock(array->elements[elementNumber].lock);
      array->elements[elementNumber].lockCount--;
      /* if this was the last read lock by this process, release the file lock on the data as well */
      if (array->elements[elementNumber].lockCount == 0)
      {
         WOShmem_unlock(array->elements[elementNumber].lockHandle);
         array->elements[elementNumber].lockHandle = NULL;
      }
      WA_unlock(array->elements[elementNumber].lock);
#ifdef EXTRA_DEBUGGING_LOGS
      WOLog(WO_DBG, "sha_checkin(): checked in %s element %d", array->name, elementNumber);
#endif
   }
}

void *sha_lock(ShmemArray *array, unsigned int elementNumber)
{
   void *element;
   if (elementNumber < array->elementCount)
   {
#ifdef EXTRA_DEBUGGING_LOGS
      WOLog(WO_DBG, "sha_lock(): about to lock %s element %d", array->name, elementNumber);
#endif
      /* block read lock requests */
      WA_lock(array->elements[elementNumber].writeLock);
      WA_lock(array->elements[elementNumber].lock);
      while (array->elements[elementNumber].lockCount > 0)
      {
         WA_unlock(array->elements[elementNumber].lock);
         WA_yield();
         WA_lock(array->elements[elementNumber].lock);
      }
      element = array->elements[elementNumber].element;
      array->elements[elementNumber].lockHandle = WOShmem_lock(element, array->elementSize, 1);
      WA_unlock(array->elements[elementNumber].lock);
#ifdef EXTRA_DEBUGGING_LOGS
      WOLog(WO_DBG, "sha_lock(): locked %s element %d", array->name, elementNumber);
#endif
   } else {
      element = NULL;
      WOLog(WO_ERR, "sha_lock(): failed to lock %s element %d", array->name, elementNumber);
   }
   return element;
}

void sha_unlock(ShmemArray *array, unsigned int elementNumber)
{
   if (elementNumber < array->elementCount)
   {
#ifdef EXTRA_DEBUGGING_LOGS
      WOLog(WO_DBG, "sha_unlock(): about to unlock %s element %d", array->name, elementNumber);
#endif
      WA_lock(array->elements[elementNumber].lock);
      /* release the file lock on the data */
      WOShmem_unlock(array->elements[elementNumber].lockHandle);
      array->elements[elementNumber].lockHandle = NULL;
      WA_unlock(array->elements[elementNumber].lock);
      WA_unlock(array->elements[elementNumber].writeLock);
#ifdef EXTRA_DEBUGGING_LOGS
      WOLog(WO_DBG, "sha_unlock(): unlocked %s element %d", array->name, elementNumber);
#endif
   }
}

int shmem_do_tests()
{
   int res = 0;

   if(res == 0 && sizeof(void *) == 8)
   {
      long long testaddr  = 0x111122227fffffff;
      long long testaddr2 = 0x1111222280000003;
      ShmemArray *mem = sha_alloc("Test", (void *)testaddr, 4, 2);

      printf("Testing against 64 bit address handling (long long=%d, long=%d, int=%d, void *=%d)\n", (int)sizeof(long long), (int)sizeof(long), (int)sizeof(int), (int)sizeof(void *));

      if(mem->elements[1].element != (void *)testaddr2)
      {
         printf("Test error in shmem, Test 1 ");
         res = 1;
      }
   }

   if(res == 0)
      printf(" => OK");
   return 0;
}

