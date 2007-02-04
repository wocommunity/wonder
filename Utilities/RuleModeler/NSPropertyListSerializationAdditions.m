//
//  NSPropertyListSerializationAdditions.m
//  RuleModeler
//
//  Created by King Chung Huang on 11/3/04.
//  Copyright 2004 King Chung Huang. All rights reserved.
//

#import "NSPropertyListSerializationAdditions.h"

#define NO_INDENT_LEVEL (INT_MIN)
#define INDENT_CHARS    (2)

@interface NSPropertyListSerialization(NSPropertyListSerializationAdditions_Private)
+ (void)_appendObject:(id)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII canCreateNewLine:(BOOL)canCreateNewLine;
+ (void)_appendString:(NSString *)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII;
+ (void)_appendArray:(NSArray *)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII;
@end

@implementation NSPropertyListSerialization (NSPropertyListSerializationAdditions)

+ (NSString *)openStepFormatStringFromPropertyList:(id)plist level:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII errorDescription:(NSString **)errorString {
    NSMutableString *str = [NSMutableString stringWithCapacity:512];
    
    [NSPropertyListSerialization _appendObject:plist toMutableString:str level:0 maxLevel:maxLevel escapeNonASCII:escapeNonASCII canCreateNewLine:NO];
    
    return str;
}

+ (NSString *)openStepFormatStringFromPropertyList:(id)plist prettyPrint:(BOOL)flag escapeNonASCII:(BOOL)escapeNonASCII errorDescription:(NSString **)errorString {
    return [self openStepFormatStringFromPropertyList:plist level:(flag ? INT_MAX : 0) escapeNonASCII:escapeNonASCII errorDescription:errorString];
}

+ (NSData *)openStepFormatDataFromPropertyList:(id)plist prettyPrint:(BOOL)flag escapeNonASCII:(BOOL)escapeNonASCII errorDescription:(NSString **)errorString {
    return [[self openStepFormatStringFromPropertyList:plist prettyPrint:flag escapeNonASCII:escapeNonASCII errorDescription:errorString] dataUsingEncoding:NSUTF8StringEncoding];  // NSNEXTSTEPStringEncoding?
}

+ (void)_appendObject:(id)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII canCreateNewLine:(BOOL)canCreateNewLine {
    if(canCreateNewLine && level > 0 && level <= maxLevel) [str appendFormat:@"\n%*c", level * INDENT_CHARS, ' '];
    if ([plist isKindOfClass:[NSString class]]) {
        [NSPropertyListSerialization _appendString:plist toMutableString:str level:level+1 maxLevel:maxLevel escapeNonASCII:escapeNonASCII];
    } else if ([plist isKindOfClass:[NSArray class]]) {
        [NSPropertyListSerialization _appendArray:plist toMutableString:str level:level+1 maxLevel:maxLevel escapeNonASCII:escapeNonASCII];
    } else if ([plist isKindOfClass:[NSDictionary class]]) {
        [NSPropertyListSerialization _appendDictionary:plist toMutableString:str level:level+1 maxLevel:maxLevel escapeNonASCII:escapeNonASCII];
    } /*else if ([plist isKindOfClass:[NSData data]]) {
		[NSPropertyListSerialization _appendData:plist toMutableString:str level:level+1];
    } */else {
        NSLog(@"Unsupported class: %@", [plist class]);
    }
}

+ (void)_appendString:(NSString *)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII {
    // \n has to be replaced by "\n", literally; \t by "\t", \ by "\\"
    // and non-ASCII (and non-printable) characters by \Uxxxx
    // where xxxx is the Unicode code of the character.
    // We also need to escape " by \"
    NSMutableString *escapedString = [plist mutableCopy];
    int             i = [escapedString length] - 1;
    
    for (; i >= 0; i--) {
        unichar aChar = [escapedString characterAtIndex:i];
        
        switch(aChar) {
            case '\n':
                [escapedString replaceCharactersInRange:NSMakeRange(i, 1) withString:@"\\n"]; break;
            case '\t':
                [escapedString replaceCharactersInRange:NSMakeRange(i, 1) withString:@"\\t"]; break;
            case '"':
                [escapedString insertString:@"\\" atIndex:i]; break;
            case '\\':
                [escapedString insertString:@"\\" atIndex:i]; break;
            default:
                if (aChar < 32 || (escapeNonASCII && aChar >= 127)) {
                    [escapedString replaceCharactersInRange:NSMakeRange(i, 1) withString:[NSString stringWithFormat:@"\\U%04x", aChar]]; 
                }
        }
    }

    [str appendString:[NSString stringWithFormat:@"\"%@\"", escapedString]];
    [escapedString release];
}

+ (void)_appendArray:(NSArray *)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII {
    [str appendString:@"("];
    
    int i, count = [plist count];
    
    for (i = 0; i < count; i++) {
        if (i > 0) {
            [str appendString:@", "];
        }
        
        [NSPropertyListSerialization _appendObject:[plist objectAtIndex:i] toMutableString:str level:level maxLevel:maxLevel escapeNonASCII:escapeNonASCII canCreateNewLine:YES];
    }
    if (level <= maxLevel) {
        if(level > 1)
            [str appendFormat:@"\n%*c", (level - 1) * INDENT_CHARS, ' '];
        else
            [str appendString:@"\n"];
    }
    [str appendString:@")"];
}

+ (void)_appendDictionary:(NSDictionary *)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII {
    [str appendString:@"{"];
    
    NSArray *keys = [[plist allKeys] sortedArrayUsingSelector:@selector(compare:)];
    int i, count = [keys count];
    id key, value;
    
    for (i = 0; i < count; i++) {
        key = [keys objectAtIndex:i];
        value = [plist objectForKey:key];
        
        [NSPropertyListSerialization _appendObject:key toMutableString:str level:level maxLevel:maxLevel escapeNonASCII:escapeNonASCII canCreateNewLine:YES];
        [str appendString:@" = "];
        [NSPropertyListSerialization _appendObject:value toMutableString:str level:level maxLevel:maxLevel escapeNonASCII:escapeNonASCII canCreateNewLine:NO];
        
        [str appendString:@"; "];
    }
    
    if (level <= maxLevel) {
        if(level > 1)
            [str appendFormat:@"\n%*c", (level - 1) * INDENT_CHARS, ' '];
        else
            [str appendString:@"\n"];
    }
    [str appendString:@"}"];
}

@end
