//
//  RMModel.m
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "RMModel.h"
#import "RMModelEditor.h"
#import "Rule.h"
#import "Assignment.h"
#import "NSPlistDescriptions.h"

#import "EOControl.h"

static NSString *ruleModelType = @"Apple D2WModel File";

@implementation RMModel

- (id)init {
    if (self = [super init]) {
        [self setRules:[NSMutableArray arrayWithCapacity: 32]];
    }
    
    return self;
}

- (void)dealloc {
    [_rules release];
    [super dealloc];
}

- (void)makeWindowControllers {
    RMModelEditor *editor = [[[RMModelEditor alloc] init] autorelease];
    
    [self addWindowController:editor];
}

- (NSData *)dataRepresentationOfType:(NSString *)type {
    if ([type isEqualTo:ruleModelType]) {
        
        EOKeyValueArchiver *archiver = [[[EOKeyValueArchiver alloc] init] autorelease];
        NSArray *rules = [[[self rules] mutableCopy] autorelease];
        
        NSSortDescriptor *descriptor=[[[NSSortDescriptor alloc] initWithKey:@"sortOrder" ascending:YES] autorelease];
        NSArray *sortDescriptors=[NSArray arrayWithObject:descriptor];
        rules = [[[rules sortedArrayUsingDescriptors:sortDescriptors] mutableCopy] autorelease];
        
        [archiver encodeObject:rules forKey:@"rules"];
        
        NSDictionary *plist = [archiver dictionary];
        NSMutableArray *loadedRules = [[[plist objectForKey:@"rules"] mutableCopy] autorelease];
        
        NSArray *loadedRuleClassNames = [rules valueForKeyPath:@"rhs.assignmentClass"];
        
        NSMutableDictionary *rule;
        NSMutableDictionary *rhs;
        
        int i, count = [loadedRules count];
        for (i = 0; i < count; i++) {
            rule = [[[loadedRules objectAtIndex:i] mutableCopy] autorelease];
            rhs = [[[rule objectForKey:@"rhs"] mutableCopy] autorelease];
            
            [rule setObject:@"com.webobjects.directtoweb.Rule" forKey:@"class"];
            [rhs setObject:[loadedRuleClassNames objectAtIndex:i] forKey:@"class"];
            
            if (rhs) {
                [rule setObject:rhs forKey:@"rhs"];
            }
            [loadedRules replaceObjectAtIndex:i withObject:rule];
        }
        
        NSString *errorDesc = nil;
        BOOL prettyPrint = ![[NSUserDefaults standardUserDefaults] boolForKey:@"saveRulesInSingleRows"];
        NSDictionary *dict = [NSDictionary dictionaryWithObject:loadedRules forKey:@"rules"];
        NSData *data = [NSPropertyListSerialization openStepFormatDataFromPropertyList:dict prettyPrint:prettyPrint errorDescription:&errorDesc];
        
        if (errorDesc) {
            NSLog(errorDesc);
        }
        return data;
    }
    
    return nil;
}
- (BOOL)saveToURL:(NSURL *)absoluteURL ofType:(NSString *)typeName forSaveOperation:(NSSaveOperationType)saveOperation error:(NSError **)outError {
    BOOL result = [super saveToURL:absoluteURL ofType:typeName forSaveOperation:saveOperation error:outError];
    if(result) {
        NSArray *rules = [[[self rules] mutableCopy] autorelease];
        
        NSSortDescriptor *descriptor=[[[NSSortDescriptor alloc] initWithKey:@"sortOrder" ascending:YES] autorelease];
        NSArray *sortDescriptors=[NSArray arrayWithObject:descriptor];
        rules = [[[rules sortedArrayUsingDescriptors:sortDescriptors] mutableCopy] autorelease];
        NSString *errorDesc = nil;
        NSString *description = [rules description];
        NSURL *url = absoluteURL;
        url = (url == nil ? [self fileURL] : url);
        if(url != nil) {
            NSString *path = [[url path] stringByAppendingPathExtension:@"txt"];
            if(![description writeToFile:path atomically:YES encoding:NSUTF8StringEncoding error:&errorDesc]) {
                NSLog(@"Save failed: %@ %@", path, errorDesc);
            }
        }
    }
    return result;
}

- (BOOL)loadDataRepresentation:(NSData *)data ofType:(NSString *)type {
    if ([type isEqualTo:ruleModelType]) {
	NSString *error = nil;
	NSDictionary *plist = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListMutableContainersAndLeaves format:NULL errorDescription:&error];
	
	if (error) {
	    return NO;
	}
	
	EOKeyValueUnarchiver *unarchiver = [[[EOKeyValueUnarchiver alloc] initWithDictionary:plist] autorelease];
        
        NSArray *loadedRules = [plist objectForKey:@"rules"];
        NSArray *loadedRuleClassNames = [loadedRules valueForKeyPath:@"rhs.class"];
        [loadedRules takeValue:@"Assignment" forKeyPath:@"rhs.class"];
        
	[self setRules:[unarchiver decodeObjectForKey:@"rules"]];
        
	[unarchiver finishInitializationOfObjects];
	[unarchiver awakeObjects];
        
        int i, count = [[self rules] count];
        Rule *rule;
        
        for (i = 0; i < count; i++) {
            rule = [[self rules] objectAtIndex:i];
            [[rule rhs] setAssignmentClass:[loadedRuleClassNames objectAtIndex:i]];
        }
	
	if ([self rules] == nil) {
	    return NO;
	}
        
	return YES;
    }
    
    return NO;
}

- (NSMutableArray *)rules {
    return _rules;
}

- (void)setRules:(NSMutableArray *)newRules {
    [_rules release];
    _rules = [newRules mutableCopy];
}

- (NSString *) description {
    return [[self rules] description];
}

- (NSString *) displayName {
    if([self fileURL]) {
        return [[self fileURL] path];
    }
    return [super displayName];
}


@end
