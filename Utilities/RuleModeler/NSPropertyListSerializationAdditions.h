//
//  NSPropertyListSerializationAdditions.h
//  RuleModeler
//
//  Created by King Chung Huang on 11/3/04.
//  Copyright 2004 King Chung Huang. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface NSPropertyListSerialization (NSPropertyListSerializationAdditions)

+ (NSString *)openStepFormatStringFromPropertyList:(id)plist prettyPrint:(BOOL)flag escapeNonASCII:(BOOL)escapeNonASCII errorDescription:(NSString **)errorString;
+ (NSData *)openStepFormatDataFromPropertyList:(id)plist prettyPrint:(BOOL)flag escapeNonASCII:(BOOL)escapeNonASCII errorDescription:(NSString **)errorString;
+ (NSString *)openStepFormatStringFromPropertyList:(id)plist level:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII errorDescription:(NSString **)errorString;

+ (void)_appendDictionary:(NSDictionary *)plist toMutableString:(NSMutableString *)str level:(int)level maxLevel:(int)maxLevel escapeNonASCII:(BOOL)escapeNonASCII;

@end
