/*
  Copyright (C) 2000-2005 SKYRIX Software AG

  This file is part of SOPE.

  SOPE is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the
  Free Software Foundation; either version 2, or (at your option) any
  later version.

  SOPE is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
  License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with SOPE; see the file COPYING.  If not, write to the
  Free Software Foundation, 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
*/

#include <stdio.h>
#include "EOQualifier.h"
//#include "EOKeyValueCoding.h"
#include "common.h"
#include "EONull.h"
#import <Foundation/NSException.h>

@interface NSObject(QualifierDescription)
- (NSString *)qualifierDescription;
@end

@implementation EOQualifier

static NSMapTable *operatorToSelector = NULL;
static NSMapTable *selectorToOperator = NULL;
static EONull     *null = nil;

+ (void)initialize {
  if (null == nil) null = [EONull null];
  
  if (operatorToSelector == NULL) {
    operatorToSelector = NSCreateMapTable(NSObjectMapKeyCallBacks,
                                          NSNonOwnedPointerMapValueCallBacks,
                                          10);
    NSMapInsert(operatorToSelector, @"=",   EOQualifierOperatorEqual);
    NSMapInsert(operatorToSelector, @"==",  EOQualifierOperatorEqual);
    NSMapInsert(operatorToSelector, @"!=",  EOQualifierOperatorNotEqual);
    NSMapInsert(operatorToSelector, @"<>",  EOQualifierOperatorNotEqual);        
    NSMapInsert(operatorToSelector, @"<",   EOQualifierOperatorLessThan);
    NSMapInsert(operatorToSelector, @">",   EOQualifierOperatorGreaterThan);
    NSMapInsert(operatorToSelector, @"<=",  EOQualifierOperatorLessThanOrEqualTo);
    NSMapInsert(operatorToSelector, @"like",EOQualifierOperatorLike);
    NSMapInsert(operatorToSelector, @"LIKE",EOQualifierOperatorLike);
    NSMapInsert(operatorToSelector, @">=",
                EOQualifierOperatorGreaterThanOrEqualTo);
    NSMapInsert(operatorToSelector, @"caseInsensitiveLike",
                EOQualifierOperatorCaseInsensitiveLike);
  }
  if (selectorToOperator == NULL) {
    selectorToOperator = NSCreateMapTable(NSObjectMapKeyCallBacks,
                                          NSObjectMapValueCallBacks,
                                          10);
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorEqual),
                @"=");
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorNotEqual),
#ifdef WO_JAVA_COMPATIBILITY
                @"!=");
#else
                @"<>");
#endif
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorLessThan),
                @"<");
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorGreaterThan),
                @">");
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorLessThanOrEqualTo),
                @"<=");
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorLike),
                @"like");
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorGreaterThanOrEqualTo),
                @">=");
    NSMapInsert(selectorToOperator,
                NSStringFromSelector(EOQualifierOperatorCaseInsensitiveLike),
                @"caseInsensitiveLike");
  }
}

+ (EOQualifier *)qualifierToMatchAnyValue:(NSDictionary *)_values {
  /* OR qualifier */
  NSEnumerator *keys;
  NSString     *key;
  NSArray      *array;
  unsigned i;
  id qualifiers[[_values count] + 1];
  
  keys = [_values keyEnumerator];
  for (i = 0; (key = [keys nextObject]); i++) {
    id value;
    
    value = [_values objectForKey:key];
    qualifiers[i] =
      [[EOKeyValueQualifier alloc]
                            initWithKey:key
                            operatorSelector:EOQualifierOperatorEqual
                            value:value];
    qualifiers[i] = [qualifiers[i] autorelease];
  }
  array = [NSArray arrayWithObjects:qualifiers count:i];
  return [[[EOOrQualifier alloc] initWithQualifierArray:array] autorelease];
}

+ (EOQualifier *)qualifierToMatchAllValues:(NSDictionary *)_values {
  /* AND qualifier */
  NSEnumerator *keys;
  NSString     *key;
  NSArray      *array;
  unsigned i;
  id qualifiers[[_values count] + 1];
  
  keys = [_values keyEnumerator];
  for (i = 0; (key = [keys nextObject]); i++) {
    id value;
    
    value = [_values objectForKey:key];
    qualifiers[i] =
      [[EOKeyValueQualifier alloc]
                            initWithKey:key
                            operatorSelector:EOQualifierOperatorEqual
                            value:value];
    qualifiers[i] = [qualifiers[i] autorelease];
  }
  array = [NSArray arrayWithObjects:qualifiers count:i];
  return [[[EOAndQualifier alloc] initWithQualifierArray:array] autorelease];
}

+ (SEL)operatorSelectorForString:(NSString *)_str {
  SEL s;

  if ((s = NSMapGet(operatorToSelector, _str)))
    return s;
  else
    return NSSelectorFromString(_str);
}

+ (NSString *)stringForOperatorSelector:(SEL)_sel {
  NSString *s, *ss;
  
  if ((s = NSStringFromSelector(_sel)) == nil)
    return nil;
  
  if ((ss = NSMapGet(selectorToOperator, s)))
    return ss;
  
  return s;
}

static BOOL useParenthesesForComparisonQualifier = NO;
+ (void)setUseParenthesesForComparisonQualifier:(BOOL)flag {
    useParenthesesForComparisonQualifier = flag;
}

+ (BOOL)useParenthesesForComparisonQualifier {
    return useParenthesesForComparisonQualifier;
}

- (NSString *)qualifierDescription {
    return [self description];
}

/* bindings */

- (EOQualifier *)qualifierWithBindings:(NSDictionary *)_bindings
  requiresAllVariables:(BOOL)_reqAll
{
  return self;
}

- (NSArray *)bindingKeys {
  return nil;
}

- (BOOL)requiresAllQualifierBindingVariables {
  return YES;
}

/* keys */

- (NSSet *)allQualifierKeys {
  /* new in WO 4.5 */
  id set;

  set = [NSMutableSet setWithCapacity:64];
  [self addQualifierKeysToSet:set];
  return [[set copy] autorelease];
}

- (void)addQualifierKeysToSet:(NSMutableSet *)_keys {
  /* new in WO 4.5 */
}

/* Comparing */

- (unsigned)hash {
    return [[self description] hash];
}

- (BOOL)isEqual:(id)_obj {
  if ([_obj isKindOfClass:[self class]])
    return [self isEqualToQualifier:(EOQualifier *)_obj];
  
  return NO;
}

- (BOOL)isEqualToQualifier:(EOQualifier *)_qual {
  [self doesNotRecognizeSelector:_cmd];
  return NO;
}

/* remapping keys */

- (EOQualifier *)qualifierByApplyingTransformer:(id)_t inContext:(id)_ctx {
  if ([_t respondsToSelector:@selector(transformQualifier:inContext:)])
    return [_t transformQualifier:self inContext:_ctx];
  else
    return [[self retain] autorelease];
}
- (EOQualifier *)qualifierByApplyingKeyMap:(NSDictionary *)_key {
  return [[self copy] autorelease];
}

/* GDL2 compatibility */

- (EOQualifier *)qualifierByApplyingBindings:(NSDictionary *)_bindings {
  return [self qualifierWithBindings:_bindings 
	       requiresAllVariables:
		 [self requiresAllQualifierBindingVariables]];
}

/* BDControl additions */

- (unsigned int)count {
  return [[self subqualifiers] count];
}
- (NSArray *)subqualifiers {
  return nil;
}

/* debugging */

+ (BOOL)isEvaluationDebuggingEnabled {
  static int evalDebug = -1;
  if (evalDebug == -1) {
    evalDebug = [[NSUserDefaults standardUserDefaults] 
                  boolForKey:@"EOQualifierDebugEvaluation"] ? 1 : 0;
    if (evalDebug)
      NSLog(@"WARNING: qualifier evaluation debugging is enabled !");
  }
  return evalDebug ? YES : NO;
}

/* QuickEval */

- (BOOL)evaluateWithObject:(id)_object {
    [self doesNotRecognizeSelector:_cmd];
    return NO;
}

- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx {
  return [self evaluateWithObject:_object];
}

/* key/value archiving */

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
  return [super init];
}
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
#ifdef WO_JAVA_COMPATIBILITY
    [_archiver encodeObject:[NSString stringWithFormat:@"com.webobjects.eocontrol.%@", NSStringFromClass([self class])] forKey:@"class"];
#endif
}

/* NSCopying */

- (id)copyWithZone:(NSZone *)_zone {
  /* EOQualifiers are supposed to be immutable */
  return [self retain];
}

@end /* EOQualifier */
