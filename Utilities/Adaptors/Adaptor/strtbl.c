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
 *	A simple key/value list where all entries are strings
 *
 *	Best suited for applications where the number of elements is
 *	low and lookups are infrequent.
 *
 */
#include "config.h"
#include "strtbl.h"
#include "womalloc.h"
#include "log.h"
#include "wastring.h"

#include <string.h>
#include <ctype.h>
#include <stdlib.h>


/* Minimum capacity of a string table. Anything less will be rounded up. */
#define MIN_CAPACITY 8

/*
 * Attempt to expand the capacity of the string table.
 * If the allocation fails, the string table is unchanged.
 */
static void st_setCapacity(strtbl *st, unsigned newsize)
{
   strtblelem *newHead;
   if (newsize < MIN_CAPACITY)
      newsize = MIN_CAPACITY;
   if (st->head != NULL)
      newHead = WOREALLOC(st->head, newsize * sizeof(strtblelem));
   else
      newHead = WOMALLOC(newsize * sizeof(strtblelem));
   if (newHead)
   {
      st->head = newHead;
      st->capacity = newsize;
   } else
      WOLog(WO_ERR, "st_setCapacity(): failed to expand capacity (%d)", newsize);
   return;
}


strtbl	*st_new(int hint)
{
   strtbl *st = WOMALLOC(sizeof(strtbl));
   if (st)
   {
      memset(st, 0, sizeof(strtbl));
      st_setCapacity(st, hint);
   }
   return st;
}


void st_free(strtbl *st)
{
   int i;
   strtblelem *el;

   for (i=0, el=st->head; i < st->count; i++, el++)
   {
      if (el->key != NULL) {
         if (el->key && (el->flags & STR_FREEKEY))
            WOFREE((void *)el->key);
         if (el->value && (el->flags & STR_FREEVALUE))
            WOFREE((void *)el->value);
      }
   }
   if (st->head)
      WOFREE(st->head);
   WOFREE(st);
   return;
}

/*
 * Return a pointer to the next available element.
 * Returns NULL if none is available and could not expand capacity.
 */
static strtblelem *st_newKV(strtbl *st)
{
   int i;

   for (i = st->firstNull; i < st->count; i++)
      if (st->head[i].key == NULL)
         break;

   if (i == st->capacity)
   {
      /* no free elements; allocate more room */
      st_setCapacity(st, st->capacity * 2);
   }
   return i < st->capacity ? &st->head[i] : NULL;
}


void st_add(strtbl *st, const char *key, const char *value, int flags)
{
   strtblelem *kv;

   kv = st_newKV(st);
   if (kv != NULL)
   {
      st->count++;
      kv->flags = flags;
      if (flags & STR_COPYKEY)
      {
         kv->key = WOSTRDUP(key);
         kv->flags |= STR_FREEKEY;
      } else
         kv->key = key;
      if (flags & STR_COPYVALUE)
      {
         kv->value = WOSTRDUP(value);
         kv->flags |= STR_FREEVALUE;
      } else
         kv->value = value;
   } else
      WOLog(WO_ERR, "st_add(): no space to add element: %s = %s", key, value);
    return;
}


inline
static int st_findKey(strtbl *st, const char *key)
{
   int index;
   strtblelem *el;

   for (index=0, el=st->head; index < st->count; index++, el++)
      if (el->key && strcasecmp(el->key, key) == 0)
         return index;
   return -1;
}


void st_setValueForKey(strtbl *st, const char *key, const char *value, int flags)
{
   int index;
   strtblelem *el;

   index = st_findKey(st, key);
   if (index == -1)
   {
      st_add(st, key, value, flags);
   } else {
      el = &st->head[index];
      if (el->flags & STR_FREEVALUE)
         WOFREE((void *)el->value);
      /* keep the old key flags, but take the new value flags */
      el->flags = (el->flags & (STR_COPYKEY|STR_FREEKEY)) | (flags & (STR_COPYVALUE | STR_FREEVALUE));
      if (flags & STR_COPYVALUE)
      {
         el->value = WOSTRDUP(value);
         el->flags |= STR_FREEVALUE;
      } else
         el->value = value;
   }
   return;
}

/*
 *	we don't compact the table, rather we just nullify the entry
 */
void st_removeKey(strtbl *st, const char *key)
{
   int index;
   strtblelem *el;

   index = st_findKey(st, key);
   if (index != -1) {
      el = &st->head[index];
      if (el->flags & STR_FREEKEY)
         WOFREE((void *)el->key);
      if (el->flags & STR_FREEVALUE)
         WOFREE((void *)el->value);
      *el = STRTBL_NULLELEM;	/* clear the element */
      if (st->firstNull > index)
         st->firstNull = index;
      st->count--;
   }
   return;
}

const char *st_valueFor(strtbl *st, const char *key)
{
   int index;

   index = st_findKey(st, key);
   return (index != -1) ? st->head[index].value : NULL;
}

void st_perform(strtbl *st, st_perform_callback callback, void *userdata)
{
   int i;
   strtblelem *el;

   for (i=0, el=st->head; i < st->count; el++)
   {
      if (el->key != NULL)
      {
         i++;
         callback(el->key, el->value, userdata);
      }
   }
   return;
}


static void st_description_callback(const char *key, const char *value, void *userdata)
{
   String *str = (String *)userdata;

   str_append(str, key);
   str_appendLiteral(str, " = ");
   str_append(str, value);
   str_appendLiteral(str, "\n");
}

char *st_description(strtbl *st)
{
   String *str;
   char *desc;

   str = str_create("String table:\n", 0);
   if (str)
   {
      st_perform(st, st_description_callback, str);
      str->text[str->length-1] = 0; /* strip of the extra newline */
      desc = str_unwrap(str);
   } else
      desc = WOSTRDUP("empty string table");
   return desc;
}

/*
 *	psuedo plist support:
 *	  all we really do is look for something in the form of
 *		'{' [ string '=' string ;]* '}'
 *		where 'string' can be quoted or unquoted.
 *
 *	we do all required bounds checking, but offer little in the way
 *	of diagnostics if the input is unacceptable.
 *
 */

#define LEFTCURLY	'{'
#define	RIGHTCURLY	'}'
#define	QUOTE	'"'
#define	EQUAL	'='
#define	SEMI	';'
#define	MAXSTR	256

#define	ATEND(STR)	(isspace((int)*STR) || (*STR == EQUAL) || (*STR == SEMI) || (*STR == RIGHTCURLY))

inline
static const char *_getstr(const char *s, char *str)
{
   int count = MAXSTR - 1;
   while (*s && isspace((int)*s))	s++;
   if (*s == QUOTE) {
      while (*s && (*++s != QUOTE))
         if (count--)
            *str++ = *s;
      if (*s && (*s == QUOTE))	s++;		/* skip over the quote */
   } else while (*s &&  !ATEND(s))
      if (count--)	/* insure buffer doesn't overrun */
         *str++ = *s++;
      else
         s++;
   *str = '\0';
   return s;
}

strtbl *st_newWithString(const char *s)
{
   strtbl *st = NULL;
   char key[MAXSTR], value[MAXSTR];

   if ((s == NULL) || (*s != LEFTCURLY))
      return NULL;
   /*
    *	repeat over key = value pairs
    */
   s++;
   while (*s != RIGHTCURLY) {
      s = _getstr(s, key);
      while (*s && isspace((int)*s))	s++;
      if (*s == EQUAL)
         s = _getstr(s+1, value);

      if (*key && *value) {
         if (st == NULL)
            st = st_new(0);
         st_add(st, key, value, STR_COPYKEY|STR_COPYVALUE);
      }
      while (*s && (isspace((int)*s) || (*s == SEMI)))	s++;
   }

   return st;
}

