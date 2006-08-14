//
//  EOQualifierUpdates.m
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "EOQualifierUpdates.h"


@implementation EOKeyValueQualifier (EOQualifierUpdates)

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
    NSString *key = [unarchiver decodeObjectForKey:@"key"];
    NSString *selectorName = [unarchiver decodeObjectForKey:@"selectorName"];
    NSObject *value = [unarchiver decodeObjectForKey:@"value"];
    
    if (![selectorName hasSuffix:@":"])
		selectorName = [NSString stringWithFormat:@"%@:", selectorName];
        
    return [self initWithKey:key operatorSelector:NSSelectorFromString(selectorName) value:value];
}

@end

@implementation EOAndQualifier (RuleModeler)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[EOAndQualifier class]]) {
        return [[self qualifiers] isEqual:[anObject qualifiers]]; // We should ignore order in our case, shouldn't we?
    } else {
        return NO;
    }
}

@end

@implementation EONotQualifier (RuleModeler)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[EONotQualifier class]]) {
        return [[self qualifier] isEqual:[anObject qualifier]];
    } else {
        return NO;
    }
}

@end

@implementation EOOrQualifier (RuleModeler)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[EOOrQualifier class]]) {
        return [[self qualifiers] isEqual:[anObject qualifiers]]; // We should ignore order in our case, shouldn't we?
    } else {
        return NO;
    }
}

@end

@implementation EOKeyComparisonQualifier (RuleModeler)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[EOKeyComparisonQualifier class]]) {
        return ([[self leftKey] isEqual:[anObject leftKey]] && [self selector] == [anObject selector] && [[self rightKey] isEqual:[anObject rightKey]]);
    } else {
        return NO;
    }
}

@end

@implementation EOKeyValueQualifier (RuleModeler)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[EOKeyValueQualifier class]]) {
        return ([[self key] isEqual:[anObject key]] && [self selector] == [anObject selector] && [[self value] isEqual:[anObject value]]);
    } else {
        return NO;
    }
}

@end

