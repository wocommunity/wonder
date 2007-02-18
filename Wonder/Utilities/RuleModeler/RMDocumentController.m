/*
 RMDocumentController.m
 RuleModeler

 Created by davelopper on 8/20/06.


 Copyright (c) 2004-2007, Project WONDER <http://wonder.sourceforge.net/>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  * Neither the name of the Project WONDER nor the names of its contributors may
    be used to endorse or promote products derived from this software without
    specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#import "RMDocumentController.h"
#import "RMModelGroup.h"


@implementation RMDocumentController

- (BOOL)dontAddRecentDocument {
    return dontAddRecentDocument;
}

- (void)setDontAddRecentDocument:(BOOL)newDontAddRecentDocument {
    dontAddRecentDocument = newDontAddRecentDocument;
}

- (void)noteNewRecentDocumentURL:(NSURL *)aURL {
    if (![self dontAddRecentDocument]) {
        [super noteNewRecentDocumentURL:aURL];
    } 
}

- (void)closeAllDocumentsWithDelegate:(id)delegate didCloseAllSelector:(SEL)didCloseAllSelector contextInfo:(void *)contextInfo {
    // FIXME In order to be able to close all documents (on quit), we need to reorder them, because we can't close a Model
    // referenced by a Model Group. Strangely, we need to order documents with Models first, then Group Models, in order for
    // documentController to first close model groups.
    // This will probably break someday!
    sortDocuments = YES;
    [super closeAllDocumentsWithDelegate:delegate didCloseAllSelector:didCloseAllSelector contextInfo:contextInfo];
    sortDocuments = NO;
}

static int sortDocumentsByType(id doc1, id doc2, void *context){
    if ([doc1 class] != [doc2 class])
        return ([doc1 isKindOfClass:[RMModelGroup class]] ? NSOrderedDescending:NSOrderedAscending);
    else
        return NSOrderedSame;
}

- (NSArray *)documents {
    NSArray *docs = [super documents];
    
    if (sortDocuments) {
        docs = [docs sortedArrayUsingFunction:sortDocumentsByType context:nil];
    }
    
    return docs;
}

@end
