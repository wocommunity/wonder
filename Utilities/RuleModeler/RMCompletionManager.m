/*
 RMCompletionManager.m
 RuleModeler

 Created by davelopper on 2/22/07.


 Copyright (c) 2004-2007, Project WONDER <http://wonder.sourceforge.net/>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  * Neither the name of the Project WONDER nor the names of its contributors may
    be used to endorse or promote products derived from this software without
    specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#import "RMCompletionManager.h"


@implementation RMCompletionManager

+ (id)sharedManager {
    static RMCompletionManager *sharedManager = nil;
    
    if(sharedManager == nil)
        sharedManager = [[self alloc] init];
    return sharedManager;
}

- (id) init {
    self = [super init];
    if (self != nil) {
        completionListByName = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void) dealloc {
    [completionListByName release];
    [super dealloc];
}

- (NSArray *)textView:(NSTextView *)textView completionsForPartialWordRange:(NSRange)charRange indexOfSelectedItem:(int *)index fromCompletionListNamed:(NSString *)completionListName {
    NSArray *completionList = [self completionsInCompletionListNamed:completionListName];
    NSArray *matches = [completionList filteredArrayUsingPredicate:[NSComparisonPredicate predicateWithLeftExpression:[NSExpression expressionForEvaluatedObject] rightExpression:[NSExpression expressionForConstantValue:[[textView string] substringWithRange:charRange]] modifier:NSDirectPredicateModifier type:NSBeginsWithPredicateOperatorType options:NSCaseInsensitivePredicateOption]];
    
    return matches;
}

- (NSArray *)completionsInCompletionListNamed:(NSString *)completionListName {
    NSArray     *completions = [completionListByName objectForKey:completionListName];
    
    if (completions == nil) {
        [self loadCompletionListNamed:completionListName];
        completions = [completionListByName objectForKey:completionListName];
    }
    
    return completions;
}

- (NSString *)completedString:(NSString *)string fromCompletionListNamed:(NSString *)completionListName {
    NSArray     *completionList = [self completionsInCompletionListNamed:completionListName];
    int         i, count = [completionList count];
    NSString    *complete;
    
    for (i = 0; i < count; i++) {
        complete = [completionList objectAtIndex:i];
        
        // Case-insensitive in this case too?
        if ([complete hasPrefix:string]) {
            return complete;
        }
    }
    
    return string;
}

- (void)addWord:(NSString *)word toCompletionListNamed:(NSString *)completionListName {
    NSMutableArray  *completionList = (NSMutableArray *)[self completionsInCompletionListNamed:completionListName];
    
    if(![completionList containsObject:word]){
        [completionList addObject:word];
        [completionList sortUsingSelector:@selector(caseInsensitiveCompare:)];
    }
}

- (void)addWords:(NSSet *)words toCompletionListNamed:(NSString *)completionListName {
    NSMutableSet    *allWords = [NSMutableSet setWithArray:[self completionsInCompletionListNamed:completionListName]];
    NSMutableArray  *completionList;

    [allWords unionSet:words];
    [allWords removeObject:[NSNull null]];
    [allWords removeObject:@""];
    completionList = [[allWords allObjects] mutableCopy];
    [completionList sortUsingSelector:@selector(caseInsensitiveCompare:)];
    [completionListByName setObject:completionList forKey:completionListName];
//    [completionList writeToFile:[NSTemporaryDirectory() stringByAppendingPathComponent:[[@"RMCompletionManager-" stringByAppendingString:completionListName] stringByAppendingPathExtension:@"plist"]] atomically:NO];
    [completionList release];
}

- (void)loadCompletionListNamed:(NSString *)completionListName {
    NSMutableArray  *completionList;
    NSMutableSet    *allWords;
    NSString        *aName = [@"RMCompletionManager-" stringByAppendingString:completionListName];
    NSArray         *defaultWords = [[NSUserDefaults standardUserDefaults] arrayForKey:aName];
    NSArray         *commonWords = [NSArray arrayWithContentsOfFile:[[NSBundle mainBundle] pathForResource:aName ofType:@"plist"]];
    
    allWords = [NSMutableSet setWithArray:[commonWords mutableCopy]];
    [allWords addObjectsFromArray:defaultWords];
    [allWords removeObject:@""];

    completionList = [[allWords allObjects] mutableCopy];
    [completionList sortUsingSelector:@selector(caseInsensitiveCompare:)];
//    [completionList writeToFile:[NSTemporaryDirectory() stringByAppendingPathComponent:[aName stringByAppendingPathExtension:@"plist"]] atomically:NO];
    [completionListByName setObject:completionList forKey:completionListName];
    [completionList release];
}

@end
