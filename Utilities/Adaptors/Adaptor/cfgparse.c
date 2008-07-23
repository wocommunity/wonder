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

#ifdef USE_WO_CONF_PARSER
/*
 This parser is deprecated. This code is included only for use by customers
 which depend on the older config file format and cannot use xml.

 This parser is no longer maintained, or tested. It may not be working as is.
 */

/*
 *	This is the parser for the historical WebObjects.conf files.  The
 *	file format is
 *		appname:instance_number@hostname port [{ key=value; ...}]
 *
 *	The additional key/value property list is an embellishment to allow
 *	some of the newer adaptor features.  Any property added is specific to
 *	the instance on which they occur, though.
 *
 */
#include "config.h"
#include "appcfg.h"
#include "log.h"
#include "hostlookup.h"
#include "transport.h"
#include "loadbalancing.h"
#include "womalloc.h"
#include "list.h"

#include <string.h>
#include <ctype.h>
#include <stdlib.h>

/*
 *	here's where we hook in to the adaptor's config handlers
 */
static int conf_parseConfiguration(char *buf, int len);

static const char * const document_types[] = {"conf", "text/plain", NULL};
const WebObjects_config_handler WebObjects_conf_parse  = {
   document_types, conf_parseConfiguration };



/*
 * parse: <appname>:[-]<instance_number>[@<hostname>] <port_number> [{stuff}]
 * This builds up a single dictionary with all the settings in it.
 */
static strtbl *split(char *str)
{
   strtbl *opts;
   char *name, *instanceNumber, *hostname, *port;

   while (*str && isspace(*str))
      str++;
   if (*str)
      name = str;				/* start of app */
   else
      return NULL;

   while (*str && (*str != ':'))
      str++;
   if (*str == ':')  {
      *str++ = '\0';
      while (*str && isspace(*str))
         str++;
      instanceNumber = str;			/* start of instance number */
   } else
      return NULL;

   while (*str && isdigit(*str))
      str++;
   if (*str == '@') {
      *str++ = '\0';
      hostname = str;				/* start of host name */
      while (*str && !isspace(*str))
         str++;
      *str++ = '\0';
   } else
      return NULL;

   while (str && *str && isspace(*str))
      str++;
   if (*str && isdigit(*str)) {
      port = str;				/* start of port number */
      while (*str && isdigit(*str))
         str++;
      *str++ = '\0';
   } else
      return NULL;

   while (str && *str && isspace(*str))	str++;
   if (*str && (*str == '{')) {
      opts = st_newWithString(str);		/* start of options */
   } else
      opts = st_new(0);
   st_add(opts, WOAPPNAME, name, 0);
   st_add(opts, WOINSTANCENUMBER, instanceNumber, 0);
   st_add(opts, WOHOST, hostname, 0);
   st_add(opts, WOPORT, port, 0);
   return opts;
}



/*
 * scan one line worth of text
 * removes line continuations ( "\" ) on the fly, compacting the buffer in place
 * changes the first \n found to a 0, and returns the number of characters
 * processed in buffer, which may be larger than the string appearing at startPos
 * when we are done. The returned number should be used as startPos on the next
 * iteration.
 */
int prepareLine(char *buffer, int bufferSize, int startPos)
{
   int i, current, done;

   current = startPos;
   done = 0;
   i = startPos;
   while (i<startPos && !done)
   {
      switch (buffer[i])
      {
         case '\n':
            buffer[i] = 0;
            done = 1;
            break;
         case '\\':
            break;
         default:
            if (current != i)
               buffer[current] = buffer[i];
            current++;
            break;
      }
      i++;
   }
   return i;
}


static int conf_parseConfiguration(char *buf, int len)
{
   char *line;
   strtbl *opts;
   list *l;
   int start = 0, end;

   /* note: buf came from file_config(), so we know there is a null byte at buf[len] */
   l = list_new(1);
   while (start < len) {
      end = prepareLine(buf, len, start);
      line = buf+start;
      if (line[0] == '\\')
      {
         WOLog(WO_ERR, "Bad configuration: found \\ at end of file");
         return 1;
      }
      start = end;
      if (line[0] == '#' || line[0] == 0)
         continue;			/* allow for comments, null lines */

      if ((opts = split(line)) != NULL) {
         WOLog(WO_ERR, "Invalid entry in configuration: (%s)", line);
      } else {
         list_add(l, opts);
         ac_updateApplication(opts, l);
         list_removeAt(l, 0);
         st_free(opts);
      }
   }
   list_dealloc(l);
   return 0;
}
#endif
