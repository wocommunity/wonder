//
//  Assignment.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Rule;
@class EOKeyValueUnarchiver;
@class EOKeyValueArchiver;

@interface Assignment : NSObject {
    
    @private
    NSString	    *_keyPath;
    id              _value;
    NSString        *_assignmentClass;
    NSString        *_assignmentClassDescription;
	Rule			*_rule; // Back-pointer - not retained
    NSString        *_valueDescription;

}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver;
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver;

- (NSString *)assignmentClass;
- (void)setAssignmentClass:(NSString *)classname;
- (NSString *)assignmentClassDescription;
- (void)setAssignmentClassDescription:(NSString *)classnameDescription;

- (NSString *)keyPath;
- (void)setKeyPath:(NSString *)keyPath;

- (id)value;
- (void)setValue:(id)value;
- (NSString *)valueDescription;

- (void)setRule:(Rule *)rule;

// Undo management
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

- (BOOL)isEqualToAssignment:(Assignment *)assignment;

+ (void)setD2wclientConfigurationPaths:(NSArray *)paths;
+ (NSArray *)d2wclientConfigurationPaths;
+ (void)refreshToolTipDictionary;

@end
