#import <Foundation/Foundation.h>
#import <EOControl/EOSortOrdering.h>

/** PBXTool
Usage:

 PBXTool somedir.pbproj/project.pbxproj 
   - sorts the project and its groups

 PBXTool somedir.pbproj/project.pbxproj "some new group"
   - creates a new top level group if it doesn't exist
   - sorts the project and its groups

 PBXTool somedir.pbproj/project.pbxproj "some (new) group" file1 file2 ...
   - creates a new top level group if it doesn't exist
   - moves files to group, creating it if needed
   - sorts the project and its groups

Be sure to backup your project file first!

*/

@implementation NSMutableDictionary(additions)
/** these work on (items of) the "object" dictionary **/
- (NSString *)refNamed:(NSString *)name {
    NSEnumerator* e=[self keyEnumerator];
    id o;
    while (o=[e nextObject]) {
	id entry = [self objectForKey:o];
        if([name isEqualToString:[entry objectForKey:@"name"]] || [name isEqualToString:[entry objectForKey:@"path"]])
	    return o;
    }
    return nil;
}

- (NSMutableDictionary *) parentOf: (NSString *)refname {
    NSEnumerator* e=[self objectEnumerator];
    id o;
    while (o=[e nextObject]) {
        NSString* type=[o objectForKey:@"isa"];
        if ([type isEqualTo:@"PBXGroup"]) {
            NSArray* children=[o objectForKey:@"children"];
            NSEnumerator* e2=[children objectEnumerator];
            NSString* childID;

            while (childID=[e2 nextObject]) {
                if([refname isEqualToString:childID] )
		    return o;
	    }
	}
    }
    return nil;
}

- (void) removeFromChildren:(NSString *) ref {
    NSMutableArray *children = [self valueForKey:@"children"];
    [children removeObject:ref];
}

- (void) addToChildren:(NSString *) ref {
    NSMutableArray *children = [self valueForKey:@"children"];
    [children addObject:ref];
}

- (void)sortChildren {
    NSEnumerator* e=[self objectEnumerator];
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
		NSString* name=[[self objectForKey:childID] objectForKey:@"name"];

		if (!name)
		    name=[[self objectForKey:childID] objectForKey:@"path"];

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
}

@end


int main (int argc, const char * argv[]) {
    int result=0;
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    NSArray* arguments = [[NSProcessInfo processInfo] arguments];
    NSString* appName = @"pbxtool";
    NSDictionary* plist;
    NSMutableDictionary* objects;
    
    if ([arguments count] > 0) {
        appName=[arguments objectAtIndex:0];
    }
    if ([arguments count] < 2) {
        
        NSLog(@"Usage: pbxtool <source pbx> [(new)groupname item1 item2]");
        return 1;
    }
    
    plist = [NSDictionary dictionaryWithContentsOfFile:[arguments objectAtIndex:1]];
    objects = [plist objectForKey:@"objects"];
    if([arguments count] > 2) {
	int i;
	NSString *newGroupName = [arguments objectAtIndex:2];
	NSMutableDictionary *newGroup = [objects objectForKey:[objects refNamed:newGroupName]];
	
	if(newGroup == nil) {
	    NSMutableArray *newChildren = [NSMutableArray new];
	    NSString *mainGroupRef = [[objects valueForKey:[plist valueForKey:@"rootObject"]] valueForKey:@"mainGroup"];
	    
	    newChildren = [NSMutableArray new];
	    newGroup = [NSMutableDictionary dictionaryWithObjectsAndKeys:@"PBXGroup", @"isa", newGroupName, @"name", @"4", @"refType", nil, nil];

	    [newGroup setObject: newChildren forKey:@"children"];
	    // the keys must be unique, but I suppose we are safe as the key gets overwritten on the next safe in pbx
	    [objects setObject:newGroup forKey:newGroupName];

	    [[[objects valueForKey:mainGroupRef] valueForKey:@"children"] addObject:newGroupName];
	    NSLog(@"Created group %@", newGroupName);
	}
	for(i = 3; i < [arguments count]; i++) {
	    NSString *name = [arguments objectAtIndex:i];
	    NSString *ref = [objects refNamed:[arguments objectAtIndex:i]];
	    id parent = [objects parentOf:ref];
	    if(parent != newGroup) {
		if(ref != nil) {
		    [parent removeFromChildren:ref];
		    [newGroup addToChildren:ref];
		    NSLog(@"Moved %@ from %@ to %@", name, [parent valueForKey:@"name"], newGroupName);
		} else {
		    NSLog(@"Warn: item '%@' not found", [arguments objectAtIndex:i]);
		}
	    } else {
		NSLog(@"Ommited '%@', it's already in '%@'", name, newGroupName);
	    }
	}
    }
    
    [objects sortChildren];
    
    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInt:1]
                                              forKey:@"NSWriteOldStylePropertyLists"];
    result = [plist writeToFile:[arguments objectAtIndex:1] atomically:NO];
     
    [pool release];
    return result;
}


