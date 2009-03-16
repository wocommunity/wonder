/*
 Rule.h
 RuleModeler

 Created by King Chung Huang on 1/29/04.


 Copyright (c) 2004 King Chung Huang

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
*/

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
    NSString	    *_lhsFormattedDescription;
    Assignment	    *_rhs;
    BOOL	    _enabled;
	RMModel		*_model; // Back-pointer - not retained
    BOOL        isNewRule; // FIXME Dependency on RMFilteringArrayController
}

+ (void)setDefaultRulePriority:(int)priority;
+ (int)defaultRulePriority;

+ (NSArray *)rulesFromMutablePropertyList:(id)plist;

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver;
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver;

- (NSString *)extendedDescription;

- (Assignment *)rhs;
- (void)setRhs:(Assignment *)rhs;

- (EOQualifier *)lhs;
- (void)setLhs:(EOQualifier *)lhs;
- (NSString *)lhsDescription;
- (NSString *)lhsFormattedDescription;

- (int)author;
- (void)setAuthor:(int)value;
- (int)priority;

- (BOOL)enabled;
- (void)setEnabled:(BOOL)flag;

- (RMModel *)model;
- (void)setModel:(RMModel *)model;

// Undo management
- (NSUndoManager *)undoManager;
- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue;

- (BOOL)isEqualToRule:(Rule *)rule;

- (void)resetDescriptionCaches;

@end
