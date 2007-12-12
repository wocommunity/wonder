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

- (NSString*)encodePercentEscapesPerRFC2396 {
	return (NSString*)[(NSString*)CFURLCreateStringByAddingPercentEscapes (NULL, (CFStringRef)self, NULL, NULL, kCFStringEncodingUTF8) autorelease] ;
}

- (NSString*)encodePercentEscapesStrictlyPerRFC2396 {
	
	CFStringRef decodedString = (CFStringRef)[self decodeAllPercentEscapes] ;
	// The above may return NULL if url contains invalid escape sequences like %E8me, %E8fe, %E800 or %E811,
	// because CFURLCreateStringByReplacingPercentEscapes() isn't smart enough to ignore them.
	CFStringRef recodedString = CFURLCreateStringByAddingPercentEscapes (kCFAllocatorDefault, decodedString, NULL, NULL, kCFStringEncodingUTF8);
	// And then, if decodedString is NULL, recodedString will be NULL too.
	// So, we recover from this rare but possible error by returning the original self
	// because it's "better than nothing".
	NSString* answer = (recodedString != NULL) ? [(NSString*) recodedString autorelease] : self ;
	// Note that if recodedString is NULL, we don't need to CFRelease() it.
	// Actually, unlike [nil release], CFRelease(NULL) causes a crash. Thanks, Apple!
	return answer ;
}

- (NSString*)encodePercentEscapes {
	return [self encodePercentEscapesPerRFC2396ButNot:@"" butAlso:@"&"];
}

- (NSString*)encodePercentEscapesPerRFC2396ButNot:(NSString*)butNot butAlso:(NSString*)butAlso {
	return (NSString*)[(NSString*)CFURLCreateStringByAddingPercentEscapes (NULL, (CFStringRef)self, (CFStringRef)butNot, (CFStringRef)butAlso, kCFStringEncodingUTF8) autorelease] ;
}

- (NSString*)decodeAllPercentEscapes {
	// Unfortunately, CFURLCreateStringByReplacingPercentEscapes() seems to only replace %[NUMBER] escapes
	return (NSString*)[(NSString*) CFURLCreateStringByReplacingPercentEscapes(kCFAllocatorDefault, (CFStringRef)self, CFSTR("")) autorelease] ;
}

@end
