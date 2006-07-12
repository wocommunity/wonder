//
//  Rule.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EOControl.h"

@class Assignment;

@interface Rule : NSObject {
    
    @private
    int		    _author;
    EOQualifier	    *_lhs;
    Assignment	    *_rhs;
    BOOL	    _enabled;
    
    NSUndoManager   *_undoManager;

}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver;
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver;

- (NSString *)extendedDescription;

- (Assignment *)rhs;
- (void)setRhs:(Assignment *)rhs;

- (EOQualifier *)lhs;
- (void)setLhs:(EOQualifier *)lhs;
- (NSString *)lhsDescription;

- (int)author;
- (void)setAuthor:(int)value;
- (int)priority;

- (BOOL)enabled;
- (void)setEnabled:(BOOL)flag;

// Undo management
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

@end
