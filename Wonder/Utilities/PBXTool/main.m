#import <Foundation/Foundation.h>
#import <EOControl/EOSortOrdering.h>

int sortSourceIntoDest(NSString* source, NSString* dest);



int main (int argc, const char * argv[]) {
    int result=0;
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    NSArray* arguments = [[NSProcessInfo processInfo] arguments];
    NSString* appName = @"pbxtool";
    if ([arguments count] > 0) {
        appName=[arguments objectAtIndex:0];
    }
    if ([arguments count] != 3) {
        
        NSLog(@"Usage: pbxtool <source pbx> <dest pbx>");
        return 1;
    }

    result=sortSourceIntoDest([arguments objectAtIndex:1],[arguments objectAtIndex:2]);
    
    [pool release];
    return result;
}


int sortSourceIntoDest(NSString* source, NSString* dest) {
    NSDictionary* plist=[NSDictionary dictionaryWithContentsOfFile:source];
    NSDictionary* objects=[plist objectForKey:@"objects"];
    NSEnumerator* e=[objects objectEnumerator];
    NSMutableDictionary* o;
    while (o=[e nextObject]) {
        NSString* type=[o objectForKey:@"isa"];
        if ([type isEqualTo:@"PBXGroup"]) {
            NSArray* children=[o objectForKey:@"children"];
            NSMutableArray* childrenBody=[NSMutableArray new];
            NSEnumerator* e2=[children objectEnumerator];
            NSString* childID;
            NSArray *nameOrdering = [NSArray arrayWithObjects:
                [EOSortOrdering sortOrderingWithKey:@"name" selector:EOCompareAscending],
                nil];
            
            while (childID=[e2 nextObject]) {
                NSMutableDictionary* pair=[NSMutableDictionary new];
                NSString* name=[[objects objectForKey:childID] objectForKey:@"name"];

                if (!name)
                    name=[[objects objectForKey:childID] objectForKey:@"path"];

                [pair setObject:childID forKey:@"id"];
                
                [pair setObject:name forKey:@"name"];
                [childrenBody addObject:pair];
            }

            {
                NSArray* sortedChildren=[childrenBody sortedArrayUsingKeyOrderArray:nameOrdering];
                NSMutableArray* sortedChildIds=[NSMutableArray new];
                NSEnumerator* e3=[sortedChildren objectEnumerator];
                NSDictionary* child;
                while (child=[e3 nextObject]) {
                    [sortedChildIds addObject:[child objectForKey:@"id"]];
                }
                // finally replace
                [o setObject:sortedChildIds forKey:@"children"];
            }

        }
    }
    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInt:1]
                                              forKey:@"NSWriteOldStylePropertyLists"];
    return [plist writeToFile:dest atomically:NO];

}





