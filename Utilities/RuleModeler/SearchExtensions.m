//
//  SearchExtensions.m
//  RuleModeler
//
//  Created by Anjo Krank on 12.07.06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import "SearchExtensions.h"
#import "EOControl.h"


@implementation NSArray(SearchExtensions)
- matchesPattern:(NSString *)pattern caseInsensitive:(BOOL)yesNo {
    return [[self description] matchesPattern:pattern caseInsensitive:yesNo];
}
@end

@implementation NSDictionary(SearchExtensions)
- matchesPattern:(NSString *)pattern caseInsensitive:(BOOL)yesNo {
    return [[self description] matchesPattern:pattern caseInsensitive:yesNo];
}
@end

@implementation EONull(SearchExtensions)
- matchesPattern:(NSString *)pattern caseInsensitive:(BOOL)yesNo {
    return [[self description] matchesPattern:pattern caseInsensitive:yesNo];
}
@end
