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
#ifndef _XMLCDOCUMENT_
#define _XMLCDOCUMENT_

#include "xmlcdefines.h"


typedef struct {
	void *document;
} XMLCDocument;

typedef void (*xmlCreateElementProcPtr)(XMLCDocument *document, const XMLCCharacter *name, const unsigned int length);
typedef void (*xmlCreateAttributeProcPtr)(XMLCDocument *document, const XMLCCharacter *name, const unsigned int nameLength, const XMLCCharacter *value, const unsigned int valueLength);
typedef void (*xmlEndElementNamedProcPtr)(XMLCDocument *document, const XMLCCharacter *name, const unsigned int length);
typedef void (*xmlEndElementProcPtr)(XMLCDocument *document);
typedef void (*xmlCreateCDataSectionProcPtr)(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length);
typedef void (*xmlCreateContentProcPtr)(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length);
typedef void (*xmlCreateCommentProcPtr)(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length);
typedef void (*xmlCreateDeclarationProcPtr)(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length);
typedef void (*xmlCreateProcessingCommandProcPtr)(XMLCDocument *document, const XMLCCharacter *string, const unsigned int length);

typedef struct {
    XMLCDocument *document;

    xmlCreateElementProcPtr createElementNamed;
    xmlCreateAttributeProcPtr createAttribute;
    xmlEndElementNamedProcPtr endElementNamed;
    xmlCreateCDataSectionProcPtr createCDataSection;
    xmlCreateContentProcPtr createContent;
    xmlCreateCommentProcPtr createComment;
    xmlCreateDeclarationProcPtr createDeclaration;
    xmlCreateProcessingCommandProcPtr createProcessingCommand;
    
} XMLCDocumentHandler;

#endif /* _XMLCDOCUMENT_ */
