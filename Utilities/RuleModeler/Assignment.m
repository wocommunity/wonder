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

- (void) dealloc {
    [_value release];
    [_keyPath release];
    [_assignmentClass release];
    [_valueDescription release];
    [super dealloc];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
    if (self = [super init]) {
	[self setKeyPath: [unarchiver decodeObjectForKey:(@"keyPath")]];
        [self setValue: [unarchiver decodeObjectForKey:(@"value")]];
    }
    
    return self;
}

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
    [archiver encodeObject:_keyPath forKey:@"keyPath"];
    [archiver encodeObject:_value forKey:@"value"];
}

- (NSString *)valueDescription {
    if(_valueDescription == nil) {
        _valueDescription = [[NSString stringWithFormat:@"%@", [[self value] description]] retain];
        _valueDescription = [_valueDescription mutableCopy];
        [_valueDescription replaceOccurrencesOfString:@"\n" 
                                           withString:@" " 
                                              options:0 
                                                range:NSMakeRange(0, [_valueDescription length])];
    }
    return _valueDescription;
}

- (void)setValueDescription:(NSString *)value {
    [self setValue: value];
}

- (NSString *)description {
    return [NSString stringWithFormat:@"(%@) %@ = %@", [self assignmentClass], [self keyPath], [self valueDescription]];
}

- (NSString *)assignmentClass {
    return _assignmentClass;
}

- (void)setAssignmentClass:(NSString *)assignmentClass {
    if(_assignmentClass != assignmentClass) {
        [[[self undoManager] prepareWithInvocationTarget:self] setAssignmentClass:_assignmentClass];
        [self _setActionName:@"Set RHS Class to %@" old:_assignmentClass new:assignmentClass];
        
        [_assignmentClass release];
        _assignmentClass = [assignmentClass retain];
    }
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

        [_valueDescription release];
        _valueDescription = nil;
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
