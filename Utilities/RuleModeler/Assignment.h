//
//  Assignment.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EOControl.h"

@interface Assignment : NSObject {
    
    @private
    NSString	    *_keyPath;
    NSObject	    *_value;
    NSString        *_assignmentClass;

}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver;
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver;

- (NSString *)assignmentClass;
- (void)setAssignmentClass:(NSString *)classname;

- (NSString *)keyPath;
- (void)setKeyPath:(NSString *)keyPath;

- (NSObject *)value;
- (void)setValue:(NSObject *)value;

// Undo management
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

@end
