//
//  RMModel.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <AppKit/AppKit.h>
#import "NSPropertyListSerializationAdditions.h"

@class EOKeyValueArchiver;
@class EOKeyValueUnarchiver;

@interface RMModel : NSDocument {
    
    @private
    NSMutableArray		*_rules;   

}

- (NSMutableArray *)rules;
- (void)setRules:(NSMutableArray *)rules;

@end
