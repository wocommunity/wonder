#import <Foundation/Foundation.h>

/*
 D2WSort is a tool to sort d2wModel files prior to saving them to cvs.

 Usage:
  D2WSort user.d2wmodel author.intValue rhs.keyPath description

 This would sort first in the rule priority, then the keypath of the right hand side and last on the description.

 Author: Anjo Krank (ak@prnet.de) (c) 2001
 */


static NSArray *sortArray;
static NSString *key1, *key2, *key3;

static int compareDictEntries(id o1, id o2, void *dummy) {
    NSComparisonResult result;

    result = [[o1 valueForKeyPath:key1] compare:[o2 valueForKeyPath:key1]];
    // NSLog(@"%d:%@-%@",result,[o1 valueForKeyPath:key1],[o2 valueForKeyPath:key1]);
    if(result == NSOrderedSame && key2 != nil) {
	result = [[o1 valueForKeyPath:key2] compare:[o2 valueForKeyPath:key2]];
	// NSLog(@"  %d:%@-%@",result,[o1 valueForKeyPath:key2],[o2 valueForKeyPath:key2]);
	if(result == NSOrderedSame && key3 != nil) {
	    result = [[o1 valueForKeyPath:key3] compare:[o2 valueForKeyPath:key3]];
	    // NSLog(@"    %d:%@-%@",result,[o1 valueForKeyPath:key3],[o2 valueForKeyPath:key3]);
	}
    }
    return result;
}

@implementation NSMutableDictionary(additions)
- (NSComparisonResult)compare:(id)other {
    return [[self description] compare:[other description]];
}

/*
- (id) author {
    return [NSDecimalNumber decimalNumberWithString:[self objectForKey:@"author"]];
}*/
@end

int main (int argc, const char * argv[]) {
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    NSArray *arguments = [[NSProcessInfo processInfo] arguments];
    NSMutableDictionary *objects;
    id plist;
    NSString *appName;
    
    // insert code here...
    if ([arguments count] > 0) {
        appName=[arguments objectAtIndex:0];
    }
    if ([arguments count] < 2) {

        NSLog(@"Usage: D2WSorter <d2wmodel> key1 [key2 [key3]]\n keyN = author|rhs|lhs");
        return 1;
    }
    key1 = [arguments objectAtIndex:2];
    if([arguments count] > 3)
	key2 = [arguments objectAtIndex:3];
    if([arguments count] > 4)
	key3 = [arguments objectAtIndex:4];
    // NSLog(@"%@,%@,%@", key1, key2, key3);
   
    plist = [NSDictionary dictionaryWithContentsOfFile:[arguments objectAtIndex:1]];
    // NSLog([plist description]);

    [plist setObject:[[plist objectForKey:@"rules"] sortedArrayUsingFunction:compareDictEntries context:nil] forKey:@"rules"];

    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInt:1] forKey:@"NSWriteOldStylePropertyLists"];
    [plist writeToFile:[arguments objectAtIndex:1] atomically:NO];
    // NSLog([[[[plist objectForKey:@"rules"] objectAtIndex:0] valueForKeyPath:key2] description]);
    [pool release];
    return 0;
}
