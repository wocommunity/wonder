/*
 Rule.m
 RuleModeler

 Created by King Chung Huang on 1/29/04.


 Copyright (c) 2004 King Chung Huang

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
*/

#import "Rule.h"

#import "Assignment.h"
#import "RMModel.h"
#import "EOControl.h"
#import "EOQualifier+RuleModeler.h"

@implementation Rule

+ (void)initialize {
    // Do not call super - see +initialize documentation
    [self setKeys:[NSArray arrayWithObjects:@"author", @"lhs", @"rhs", nil] triggerChangeNotificationsForDependentKey:@"extendedDescription"];
    [self setKeys:[NSArray arrayWithObject:@"author"] triggerChangeNotificationsForDependentKey:@"priority"];
    [self setKeys:[NSArray arrayWithObject:@"lhs"] triggerChangeNotificationsForDependentKey:@"lhsDescription"];
    [self setKeys:[NSArray arrayWithObject:@"lhs"] triggerChangeNotificationsForDependentKey:@"lhsFormattedDescription"];
    [self setKeys:[NSArray arrayWithObject:@"rhs"] triggerChangeNotificationsForDependentKey:@"rhsDescription"];
    [EOQualifier registerValueClass:[NSDecimalNumber class] forTypeName:@"java.math.BigDecimal"];
}

static int defaultRulePriority = 0;
+ (void)setDefaultRulePriority:(int)priority {
    defaultRulePriority = (priority < 0 ? 0:priority);
}

+ (int)defaultRulePriority {
    return defaultRulePriority;
}

- (id)init {
    if (self = [super init]) {
        _author = [[self class] defaultRulePriority];
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
        id      aClassName = [loadedRuleClassNames objectAtIndex:i];
        
        // aClassName might be NSNull, in case there was no assignment for that rule
        if(aClassName == [NSNull null])
            aClassName = nil;
        [[rule rhs] setAssignmentClass:aClassName];
    }
    
    return decodedRules;
}

- (void) dealloc {
    [_lhsDescription release];
    [_lhsFormattedDescription release];
    [_rhs removeObserver:self forKeyPath:@"value"];
    [_rhs removeObserver:self forKeyPath:@"keyPath"];
    [_rhs removeObserver:self forKeyPath:@"assignmentClass"];
    [_lhs release];
    [_rhs release];
    [super dealloc];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
    if (self = [super init]) {
    Assignment   *anAssignment;
        
	_enabled = YES;
	
	_author = [unarchiver decodeIntForKey:@"author"];
	_lhs = [[unarchiver decodeObjectForKey:@"lhs"] retain];
    anAssignment = [unarchiver decodeObjectForKey:@"rhs"];
    // If there was no saved assignment, we create an empty one (automatic fix of model)
    if(!anAssignment)
        anAssignment = [[[Assignment alloc] init] autorelease];
    [self setRhs:anAssignment];
	
	if ([_lhs isKindOfClass:[EOAndQualifier class]]) {
            NSMutableArray *innerQuals = [[(EOAndQualifier *)_lhs qualifiers] mutableCopy];
            
            if ([innerQuals count] == 2 || [innerQuals count] == 1) {
                EOQualifier *qual = [innerQuals objectAtIndex:0];
                
                if (![qual isKindOfClass:[EOKeyValueQualifier class]] && [innerQuals count] > 1) {
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
                        
                        if([innerQuals count] > 0)
                            _lhs = [[innerQuals objectAtIndex:0] retain];
                        else
                            _lhs = nil;
                    }
                }
	    }
		[innerQuals release];
	}
    }
    
    return self;
}

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
    EOQualifier *lhs = _lhs;
    if (_enabled == NO) {
        EOKeyValueQualifier *kvq = [[EOKeyValueQualifier alloc] initWithKey:@"RuleIsDisabled" operatorSelector:@selector(isEqual:) value:@"YES"];
        lhs = [[EOAndQualifier alloc] initWithQualifierArray: [[[NSArray alloc] initWithObjects:kvq, _lhs, nil] autorelease]]; // _lhs might be nil
        [kvq release];
    }
    
    [archiver encodeInt:_author forKey:@"author"];
    if(lhs != nil) // RuleEditor does it like this
        [archiver encodeObject:lhs forKey:@"lhs"];
    [archiver encodeObject:_rhs forKey:@"rhs"];
    [archiver encodeObject:@"com.webobjects.directtoweb.Rule" forKey:@"class"];
    if (lhs != _lhs) {
        [lhs release];
    }
}

- (NSString *)extendedDescription {
    EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
    
    [archiver encodeObject:self forKey:@"raw"];
    
    NSDictionary *dict = [archiver dictionary];
    
    NSObject *desc = [dict objectForKey:@"raw"];
    NSString *extendedDescription = [[desc description] retain];
	[archiver release];
    
    return [extendedDescription autorelease];
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
        [self resetDescriptionCaches];
	}
}

- (NSString *)lhsDescription {
    if(_lhsDescription == nil) {
        _lhsDescription = [(_lhs != nil ? [_lhs description] : nil) retain];
    }
    return _lhsDescription;
}

- (NSString *)lhsFormattedDescription {
    if(_lhsFormattedDescription == nil) {
        _lhsFormattedDescription = [(_lhs != nil ? [_lhs formattedDescription] : nil) retain];
    }
    return _lhsFormattedDescription;
}
/*
-(BOOL)isNewRule {
    // http://www.cocoabuilder.com/archive/message/cocoa/2004/5/10/106763
    return ([[self rhs] keyPath] == nil && ([[self rhs] value] == nil) && ([self lhs] == nil));
}
*/
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
        NSDictionary *dict = [NSDictionary dictionaryWithObject:[NSString stringWithFormat:NSLocalizedString(@"This is not a valid qualifier:\n\n%@", @"Validation error description"), localException] forKey:NSLocalizedDescriptionKey];
        *outError = [NSError errorWithDomain:@"EOQualifier" code:0 userInfo:dict];
        return NO;
    } NS_ENDHANDLER;
    return YES;
}

-(BOOL)validateLhsFormattedDescription:(id *)ioValue error:(NSError **)outError {
    if (*ioValue == nil) {
        return YES;
    }
    NS_DURING {
        NSString    *description = (NSString *)*ioValue;
        if ([description length] > 0 && ![description isEqualToString:@"*true*"]) {
            EOQualifier *newQualifier = [EOQualifier qualifierWithQualifierFormat:description];
            *ioValue = [newQualifier formattedDescription]; // Necessary for automatic update of edited qualifier
        }
    } NS_HANDLER {
        NSDictionary *dict = [NSDictionary dictionaryWithObject:[NSString stringWithFormat:NSLocalizedString(@"This is not a valid qualifier:\n\n%@", @"Validation error description"), localException] forKey:NSLocalizedDescriptionKey];
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
                [[self undoManager] setActionName:NSLocalizedString(@"Set Left-Hand Side", @"Undo-redo action name")];
                
                [self setLhs:qual];
            }
        } else {
            [[[self undoManager] prepareWithInvocationTarget:self] setLhsDescription:[_lhs description]];
            [[self undoManager] setActionName:NSLocalizedString(@"Set Left-Hand Side", @"Undo-redo action name")];
            
            [self setLhs:nil];
        }        
    } NS_HANDLER {
        NSBeep();
    } NS_ENDHANDLER;
}

- (void)setLhsFormattedDescription:(NSString *)description {
    NS_DURING {
        if ([description length] > 0) {
            EOQualifier *qual = [EOQualifier qualifierWithQualifierFormat:description];
            
            if (qual) {
                [[[self undoManager] prepareWithInvocationTarget:self] setLhsFormattedDescription:[_lhs formattedDescription]];
                [[self undoManager] setActionName:NSLocalizedString(@"Set Left-Hand Side", @"Undo-redo action name")];
                
                [self setLhs:qual];
            }
        } else {
            [[[self undoManager] prepareWithInvocationTarget:self] setLhsFormattedDescription:[_lhs formattedDescription]];
            [[self undoManager] setActionName:NSLocalizedString(@"Set Left-Hand Side", @"Undo-redo action name")];
            
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
		[self _setActionName:NSLocalizedString(@"Set Priority to %@", @"Undo-redo action name") old:[NSNumber numberWithInt:_author] new:[NSNumber numberWithInt:value]];
		
		_author = value;
	}
}

- (int)priority {
    return [self author];
}

- (BOOL)enabled {
    return _enabled;
}

- (void)setEnabled:(BOOL)flag {
	if(_enabled != flag){
		[[[self undoManager] prepareWithInvocationTarget:self] setEnabled:_enabled];
		[[self undoManager] setActionName:NSLocalizedString(@"Enabled", @"Undo-redo action name")];
		
		_enabled = flag;
	}
}

- (RMModel *)model {
    return _model;
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
    NSMutableString *rhsValue = nil;    
	id              rhsValueObject = [[self rhs] value];
    
    if ([rhsValueObject isKindOfClass:[NSDictionary class]]) {
        rhsValue = [NSMutableString stringWithCapacity:1024];
        [NSPropertyListSerialization _appendDictionary:rhsValueObject toMutableString:rhsValue level:0 maxLevel:0 escapeNonASCII:YES];
    }
    else {
        rhsValue = [[[rhsValueObject description] mutableCopy] autorelease];
    }
	
    [rhsValue replaceOccurrencesOfString:@"\n    " withString:@"" options:0 range:NSMakeRange(0,[rhsValue length])];
    return [NSString stringWithFormat:@"%d : %@ => %@ = %@ [%@]", 
        [self priority], [self lhsDescription] ? [self lhsDescription]:@"*true*", [[self rhs] keyPath], rhsValue, [[self rhs] assignmentClass]];
}


- (id)copyWithZone:(NSZone *)zone {
    EOKeyValueArchiver *archiver = [[[EOKeyValueArchiver allocWithZone:zone] init] autorelease];
    
    [archiver encodeObject:[NSArray arrayWithObject:self] forKey:@"rules"];
    
    NSDictionary *dict = [archiver dictionary];

    return [[[Rule rulesFromMutablePropertyList:dict] lastObject] retain];
}

// We don't implement -isEqual:, because some controller methods rely on -isEqual:,
// and we would need to reimplement -hash
- (BOOL)isEqualToRule:(Rule *)rule {
    NSParameterAssert([rule isKindOfClass:[Rule class]]);
    BOOL    isEqual = ([self author] == [rule author]
                       && ((![self lhs] && ![rule lhs]) || ([[self lhs] isEqual:[rule lhs]])) 
                       && [[self rhs] isEqualToAssignment:[rule rhs]] 
                       && ![self enabled] == ![rule enabled]);
    
    return isEqual;
}

// Following method provides 'l', 'rk' and 'rv' keys to get lhsDescription, 
// rhs.keyPath and rhs.valueDescription results.
// Useful when user types search criteria: he can now type "l like '*task*'" 
// instead of "lhsDescription like '*task*'"
- (id)valueForKey:(NSString *)key {
    if ([key isEqualToString:@"l"])
        key = @"lhsDescription";
    else if ([key isEqualToString:@"rk"]) {
        return [self valueForKeyPath:@"rhs.keyPath"];
    }
    else if ([key isEqualToString:@"rv"]) {
        return [self valueForKeyPath:@"rhs.valueDescription"];
    }
    return [super valueForKey:key];
}

- (void)resetDescriptionCaches {
    [self willChangeValueForKey:@"lhsDescription"];
    [_lhsDescription release];
    _lhsDescription = nil;
    [self didChangeValueForKey:@"lhsDescription"];
    [self willChangeValueForKey:@"lhsFormattedDescription"];
    [_lhsFormattedDescription release];
    _lhsFormattedDescription = nil;
    [self didChangeValueForKey:@"lhsFormattedDescription"];
}

@end
