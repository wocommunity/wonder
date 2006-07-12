//
//  NSArrayAdditions.m
//  RuleModeler
//
//  Created by King Chung Huang on 10/31/04.
//  Copyright 2004 King Chung Huang. All rights reserved.
//

#import "NSArrayAdditions.h"

@implementation NSArray (NSArrayAdditions)
- (NSArray *)objectsAtIndexes:(NSIndexSet *)indexes {
	NSMutableArray *objects = [NSMutableArray arrayWithCapacity:[indexes count]];
	unsigned int index = [indexes firstIndex];
	
	while (index != NSNotFound) {
		[objects addObject:[self objectAtIndex:index]];
		
		index = [indexes indexGreaterThanIndex:index];
	}
	
	return objects;
}

@end
