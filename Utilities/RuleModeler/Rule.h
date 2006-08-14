//
//  Rule.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Assignment;
@class RMModel;
@class EOQualifier;
@class EOKeyValueUnarchiver;
@class EOKeyValueArchiver;

@interface Rule : NSObject <NSCopying> {
    
    @private
    int		    _author;
    EOQualifier	    *_lhs;
    NSString	    *_lhsDescription;
    Assignment	    *_rhs;
    BOOL	    _enabled;
	RMModel		*_model; // Back-pointer - not retained
}

+ (NSArray *)rulesFromMutablePropertyList:(id)plist;

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

- (void)setModel:(RMModel *)model;

// Undo management
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

@end
