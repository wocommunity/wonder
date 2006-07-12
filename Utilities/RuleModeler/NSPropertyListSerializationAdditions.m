//
//  NSPropertyListSerializationAdditions.m
//  RuleModeler
//
//  Created by King Chung Huang on 11/3/04.
//  Copyright 2004 King Chung Huang. All rights reserved.
//

#import "NSPropertyListSerializationAdditions.h"


@implementation NSPropertyListSerialization (NSPropertyListSerializationAdditions)

+ (NSData *)openStepFormatDataFromPropertyList:(id)plist prettyPrint:(BOOL)flag errorDescription:(NSString **)errorString {
    NSMutableString *str = [NSMutableString stringWithCapacity:512];
    
    int level = (flag) ? 0 : -1;
    
    [NSPropertyListSerialization _appendObject:plist toMutableString:str level:level];
    return [str dataUsingEncoding:NSUTF8StringEncoding];  // NSNEXTSTEPStringEncoding?
}

+ (void)_appendObject:(id)plist toMutableString:(NSMutableString *)str level:(int)level {
    if(level > 0 && level < 2) [str appendString:@"\n"];
    if ([plist isKindOfClass:[NSString class]]) {
        [NSPropertyListSerialization _appendString:plist toMutableString:str level:level+1];
    } else if ([plist isKindOfClass:[NSArray class]]) {
        [NSPropertyListSerialization _appendArray:plist toMutableString:str level:level+1];
    } else if ([plist isKindOfClass:[NSDictionary class]]) {
        [NSPropertyListSerialization _appendDictionary:plist toMutableString:str level:level+1];
    } /*else if ([plist isKindOfClass:[NSData data]]) {
		[NSPropertyListSerialization _appendData:plist toMutableString:str level:level];
    } */else {
        NSLog(@"Unsupported class: %@", [plist class]);
    }
}

+ (void)_appendString:(NSString *)plist toMutableString:(NSMutableString *)str level:(int)level {
    [str appendString:[NSString stringWithFormat:@"\"%@\"", plist]];
}

+ (void)_appendArray:(NSArray *)plist toMutableString:(NSMutableString *)str level:(int)level {
    [str appendString:@"("];
    
    int i, count = [plist count];
    
    for (i = 0; i < count; i++) {
        if (i > 0) {
            [str appendString:@", "];
        }
        
        [NSPropertyListSerialization _appendObject:[plist objectAtIndex:i] toMutableString:str level:level ];
    }
    
    [str appendString:@")"];
}

+ (void)_appendDictionary:(NSDictionary *)plist toMutableString:(NSMutableString *)str level:(int)level {
    [str appendString:@"{"];
    
    NSArray *keys = [plist allKeys];
    int i, count = [keys count];
    id key, value;
    
    for (i = 0; i < count; i++) {
        key = [keys objectAtIndex:i];
        value = [plist objectForKey:key];
        
        [NSPropertyListSerialization _appendObject:key toMutableString:str level:level];
        [str appendString:@" = "];
        [NSPropertyListSerialization _appendObject:value toMutableString:str level:level];
        
        [str appendString:@"; "];
    }
    
    [str appendString:@"}"];
}

+ (int)_nextLevel:(int)currentLevel {
    return (currentLevel != -1) ? currentLevel + 1 : currentLevel;
}

@end
