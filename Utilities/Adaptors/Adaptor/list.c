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
#include "config.h"
#include "list.h"
#include "womalloc.h"
#include "log.h"

#include <stdlib.h>
/*
 *	Simple unordered pointer list.
 */

#define DEFAULT_SIZE	8

list *wolist_new(int hint) {
   list *l = WOCALLOC(1,sizeof(list));
   if (l && hint)	wolist_setCapacity(l,hint);
   return l;
}

void wolist_dealloc(list *l) {
   if (l->head)
      WOFREE(l->head);
   WOFREE(l);
}


int wolist_add(list *l, void *new_member) {
   if (l->count == l->capacity)
      wolist_setCapacity(l, (l->capacity) ? l->capacity * 2 : DEFAULT_SIZE);
   if (l->count < l->capacity) /* might fail to expand capacity */
   {
      l->head[l->count] = new_member;
      l->count++;
      return 0;
   }
   return 1;
}

void wolist_removeAt(list *l, int index) {
   if (index < l->count && index >= 0) {
      l->count--;
      for (; index < l->count; index++)
         l->head[index] = l->head[index+1];
   } else
      WOLog(WO_ERR, "wolist_removeAt(): attempted to remove out of range index: %d (count = %d)", index, l->count);
}

void *wolist_remove(list *l, void *member) {
   int i;
   i = wolist_indexOf(l, member);
   if (i != wolist_elementNotFound) {
      wolist_removeAt(l, i);
      return member;
   }
   return NULL;		/* not found */
}

int wolist_indexOf(list *l, void *member) {
   int i;
   for (i=0; (i < l->count); i++)
      if (l->head[i] == member) {
         return i;
      }
         return wolist_elementNotFound;		/* not found */
}


void wolist_setCapacity(list *l, int size) {
   if (size < l->capacity)
      return;
   if (size < DEFAULT_SIZE)
      size = DEFAULT_SIZE;
   if (l->head)
   {
      void *tmp;
      tmp = WOREALLOC(l->head,size * sizeof(void *));
      if (tmp)
      {
         l->head = tmp;
         l->capacity = size;
      }
   } else {
      l->head = WOCALLOC(size, sizeof(void *));
      if (l->head)
         l->capacity = size;
   }
}

void wolist_sort(list *l,int (*compare)(const void *, const void *)) {
   if (l->count > 1)
      qsort(l->head, l->count, sizeof(void *), compare);
}

void *wolist_bsearch(list *l,const void *key, int (*compare)(const void *, const void *))
{
   void **element;
   if (l->count > 0) {
      element = bsearch(key, l->head, l->count, sizeof(void *), compare);
      return (element != NULL) ? *element : NULL;
   } else
      return NULL;
}

