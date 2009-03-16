//
//  NSPlistDescriptions.h
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface NSDictionary (NSPlistDescriptions)

- (NSString *)descriptionNoWrap;

@end


@interface NSArray (NSPlistDescriptions)

- (NSString *)descriptionNoWrap;

@end
