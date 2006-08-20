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

@end
