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
#include "xmlctokenizer.h"


/* doesn't seem to make a bit of difference using the property table or not */
#define usePropertyTable 0

#if usePropertyTable

/* these could be sped up with character property tables, maybe */
#define isLetterTest(x) ((x >='a' && x <='z') || (x >='A' && x <='Z') || (x >= 0x100))
#define isNameStartCharTest(x) ((isLetterTest(x)) || (x == '_') || (x == ':'))
#define isDigitTest(x) ((x >='0') && (x <='9'))
#define isNameCharTest(x) ((isNameStartCharTest(x)) || (isDigit(x)) || (x == ':') || (x == '_') || (x == '-') || (x == '.'))
#define isEOLTest(x) ((x == '\n') || (x == '\r'))
#define isWhiteSpaceTest(x) (isEOLTest(x) || (x == ' ') || (x == '\t'))

/* character properties.*/
enum {

    PROPERTY_isLetter = 1,
    PROPERTY_isNameStartChar = 2,
    PROPERTY_isDigit = 4,
    PROPERTY_isNameChar = 8,
    PROPERTY_isEOL = 16,
    PROPERTY_isWhiteSpace = 32
    
} PROPERTY_BITS;

/* fewer tests, multiply*/
#define isLetter(x) ((x >= 0x100) || (characterPropertiesTable[x] & PROPERTY_isLetter))
#define isNameStartChar(x) ((x >= 0x100) || (characterPropertiesTable[x] & PROPERTY_isNameStartChar))
#define isNameChar(x) ((x >= 0x100) || (characterPropertiesTable[x] & PROPERTY_isNameChar))
#define isDigit(x) ((x < 0x100) && (characterPropertiesTable[x] & PROPERTY_isDigit))
#define isEOL(x) ((x < 0x100) && (characterPropertiesTable[x] & PROPERTY_isEOL))
#define isWhiteSpace(x) ((x < 0x100) && (characterPropertiesTable[x] & PROPERTY_isWhiteSpace))

static XMLCUInt characterPropertiesTable[0x100];

#else

/* more tests, no multiply */
#ifndef _XMLC_8BITCHAR_
#define isLetter(x) ((x >='a' && x <='z') || (x >='A' && x <='Z') || (x >= 0x100))
#else
// 2009/04/23: 'x' is a char (= 8 bit) and the condition '(x >= 0x100)' 
//             is not very useful in such a situation
#define isLetter(x) ((x >='a' && x <='z') || (x >='A' && x <='Z'))
#endif
#define isNameStartChar(x) ((isLetter(x)) || (x == '_'))
#define isDigit(x) ((x >='0') && (x <='9'))
#define isNameChar(x) ((isNameStartChar(x)) || (isDigit(x)) || (x == ':') || (x == '_') || (x == '-') || (x == '.'))
#define isEOL(x) ((x == '\n') || (x == '\r'))
#define isWhiteSpace(x) (isEOL(x) || (x == ' ') || (x == '\t'))

#endif



/* tokenizer states. */
enum {
    T_IN_NOTHING,
    T_IN_START_PCDATA,
    T_IN_PCDATA,
    T_IN_TAG,
    T_IN_QUOTE_STRING,
    T_IN_NAME_STRING,
    T_IN_DECLARATION,
    T_IN_COMMENT,
    T_IN_COMMAND,
    T_IN_CDATA
} XMLC_TOKENIZER_STATES;


XMLCTokenizer *xmlcTokenizerInit()
{
    XMLCTokenizer *s = (XMLCTokenizer*)WOMALLOC(sizeof(XMLCTokenizer));

#if usePropertyTable
        XMLCUInt i;

        /* create character property table */
        /* FIXME: creating property table not thread safe */
        for (i=0; i<0x100; ++i) {
            XMLCUInt properties = 0;
            if (isLetterTest(i))
                properties = properties | PROPERTY_isLetter;
            if (isNameStartCharTest(i))
                properties = properties | PROPERTY_isNameStartChar;
            if (isDigitTest(i))
                properties = properties | PROPERTY_isDigit;
            if (isNameCharTest(i))
                properties = properties | PROPERTY_isNameChar;
            if (isEOLTest(i))
                properties = properties | PROPERTY_isEOL;
            if (isWhiteSpaceTest(i))
                properties = properties | PROPERTY_isWhiteSpace;
            characterPropertiesTable[i] = properties;
        }
#endif
	xmlcTokenizerReset(s);
    return s;
}


void xmlcTokenizerDealloc(XMLCTokenizer *s)
{
    WOFREE(s);
}


void xmlcTokenizerReset(XMLCTokenizer *s)
{
    s->tokState = T_IN_PCDATA;
    s->line_number = 1;
    s->buffer_start = 0;
	s->buffer_end = 0;
}


void xmlcTokenizerSetBuffer(XMLCTokenizer *s, XMLCCharacter *aString, XMLCUInt length)
{
	s->buffer_position = s->buffer_start = aString;
	s->buffer_end = s->buffer_start + length;
}


XMLCUInt lineNumber(XMLCTokenizer *s)
{
    return s->line_number;
}


XMLCParseError xmlcTokenizerError(XMLCParseError error)
{
	return error;
}



#define getChar(x)  if (s->buffer_position < s->buffer_end) x = *s->buffer_position++; else return PARTIAL_TOKEN;
#define unGetChar(x) --s->buffer_position;

XMLCToken xmlcTokenizerNextToken(XMLCTokenizer *s)
{
    XMLCCharacter ch;
    XMLCCharacter *mark = s->token_start = s->buffer_position;
    XMLCUInt i;


    while (s->buffer_position < s->buffer_end) {

        getChar(ch);

		/* states that do their own getChar aren't allowed to eat these */
        if (ch == '\n' || ch == '\r') {
            s->line_number++;
            s->line_start = s->buffer_position;
       	}

     /*   printf("char '%c' props %x s->tokState: %@",  ch, characterPropertiesTable[ch], [_tokenStateNameStrings objectAtIndex:s->tokState]); */
        switch (s->tokState) {

            /* FIXME: do I need this state? */
            case T_IN_START_PCDATA:
                s->token_start = mark;
                s->tokState = T_IN_PCDATA;
                /* fall through */

            case T_IN_PCDATA: 
                if (ch == '<') { /* ^<Marker */
                    unGetChar(ch);
                    s->tokState = T_IN_TAG;
                    if (s->buffer_position > s->token_start) {
                        if(s->isWhiteSpaceToken) 
                        	return s->tokType = WHITESPACE_TOKEN;
                        else
                            return s->tokType = PCDATA_TOKEN;
                    }
                }
                if(!isWhiteSpace(ch))
                    s->isWhiteSpaceToken = 0;
                break;

            case T_IN_TAG:   /* ^<Marker */
                if (ch == '<') {
                    getChar(ch);
                    if (ch == '?') {  /* <? */
                        s->token_start = mark;
                        s->tokState = T_IN_COMMAND;
                    } else if (ch == '/') {
                        return s->tokType = OPEN_SLASH_ELEMENT_TOKEN;  /* </ */
                    } else if (ch == '!') {
                        s->token_start = mark;
                        s->tokState = T_IN_DECLARATION;
                        getChar(ch);
                        if (ch =='-') {
                            getChar(ch);
                            if (ch == '-') { /* <!-- */
                                s->token_start = mark;
                                s->tokState = T_IN_COMMENT;
                            }
                        } else if (ch == '[') {
                            s->tokState = T_IN_CDATA;
                            for(i=0;i<6 && s->tokState == T_IN_CDATA;i++) {
                                getChar(ch);
                                if (!ch == "CDATA["[i])
                              		s->tokState = T_IN_DECLARATION;
                            }
                            if (s->tokState == T_IN_CDATA)
                            	s->token_start = mark;
                        } else { /* <! */
                            unGetChar(ch);
                            s->token_start = mark;
                            s->tokState = T_IN_DECLARATION;
                        }
                    }
                    if (s->tokState == T_IN_TAG) { /* < */
                        unGetChar(ch);
                        return s->tokType = OPEN_ELEMENT_TOKEN;
                    }

                } else if (ch == '>') {
                    s->token_start = s->buffer_position;
                    s->isWhiteSpaceToken = 1;
                    s->tokState = T_IN_PCDATA;
                    return s->tokType =  CLOSE_ELEMENT_TOKEN;
                } else if (ch == '/') {
                    getChar(ch);
                    if (ch =='>') {
                        s->token_start = mark;
                        s->tokState = T_IN_PCDATA;
                        return s->tokType = SLASH_CLOSE_ELEMENT_TOKEN;
                    } else {
                    	unGetChar(ch);
                        return s->tokType = '/'; /* FIXME: is this an error, the parser should complain */
                    }
                } else if (ch == '=') {
                    s->token_start = mark;
                    return s->tokType = '=';
                } else if (ch == '"') {
                    s->token_start = mark;
                    s->end_quote_char = '"';
                    s->tokState = T_IN_QUOTE_STRING;
                } else if (ch == 0x0027) { /*#	APOSTROPHE */
                    s->token_start = mark;
                    s->end_quote_char = 0x0027;
                    s->tokState = T_IN_QUOTE_STRING;
                } else if (isNameStartChar(ch)) {
                    /* FIXME: whitespace tokens inside tags aren't recognized */ 
                    /* need to handle whitespace tokens in tags, could eliminate s->buffer_position-1 and use _mark */
                    s->token_start = s->buffer_position-1;
                    s->tokState = T_IN_NAME_STRING;
                } else if (!isWhiteSpace(ch)) {
                	/* don't know what this is, return it as itself, let the parser deal with it */
                    return s->tokType = ch;
                } else {
                   mark = s->buffer_position;
                }
                break;

            case T_IN_DECLARATION: /* scan until >  FIXME: allow everything? */
                if (ch == '>') {
                    s->tokState = T_IN_START_PCDATA;
                    return s->tokType = DECLARATION_TOKEN;
                }

            case T_IN_COMMENT: /* scan until --> allow everything */
                if (ch == '-') {
                    getChar(ch);
                    if (ch == '-') {
                        getChar(ch);
                        if (ch == '>') {
                            s->tokState = T_IN_START_PCDATA;
                            return s->tokType = COMMENT_TOKEN;
                        }
                    }
                }
                break;

            case T_IN_COMMAND: /* scan until ?>  FIXME: allow everything? */
                if (ch == '?') {
                    getChar(ch);
                    if (ch == '>') {
                        s->tokState = T_IN_START_PCDATA;
                        return s->tokType = COMMAND_TOKEN;
                    }
                }
                break;
                
            case T_IN_CDATA: /* scan until ]]> allow everything */
                if (ch == ']') {
                    getChar(ch);
                    if (ch == ']') {
                        getChar(ch);
                        if (ch == '>') {
                            s->tokState = T_IN_START_PCDATA;
                            return s->tokType = CDATA_TOKEN;
                        }
                    }
                }
                break;

            case T_IN_NAME_STRING:   /* <M^arker */
                if (!(isNameChar(ch))) {
                    unGetChar(ch);
                    s->tokState = T_IN_TAG;
                    return s->tokType = NAME_TOKEN;
                }
                break;

            case T_IN_QUOTE_STRING:   /* FIXME: handle &quote; type things (and whitespace eating?) */
                if (ch == s->end_quote_char) {
                    s->tokState = T_IN_TAG;
                    return s->tokType = QUOTE_STRING_TOKEN;
                }
                break;
        }
    }
    return s->tokType = s->tokState != T_IN_PCDATA ? PARTIAL_TOKEN : EOF_TOKEN;
}

