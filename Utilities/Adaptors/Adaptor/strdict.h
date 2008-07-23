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
#ifndef	STRDICT_H_INCLUDED
#define	STRDICT_H_INCLUDED
/*
 *	Simple string based dictionary.  This differs from the
 *	strtbl in the lookup, keys are hashed for making 
 *	frequent lookups efficient.
 *
 *	Regarding memory ownership:
 *	- strings passed in as keys are strdup'd, the allocated memory will
 *	  be freed during sd_free().
 *	- the memory referenced by the value pointer is not copied, but will
 *	  be freed during sd_free().
 *
 */

typedef struct _strdictel {
	char *key;
	void *value;
} strdictel;

typedef struct _strdict {
	unsigned short count, capacity;
	strdictel *head;
} strdict;

#define	STRDICT_INITIALIZER	((strdict){0,0,NULL})

strdict	*sd_new(int hint);

void sd_free(strdict *sd);	/* will free the 'value' elements */

void *sd_add(strdict *sd, const char *key, void *value); /* returns the old value */

void *sd_removeKey(strdict *sd, const char *key);

void *sd_valueFor(strdict *sd, const char *key);

/*
 *	iterate over the members and call back.  userdata is passed as 
 *	a convenience.  
 */
void sd_perform(strdict *sd, void (*callback)(const char *key, void *value, void *userdata), void *userdata);

/*
 *	for debugging, returns a malloc'd string description 
 */
char *sd_description(strdict *sd);

#endif
