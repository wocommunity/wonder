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
#ifndef HOSTLOOKUP_H_INCLUDED
#define HOSTLOOKUP_H_INCLUDED
/*
 *	Wrap gethostbyname() & cache the result to solve some reentrancy issues.
 *
 */

#if defined(WIN32)
#include <winsock.h>
#else
#include <netdb.h>
#endif

extern char *this_host; /* the name of the machine running the adaptor */

typedef	struct hostent *hostent_t;
struct _strtbl;

/*
 *      Initialize this module. Returns nonzero if an error occurrs.
 */
int hl_init(struct _strtbl *options);

/*
 *	wrap gethostbyname(), gethostbyaddress() since there are 
 *	some subtle reentrancy issues
 *
 *	This call is safe *only* in the case of the CGI adaptor since
 *	it returns a malloc'd hostent
 */
hostent_t hostlookup(const char *hostname);

/*
 *	cached hostents.  
 *
 *	there is no mutex used on these, I assume it's being called from the
 *	read_config routines which are serialized already.  If you're calling
 *	from a function which is not mutex'd, use one.
 *
 *
 */ 
void	hl_flushhosts();			/* clear cache */
/*
 *	looks in cache, if not found, use gethostbyname() & add to cache.
 */
hostent_t hl_find(const char *hostname);

/* See if this is the real host name of localhost */
int compareToLocalhost(char *name);

/* Compare the names addresses of two hosts */
int compareHostNames(char *name1, char *name2);


#endif
