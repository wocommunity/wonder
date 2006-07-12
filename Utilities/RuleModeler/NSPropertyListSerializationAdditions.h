//
//  NSPropertyListSerializationAdditions.h
//  RuleModeler
//
//  Created by King Chung Huang on 11/3/04.
//  Copyright 2004 King Chung Huang. All rights reserved.
//

#import <Cocoa/Cocoa.h>


@interface NSPropertyListSerialization (NSPropertyListSerializationAdditions)

+ (NSData *)openStepFormatDataFromPropertyList:(id)plist prettyPrint:(BOOL)flag errorDescription:(NSString **)errorString;

+ (void)_appendObject:(id)plist toMutableString:(NSMutableString *)str level:(int)level;
+ (void)_appendString:(NSString *)plist toMutableString:(NSMutableString *)str level:(int)level;
+ (void)_appendArray:(NSArray *)plist toMutableString:(NSMutableString *)str level:(int)level;
+ (void)_appendDictionary:(NSDictionary *)plist toMutableString:(NSMutableString *)str level:(int)level;
+ (int)_nextLevel:(int)currentLevel;

@end
