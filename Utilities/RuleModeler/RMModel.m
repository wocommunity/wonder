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
		[self setRules:[NSArray array]];
    }
    
    return self;
}

- (void)dealloc {
    [_rules release];
    [super dealloc];
}

- (void)makeWindowControllers {
    RMModelEditor *editor = [[RMModelEditor alloc] init];
    
    [self addWindowController:editor];
    [editor release];
}

- (NSData *)dataOfType:(NSString *)typeName error:(NSError **)outError {
    if ([typeName isEqualToString:ruleModelType]) {
        
        EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
        NSMutableArray *rules = [[self rules] mutableCopy];
        
        NSSortDescriptor *descriptor=[[NSSortDescriptor alloc] initWithKey:@"sortOrder" ascending:YES];
        NSArray *sortDescriptors=[NSArray arrayWithObject:descriptor];
        [rules sortUsingDescriptors:sortDescriptors];
        
        [archiver encodeObject:rules forKey:@"rules"];
        
        NSDictionary *plist = [archiver dictionary];
        NSString *errorDesc = nil;
        BOOL prettyPrint = ![[NSUserDefaults standardUserDefaults] boolForKey:@"saveRulesInSingleRows"];
        NSData *data = [[NSPropertyListSerialization openStepFormatStringFromPropertyList:plist level:(prettyPrint ? INT_MAX:2) escapeNonASCII:YES errorDescription:&errorDesc] dataUsingEncoding:NSUTF8StringEncoding];
        
        if (errorDesc) {
            NSLog(errorDesc);
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:errorDesc forKey:NSLocalizedDescriptionKey]];
        }
        
        [archiver release];
        [rules release];
        [descriptor release];
        
        return data;
    }
    else{
        *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:@"Unknown file type" forKey:NSLocalizedDescriptionKey]];
    }
    
    return nil;
}
- (BOOL)saveToURL:(NSURL *)absoluteURL ofType:(NSString *)typeName forSaveOperation:(NSSaveOperationType)saveOperation error:(NSError **)outError {
    BOOL result = [super saveToURL:absoluteURL ofType:typeName forSaveOperation:saveOperation error:outError];
    if(result) {
        NSMutableArray *rules = [[[self rules] mutableCopy] autorelease];
        
        NSSortDescriptor *descriptor=[[[NSSortDescriptor alloc] initWithKey:@"sortOrder" ascending:YES] autorelease];
        NSArray *sortDescriptors=[NSArray arrayWithObject:descriptor];
        [rules sortUsingDescriptors:sortDescriptors];
        NSError *errorDesc = nil;
        NSString *description = [rules description];
        NSURL *url = absoluteURL;
        url = (url == nil ? [self fileURL] : url);
        if(url != nil) {
            NSString *path = [[url path] stringByAppendingPathExtension:@"txt"];
            result = [description writeToFile:path atomically:YES encoding:NSUTF8StringEncoding error:&errorDesc];
            if(!result) {
                *outError = errorDesc;
                NSLog(@"Save failed: %@ %@", path, errorDesc);
            }
        }
    }
    return result;
}

- (BOOL)readFromData:(NSData *)data ofType:(NSString *)typeName error:(NSError **)outError {
    if([typeName isEqualToString:ruleModelType]){
        NSString        *error = nil;
        NSDictionary    *plist = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListMutableContainersAndLeaves format:NULL errorDescription:&error];
        
        if(error){
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:error forKey:NSLocalizedDescriptionKey]];
            
            return NO;
        }
        
        [[self undoManager] disableUndoRegistration];
        NS_DURING
            [self setRules:[Rule rulesFromMutablePropertyList:plist]];
        NS_HANDLER
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:[localException reason] forKey:NSLocalizedDescriptionKey]];
            return NO;
        NS_ENDHANDLER
        
		[[self undoManager] enableUndoRegistration];
        if([self rules] == nil) {
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:@"No 'rules' key-value pair" forKey:NSLocalizedDescriptionKey]];
            return NO;
        }
        
        return YES;
    }
    else{
        *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:@"Unknown file type" forKey:NSLocalizedDescriptionKey]];
    }
    
    return NO;
}

- (NSArray *)rules {
    return _rules;
}

- (void)setRules:(NSArray *)newRules {
	[_rules makeObjectsPerformSelector:@selector(setModel:) withObject:nil];
	[_rules autorelease];
	
	_rules = [newRules mutableCopy];
	[_rules makeObjectsPerformSelector:@selector(setModel:) withObject:self];
}

- (NSString *)description {
    return [[self rules] description];
}

- (NSString *)displayName {
    if([self fileURL]) {
        return [[self fileURL] path];
    }
    return [super displayName];
}

@end
