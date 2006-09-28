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
#include "wastring.h"
#include "womalloc.h"
#include "log.h"
#include <stdio.h>
#include <string.h>

/* define a minimum size */
#define MINIMUM_CAPACITY 64

/* The buffer doubles up to this size, then increases linearly by this amount */
#define MAX_QUANTUM_TO_ADD 8192

/*
 * The String structures (and their associated buffers) get cached for reuse.
 * This is an attempt to reduce the overhead of malloc/free because Strings get
 * used often. The cache is implemented as a simple linked list of free String
 * structures. The variable 'cache' points to the head of the list.
 */

/* Synchronizes access to the cache. */
static WA_recursiveLock str_lock = NULL;

/* The head of the String cache */
static String *cache = NULL;

/* If a string buffer is larger than this, don't keep the buffer. */
#define MAX_BUFFER_SIZE_TO_CACHE 256

int str_init()
{
   str_lock = WA_createLock("str_lock");
   return str_lock == NULL;
}

int str_ensureCapacity(String *s, int newMinCapacity)
{
   int size;
   char *newBuffer;

   //WOLog(WO_DBG, "str_ensureCapacity() called, capacity = %d, newMinCapacity = %d", s->bufferSize, newMinCapacity);
   if (newMinCapacity < s->bufferSize)		/* never let the buffer shrink */
      return 1;
   if (newMinCapacity < MINIMUM_CAPACITY)	/* always get at least MINIMUM_CAPACITY bytes */
      newMinCapacity = MINIMUM_CAPACITY;

   /* Determine the new buffer size. */
   /* Double the buffer size up to a limit, then increase linearly. */
   size = s->bufferSize < MINIMUM_CAPACITY ? MINIMUM_CAPACITY : s->bufferSize;
   while (size < newMinCapacity)
      size += size < MAX_QUANTUM_TO_ADD ? size : MAX_QUANTUM_TO_ADD;
   newBuffer = WOMALLOC(size);
   if (newBuffer)
   {
      if (s->length > 0)
         memcpy(newBuffer, s->text, s->length);
      newBuffer[s->length] = 0;
      if (s->text)
          WOFREE(s->text);
      s->text = newBuffer;
      s->bufferSize = size;
   }
   return s->bufferSize >= newMinCapacity;
}

String *str_create(const char *initialText, int sizeHint)
{
   String *s;

   /* attempt to grab one out of the cache */
   WA_lock(str_lock);
   if (cache)
   {
      s = cache;
      cache = cache->next;
   } else
      s = NULL;
   WA_unlock(str_lock);

   /* if there wan't one in the cache, allocate a new one */
   if (s == NULL)
   {
      //WOLog(WO_DBG, "str_create(): allocating string ");
      /* note that this structure will be reused indefinitely and never gets freed */
      s = (String *)WOMALLOC(sizeof(String));
      s->bufferSize = 0;
      s->text = NULL;
   }

   if (s)
   {
      s->length = 0;
      if (s->bufferSize > 0)
         s->text[0] = 0;
      s->next = NULL;
      s->cached = 0;
      if (sizeHint && sizeHint > s->bufferSize)
         str_ensureCapacity(s, sizeHint);
      if (initialText)
      {
         if (str_append(s, initialText))
         {
            str_free(s);
            s = NULL;
         }
      }
   }
   return s;
}
   

void str_free(String *s)
{
   String *str;
   if (s)
   {
      str = s;
      do {
         str->cached = 1;
         if (str->bufferSize > MAX_BUFFER_SIZE_TO_CACHE)
         {
            //WOLog(WO_DBG, "str_free(): freeing string buffer (size = %d)", str->bufferSize);
            WOFREE(str->text);
            str->bufferSize = 0;
            str->text = NULL;
         }
         if (!str->next || (str->next && str->next->cached))
         {
            /* got to the end of the list; chain to the cache */
            WA_lock(str_lock);
            str->next = cache;
         }
         str = str->next;
      } while (str != cache);
      cache = s;
      WA_unlock(str_lock);
   }
}

char *str_unwrap(String *s)
{
   char *text;

   text = s->text;
   s->text = NULL;
   s->bufferSize = 0;
   str_free(s);
   return text;
}

int str_appendLength(String *s, const char * const str, int length)
{
   int requiredBufferSize;

   requiredBufferSize = s->length + length + 1;
   if (requiredBufferSize > s->bufferSize)
      if (str_ensureCapacity(s, requiredBufferSize) == 0)
         return 1;
   memcpy(&s->text[s->length], str, length);
   s->length = s->length + length;
   s->text[s->length] = 0;
   return 0;
}

int str_append(String *s, const char * const str)
{
   int newTextLength;

   newTextLength = strlen(str);
   return str_appendLength(s, str, newTextLength);
}

int str_vappendf(String *s, const char *format, va_list args)
{
   int i, len, requiredBufferSize;
   va_list sizer;
   va_copy(sizer, args);

   /* figure out how much space we will need */
   len=1; /* start len at 1 to include the terminator */
   for (i=0; format[i]; i++)
   {
      if (format[i] == '%')
      {
         i++;
         switch (format[i])
         {
            case 's':
               len += strlen(va_arg(sizer, char *));
               break;
            case 'd':
            case 'x':
               len += 11; /* assume 10 digits + sign for 32 bit number */
               (void)va_arg(sizer, char *);
               break;
            default:
               len += 1024; /* hope 1k is big enough */
               (void)va_arg(sizer, int);
         }
      } else
         len++;
   }
   va_end(sizer);
   requiredBufferSize = s->length + len + 1;
   if (requiredBufferSize > s->bufferSize)
      if (str_ensureCapacity(s, requiredBufferSize) == 0)
         return 1;
   s->length += vsprintf(&s->text[s->length], format, args);
   s->text[s->length] = 0;
   return 0;
}

int str_appendf(String *s, const char *format, ...)
{
   va_list args;
   int ret;

   /* figure out how much space we will need */
   va_start(args, format);
   ret = str_vappendf(s, format, args);
   va_end(args);
   return ret;
}

void str_truncate(String *s, unsigned int length)
{
   if (length < s->length)
   {
      s->length = length;
      s->text[length] = 0;
   }
}
