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
#ifndef _XMLCPARSER_
#define _XMLCPARSER_
 
#include "xmlctokenizer.h"
#include "xmlcdocument.h"

/* error codes */

typedef enum {

	XMLCParser_err_expected_attribute_name = 500, /* "expected attribute name or > or />" */
	XMLCParser_err_expected_tag_name, 			/* "expected tag name" */
	XMLCParser_err_expected_equals, 				/* "expected =" */
	XMLCParser_err_expected_quoted_string, 		/* "expected quoted string" */
	XMLCParser_err_expected_close, 				/* "expected >" */
	XMLCParser_err_partial_token	 				/* "partial token" */
    
} XMLC_PARSER_ERRORS;


typedef struct {

    XMLCTokenizer 	*tokenizer;

    XMLCUInt 	    parse_state;
    XMLCUInt 	    length;
    XMLCCharacter 	*buffer;
    XMLCBoolean 	preserveWhiteSpace;
    XMLCEncoding 	encoding;
    XMLCParseError 	lastError;
    
} XMLCParser;


XMLCParser *xmlcParserInit();
void xmlcParserDealloc(XMLCParser *s);
void xmlcParserReset(XMLCParser *s);
XMLCParseError xmlcParserError(XMLCParser *s, XMLCParseError aReason);
const char *xmlcParserErrorDescription(XMLCParseError err);
void xmlcParserSetBuffer(XMLCParser *s, XMLCCharacter *aString, XMLCUInt length);
void xmlcParserSetPreserveWhiteSpace(XMLCParser *s, XMLCBoolean preserve);
XMLCParseError xmlcParserParse(XMLCParser *s, XMLCDocumentHandler *dochandler);


#endif /* _XMLCPARSER_ */
