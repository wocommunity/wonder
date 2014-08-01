/*

Copyright © 2000-2007 Apple, Inc. All Rights Reserved.

The contents of this file constitute Original Code as defined in and are
subject to the Apple Public Source License Version 1.1 (the 'License').
You may not use this file except in compliance with the License. 
Please obtain a copy of the License at http://www.apple.com/publicsource 
and read it before using this file.

This Original Code and all software distributed under the License are
distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT. 
Please see the License for the specific language governing rights 
and limitations under the License.


*/

#ifndef SHMEM_H
#define SHMEM_H

#include <stddef.h>

/*
 * This module contains functions for managing a chunk of shared memory.
 * The memory is mapped into each running adaptor, and is backed by a
 * file in the filesystem. (See the documentation for mmap().)
 *
 * This implementation provides support for naming regions of shared memory.
 * The shared memory region has special usage semantics. Each
 * allocation has both a size and a name. When an allocation is attempted
 * a search is performed for the name. If it is found, that region is
 * returned, possibly with a size that was different from what was
 * requested. If there has never been a region with that particular
 * name allocated, it will be created with the requested size if
 * there is enough room.
 *
 * This module also provides functions for implementing an array of
 * generic elements in a region of shared memory, including both
 * shared and exclusive locking functions.
 */


/*
 * Initialize shared memory using the given file. The file must
 * be able to be opened for both reading and writing. Returns 0 on
 * success, nonzero on error. If a nonzero value is returned, shared
 * memory access is disabled and all allocations will fall back to
 * using WOMALLOC().
 * If the file does not exist, it will be created. memsize specifies
 * a minimum file size. The file will be expanded to be at least this
 * size by appending zeros.
 */
int WOShmem_init(const char *file, size_t memsize);

/*
 * Allocate or look up a chunk of shared memory.
 * regionName specifies the name of the region to locate.
 * If a region named regionName is found, and it's element
 * size is elementSize, a pointer to the region is returned
 * and the number of elements actually present in the region
 * is returned in *elementCount. If a region named regionName
 * is found and the element size differs, NULL is returned.
 * If there is no existing region named regionName, a new
 * region is created with that name, its element size is
 * set to elementSize, and enough storage is allocated to
 * hold *elementCount elements. The new region is returned,
 * or if there is no room to create the region NULL is returned.
 * Once a region is created there is no way to destroy/deallocate it
 * short of deleting the file which backs the shared memory and
 * restarting all processes which use the memory.
 * The returned pointer will not change for the lifetime of the process,
 * so it may be kept to avoid repeating the region lookup.
 */
void *WOShmem_alloc(const char *regionName, size_t elementSize, unsigned int *elementCount);

/*
 * Obtain a lock on a chunk of shared memory. The range of memory
 * begins at address addr, and is size bytes in length. If exclusive
 * is zero the lock is a shared lock for read only access to the region.
 * If exclusive is nonzero the lock is an exclusive lock for write
 * access to the region.
 * The return value is a handle which must be supplied to the WOShmem_unlock
 * function when the lock is released. If some error occurrs and
 * a lock could not be obtained, NULL is returned.
 */
void *WOShmem_lock(const void *addr, size_t size, int exclusive);

/*
 * Release a lock obtained by WOShmem_lock. handle is the value returned
 * by WOShmem_lock.
 */
void WOShmem_unlock(void *handle);

/* None of the above are implemented on windows */
#ifdef WIN32
#ifndef DISABLE_SHARED_MEMORY
#define DISABLE_SHARED_MEMORY
#endif
#endif

#ifdef DISABLE_SHARED_MEMORY
/* If shared memory is not enabled, these calls all degrade to simply using calloc. */
#define WOShmem_init(file, size) 0
#define WOShmem_alloc(name, size, count)  WOCALLOC(*(count), size)
/* Note: can't use NULL here because NULL represents a failure value. */
#define WOShmem_lock(addr, size, exclusive) ((void *)1)
#define WOShmem_unlock(handle)
#endif


/*
 * Functions to implement an array in shared memory.
 * These are implemented on top of the WOShmem_* functions, and
 * still work if shared memory is disabled. In this case they
 * simply provide an in process per-element locking mechanism
 * for an arbitrary array.
 */

struct _strdict;
struct _list;

typedef struct {
   void *element;								/* pointer to the element in shared memory */
   WA_recursiveLock lock;						/* lock for this structure */
   WA_recursiveLock writeLock;					/* write lock for the element */
   int lockCount;								/* how many read locks */
   void *lockHandle;							/* handle for the shared mem lock */
   struct _strdict *localData;					/* dictionary to store local data associated with the element */
   struct _list *localDataCleanupCallbacks;		/* callbacks to clear out local data */
} ShmemArrayElement;

struct _ShmemArray {
   const char *name;					/* name of the array, for debugging logs */
   size_t elementSize;					/* the size of an element */
   unsigned int elementCount;			/* number of elements */
   ShmemArrayElement elements[1];		/* array of element descriptors */
};

typedef struct _ShmemArray ShmemArray;

/*
 * This macro translates an array element number into a pointer.
 * The array bounds are checked, and NULL is returned if element is out of range.
 */
#define elementPointer(array, elementNum) ((unsigned)elementNum < array->elementCount ? array->elements[elementNum].element : NULL)

/*
 * Allocate a shared memory array. arrayBsae is the address of the start of the array data
 * in shared memory (probably returned from WOShmem_alloc()), elementSize is the size of an
 * element in the array, and elementCount is the number of elements in the array.
 * name is a human readable name for the array used only for debugging logs, and is stored
 * by reference (not copied).
 * Returns a new array, or NULL the array could not be constructed.
 */
ShmemArray *sha_alloc(const char *name, void *arrayBase, size_t elementSize, unsigned int elementCount);

/*
 * Obtain a read lock on a particular array element. A read lock prevents the element data
 * from being changed, but does not prevent other threads/processes from accessing the data.
 * Returns a pointer to the checked out element, or NULL if elementNumber is out of range
 * or an error occurrs.
 * A read lock is a recursive lock, so a single thread can invoke sha_checkout()
 * multiple times without blocking. (sha_checkin() should be called the same number of times.)
 */
void *sha_checkout(ShmemArray *array, unsigned int elementNumber);

/*
 * Release a read lock on a particular array element.
 */
void sha_checkin(ShmemArray *array, unsigned int elementNumber);


/*
 * Obtain a write lock on a particular array element. A write lock provides exclusive access
 * to the element data. A write lock cannot be obtained until all read locks have
 * been released, and read lock attempts will block a write lock is either pending or held.
 * Returns a pointer to the checked out element, or NULL if elementNumber is out of range
 * or an error occurrs.
 * A write lock is not recursive. If the same thread calls sha_lock() twice
 * on the same element (without calling sha_unlock() in between) it will deadlock.
 */
void *sha_lock(ShmemArray *array, unsigned int elementNumber);

/*
 * Release a write lock on a particular array element.
 */
void sha_unlock(ShmemArray *array, unsigned int elementNumber);

/*
 * The shared array contains support for associating data in the local process with each
 * element in the shared array. The data is stored in a dictionary of key/value pairs.
 * Below are functions for setting, fetching, and clearing the local data.
 */

/*
 * Any module which registers local data must provide a callback to free the data.
 * This is the callback type. See sha_clearLocalData() for more.
 * The array and element number are provided to inform the callback which element's data
 * should be freed. This function is not thread safe and must be externally synchronized.
 */
typedef void (*sha_clearLocalDataCallback)(ShmemArray *array, unsigned int elementNumber);

/*
 * Retrieve local data for elementNumber in array associated with the key.
 * Returns the retrieved pointer or NULL if no local data has been set.
 * This function is not thread safe and must be externally synchronized.
 */
void *sha_localDataForKey(ShmemArray *array, unsigned int elementNumber, const char *key);

/*
 * Store a local data pointer in elementNumber's local dictionary. array is the array containing
 * the element, key is the dictionary key, data is the pointer to store, and clearCallback is
 * the callback to invoke to free data.
 * This function is not thread safe and must be externally synchronized.
 * Returns the previously set value for key, or NULL if there was none.
 */
void *sha_setLocalDataForKey(ShmemArray *array, unsigned int elementNumber, const char *key, void *data, sha_clearLocalDataCallback clearCallback);

/*
 * This function invokes all the stored callback functions to clear the local data associated with
 * elementNumber in array. Each distinct callback is invoked only once, regardless of how many
 * times it was passed to sha_setLocalDataForKey(). Each callback function should free whatever
 * local data it was associated with in sha_setLocalDataForKey().
 * This function is not thread safe and must be externally synchronized.
 */
void sha_clearLocalData(ShmemArray *array, unsigned int elementNumber);

/*
  perform automated tests
  returns 0 on success
  returns 1 on failure
  prints to stdout
*/
int shmem_do_tests();

#endif /* SHMEM_H */

 
