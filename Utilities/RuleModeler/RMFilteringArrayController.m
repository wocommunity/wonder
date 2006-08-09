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
    NSPredicate *predicate = nil;
    
    if (value != nil && [value length] > 0) {
        NS_DURING {
            predicate = [NSPredicate predicateWithFormat:[value stringByAppendingString:@" or (isNewRule = YES)"]];
        } NS_HANDLER {
            NSArray *strings = [value componentsSeparatedByString:@" "];
            NSMutableArray *predicates = [NSMutableArray arrayWithCapacity: [strings count]];
            NSMutableArray *arguments = [NSMutableArray arrayWithCapacity: [strings count]];
            int i;
            for(i = 0; i < [strings count]; i++) {
                NSString *string = [strings objectAtIndex:i];
                NSRange range = [string rangeOfString:@"*"];
                if (range.location == NSNotFound) {
                    string = [NSString stringWithFormat:@"*%@*", string];
                }
                
                [arguments addObject: string];
                [arguments addObject: string];
                [arguments addObject: string];
                [predicates addObject:@"((lhsDescription like[c] %@) or (rhs.keyPath like[c] %@) or (rhsValueDescription like[c] %@))"];
                
            }
            NSString *format = [NSString stringWithFormat:@"%@ or (isNewRule = YES)", [predicates componentsJoinedByString:@" and "]];
            predicate = [NSPredicate predicateWithFormat:format argumentArray:arguments];
            NSLog(@"searching for: %@", predicate);
        } NS_ENDHANDLER;
    }
        [self setFilterPredicate:predicate];
        [self rearrangeObjects];
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
