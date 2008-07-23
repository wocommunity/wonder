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
#include <stdio.h>
#include <stdlib.h>
#include "womalloc.h"
#include "xmlcparser.h"

/* parser states. */
typedef enum {

    P_IN_CONTENT,
    P_IN_START_TAG,
    P_IN_END_TAG,
    P_EXPECT_CLOSE_ELEMENT,
    P_IN_TAG_ATTRIBUTES,
    P_IN_TAG_ATTRIBUTES_EQUALS, /* expecting = or whitespace */
    P_IN_TAG_ATTRIBUTES_VALUE  /* expecting quoted value or whitespace */
    
} XML_PARSER_STATE;


XMLCParser *xmlcParserInit()
{
	XMLCParser *s;

    s = (XMLCParser *)WOMALLOC(sizeof(XMLCParser));
    s->tokenizer = xmlcTokenizerInit();
	xmlcParserReset(s);
	return s;
}

void xmlcParserDealloc(XMLCParser *s)
{
	xmlcTokenizerDealloc(s->tokenizer);
	WOFREE(s);
}

const char *xmlcParserErrorDescription(XMLCParseError err)
{
	switch (err) {
		case XMLCParser_err_expected_attribute_name: 
			return "expected attribute name or > or />";
		case XMLCParser_err_expected_tag_name: 
			return "expected tag name";
		case XMLCParser_err_expected_equals: 
			return "expected =";
		case XMLCParser_err_expected_quoted_string: 
			return "expected quoted string";
		case XMLCParser_err_expected_close: 
			return "expected >";
		case XMLCParser_err_partial_token: 
			return "partial token";
		default: 
			return "unknown error";
	}
}

void xmlcParserReset(XMLCParser *s)
{
    s->parse_state = P_IN_CONTENT;
    s->buffer = (XMLCCharacter *)0;
    s->length = 0;
    s->preserveWhiteSpace = 1;
	xmlcTokenizerReset(s->tokenizer);
}

XMLCParseError xmlcParserError(XMLCParser *s, XMLCParseError aReason)
{
	s->lastError = aReason;
	return aReason;
}


void xmlcParserSetPreserveWhiteSpace(XMLCParser *s, XMLCBoolean preserve)
{
    s->preserveWhiteSpace = preserve;
}

XMLCParseError xmlcParserParse(XMLCParser *s, XMLCDocumentHandler *dochandler)
{
    XMLCCharacter *string = 0;
    XMLCUInt stringLength = 0;
    XMLCCharacter *attributeName = 0;
    XMLCUInt attributeNameLength = 0;
    XMLCCharacter *elementName = 0;
    XMLCUInt elementNameLength = 0;
    XMLCToken tokType;
    
    for (;;) {
    
    	tokType = xmlcTokenizerNextToken(s->tokenizer);
    	if (tokType == PARTIAL_TOKEN)
    		return xmlcParserError(s,XMLCParser_err_partial_token);
    	else if (tokType == EOF_TOKEN)
    		return 0;
    		
        switch(s->parse_state) {

            case P_IN_CONTENT:
                if (tokType == OPEN_ELEMENT_TOKEN) { /* < */
                    s->parse_state = P_IN_START_TAG;
                } else if (tokType == OPEN_SLASH_ELEMENT_TOKEN) {
                    s->parse_state = P_IN_END_TAG; /* wait for name, no attributes allowed */
                } else if (tokType == CDATA_TOKEN) {
                	string = xmlcTokenizerString(s->tokenizer);
                	stringLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->createCDataSection(dochandler->document, string, stringLength);
                } else if (tokType == PCDATA_TOKEN || (tokType == WHITESPACE_TOKEN && s->preserveWhiteSpace)) {
					string = xmlcTokenizerString(s->tokenizer);
                	stringLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->createContent(dochandler->document, string, stringLength);
                } else if (tokType == COMMENT_TOKEN && s->preserveWhiteSpace) {
					string = xmlcTokenizerString(s->tokenizer);
                	stringLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->createComment(dochandler->document, string, stringLength);
                } else if (tokType == COMMAND_TOKEN) {
					string = xmlcTokenizerString(s->tokenizer);
                	stringLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->createProcessingCommand(dochandler->document, string, stringLength);
                } else if (tokType == DECLARATION_TOKEN) {
					string = xmlcTokenizerString(s->tokenizer);
                	stringLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->createDeclaration(dochandler->document, string, stringLength);
                }
                break;

            case P_IN_START_TAG:
                if (tokType == NAME_TOKEN) {/* wait for name */
					elementName = xmlcTokenizerString(s->tokenizer);
                	elementNameLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->createElementNamed(dochandler->document, elementName, elementNameLength);
                    s->parse_state = P_IN_TAG_ATTRIBUTES;
                } else {
                    return xmlcParserError(s,XMLCParser_err_expected_tag_name);
                }
                break;

            case P_IN_TAG_ATTRIBUTES: /* expecting attribute name or whitespace */
                if (tokType == CLOSE_ELEMENT_TOKEN) { /* > */
                    s->parse_state = P_IN_CONTENT;
                } else if (tokType == SLASH_CLOSE_ELEMENT_TOKEN)  {/* /> */
                    dochandler->endElementNamed(dochandler->document, elementName, elementNameLength);
                    s->parse_state = P_IN_CONTENT;
                } else if (tokType == WHITESPACE_TOKEN && s->preserveWhiteSpace) {
                	/* whitespace between attributes */
                } else if (tokType == NAME_TOKEN) {
                    attributeName = xmlcTokenizerString(s->tokenizer);
                    attributeNameLength = xmlcTokenizerStringLength(s->tokenizer);
                    s->parse_state = P_IN_TAG_ATTRIBUTES_EQUALS;
                } else {
                    return xmlcParserError(s,XMLCParser_err_expected_attribute_name);
                }
                break;

            case P_IN_TAG_ATTRIBUTES_EQUALS: /* expecting = or whitespace */
                if (tokType == WHITESPACE_TOKEN && s->preserveWhiteSpace) {
                	/* whitespace between attributes */
                } else if (tokType == '=') {
                    s->parse_state = P_IN_TAG_ATTRIBUTES_VALUE;
                } else {
                    return xmlcParserError(s,XMLCParser_err_expected_equals);
                }
                break;
 
            case P_IN_TAG_ATTRIBUTES_VALUE: /* expecting quoted value or whitespace */
                if (tokType == WHITESPACE_TOKEN && s->preserveWhiteSpace) {
                	/* whitespace between attributes */
                } else if (tokType == QUOTE_STRING_TOKEN) {
                    string = xmlcTokenizerQuotedString(s->tokenizer);
                    stringLength = xmlcTokenizerQuotedStringLength(s->tokenizer);
                    dochandler->createAttribute(dochandler->document, attributeName, attributeNameLength, string, stringLength);
                    s->parse_state = P_IN_TAG_ATTRIBUTES;
                } else {
                    return xmlcParserError(s,XMLCParser_err_expected_quoted_string);
                }
                break;

            case P_IN_END_TAG:
                if (tokType == NAME_TOKEN) {/* wait for name, attributes are not allowed */
                    string = xmlcTokenizerString(s->tokenizer);
                    stringLength = xmlcTokenizerStringLength(s->tokenizer);
                    dochandler->endElementNamed(dochandler->document, string, stringLength);
                    s->parse_state = P_EXPECT_CLOSE_ELEMENT;
                } else {
                    return xmlcParserError(s,XMLCParser_err_expected_tag_name);
                }
                break;

            case P_EXPECT_CLOSE_ELEMENT:
                if (tokType == CLOSE_ELEMENT_TOKEN) { /* wait for > */
                    s->parse_state = P_IN_CONTENT;
                } else {
                    return xmlcParserError(s,XMLCParser_err_expected_close);
                }
                break;
        }
    }
}
