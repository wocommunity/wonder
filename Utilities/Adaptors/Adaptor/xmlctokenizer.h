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

#ifndef _XMLCTOKENIZER_
#define _XMLCTOKENIZER_

#include "xmlcdefines.h"

/* token types */
extern enum {

    NO_TOKEN = 0x100,
    ERROR_TOKEN,

    NAME_TOKEN,            /* element or attribute names */
    PCDATA_TOKEN,          /* content text */
    WHITESPACE_TOKEN,      /* space, \t, \n, \r */
    QUOTE_STRING_TOKEN,    /* "xxxx" or 'xxxx' */
    COMMENT_TOKEN,         /* <!-- xxxxxxxxxx --> */
    DECLARATION_TOKEN,     /* <! xxxxxxxxxx > */
    COMMAND_TOKEN,         /* <? xxxxxxxxxx ?> */
    CDATA_TOKEN,           /* <![CDATA[xxxx]]> */

    SLASH_CLOSE_ELEMENT_TOKEN,   /* /> */
    OPEN_SLASH_ELEMENT_TOKEN,    /* </ */
    OPEN_ELEMENT_TOKEN,         /* < */
    CLOSE_ELEMENT_TOKEN,         /* > */
    
    PARTIAL_TOKEN,  /* token chopped at end of buffer */
    EOF_TOKEN = 0
    
} XMLCTokenTypes;


typedef struct {

    /* tokenizer info */
    XMLCUInt 		tokState;
    XMLCUInt 		line_number;
    XMLCCharacter	*line_start;

    /* buffer info */
    XMLCCharacter 	*buffer_position;
    XMLCCharacter 	*buffer_start;
    XMLCCharacter 	*buffer_end;

    /* token info */
    XMLCUInt 		tokType;
    XMLCCharacter 	end_quote_char;
    XMLCCharacter 	*token_start;			/* first char of token */
    XMLCBoolean 	isWhiteSpaceToken;
    
} XMLCTokenizer;


/* accessors */
#define xmlcTokenizerString(s) 					(s->token_start)
#define xmlcTokenizerStringLength(s)			(s->buffer_position - s->token_start)
#define xmlcTokenizerQuotedString(s) 			(s->token_start + 1)
#define xmlcTokenizerQuotedStringLength(s)		(s->buffer_position - s->token_start - 2)

/* methods */
XMLCTokenizer *xmlcTokenizerInit();
void xmlcTokenizerDealloc(XMLCTokenizer *s);
void xmlcTokenizerReset(XMLCTokenizer *s);
void xmlcTokenizerSetBuffer(XMLCTokenizer *s, XMLCCharacter *aString, XMLCUInt length);
XMLCUInt lineNumber(XMLCTokenizer *s);
XMLCParseError xmlcTokenizerError(XMLCParseError error);
XMLCToken xmlcTokenizerNextToken(XMLCTokenizer *s);

#endif
