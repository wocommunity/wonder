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
/*
 *	Some gethostby- name & address return a shared buffer and are not reentrant.
 *	Since it's called from several points in this code, wrap up the gnarly bits
 *	here.
 *
 *	There's no point using some mutex since we can't guarantee that some
 *	other bit of code that we have no control over doesn't have it's own
 *	scheme...
 *
 *	If the host supports it, use the reentrant versions.
 *
 */
#if defined(SOLARIS) || defined(HPUX)
#define NEEDS_HSTRERR
#define HAS_REENTRANT_GETHOSTENT
#endif

#if defined(HAS_REENTRANT_GETHOSTENT) && !defined(_REENTRANT)
#define _REENTRANT		/* must be defined for proper structs */
#endif

#include "config.h"
#include "hostlookup.h"
#include "transport.h"
#include "log.h"
#include "strdict.h"
#include "strtbl.h"
#include "womalloc.h"


#include <sys/types.h>
#if !defined(WIN32)
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <sys/param.h>
#endif
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>


/*
 *	cached hostent stuff
 */
static strdict *hosts = NULL;

char *this_host; /* the name of the machine running the adaptor */

#ifndef MAXHOSTNAMELEN
#define MAXHOSTNAMELEN 256
#endif

int hl_init(struct _strtbl *options)
{
   char host[MAXHOSTNAMELEN];
   int len;

   /* figure out the host name */
   if (gethostname(host, sizeof(host)))
   {
      char *errMsg = WA_errorDescription(WA_error());
      WOLog(WO_ERR, "Failed to get local host name: %s", errMsg);
      WA_freeErrorDescription(errMsg);
      this_host = "";
   } else {
      len = strlen(host);
      this_host = WOMALLOC(len + 1);
      strcpy(this_host, host);
   }

   hosts = sd_new(16);
   return 0;
}

void	hl_flushhosts()
{
   if (hosts != NULL) {
      sd_free(hosts);
   }
   hosts = sd_new(16);
   return;
}

hostent_t hl_find(const char *name)
{
   hostent_t host;

   if (name == NULL)
      name = "localhost";

   host = (hosts) ? (hostent_t)sd_valueFor(hosts,name) : NULL;
   if (host != NULL)
      return host;

   /*
    *	not found - look it up & stash it
    */
   host = hostlookup(name);	/* finds & copies hostent into malloc'd area */

   if (host != NULL) {
      sd_add(hosts, name, host);
      WOLog(WO_INFO,"Caching hostent for %s",name);
   }

   return host;
}


/*
 *	Solaris & HP-UX use different definitions for the _r() versions.
 */

#define ROUND_UP(n, m)  (((size_t)(n) + (m) - 1) & ~((m) - 1))


#if	defined(NEEDS_HSTRERR)
#ifndef	NETDB_SUCCESS
#define	NETDB_SUCCESS 0
#endif
const char *hstrerror(int herr)
{
   if (herr == -1)				/* see errno */
      return strerror(errno);
   else if (herr == HOST_NOT_FOUND)
      return "Host not found";
   else if (herr == TRY_AGAIN)
      return "Try again";				/* ? */
   else if (herr == NO_RECOVERY)
      return "Non recoverable error";
   else if (herr == NO_DATA)
      return "No data";
   else if (herr == NO_ADDRESS)
      return "No address";			/* same as no data */
   else if (herr == NETDB_SUCCESS)
      return "No error";				/* strange */
   else
      return "unknown error";
}
#endif

/*
 *	This is bizarre, but I guess necessary.  We need to cache a copy of the
 *	hostent outside of the library static area or (in the case of reentrant
                                                  *	versions) off the stack.
 */
static hostent_t copyhostent(hostent_t host)
{
   size_t len;
   intptr_t alias_ct = 0, addr_ct = 0;
   char **l, **a;
   hostent_t h;
   void *m;

   /*
    *	get enough space for struct + data
    */
   len = ROUND_UP(sizeof(struct hostent), sizeof(void *));
   len += strlen(host->h_name) + 1;			/* str space for host name */
   /* str space + ptr space for each alias */
   for (l=host->h_aliases; l && (*l != NULL); l++) {
      len += strlen(*l) + 1;
      alias_ct++;
      WOLog(WO_DBG, "alias: %s", *l);
   }
   len = ROUND_UP(len, sizeof(char *));
   len += (alias_ct+1) * sizeof(char *);		/* NULL terminated list */

   for (l=host->h_addr_list; l && (*l != NULL); l++) {
      /* addr space + ptr space */
      addr_ct++;
   }
   /*
    *	add pointer and address space for addr_list
    */
   len += addr_ct * (sizeof(char *) + host->h_length + 1);

   m = WOMALLOC(ROUND_UP(len,sizeof(char *)));
   /*
    *	lay out the memory:
    *		- struct,
    *		- ptr lists
    *		- stuff that the ptr lists point to
    *
    *	placing the struct first allows it to be the argument to free()
    *	to get rid of the whole thing
    */
   h = m;
   h->h_addrtype = host->h_addrtype;
   h->h_length = host->h_length;

   m += ROUND_UP(sizeof(struct hostent), sizeof(void *));
   h->h_aliases = (char **)m;
   m += (alias_ct+1) * sizeof(char *);
   h->h_addr_list = (char **)m;
   m += (addr_ct+1) * sizeof(char *);

   h->h_name = m;
   strcpy(h->h_name, host->h_name);
   m += strlen(h->h_name)+1;

   /*
    *	copy the host aliases
    */
   for (l=host->h_aliases, a=h->h_aliases; l && (*l != NULL); l++, a++) {
      *a = (char *)m;
      strcpy((char *)m, *l);
      m += strlen(*l) + 1;
   }
   *a = NULL;

   m = (void *)ROUND_UP(m, h->h_length);
   for (l=host->h_addr_list, a=h->h_addr_list; l && (*l != NULL); l++, a++) {
      *a = (char *)m;
      memcpy(*a, *l, h->h_length);
      m += h->h_length;
   }
   *a = NULL;

   return h;
}

/*
 *	do the proper kind of lookup (by name or by address) using the
 *	proper library (reentrant or traditional) then copy the hostent
 *	and accompanying data to a malloc'd buffer
 */
hostent_t hostlookup(const char *name)
{
   struct hostent *host, __host;
   struct in_addr hostaddr;
   char *addrlist[2] = {0,0};
   int error = 0;
#if	defined(HAS_REENTRANT_GETHOSTENT)
#if defined(SOLARIS)
#define	BUFLEN	4096
   char the_hostent[BUFLEN];
#else /* hpux or other */
   /*	struct hostent_data the_hostent;  why does this fail? */
   char the_hostent[4096];		/* cross fingers */
#endif
#endif

   if (name == NULL)
      name = "localhost";

   if (isdigit((int)*name) && ((hostaddr.s_addr = inet_addr(name)) != -1)) {
      /*
       *	dotted notation, we have all the info we need...
       */
      __host.h_name = (char *)name;
      __host.h_addr_list = addrlist;
      __host.h_addr = (char *)&hostaddr;
      __host.h_length = sizeof(struct in_addr);
      __host.h_addrtype = AF_INET;
      __host.h_aliases = NULL;
      host = &__host;
   } else {
      /*
       *	look it up the typical way
       */
#if	defined(HAS_REENTRANT_GETHOSTENT)
#if defined(SOLARIS)
      host = gethostbyname_r(name, &__host, the_hostent, BUFLEN, &error);
#endif
#if defined(HPUX)
      host = gethostbyname(name);
#endif /* _r differences */
#else
      host = gethostbyname(name);
#if defined(HPUX) && defined(NSAPI)
      /* Netscape3.6 cannot load a shared library linked against libc.2 on HP11 it requires libc.1, which does not contain h_errno. So put -1 instead. It is for logging purpose only anyway. */
      error = (host) ? 0 : -1;
#else
      error = (host) ? 0 : h_errno;
#endif
#endif
   }

   if (host == NULL) {
      WOLog(WO_WARN, "gethostbyname(%s) returns no host: %s",
            name,	 hstrerror(error));
      return NULL;
   }

   if (host->h_addrtype != AF_INET) {
      WOLog(WO_ERR, "Wrong address type in hostptr for host %s",name);
   }

   return (host) ? copyhostent(host) : NULL;
}

