//
//  RMFilteringArrayController.h
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RMFilteringArrayController : NSArrayController {
    NSMutableArray *_newObjects;
}

- (void)search:(id)sender;
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

@end
