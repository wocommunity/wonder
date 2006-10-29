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
            NSMutableArray *predicates = [[NSMutableArray alloc] initWithCapacity:[strings count]];
            NSMutableArray *arguments = [[NSMutableArray alloc] initWithCapacity:[strings count]];
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
                [predicates addObject:@"((lhsDescription like[c] %@) or (rhs.keyPath like[c] %@) or (rhs.valueDescription like[c] %@))"];
                
            }
            NSString *format = [NSString stringWithFormat:@"%@ or (isNewRule = YES)", [predicates componentsJoinedByString:@" and "]];
            predicate = [NSPredicate predicateWithFormat:format argumentArray:arguments];
            [predicates release];
            [arguments release];
        } NS_ENDHANDLER;
    }
    [self setFilterPredicate:predicate];
    [self rearrangeObjects];
}

// Adding and removing objects
- (NSString *)actionNameWhenInserting:(BOOL)inserting ruleCount:(int)ruleCount 
{
    NSUndoManager *um = [self undoManager];
    NSString *anActionNameFormat;
    
    if ((inserting && ![um isUndoing]) || (!inserting && [um isUndoing])) {
        if(ruleCount > 1)
            anActionNameFormat = NSLocalizedString(@"Insert %i Rules", @"Undo-redo action name");
        else
            anActionNameFormat = NSLocalizedString(@"Insert %i Rule", @"Undo-redo action name");
    }
    else{
        if(ruleCount > 1)
            anActionNameFormat = NSLocalizedString(@"Remove %i Rules", @"Undo-redo action name");
        else
            anActionNameFormat = NSLocalizedString(@"Remove %i Rule", @"Undo-redo action name");
    }
    
    return [NSString stringWithFormat:anActionNameFormat, ruleCount];
}

- (void)insertObject:(id)object atArrangedObjectIndex:(unsigned int)index {
    NSUndoManager *um = [self undoManager];
    [[[self undoManager] prepareWithInvocationTarget:self] removeObjectAtArrangedObjectIndex:index];
    [um setActionName:[self actionNameWhenInserting:YES ruleCount:1]];
    [super insertObject:object atArrangedObjectIndex:index];
}

- (void)insertObjects:(NSArray *)objects atArrangedObjectIndexes:(NSIndexSet *)indexes {
    NSUndoManager *um = [self undoManager];
    [[um prepareWithInvocationTarget:self] removeObjectsAtArrangedObjectIndexes:indexes];
    
    [um disableUndoRegistration];
    [super insertObjects:objects atArrangedObjectIndexes:indexes];
    [um enableUndoRegistration];
    
    [um setActionName:[self actionNameWhenInserting:YES ruleCount:[indexes count]]];
}

- (void)removeObjectAtArrangedObjectIndex:(unsigned int)index {
    NSUndoManager *um = [self undoManager];
    id object = [[self arrangedObjects] objectAtIndex:index];
    [[um prepareWithInvocationTarget:self] insertObject:object atArrangedObjectIndex:index];
    [um setActionName:[self actionNameWhenInserting:NO ruleCount:1]];
    
    [super removeObjectAtArrangedObjectIndex:index];
}

- (void)removeObjectsAtArrangedObjectIndexes:(NSIndexSet *)indexes {
    NSUndoManager *um = [self undoManager];
    NSArray *objects = [[self arrangedObjects] objectsAtIndexes:indexes];
    [[um prepareWithInvocationTarget:self] insertObjects:objects atArrangedObjectIndexes:indexes];
    
    [um disableUndoRegistration];
    [super removeObjectsAtArrangedObjectIndexes:indexes];
    [um enableUndoRegistration];
    
    [um setActionName:[self actionNameWhenInserting:NO ruleCount:[indexes count]]];
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
