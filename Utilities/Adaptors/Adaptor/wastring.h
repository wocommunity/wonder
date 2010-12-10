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
#ifndef	string_included
#define	string_included

#include <stdarg.h>

/*
 *	A simple growable string.
 *      The string buffers are cached and reused to reduce malloc overhead.
 */

typedef struct _String {
   unsigned int bufferSize;	/* the size of the buffer pointed to by data */
   unsigned int length;		/* the length of the string */
   struct _String *next;	/* used to chain several Strings together */
   char *text;			/* the string data, null terminated */
   char cached;			/* true if this is a free String which is in (or being added to) the cache */
} String;


/*
 *     Initialize this module. Returns nonzero if initialization fails.
 */
int str_init();

/*
 *     Create a new String. If initialText is not NULL it is copied into the String.
 *     If sizeHint is nonzero, a buffer of at least that size is allocated immediately.
 */
String *str_create(const char *initialText, int sizeHint);

/*
 *     Free a String. Frees the entire list of strings starting at s.
 */
void str_free(String *s);

/*
 *     Disassociates the String struct from the text buffer.
 *     The text buffer is returned, and the String struct is
 *     freed per str_free().
 *     Returns the text buffer, or NULL if no buffer was allocated.
 *     The caller must free the text buffer.
 */
char *str_unwrap(String *s);

/*
 *     Append length characters at str to the String.
 *     Returns nonzero if memory could not be allocated, in which case
 *     the String is unchanged.
 */
int str_appendLength(String *s, const char * const str, int length);

/*
 *     Appending a literal this way avoids an extra strlen().
 */
#define str_appendLiteral(str, text) str_appendLength(str, text, sizeof(text)-1)

/*
 *     Append the null terminated string str.
 *     Returns nonzero if memory could not be allocated, in which case
 *     the String is unchanged.
 */
int str_append(String *s, const char * const str);

/*
 *     Append a string in the manner of sprintf. This function
 *     examines format to take a best guess at the required length
 *     of the result, allocates enough storage, and uses sprintf to
 *     append the formatted text.
 *     This currently only supports %s and %d conversion specifiers,
 *     with no flags or precision specifiers.
 */
int str_appendf(String *s, const char *format, ...);
int str_vappendf(String *s, const char *format, va_list args);

/*
 *      Truncates the string at the given length.
 *      If length is greater than the length of the string then
 *      the string is not modified.
 */
void str_truncate(String *s, unsigned int length);

/*
 *	Ensure that the capacity of the string buffer is at lease newMinCapacity.
 *	If memory allocation fails the capacity remains unchanged. In any case,
 *	the contents of the buffer are unchanged.
 *	Returns nonzero if the capacity requirement was met, zero if the allocation
 *	failed.
 */
int str_ensureCapacity(String *s, int newMinCapacity);

#endif
