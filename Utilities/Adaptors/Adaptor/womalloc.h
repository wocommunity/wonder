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
 *	malloc() cover routines for finding leaks in the adaptor
 */
#ifndef WOMALLOC_H_INCLUDED
#define WOMALLOC_H_INCLUDED
#include <stdlib.h>
#include <string.h>

/* #define FINDLEAKS 1 	*/		/* turn on to debug leaks */

#if	!defined(FINDLEAKS)

#define WOMALLOCINIT()
#define	WOMALLOC(SZ)	malloc(SZ)
#define	WOCALLOC(CT,SZ)	calloc(CT,SZ)
#define	WOREALLOC(PTR,SZ)	realloc(PTR,SZ)
#define WOSTRDUP(S)		strdup(S)
#define	WOFREE(PTR)		free(PTR)

#else

/*
 *	these leave a trail of mallocs & frees so we can tally them
 *	up at the end of a request to find out what's lingering...
 */

void womallocinit();
void *womalloc(const char *srcfile, int line, size_t sz);
void *wocalloc(const char *srcfile, int line, size_t ct, size_t sz);
void *worealloc(const char *srcfile, int line, void *ptr, size_t sz);
void wofree(const char *srcfile, int line, void *ptr);
char *wostrdup(const char *srcfile, int line, const char *s1);

#define WOMALLOCINIT() womallocinit()
#define	WOMALLOC(SZ)	womalloc(__FILE__, __LINE__, SZ)
#define	WOCALLOC(CT,SZ)	wocalloc(__FILE__, __LINE__, CT,SZ)
#define	WOREALLOC(PTR,SZ)	worealloc(__FILE__, __LINE__, PTR,SZ)
#define WOSTRDUP(str)		wostrdup(__FILE__,__LINE__,str)
#define	WOFREE(PTR)		wofree(__FILE__, __LINE__, PTR)

/*
 *	to be called before exit() to show what was malloc()d but not free()d
 */
void showleaks();

#endif

#endif /* WOMALLOC_H_INCLUDED */
