//
//  RMDocumentController.m
//  RuleModeler
//
//  Created by Dave Lopper on 8/20/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import "RMDocumentController.h"


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

@end
