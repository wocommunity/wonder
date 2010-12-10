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
#include "config.h"
#include "strdict.h"
#include "womalloc.h"
#include "log.h"
#include "wastring.h"

#include <string.h>
#include <stdlib.h>

strdict	*sd_new(int hint)
{
   strdict *sd = WOMALLOC(sizeof(strdict));
   sd->capacity = (hint != 0) ? hint : 8;
   sd->head = WOMALLOC(sizeof(strdictel) * sd->capacity);
   sd->count = 0;
   return sd;
}

static void freeKVPair(const char *key,void *value, void *zip)
{
   if (key)	WOFREE((char *)key);
   if (value)	WOFREE((char *)value);
   return;
}

void sd_free(strdict *sd)
{
   sd_perform(sd, freeKVPair, NULL);		/* ditch the members */
   if (sd->head)	WOFREE(sd->head);
   WOFREE(sd);
   return;
}

void sd_setCapacity(strdict *sd, unsigned newsize)
{
   strdictel *newElements;
   if (sd->head != NULL)
      newElements = WOREALLOC(sd->head, newsize * sizeof(strdictel));
   else
      newElements = WOMALLOC(newsize * sizeof(strdictel));
   if (newElements)
   {
      sd->head = newElements;
      sd->capacity = newsize;
   }
   return;
}

inline
static strdictel *findNull(strdict *sd)
{
   int i;
   strdictel *el;

   for (i=0, el=sd->head; i < sd->count; i++, el++)
      if (el->key == NULL)
         return el;
   return NULL;
}

void *sd_add(strdict *sd, const char *key, void *value)
{
   strdictel *kv;
   void *oldValue;

   oldValue = sd_removeKey(sd, key);
   if (value != NULL)
   {
      if (sd->count == sd->capacity) {
         kv = findNull(sd);			/* try to recycle space */
         if (kv == NULL) {
            sd_setCapacity(sd, sd->capacity * 2);	/* make more space */
            if (sd->count == sd->capacity)
            {
               WOLog(WO_ERR, "sd_add(): could not add element (%s = %s) due to allocation failure", key, value);
               return oldValue;
            } else {
               kv = sd->head + sd->count;
               sd->count++;
            }
         }
      } else {
         kv = sd->head + sd->count;
         sd->count++;
      }
      kv->key = WOSTRDUP(key);
      kv->value = value;
   }
   return oldValue;
}

inline
static strdictel *sd_findKey(strdict *sd, const char *key)
{
   int index;
   strdictel *el;

   for (index=0, el=sd->head; index < sd->count; index++, el++)
      if (el->key && strcasecmp(el->key, key) == 0)
         return el;
   return NULL;
}

/*
 *	we don't compact the table, rather we just nullify the entry
 *	we may reclaim the space if capacity is exceeded.
 */
void *sd_removeKey(strdict *sd, const char *key)
{
   strdictel *el;
   void *value = NULL;

   el = sd_findKey(sd, key);
   if (el)
   {
      WOFREE(el->key);		/* free the key string */
      el->key = NULL;
      value = el->value;
      el->value = NULL;
   }
   return value;
}

void *sd_valueFor(strdict *sd, const char *key)
{
   strdictel *el;

   el = sd_findKey(sd, key);
   return (el != NULL) ? el->value : NULL;
}

void sd_perform(strdict *sd, void (*callback)(const char *key, void *value, void *userdata), void *userdata)
{
   int i;
   strdictel *el;

   for (i=0, el=sd->head; i < sd->count; i++, el++)
      if (el->key != NULL)
         callback(el->key, el->value, userdata);
   return;
}

static void sd_description_callback(const char *key, void *value, void *userdata)
{
   String *str = (String *)userdata;

   str_append(str, key);
   str_appendLiteral(str, " = 0x");
   str_appendf(str, "%x", value);
   str_appendLiteral(str, "\n");
}

char *sd_description(strdict *sd)
{
   String *str;
   char *desc;

   str = str_create("String table:\n", 0);
   if (str)
   {
      sd_perform(sd, sd_description_callback, str);
      str->text[str->length-1] = 0; /* strip of the extra newline */
      desc = str_unwrap(str);
   } else
      desc = WOSTRDUP("empty string table");
   return desc;
}

