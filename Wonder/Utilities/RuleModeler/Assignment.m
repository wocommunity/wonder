//
//  Assignment.m
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "Assignment.h"


@implementation Assignment

- (id)init {
    if (self = [super init]) {
        [[self undoManager] disableUndoRegistration];
        
        [self setKeyPath:nil];
        [self setValue:nil];
        [self setAssignmentClass:@"com.webobjects.directtoweb.Assignment"];
        
        [[self undoManager] enableUndoRegistration];
    }
    
    return self;
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
    if (self = [super init]) {
	_keyPath = [[unarchiver decodeObjectForKey:(@"keyPath")] retain];
	_value = [[unarchiver decodeObjectForKey:(@"value")] retain];
    }
    
    return self;
}

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
    [archiver encodeObject:_keyPath forKey:@"keyPath"];
    [archiver encodeObject:_value forKey:@"value"];
}

- (NSString *)valueDescription {
    return [NSString stringWithFormat:@"%@", [_value description]];
}

- (NSString *)description {
    return [NSString stringWithFormat:@"(%@) %@ = %@", _assignmentClass, [_keyPath description], [_value description]];
}

- (NSString *)assignmentClass {
    return _assignmentClass;
}

- (void)setAssignmentClass:(NSString *)classname {
    [[[self undoManager] prepareWithInvocationTarget:self] setAssignmentClass:_assignmentClass];
    [self _setActionName:@"Set RHS Class to %@" old:_assignmentClass new:classname];
    
    [_assignmentClass release];
    
    _assignmentClass = [classname retain];
}

- (NSString *)keyPath {
    return _keyPath;
}

- (void)setKeyPath:(NSString *)keyPath {
    if(_keyPath != keyPath) {
        [[[self undoManager] prepareWithInvocationTarget:self] setKeyPath:_keyPath];
        [self _setActionName:@"Set RHS Key to %@" old:_keyPath new:keyPath];
        
        [_keyPath release];
        
        _keyPath = [keyPath retain];
    }
}

- (NSObject *)value {
    return _value;
}

- (void)setValue:(NSObject *)value {
    if(_value != value) {
        NS_DURING {
            if([NSPropertyListSerialization propertyList:value isValidForFormat:NSPropertyListOpenStepFormat]) {
                NSString *string = (NSString*)value;
                NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
                NSString *errors = nil;
                NSPropertyListFormat format = NSPropertyListOpenStepFormat;
                NSObject *newValue = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListImmutable format:&format errorDescription:&errors];
                if(errors) {
                    NSLog(@"Error: %@", errors);
                } else {
                    value = newValue;
                    NSLog(@"Coding: %@: %@", [value className], value);
                }
            } else {
                NSLog(@"Wasn't a valid plist: %@", value);
            }
        } NS_HANDLER {
            NSLog(@"Error coding: %@: %@", [value className], value);
        } NS_ENDHANDLER;
        
        [[[self undoManager] prepareWithInvocationTarget:self] setValue:_value];
        [self _setActionName:@"Set RHS Value to %@" old:_value new:value];
        
        [_value release];
        
        _value = [value retain];
    }
}

// Undo management
- (NSUndoManager *)undoManager {
    return [[[NSDocumentController sharedDocumentController] currentDocument] undoManager];
}

- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue {
    NSUndoManager *um = [self undoManager];
    
    if ([um isUndoing]) {
        [um setActionName:[NSString stringWithFormat:format, oldValue]];
    } else {
        [um setActionName:[NSString stringWithFormat:format, newValue]];
    }
}

@end
