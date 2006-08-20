//
//  NSStringAdditions.m
//  RuleModeler
//
//  Created by Dave Lopper on 8/16/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import "NSStringAdditions.h"


@implementation NSString(Additions)

- (NSString *)pathRelativeToPath:(NSString *)basePath {
    NSParameterAssert([self isAbsolutePath]);
    NSParameterAssert([basePath isAbsolutePath]);

    NSArray *myPathComponents = [[self stringByStandardizingPath] pathComponents];
    NSArray *basePathComponents = [[[basePath stringByStandardizingPath] stringByDeletingLastPathComponent] pathComponents];
    int     i, myCount = [myPathComponents count], baseCount = [basePathComponents count];
    
    for (i = 0; i < myCount && i < baseCount; i++) {
        NSString    *myComponent = [myPathComponents objectAtIndex:i];
        NSString    *baseComponent = [basePathComponents objectAtIndex:i];
        
        if (![myComponent isEqualToString:baseComponent]) {
            break;
        }
    }
    
    NSMutableArray  *components = [NSMutableArray array];
    int             j = baseCount - i;
    
    while (j--) {
        [components addObject:@".."];
    }
    [components addObjectsFromArray:[myPathComponents subarrayWithRange:NSMakeRange(i, myCount - i)]];
    
    return [NSString pathWithComponents:components];
}

@end
