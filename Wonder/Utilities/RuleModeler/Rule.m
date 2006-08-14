//
//  Rule.m
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "Rule.h"

#import "Assignment.h"
#import "RMModel.h"
#import "EOControl.h"

@implementation Rule

+ (void)initialize {
    [super initialize];
    
    [self setKeys:[NSArray arrayWithObjects:@"author", @"lhs", @"rhs", nil] triggerChangeNotificationsForDependentKey:@"extendedDescription"];
    [self setKeys:[NSArray arrayWithObject:@"author"] triggerChangeNotificationsForDependentKey:@"priority"];
    [self setKeys:[NSArray arrayWithObject:@"lhs"] triggerChangeNotificationsForDependentKey:@"lhsDescription"];
    [self setKeys:[NSArray arrayWithObject:@"rhs"] triggerChangeNotificationsForDependentKey:@"rhsDescription"];
}

- (id)init {
    if (self = [super init]) {
        _author = 0;
        [[self undoManager] disableUndoRegistration];
        
        [self setLhs:nil];
        [self setRhs:[[[Assignment alloc] init] autorelease]];
        
        [[self undoManager] enableUndoRegistration];
	
	_enabled = YES;
    }
    
    return self;
}

+ (NSArray *)rulesFromMutablePropertyList:(id)plist {
    // Special tricks here: the archiver uses the 'class' field to create instances of that class,
    // but in our case we always want rhs to be instantiated as an Assignment, thus we temporarily replace
    // the 'class' field with 'Assignment' and then put its old value into the Assignment's assignmentClass.
    EOKeyValueUnarchiver    *unarchiver = [[[EOKeyValueUnarchiver alloc] initWithDictionary:plist] autorelease];        
    NSArray                 *loadedRules = [plist objectForKey:@"rules"];
    NSArray                 *loadedRuleClassNames = [loadedRules valueForKeyPath:@"rhs.class"];
    NSArray                 *decodedRules;
    NSEnumerator            *loadedRuleEnum = [loadedRules objectEnumerator];
    id                      eachLoadedRule;
    
    while(eachLoadedRule = [loadedRuleEnum nextObject]){
        id  rhs = [eachLoadedRule valueForKeyPath:@"rhs"];
        
        // It might happen that rhs is nil
        if(rhs)
            [eachLoadedRule takeValue:@"Assignment" forKeyPath:@"rhs.class"];
    }
    decodedRules = [unarchiver decodeObjectForKey:@"rules"];
    
    [unarchiver finishInitializationOfObjects];
    [unarchiver awakeObjects];
    
    int     i, count = [decodedRules count];
    
    for(i = 0; i < count; i++){
        Rule    *rule = [decodedRules objectAtIndex:i];
        
        [[rule rhs] setAssignmentClass:[loadedRuleClassNames objectAtIndex:i]];
    }
    
    return decodedRules;
}

- (void) dealloc {
    [_lhsDescription release];
    [_rhs removeObserver:self forKeyPath:@"value"];
    [_rhs removeObserver:self forKeyPath:@"keyPath"];
    [_rhs removeObserver:self forKeyPath:@"assignmentClass"];
    [_lhs release];
    [_rhs release];
    [super dealloc];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
    if (self = [super init]) {
	_enabled = YES;
	
	_author = [unarchiver decodeIntForKey:@"author"];
	_lhs = [[unarchiver decodeObjectForKey:@"lhs"] retain];
    [self setRhs:[unarchiver decodeObjectForKey:@"rhs"]];
	
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
                        [_lhs autorelease];
                        
                        _lhs = [[innerQuals objectAtIndex:0] retain];
                    }
                }
	    }
		[innerQuals release];
	}
    }
    
    return self;
}

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
    if (_enabled == NO) {
	NSLog(@"no");
	EOKeyValueQualifier *kvq = [[EOKeyValueQualifier alloc] initWithKey:@"RuleIsDisabled" operatorSelector:@selector(isEqual:) value:@"YES"];
	[_lhs autorelease];
	_lhs = [[EOAndQualifier alloc] initWithQualifierArray: [[[NSArray alloc] initWithObjects:_lhs, kvq, nil] autorelease]];
	[kvq release];
    }
    
    [archiver encodeInt:_author forKey:@"author"];
    [archiver encodeObject:_lhs forKey:@"lhs"];
    [archiver encodeObject:_rhs forKey:@"rhs"];
    [archiver encodeObject:@"com.webobjects.directtoweb.Rule" forKey:@"class"];
}

- (NSString *)extendedDescription {
    EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
    
    [archiver encodeObject:self forKey:@"raw"];
    
    NSDictionary *dict = [archiver dictionary];
    
    NSObject *desc = [dict objectForKey:@"raw"];
	[archiver autorelease];
    
    return [desc description];
}

- (Assignment *)rhs {
    return _rhs;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    [self willChangeValueForKey:@"extendedDescription"];
    [self didChangeValueForKey:@"extendedDescription"];
}

- (void)setRhs:(Assignment *)rhs {
    if(_rhs != rhs){
        [_rhs removeObserver:self forKeyPath:@"value"];
        [_rhs removeObserver:self forKeyPath:@"keyPath"];
        [_rhs removeObserver:self forKeyPath:@"assignmentClass"];
		[_rhs setRule:nil];
		[_rhs release];
		
        if([rhs isEqual:[NSNull null]])
            rhs = nil;
		_rhs = [rhs retain];
		[_rhs setRule:self];
        // Allows automatic update of preview
        [_rhs addObserver:self forKeyPath:@"value" options:0 context:NULL];
        [_rhs addObserver:self forKeyPath:@"keyPath" options:0 context:NULL];
        [_rhs addObserver:self forKeyPath:@"assignmentClass" options:0 context:NULL];
	}
}

- (EOQualifier *)lhs {
    return _lhs;
}

- (void)setLhs:(EOQualifier *)lhs {
	if(_lhs != lhs){
		[_lhs release];		
		_lhs = [lhs retain];
        [_lhsDescription release];
        _lhsDescription = nil;
	}
}

- (NSString *)lhsDescription {
    if(_lhsDescription == nil) {
        _lhsDescription = [(_lhs != nil ? [_lhs description] : @"*true*") retain];
    }
    return _lhsDescription;
}

-(BOOL)isNewRule {
    return ([[self rhs] keyPath] == nil); // || ([[self rhs] value] == nil);
}

-(BOOL)validateLhsDescription:(id *)ioValue error:(NSError **)outError {
    if (*ioValue == nil) {
        return YES;
    }
    NS_DURING {
        NSString    *description = (NSString *)*ioValue;
        if ([description length] > 0 && ![description isEqualToString:@"*true*"]) {
            EOQualifier *newQualifier = [EOQualifier qualifierWithQualifierFormat:description];
            *ioValue = [newQualifier description]; // Necessary for automatic update of edited qualifier
        }
    } NS_HANDLER {
        NSDictionary *dict = [NSDictionary dictionaryWithObject:[NSString stringWithFormat:@"This is not a valid qualifier:\n\n%@", localException] forKey:NSLocalizedDescriptionKey];
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
	if(_author != value){
		[[[self undoManager] prepareWithInvocationTarget:self] setAuthor:_author];
		[self _setActionName:@"Set Priority to %@" old:[NSNumber numberWithInt:_author] new:[NSNumber numberWithInt:value]];
		
		_author = value;
	}
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
	if(_enabled != flag){
		[[[self undoManager] prepareWithInvocationTarget:self] setEnabled:_enabled];
		[[self undoManager] setActionName:@"Enabled"];
		
		_enabled = flag;
	}
}

- (void)setModel:(RMModel *)model {
	_model = model; // Back-pointer - not retained
}

// Undo management
- (NSUndoManager *)undoManager {
    return [_model undoManager];
}

- (void)_setActionName:(NSString *)format old:(id)oldValue new:(id)newValue {
    NSUndoManager *um = [self undoManager];
    
    if ([um isUndoing]) {
        [um setActionName:[NSString stringWithFormat:format, oldValue]];
    } else {
        [um setActionName:[NSString stringWithFormat:format, newValue]];
    }
}

- (NSString *)description {
    NSMutableString *rhsValue = [[[[[self rhs] value] description] mutableCopy] autorelease]; 
    [rhsValue replaceOccurrencesOfString:@"\n    " withString:@"" options:0 range:NSMakeRange(0,[rhsValue length])];
    return [NSString stringWithFormat:@"%d : %@ => %@ = %@ [%@]", 
        [self priority], [self lhsDescription], [[self rhs] keyPath], rhsValue, [[self rhs] assignmentClass]];
}


- (id)copyWithZone:(NSZone *)zone {
    EOKeyValueArchiver *archiver = [[[EOKeyValueArchiver allocWithZone:zone] init] autorelease];
    
    [archiver encodeObject:self forKey:@"raw"];
    
    NSDictionary *dict = [archiver dictionary];
    
    EOKeyValueUnarchiver *unarchiver = [[[EOKeyValueUnarchiver allocWithZone:zone] initWithDictionary:dict] autorelease];
    return [[unarchiver decodeObjectForKey:@"raw"] retain];
}

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[Rule class]]) {
        return ([self author] == [anObject author] && ((![self lhs] && ![anObject lhs]) || ([[self lhs] isEqual:[anObject lhs]])) && [[self rhs] isEqual:[anObject rhs]] && ![self enabled] == ![anObject enabled]);
    } else {
        return NO;
    }
}

@end
