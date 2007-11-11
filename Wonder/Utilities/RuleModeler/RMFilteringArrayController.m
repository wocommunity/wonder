/*
 RMFilteringArrayController.m
 RuleModeler

 Created by King Chung Huang on 1/30/04.


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

#import "RMFilteringArrayController.h"
#import "EOControl.h"

@implementation RMFilteringArrayController

// It is not possible to bind the searchField's value to some custom attribute,
// because the searchField resigns firstResponder after each key press.

- (void)doSetSearchWords:(NSArray *)newSearchWords {
    if(searchWords != newSearchWords){
        [searchWords release];
        searchWords = [newSearchWords copy];
    }
}

- (IBAction)search:(id)sender {
    [self willChangeValueForKey:@"searchWords"];
    
    NSString    *value = [sender stringValue];
    NSPredicate *predicate = nil;
    BOOL        performTextualSearch = NO;
    
    if (value != nil && [value length] > 0) {
        // The following block is here to fix a bug in AppKit:
        // Paste multiple lines in searchField -> garbage on refresh of searchField -> we remove line returns from pasted string
        if([value rangeOfString:@"\n"].length > 0){
            value = [[value componentsSeparatedByString:@"\n"] componentsJoinedByString:@" "];
            [searchField setStringValue:value];
        }
        if(![self textualSearchOnly]){
            NS_DURING {
                predicate = [NSPredicate predicateWithFormat:[value stringByAppendingString:@" or (isNewRule = YES)"]];
                // WARNING If user typed a valid qualifier (format), but qualifier raises an exception when applied to rules,
                // then searchField will be reset.
            } NS_HANDLER {
                // NSPredicate doesn't know 'caseInsensitiveLike' operator, unlike EOQualifier.
                // Let's replace it with 'like[c]'.
                NSMutableString *modifiedValue = [value mutableCopy];
                unsigned        replacementCount = [modifiedValue replaceOccurrencesOfString:@" caseInsensitiveLike " withString:@" like[c] " options:0 range:NSMakeRange(0, [value length])];
                
                if (replacementCount > 0) {
                    NS_DURING {
                        predicate = [NSPredicate predicateWithFormat:[modifiedValue stringByAppendingString:@" or (isNewRule = YES)"]];
                    } NS_HANDLER {
                        performTextualSearch = YES;
                    } NS_ENDHANDLER;
                }
                    else
                        performTextualSearch = YES;
                    [modifiedValue release];
            } NS_ENDHANDLER;
        }
        else
            performTextualSearch = YES;
        
        if (performTextualSearch) {
            NSString        *caseSensitivityOption = (searchIsCaseSensitive ? @"":@"[c]");
            NSString        *operator = (searchMatchesAnyWord ? @"or":@"and");
            NSMutableArray  *strings = [[value componentsSeparatedByString:@" "] mutableCopy];
            NSMutableArray  *predicates = [[NSMutableArray alloc] initWithCapacity:[strings count]];
            NSMutableArray  *arguments = [[NSMutableArray alloc] initWithCapacity:[strings count]];
            int             i;
            
            [strings removeObject:@""];
            [self doSetSearchWords:strings];
            for(i = 0; i < [strings count]; i++) {
                NSString *string = [strings objectAtIndex:i];
                NSRange range = [string rangeOfString:@"*"];
                if (range.location == NSNotFound) {
                    string = [NSString stringWithFormat:@"*%@*", string];
                }
                
                [arguments addObject: string];
                [arguments addObject: string];
                [arguments addObject: string];
                [predicates addObject:[NSString stringWithFormat:@"((lhsDescription like%@ %%@) or (rhs.keyPath like%@ %%@) or (rhs.valueDescription like%@ %%@))", caseSensitivityOption, caseSensitivityOption, caseSensitivityOption]];
                
            }
            NSString *format = [NSString stringWithFormat:@"%@ or (isNewRule = YES)", [predicates componentsJoinedByString:[NSString stringWithFormat:@" %@ ", operator]]];
            predicate = [NSPredicate predicateWithFormat:format argumentArray:arguments];
            [predicates release];
            [arguments release];
            [strings release];
        } else {
            [self doSetSearchWords:[NSArray array]];
        }
    }
    else
        [self doSetSearchWords:[NSArray array]]; // Search has been reset
    
    NS_DURING {
        [self setFilterPredicate:predicate];
    } NS_HANDLER {
        // NSPredicate would raise for "l like ''", but only at eval time!
        [self setFilterPredicate:nil]; // Keeping current one would be as weird as clearing it; by clearing it, at least, we might show user there is a problem
        NSBeep();
    } NS_ENDHANDLER;
    [self rearrangeObjects];
    [self didChangeValueForKey:@"searchWords"];
}

- (id)newObject {
    id  newObject = [super newObject];
    
    [newObject setValue:[NSNumber numberWithBool:YES] forKey:@"isNewRule"]; // FIXME Not a good idea to hardcode that method here
    
    return newObject;
}

- (void)addObjects:(NSArray *)objects {
//    if([objects count])
//        NSLog(@"addObjects: 1st = %p, %@", [objects objectAtIndex:0], [objects objectAtIndex:0]);
    [objects setValue:[NSNumber numberWithBool:YES] forKey:@"isNewRule"]; // FIXME Not a good idea to hardcode that method here
    
    if(0/*focussedObjects*/){
        NSArray *saved = focussedObjects; // Already retained
        
        focussedObjects = nil;
        [self rearrangeObjects];
        [super addObjects:objects];
        focussedObjects = [[saved arrayByAddingObjectsFromArray:objects] retain];
        [saved release];
        [self rearrangeObjects];
    }
    else{
        if(![self clearsFilterPredicateOnInsertion]){
            [self setClearsFilterPredicateOnInsertion:YES]; // If we don't set it to YES, then an exception will raise: 'Error inserting object at arranged object index X' when (focussed and) filtered, though index is correct
            if(focussedObjects){
                NSArray     *saved = focussedObjects; // Already retained
                NSPredicate *newFilterPredicate = nil;
                
                if([[self filterPredicate] isKindOfClass:[NSCompoundPredicate class]] && [[(NSComparisonPredicate *)[[(NSCompoundPredicate *)[self filterPredicate] subpredicates] objectAtIndex:0] rightExpression] constantValue] == focussedObjects)
                    newFilterPredicate = [[[[(NSCompoundPredicate *)[self filterPredicate] subpredicates] objectAtIndex:1] retain] autorelease]; // We need to retain+autorelease it, else it would disappear in -setFilterPredicate:
//                [focussedObjects release];
                focussedObjects = nil;
                [self setFilterPredicate:nil];

                [super addObjects:objects];
                
                focussedObjects = [[saved arrayByAddingObjectsFromArray:objects] retain];
                [saved release];
                [self setFilterPredicate:newFilterPredicate];
//                [self rearrangeObjects];
            }
            else{
                NSPredicate *newFilterPredicate = [[self filterPredicate] retain];

                [self setFilterPredicate:nil];
                
                [super addObjects:objects];

                [self setFilterPredicate:newFilterPredicate];
                [newFilterPredicate release];
            }
            [self setClearsFilterPredicateOnInsertion:NO];
        }
        else
        [super addObjects:objects]; // FIXME Raises 'Error inserting object at arranged object index X' when (focussed and) filtered, though index is correct
    }
}

- (NSArray *)arrangeObjects:(NSArray *)objects {
    if (0/*focussedObjects*/) {
        // We need to check that all focussed objects are still in objects:
        // if an objects' item is deleted (externally), then we must no longer
        // display that item, but, to allow user to undo the deletion and see
        // the object back in the focussed objects, we keep it in our
        // focussedObjects list.
        // There is one problem though: if deleted item was selected, on undo it
        // is no longer selected.
#if 0
        NSMutableArray  *passedObjects = [NSMutableArray arrayWithArray:focussedObjects];
        NSEnumerator    *anEnum = [focussedObjects objectEnumerator];
        id              eachObject;
        
        while (eachObject = [anEnum nextObject]) {
            unsigned    anIndex = [objects indexOfObjectIdenticalTo:eachObject];
            
            if (anIndex == NSNotFound) {
                [passedObjects removeObjectIdenticalTo:eachObject];
            }
        }
  
        return [super arrangeObjects:passedObjects];
#else
#endif
    }
    else{
        NSArray     *result = [super arrangeObjects:objects];
//        NSLog(@"arrangeObjects:(%d objects) returns %d objects", [objects count], [result count]);
        return result;
//        return [super arrangeObjects:objects];
    }
}

- (IBAction)focus:(id)sender {
    [self willChangeValueForKey:@"focussing"];
    [self willChangeValueForKey:@"canUnfocus"];
    [self willChangeValueForKey:@"focusImage"];
    [focussedObjects release];
    focussedObjects = [[NSMutableArray alloc] initWithArray:[self selectedObjects]];
    if([[NSUserDefaults standardUserDefaults] boolForKey:@"focusResetsSearch"])
        [self setFilterPredicate:nil];
    [self rearrangeObjects];
    if(![[NSUserDefaults standardUserDefaults] boolForKey:@"focusKeepsSelection"])
        [self setSelectedObjects:[NSArray array]];
    [self didChangeValueForKey:@"focusImage"];
    [self didChangeValueForKey:@"canUnfocus"];
    [self didChangeValueForKey:@"focussing"];
}

- (IBAction)unfocus:(id)sender {
    [self willChangeValueForKey:@"focussing"];
    [self willChangeValueForKey:@"canUnfocus"];
    [self willChangeValueForKey:@"focusImage"];
//    NSAssert([[self filterPredicate] isKindOfClass:[NSCompoundPredicate class]] && [[[(NSComparisonPredicate *)[[(NSCompoundPredicate *)[self filterPredicate] subpredicates] objectAtIndex:0] rightExpression] arguments] objectAtIndex:0] == focussedObjects);
    if(focussedObjects){
        NSPredicate *newFilterPredicate = nil;
        
        if([[self filterPredicate] isKindOfClass:[NSCompoundPredicate class]] && [[(NSComparisonPredicate *)[[(NSCompoundPredicate *)[self filterPredicate] subpredicates] objectAtIndex:0] rightExpression] constantValue] == focussedObjects)
            newFilterPredicate = [[[[(NSCompoundPredicate *)[self filterPredicate] subpredicates] objectAtIndex:1] retain] autorelease]; // We need to retain+autorelease it, else it would disappear in -setFilterPredicate:
        [focussedObjects release];
        focussedObjects = nil;
        [self setFilterPredicate:newFilterPredicate];
    }
    [self rearrangeObjects];
    [self didChangeValueForKey:@"focusImage"];
    [self didChangeValueForKey:@"canUnfocus"];
    [self didChangeValueForKey:@"focussing"];
}

- (BOOL)focussing {
    return focussedObjects != nil;
}

- (BOOL)canFocus {
    unsigned    selectionCount = [[self selectionIndexes] count];
    
    return selectionCount > 0 && ((focussedObjects == nil && selectionCount != [[self valueForKeyPath:@"content.@count"] intValue]) || (focussedObjects != nil && selectionCount != [focussedObjects count]));
}

- (BOOL)canUnfocus {
    return focussedObjects != nil;
}

- (id)initWithCoder:(NSCoder *)coder {
    if (self = [super initWithCoder:coder]) {
        textualSearchOnly = [[NSUserDefaults standardUserDefaults] boolForKey:@"textualSearchOnly"];
    }
    
    return self;
}

- (void)dealloc {
    [focussedObjects release];
    [focusImage release];
    [emptyImage release];
    [searchField release];
    [super dealloc];
}

- (NSImage *)focusImage {
    // We can't rely on the 'hidden' binding, because on window opening,
    // image is always displayed in the tableView's cornerView, even when hidden is true.
    if ([self canUnfocus]) {
        if (focusImage == nil)
            focusImage = [[NSImage imageNamed:@"focus"] retain];
        return focusImage;
    } else {
        if (emptyImage == nil)
            emptyImage = [[NSImage imageNamed:@"empty"] retain];
        return emptyImage;
    }
}

- (void)setFilterPredicate:(NSPredicate *)filterPredicate {
    NSPredicate *newFilterPredicate = filterPredicate;
    
    if(focussedObjects){
        NSPredicate *focusPredicate = [NSPredicate predicateWithFormat:@"self IN %@" argumentArray:[NSArray arrayWithObject:focussedObjects]];
        
        if(filterPredicate)
            newFilterPredicate = [[[NSCompoundPredicate alloc] initWithType:NSAndPredicateType subpredicates:[NSArray arrayWithObjects:focusPredicate, filterPredicate, nil]] autorelease];
        else
            newFilterPredicate = focusPredicate;
    }
    [[self arrangedObjects] setValue:[NSNumber numberWithBool:NO] forKey:@"isNewRule"]; // FIXME Not a good idea to hardcode that method here
    [super setFilterPredicate:newFilterPredicate];
    if(filterPredicate == nil)
        [searchField setStringValue:@""];
}

- (void)updateSearchFieldPlaceholder
{
    NSString    *aPlaceholder;
    
    if(textualSearchOnly){
        if(searchIsCaseSensitive){
            if(searchMatchesAnyWord)
                aPlaceholder = NSLocalizedString(@"Terms (any, case-sensitive)", @"Search field placeholder");
            else
                aPlaceholder = NSLocalizedString(@"Terms (all, case-sensitive)", @"Search field placeholder");
        }
        else{
            if(searchMatchesAnyWord)
                aPlaceholder = NSLocalizedString(@"Terms (any, case-insensitive)", @"Search field placeholder");
            else
                aPlaceholder = NSLocalizedString(@"Terms (all, case-insensitive)", @"Search field placeholder");
        }
    }
    else
        aPlaceholder = NSLocalizedString(@"Terms or EOQualifier format", @"Search field placeholder");
    
    [[searchField cell] setPlaceholderString:aPlaceholder];
}

- (NSSearchField *)searchField
{
    return searchField;
}

- (void)setSearchField:(NSSearchField *)value {
    if (searchField != value) {
        NSSearchField *  oldValue = searchField;
        
        searchField = (value != nil ? [value retain] : nil);
        if (oldValue != nil)
            [oldValue release];
        [self updateSearchFieldPlaceholder];
    }
}

// Binding the 'value' of a menu item does not sync model when menu state is changed. Bug in AppKit?

- (IBAction)changeCaseSensitivityOption:(id)sender {
    [self willChangeValueForKey:@"searchIsCaseSensitive"];
    searchIsCaseSensitive = ([sender state] != NSOnState); // Invert option
    [self didChangeValueForKey:@"searchIsCaseSensitive"];
    [sender setState:(searchIsCaseSensitive ? NSOnState:NSOffState)];
    [self updateSearchFieldPlaceholder];
    [self search:searchField];
}

- (IBAction)changeWordMatchingOption:(id)sender {
    [self willChangeValueForKey:@"searchMatchesAnyWord"];
    searchMatchesAnyWord = ([sender state] != NSOnState); // Invert option
    [self didChangeValueForKey:@"searchMatchesAnyWord"];
    [sender setState:(searchMatchesAnyWord ? NSOnState:NSOffState)];
    [self updateSearchFieldPlaceholder];
    [self search:searchField];
}

- (IBAction)changeParsingOption:(id)sender {
    [self willChangeValueForKey:@"textualSearchOnly"];
    textualSearchOnly = ([sender state] != NSOnState); // Invert option
    [self didChangeValueForKey:@"textualSearchOnly"];
    [sender setState:(textualSearchOnly ? NSOnState:NSOffState)];
    [self updateSearchFieldPlaceholder];
    // TODO Maybe we should also disable 'search as you type' when not textual search?
    [self search:searchField];
}

- (BOOL)searchIsCaseSensitive {
    return searchIsCaseSensitive;
}

- (NSArray *)searchWords {
    return searchWords;
}

- (BOOL)textualSearchOnly {
    return textualSearchOnly;
}

- (void)setTextualSearchOnly:(BOOL)newTextualSearchOnly {
    if(textualSearchOnly != newTextualSearchOnly){
        textualSearchOnly = newTextualSearchOnly;
    }
}

- (BOOL)validateMenuItem:(id <NSMenuItem>)anItem
{
    if(([anItem action] == @selector(changeCaseSensitivityOption:) || [anItem action] == @selector(changeWordMatchingOption:)) && !textualSearchOnly)
        return NO;
    else
        return [super validateMenuItem:anItem];
}

@end
