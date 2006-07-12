//
//  Rule.m
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "Rule.h"

#import "Assignment.h"

@implementation Rule

- (id)init {
    if (self = [super init]) {
        _author = 0;
        [[self undoManager] disableUndoRegistration];
        
        [self setLhs:nil];
        [self setRhs:[[Assignment alloc] init]];
        
        [[self undoManager] enableUndoRegistration];
	
	_enabled = YES;
    }
    
    return self;
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
    if (self = [super init]) {
	_enabled = YES;
	
	_author = [unarchiver decodeIntForKey:@"author"];
	_lhs = [[unarchiver decodeObjectForKey:@"lhs"] retain];
	_rhs = [[unarchiver decodeObjectForKey:@"rhs"] retain];
	
	if ([_lhs isKindOfClass:[EOAndQualifier class]]) {
            NSMutableArray *innerQuals = [[(EOAndQualifier *)_lhs qualifiers] mutableCopy];
            
            if ([innerQuals count] == 2) {
                EOQualifier *qual = [innerQuals objectAtIndex:0];
                
                if (![qual isKindOfClass:[EOKeyValueQualifier class]]) {
                    qual = [innerQuals objectAtIndex:1];
                    
                    if (![qual isKindOfClass:[EOKeyValueQualifier class]]) {
                        qual = nil;
                    }
                }
                
                if (qual != nil) {
                    if ([[(EOKeyValueQualifier *)qual key] isEqualTo:@"RuleIsDisabled"] && [[(EOKeyValueQualifier *)qual value] isEqualTo:@"YES"]) {
                        _enabled = NO;
                        
                        [innerQuals removeObject:qual];
                        [_lhs release];
                        
                        _lhs = [[innerQuals objectAtIndex:0] retain];
                    }
                }
	    }
	}
    }
    
    return self;
}

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
    if (_enabled == NO) {
	NSLog(@"no");
	EOKeyValueQualifier *kvq = [[EOKeyValueQualifier alloc] initWithKey:@"RuleIsDisabled" operatorSelector:@selector(isEqual:) value:@"YES"];
	_lhs = [[EOAndQualifier alloc] initWithQualifierArray: [[NSArray alloc] initWithObjects:_lhs, kvq, nil]];
    }
    
    [archiver encodeInt:_author forKey:@"author"];
    [archiver encodeObject:_lhs forKey:@"lhs"];
    [archiver encodeObject:_rhs forKey:@"rhs"];
}

- (NSString *)extendedDescription {
    EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
    
    [archiver encodeObject:self forKey:@"raw"];
    
    NSDictionary *dict = [archiver dictionary];
    
    NSObject *desc = [dict objectForKey:@"raw"];
    
    return [desc description];
}

- (Assignment *)rhs {
    return _rhs;
}

- (void)setRhs:(Assignment *)rhs {
    //[[[self undoManager] prepareWithInvocationTarget:self] setRhs:_rhs];
    //[[self undoManager] setActionName:@"Set Right-Hand Side"];
    
    [_rhs release];
    
    _rhs = [rhs retain];
}

- (EOQualifier *)lhs {
    return _lhs;
}

- (void)setLhs:(EOQualifier *)lhs {
    [_lhs release];
    
    _lhs = [lhs retain];
}

- (NSString *)lhsDescription {
    return (_lhs != nil) ? [_lhs description] : @"*true*";
}

-(BOOL) isNewRule {
    return ([[self rhs] keyPath] == nil); // || ([[self rhs] value] == nil);
}

-(BOOL)validateLhsDescription:(id *)ioValue error:(NSError **)outError {
    if (*ioValue == nil) {
        return YES;
    }
    NS_DURING {
        NSString *description = (NSString *)*ioValue;
        if ([description length] > 0 && ![description isEqualToString:@"*true*"]) {
            EOQualifier *qual = [EOQualifier qualifierWithQualifierFormat:description];
        }
    } NS_HANDLER {
        NSDictionary *dict = [NSDictionary dictionaryWithObject:@"This is not a valid qualifier." forKey:NSLocalizedDescriptionKey];
        *outError = [NSError errorWithDomain:@"EOQualifier" code:0 userInfo:dict];
        return NO;
    } NS_ENDHANDLER;
    return YES;
}
- (void)setLhsDescription:(NSString *)description {
    NS_DURING {
        if ([description length] > 0) {
            EOQualifier *qual = [EOQualifier qualifierWithQualifierFormat:description];
            
            if (qual) {
                [[[self undoManager] prepareWithInvocationTarget:self] setLhsDescription:[_lhs description]];
                [[self undoManager] setActionName:@"Set Left-Hand Side"];
                
                [self setLhs:qual];
            }
        } else {
            [[[self undoManager] prepareWithInvocationTarget:self] setLhsDescription:[_lhs description]];
            [[self undoManager] setActionName:@"Set Left-Hand Side"];
            
            [self setLhs:nil];
        }        
    } NS_HANDLER {
        NSBeep();
    } NS_ENDHANDLER;
}

- (int)author {
    return _author;
}

- (void)setAuthor:(int)value {
    [[[self undoManager] prepareWithInvocationTarget:self] setAuthor:_author];
    [self _setActionName:@"Set Priority to %@" old:[NSNumber numberWithInt:_author] new:[NSNumber numberWithInt:value]];
    
    _author = value;
}

- (int)priority {
    return [self author];
}

- (BOOL)enabled {
    return _enabled;
}

- (int)sortOrder {
    return [self priority] * 1000 + [[self description] length];
}

- (void)setEnabled:(BOOL)flag {
    [[[self undoManager] prepareWithInvocationTarget:self] setEnabled:_enabled];
    [[self undoManager] setActionName:@"Enabled"];
    
    _enabled = flag;
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

- (NSString *) description {
    NSMutableString *rhsValue = [[[[self rhs] value] description] mutableCopy]; 
    [rhsValue replaceOccurrencesOfString:@"\n    " withString:@"" options:0 range:NSMakeRange(0,[rhsValue length])];
    return [NSString stringWithFormat:@"%d : %@ => %@ = %@ [%@]", 
        [self priority], [self lhsDescription], [[self rhs] keyPath], rhsValue, [[self rhs] assignmentClass]];
}


-(id) copy {
    Rule *rule = [[Rule alloc] init];
    EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
    
    [archiver encodeObject:self forKey:@"raw"];
    
    NSDictionary *dict = [archiver dictionary];
    
    EOKeyValueUnarchiver *unarchiver = [[EOKeyValueUnarchiver alloc] initWithDictionary:dict];
    return [unarchiver decodeObjectForKey:@"raw"];
}
@end
