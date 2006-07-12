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
