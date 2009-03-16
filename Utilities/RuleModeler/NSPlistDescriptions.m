//
//  NSPlistDescriptions.m
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "NSPlistDescriptions.h"

@implementation NSDictionary (NSPlistDescriptions)

- (NSString *)descriptionNoWrap {
    NSArray *keys = [[self allKeys] sortedArrayUsingSelector:@selector(compare:)];
    NSString *key;
    NSString *value;
    NSObject *object;
    
    int i, count = [keys count];
    NSMutableString *desc = [NSMutableString stringWithCapacity:count * 32];
    
    [desc appendString:@"{ "];
    
    for (i = 0; i < count; i++) {
	key = [keys objectAtIndex:i];
	object = [self objectForKey:key];
	
	if ([object respondsToSelector:@selector(descriptionNoWrap)])
		value = [object performSelector:@selector(descriptionNoWrap)];
	else
	    value = [object description];
	
	[desc appendFormat:@"%@ = %@; ", key, value];
    }
    
    [desc appendString:@"}"];
    
    return desc;
}

@end


@implementation NSArray (NSPlistDescriptions)

- (NSString *)descriptionNoWrap {
    int i, count = [self count];
    NSMutableString *desc = [NSMutableString stringWithCapacity:count * 32];
    NSObject *object;
    NSString *value;
    
    [desc appendString:@"( "];
    
    for (i = 0; i < count; i++) {
	if (i > 0)
	    [desc appendString:@",\n"];
	
	object = [self objectAtIndex:i];
	
	if ([object respondsToSelector:@selector(descriptionNoWrap)])
		value = [object performSelector:@selector(descriptionNoWrap)];
	else
	    value = [object description];
	
	[desc appendString:value];
    }
    
    [desc appendString:@" )"];
    
    return desc;
}

@end

