//
//  RMFilteringArrayController.m
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "RMFilteringArrayController.h"
#import "NSArrayAdditions.h"

@implementation RMFilteringArrayController

- (IBAction)search:(id)sender {
    NSString *value = [sender stringValue];
    
    if (value != nil && [value length] > 0) {
        EOQualifier *qual = nil;
        
        NS_DURING {
            qual = [EOQualifier qualifierWithQualifierFormat:value];
        } NS_HANDLER {
            NSRange range = [value rangeOfString:@"*"];
            if (range.location == NSNotFound)
                value = [NSString stringWithFormat:@"*%@*", value];
            
            NSMutableArray *innerQuals = [[NSMutableArray alloc] initWithCapacity:3];
            [innerQuals addObject:[[EOKeyValueQualifier alloc] initWithKey:@"lhsDescription" operatorSelector:EOQualifierOperatorCaseInsensitiveLike value:value]];
            [innerQuals addObject:[[EOKeyValueQualifier alloc] initWithKey:@"rhs.keyPath.description" operatorSelector:EOQualifierOperatorCaseInsensitiveLike value:value]];
            [innerQuals addObject:[[EOKeyValueQualifier alloc] initWithKey:@"rhs.value.description" operatorSelector:EOQualifierOperatorCaseInsensitiveLike value:value]];
            qual = [[EOOrQualifier alloc] initWithQualifierArray:innerQuals];
        } NS_ENDHANDLER;
        
        [self setQualifier:qual];
    } else {
        [self setQualifier:nil];
    }
    
    [self rearrangeObjects];
}

- (NSArray *)arrangeObjects:(NSArray *)objects {
    NSArray *arrangedObjects;
    EOQualifier *qualifier = [self qualifier];
    
    if (qualifier == nil) {
        return [super arrangeObjects:objects];
    }
    
    arrangedObjects = [objects filteredArrayUsingQualifier:qualifier];
    
    return arrangedObjects;
}

- (EOQualifier *)qualifier {
    return _qualifier;
}

- (void)setQualifier:(EOQualifier *)qualifier {
    [_qualifier release];
    
    _qualifier = [qualifier retain];
}

- (BOOL)canAdd {
    if ([self qualifier]) {
        return NO;
    }
    
    return [super canInsert];
}

// Adding and removing objects
- (void)insertObject:(id)object atArrangedObjectIndex:(unsigned int)index {
	NSUndoManager *um = [self undoManager];
	[[[self undoManager] prepareWithInvocationTarget:self] removeObjectAtArrangedObjectIndex:index];
	[um setActionName:([um isUndoing]) ? @"Remove Rule" : @"Insert Rule"];
	
	[super insertObject:object atArrangedObjectIndex:index];
}

- (void)insertObjects:(NSArray *)objects atArrangedObjectIndexes:(NSIndexSet *)indexes {
	NSUndoManager *um = [self undoManager];
	[[um prepareWithInvocationTarget:self] removeObjectsAtArrangedObjectIndexes:indexes];
	
	[um disableUndoRegistration];
	[super insertObjects:objects atArrangedObjectIndexes:indexes];
	[um enableUndoRegistration];
	
	[um setActionName:([um isUndoing]) ? [NSString stringWithFormat:@"Remove %i Rules", [indexes count]] : [NSString stringWithFormat:@"Insert %i Rules", [indexes count]]];
}

- (void)removeObjectAtArrangedObjectIndex:(unsigned int)index {
	NSUndoManager *um = [self undoManager];
	id object = [[self arrangedObjects] objectAtIndex:index];
	[[um prepareWithInvocationTarget:self] insertObject:object atArrangedObjectIndex:index];
	[um setActionName:([um isUndoing]) ? @"Insert Rule" : @"Remove Rule"];
	
	[super removeObjectAtArrangedObjectIndex:index];
}

- (void)removeObjectsAtArrangedObjectIndexes:(NSIndexSet *)indexes {
	NSUndoManager *um = [self undoManager];
	NSArray *objects = [[self arrangedObjects] objectsAtIndexes:indexes];
	[[um prepareWithInvocationTarget:self] insertObjects:objects atArrangedObjectIndexes:indexes];
	
	[um disableUndoRegistration];
	[super removeObjectsAtArrangedObjectIndexes:indexes];
	[um enableUndoRegistration];
	
	[um setActionName:([um isUndoing]) ? [NSString stringWithFormat:@"Insert %i Rules", [indexes count]] : [NSString stringWithFormat:@"Remove %i Rules", [indexes count]]];
}

// Undo management
- (NSUndoManager *)undoManager {
	return [[[NSDocumentController sharedDocumentController] currentDocument] undoManager];
}

- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue {
	NSUndoManager *um = [self undoManager];
	
	if ([um isUndoing]) {
		[um setActionName:[NSString stringWithFormat:format, oldValue]];
	} else {
		[um setActionName:[NSString stringWithFormat:format, newValue]];
	}
}

@end
