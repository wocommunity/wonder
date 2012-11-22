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
#include "config.h"
#include "appcfg.h"
#include "log.h"
#include "hostlookup.h"
#include "transport.h"
#include "loadbalancing.h"
#include "list.h"
#include "womalloc.h"

#include "xmlcdocument.h"
#include "xmlcparser.h"

#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <sys/types.h>
#ifdef WIN32
#ifndef _MSC_VER // SWK old // SWK old WO4.5 headerfile
#if !defined(MINGW)
#include <winnt-pdo.h>
#endif
#endif
#ifndef strncasecmp
#define strncasecmp _strnicmp
#endif
#endif

/*
 *	Parse an xml description of applications and instances.
 *
 *      The basic strategy here is to parse the xml attribute/values into dictionaries.
 *      Then, if the parser completes successfully the new values are pushed into the
 *      active configuration. If parsing fails, no changes are made. Unknown tags and
 *      attributes are logged and ignored.
 */



/*
 * This structure is used to store the configuration info as it is parsed.
 * It contains lists of dictionaries for WO Applications and instances, and
 * convenience pointers to what is currently being parsed.
 */
typedef struct _WOXMLEdits {
   /* 
    * The element currently being parsed. The callback from the xml parser just
    * stuffs the key/value pair into this dictionary. It could be adaptor, app,
    * or instance settings depending on what is currently being parsed.
    */
   strtbl *current_element;

   /* settings dictionary for the WOApp currently being parsed */
   strtbl *current_app;

   /* settings dictionary for the WOInstance currently being parsed */
   strtbl *current_instance;

   /* list of settings dictionaries for instances of current_app */
   /* current_instance is the last element in this list */
   list *current_app_instances;

   /* list of WOApp settings dictionaries */
   /* current_app is the last element in this list */
   list *new_apps;

   /* This is a list of lists of dictionaries. */
   /* new_app_instances[n] is the list of settings dictionaries for new_apps[n] */
   /* current_app_instances is the last element in this list */
   list *new_app_instances;
   
   unsigned char error;		/* error indicator */
   const char *errorLocation;		/* pointer to text "near" the error */
} WOXMLEdits;




/*
 * Prototypes for callbacks used by the xml parser.
 */
static void createElement(XMLCDocument *document, const XMLCCharacter *name, const unsigned int length);
static void createAttribute(XMLCDocument *document, const XMLCCharacter *name, const unsigned int nameLength, const XMLCCharacter *value, const unsigned int valueLength);
static void endElementNamed(XMLCDocument *document, const XMLCCharacter *name, const unsigned int length);
static void xmlParserPlaceholder(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length);

/* Other prototypes */
static void freeWOXMLEdits(WOXMLEdits *config);



/*
 * This structure is used to pass the callbacks to the xml parser. 
 */
static const XMLCDocumentHandler _document = {
   NULL,
   createElement,
   createAttribute,
   endElementNamed,
   xmlParserPlaceholder,
   xmlParserPlaceholder,
   xmlParserPlaceholder,
   xmlParserPlaceholder,
   xmlParserPlaceholder
};




/*
 *	here's where we hook in to the adaptor's config handlers
 */
static int xml_parseConfiguration(char *buf, int len);

static const char * const document_types[] = {
   "xml",
   "text/xml",
/* This parser does not really handle text/html, but that is the type wotaskd returns for its config. */
   "text/html",
   NULL};

const WebObjects_config_handler WebObjects_xml_parse  = { document_types, xml_parseConfiguration };




/*
 *	The entry point for the parser.
 *      Returns nonzero if there was an error during parsing.
 */
static int xml_parseConfiguration(char *buf, int len)
{
   XMLCDocumentHandler handler;
   WOXMLEdits config;
   XMLCParser *parser;
   int error = 0, i;

   /* initialize the config struct */
   config.current_element = NULL;
   config.new_apps = wolist_new(16);
   config.current_app = NULL;
   config.current_app_instances = NULL;
   config.current_instance = NULL;
   config.new_app_instances =  wolist_new(16);
   config.error = 0;
   config.errorLocation = buf;
   
   if (len == 0)
      return 1; 		/* no content is considered an error */
   
   /* Set up a new document handler struct for the parser */
   memcpy(&handler, &_document, sizeof(XMLCDocumentHandler));
   handler.document = (XMLCDocument *)(&config);

   /* set up and invoke the xml parser */
   parser = xmlcParserInit();
   xmlcTokenizerSetBuffer(parser->tokenizer, buf, len);
   xmlcParserSetPreserveWhiteSpace(parser, 0);
   error = (int)xmlcParserParse(parser, &handler);

   if (error != 0) {
      /* config error */
      WOLog(WO_ERR,"Error parsing configuration: %s", xmlcParserErrorDescription(error));
      if ((intptr_t)config.errorLocation < (intptr_t)buf + len)
      {
         char *badconfig = WOMALLOC((len+1)*sizeof(char));
         strncpy(badconfig, buf, len);
         badconfig[len] = '\0';
         WOLog(WO_ERR,"Error near:\n%s", config.errorLocation);
         WOFREE(badconfig);
      }
   } else {
      /*
       *	load the new settings...
       */
      if (config.new_apps)
         for (i=0; i<wolist_count(config.new_apps); i++)
            ac_updateApplication((strtbl *)wolist_elementAt(config.new_apps, i), (list *)wolist_elementAt(config.new_app_instances, i));
   }

   /* clean up and return */
   freeWOXMLEdits(&config);
   xmlcParserDealloc(parser);
   return error;
}




/*
 * This function cleans up all the lists and dictionaries created while parsing the config.
 */
static void freeWOXMLEdits(WOXMLEdits *config)
{
   int i;

   for (i=0; i<wolist_count(config->new_apps); i++)
      st_free(wolist_elementAt(config->new_apps, i));
   wolist_dealloc(config->new_apps);

   for (i=0; i<wolist_count(config->new_app_instances); i++)
   {
      int j;
      list *instances = wolist_elementAt(config->new_app_instances, i);
      for (j=0; j<wolist_count(instances); j++)
         st_free(wolist_elementAt(instances, j));
      wolist_dealloc(instances);
   }
   wolist_dealloc(config->new_app_instances);
}

/*
 *      Called from the xml parser.
 *	Here is where we begin parsing <application>... or <instance>...
 *	or <adaptor>...
 */
static void createElement(XMLCDocument *document, const XMLCCharacter *name, const unsigned int length)
{
   WOXMLEdits *config = (WOXMLEdits *)document;

   if (config->error != 0)		/* would be nice to tell parser to stop */
      return;

   config->errorLocation = name;
   
   if (length == 7 && strncasecmp(name, "adaptor", length) == 0)
   {
      /* do nothing; don't generate a warning */
   } else if (length == 11 && strncasecmp(name, "application", length) == 0) {
      /* begin <application> */
      if (config->current_element != NULL) {
         WOLog(WO_ERR,"Error parsing config: found unexpected <application> tag");
         config->error = 1;
         return;
      }
      /* create new app settings dictionary, instance list, and set current_element to the app dictionary*/
      config->current_app = st_new(8);
      wolist_add(config->new_apps, config->current_app);
      config->current_app_instances = wolist_new(8);
      wolist_add(config->new_app_instances, config->current_app_instances);
      config->current_element = config->current_app;

   } else if (length == 8 && strncasecmp(name, "instance", length) == 0) {
      /* begin <instance> */
      if (config->current_element != config->current_app || config->current_app == NULL) {
         WOLog(WO_ERR,"Error parsing config: found unexpected <instance> tag");
         config->error = 1;
         return;
      }
      /* create new instance settings dictionary and set current_element to point to it */
      config->current_instance = st_new(8);
      wolist_add(config->current_app_instances, config->current_instance);
      config->current_element = config->current_instance;

   } else {
      /* Got something unexpected. Ignore the tag. */
      char *buffer = WOMALLOC(length+1);
      strncpy(buffer,name,length);
      buffer[length] = '\0';
      WOLog(WO_WARN,"Unknown tag in XML: \"%s\"",buffer);
      config->current_element = NULL;
      WOFREE(buffer);
   }
   return;
}
/*
 *	here's where we end application/instance/error parsing
 */
static void endElementNamed(XMLCDocument *document, const XMLCCharacter *name, const unsigned int length)
{
   WOXMLEdits *config = (WOXMLEdits *)document;

   if (config->error != 0)		/* would be nice to tell parser to stop */
      return;

   if (length == 7 && strncasecmp(name, "adaptor", 7) == 0)
   {
      /* do nothing; don't generate a warning */
   } else if (length == 11 && strncasecmp(name, "application", 11) == 0) {
      if (config->current_element != config->current_app) {
         WOLog(WO_ERR,"Error parsing config: </application> found at top level or with open <instance> tag");
         config->error = 1;
         return;
      }
      config->current_element = NULL;
      config->current_app = NULL;
      config->current_app_instances = NULL;

   } else if (length == 8 && strncasecmp(name, "instance", 8) == 0) {
      if (config->current_element != config->current_instance) {
         WOLog(WO_ERR,"Error parsing config: unexpected </instance>");
         config->error = 1;
         return;
      }
      config->current_element = config->current_app;
      config->current_instance = NULL;

   } else if (length == 7 && strncasecmp(name, "adaptor", 7) == 0) {
      config->current_element = NULL;
   } else {
#ifdef _MSC_VER // SWK VC can't allocate dynamic char buffer[length + 1]
      char *buffer = (char *)alloca((length+1) * sizeof(char));
#else
      char buffer[length+1];
#endif
	  strncpy(buffer,name,length);
      buffer[length] = '\0';
      WOLog(WO_WARN,"Unknown end tag in XML: %s",buffer);
      if (config->current_element != NULL)	/* only an error if we were parsing something we know about */
         config->error = 1;
   }
   return;
}

/*
 * Called from the xml parser to process a tag attribute. Just adds the key/value pair to the
 * current_element dictionary.
 */
static void createAttribute(XMLCDocument *document, const XMLCCharacter *name, const unsigned int nameLength, const XMLCCharacter *value, const unsigned int valueLength)
{
   WOXMLEdits *config = (WOXMLEdits *)document;

   if (config->error != 0)		/* would be nice to tell parser to stop */
      return;
   config->errorLocation = &value[valueLength+1];
   if (config->current_element)
   {
      /* Insert null terminators into the xml buffer. */
      /* This is so we don't have to copy all the string values. They are stored by reference */
      /* until the parse completes, then they are copied into the "live" configuration. The */
      /* buffer containing the xml is not freed until after this completes. */
      ((char *)name)[nameLength] = 0;
      ((char *)value)[valueLength] = 0;
      st_add(config->current_element, name, value, 0);
   } else
      WOLog(WO_WARN, "createAttribute() called with NULL current_element.");
   return;
}


/*
 * A no-op placeholder for the xml parser for functions that we don't use.
 */
static void xmlParserPlaceholder(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length)
{
   return;
}
