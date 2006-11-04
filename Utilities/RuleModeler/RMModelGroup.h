//
//  RMModelGroup.h
//  RuleModeler
//
//  Created by Dave Lopper on 8/14/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import <Cocoa/Cocoa.h>


extern NSString *RMModelGroupType;


@class RMModel;
@class Rule;


@interface RMModelGroup : NSDocument {
    NSMutableArray  *_rules;
    NSMutableArray  *_modelContainers;
    NSURL           *_futureURL;
}

- (RMModel *)addModelWithURL:(NSURL *)url relativePath:(BOOL)relativePath error:(NSError **)outError;
- (void)addModelsWithURLs:(NSArray *)urls relativePaths:(BOOL)relativePaths error:(NSError **)outError;

- (void)removeModels:(NSArray *)models;
- (void)removeObjectFromModelContainersAtIndex:(unsigned)index;
- (void)removeModelWithURL:(NSURL *)url relativePath:(BOOL)relativePath error:(NSError **)outError;

- (NSArray *)rules;
- (unsigned)countOfRules;
- (Rule *)objectInRulesAtIndex:(unsigned)theIndex;
- (void)getRules:(Rule **)objsPtr range:(NSRange)range;
- (void)insertObject:(Rule *)obj inRulesAtIndex:(unsigned)theIndex;
- (void)removeObjectFromRulesAtIndex:(unsigned)theIndex;
- (void)replaceObjectInRulesAtIndex:(unsigned)theIndex withObject:(Rule *)obj;

- (NSArray *) modelContainers;
- (void) insertObject:(id)obj inModelContainersAtIndex:(unsigned)theIndex;
- (void) removeObjectFromModelContainersAtIndex:(unsigned)theIndex;

@end
