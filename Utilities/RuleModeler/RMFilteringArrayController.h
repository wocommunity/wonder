//
//  RMFilteringArrayController.h
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EOControl.h"

@interface RMFilteringArrayController : NSArrayController {
    
    EOQualifier *_qualifier;
    
}

- (void)search:(id)sender;

- (EOQualifier *)qualifier;
- (void)setQualifier:(EOQualifier *)qualifier;

// Undo management
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

@end
