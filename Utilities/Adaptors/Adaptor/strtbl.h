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
#ifndef	STRTBL_H_INCLUDED
#define	STRTBL_H_INCLUDED
/*
 *	A simple key/value list where all entries are strings.  This
 *	is intended for lists which are created frequently and whose
 *	elements are not looked up often (e.g. HTTP headers).  FOr
 *	lists that are created infrequently and are looked up heavily
 *	see strdict.
 */

/*
 * Flags that can appear in strtblelem->flags or st_add().
 * Note that specifying STR_COPYxxx implies STR_FREExxxx.
 */
/* Set if the key should be copied, clear if it should just be referenced */
#define STR_COPYKEY	1
/* Set if the value should be copied, clear if it should just be referenced */
#define STR_COPYVALUE	2
/* Set if the key should be freed when the list element is freed */
#define STR_FREEKEY	4
/* Set if the value should be freed when the list element is freed */
#define STR_FREEVALUE	8

typedef	struct _strtblelem {
   const char *key, *value;
   int flags;
} strtblelem;

typedef struct _strtbl {
   unsigned int count;
   unsigned int capacity;
   unsigned int firstNull;
   strtblelem *head;
} strtbl;

#define	STRTBL_INITIALIZER	((strtbl){0,0,NULL})
#define	STRTBL_NULLELEM		((strtblelem){NULL,NULL,0})

#define st_count(table) (((strtbl *)table)->count)
/*
 * Create a new string table. The initial capacity is set to hint.
 * Returns the new table, or NULL if the allocation fails.
 */
strtbl	*st_new(int hint);

/*
 * Free a string table. Frees all elements as well.
 */
void st_free(strtbl *st);


/*
 *	Primitive for adding key/value pairs to the table.
 *   Either or both or none of the key and value may be copied or added by reference.
 *   Also, either or both or none of the key and value buffers may be marked to be freed.
 *   This does not check whether key is already in the table.
 */
void st_add(strtbl *st, const char *key, const char *value, int flags);

/*
 *   If key is not already in the table, this simply calls st_add. If key is in the
 *   table, the previous value is discarded (and freed if so marked), and the new
 *   value is set, copying it if STR_COPYVALUE is set in flags. If STR_FREEVALUE
 *   is set in flags, the new value will be marked to be freed.
 */
void st_setValueForKey(strtbl *st, const char *key, const char *value, int flags);


/*
 *   Remove the key and it's definition from the table. They are freed if so marked.
 */
void st_removeKey(strtbl *st, const char *key);

/*
 *   Look up the key in the string table and return the value. If the key is not
 *   present in the table, returns NULL.
 */
const char *st_valueFor(strtbl *st, const char *key);

/*
 *	Iterate over the members and call back.  userdata is passed as
 *	a convenience.  This is the only "safe" way to iterate, since
 *	removeKey may not compact the list, leaving emtpy elements.
 */
typedef void (*st_perform_callback)(const char *key, const char *value, void *userdata);
void st_perform(strtbl *st, st_perform_callback callback, void *userdata);


/*
 *	psuedo plist support:
 *
 */
strtbl *st_newWithString(const char *s);

/*
 *	for debugging, returns a malloc'd string description
 */
char *st_description(strtbl *st);


#endif
