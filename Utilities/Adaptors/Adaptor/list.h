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
#ifndef	list_included
#define	list_included
/*
 *	A simple pointer list. Elements are stored in an array which grows
 *      as needed. When elements are removed, the array is compacted.
 */

typedef struct _list {
	unsigned short count, capacity;
	void **head;
} list;

#define	LIST_INITIALIZER	{0,0,NULL}

#define	wolist_count(LIST)	(LIST->count)

/*
 *	create new list with capacity = hint
 */
list *wolist_new(int hint);	

/*
 *	does not free members
 */
void wolist_dealloc(list *list);


/*
 *      Adds new_member as the last element in the list, and returns zero on success.
 *      If the list is full and more space could not be allocated, new_member is not
 *      added to the list and the returned value is nonzero.
 */
int wolist_add(list *l, void *new_member);

/*
 *      Removes the first occurrance of member from the list.
 *      If member is in the list, member is returned. If member is not in the list, NULL is returned.
 */
void *wolist_remove(list *l, void *member);

/*
 *      Removes the element at index from the list.
 *      Does nothing if index is larger than the list capacity.
 */
void wolist_removeAt(list *l, int index);

/*
 *      Allocate space such that the list capacity is at least size.
 *      If the memory allocation fails, the capacity is unchanged.
 *      The list size never decreases.
 */
void wolist_setCapacity(list *l, int size);

/*
 *      Sort the list using qsort().
 */
void wolist_sort(list *l,int (*compare)(const void *, const void *));

/*
 *      Traverses the list searching for member. If member is found, its position
 *      in the list is returned. If member is not found, wolist_elementNotFound is returned.
 */
#define	wolist_elementNotFound -1
int wolist_indexOf(list *l, void *member);

/*
 *	Search the list for key using bsearch(). Assumes list is sorted.
 *      Returns the element found, or NULL if an element matching key is not found.
 */
void *wolist_bsearch(list *l,const void *key, int (*compare)(const void *, const void *));

#define wolist_elementAt(l,index) ((index < (l)->count) ? (l)->head[index] : NULL)

#endif
