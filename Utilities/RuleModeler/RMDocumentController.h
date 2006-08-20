//
//  RMDocumentController.h
//  RuleModeler
//
//  Created by Dave Lopper on 8/20/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface RMDocumentController : NSDocumentController {
    BOOL    dontAddRecentDocument;
}

- (BOOL)dontAddRecentDocument;
- (void)setDontAddRecentDocument:(BOOL)newDontAddRecentDocument;

@end
