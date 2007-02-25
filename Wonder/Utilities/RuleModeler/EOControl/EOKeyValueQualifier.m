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

//#include <EOControl/EOQualifier.h>
#include "EOQualifier.h"
//#include <EOControl/EONull.h>
#include "EONull.h"
#include "common.h"

@interface NSObject(QualifierDescription)
- (NSString *)qualifierDescription;
@end

@implementation EOKeyValueQualifier

static BOOL   debugEval      = NO;
static BOOL   debugTransform = NO;
static EONull *null = nil;

+ (void)initialize {
  if (null == nil)
    null = [[EONull null] retain];
  debugEval = [EOQualifier isEvaluationDebuggingEnabled];
}

- (id)initWithKey:(NSString *)_key
  operatorSelector:(SEL)_selector
  value:(id)_value
{
  self->key      = [_key   copyWithZone:NULL];
  self->value    = [_value retain];
  self->operator = _selector;
  
  if (_selector == NULL) {
    NSLog(@"WARNING(%s): got no selector for kv qualifier (key=%@)", 
	  __PRETTY_FUNCTION__, _key);
  }
  
  return self;
}

- (id)init {
  return [self initWithKey:nil operatorSelector:NULL value:nil];
}

- (void)dealloc {
  [self->key   release];
  [self->value release];
  [super dealloc];
}

/* accessors */

- (NSString *)key {
  return self->key;
}
- (SEL)selector {
  return self->operator;
}
- (id)value {
  return self->value;
}

/* bindings */

- (EOQualifier *)qualifierWithBindings:(NSDictionary *)_bindings
  requiresAllVariables:(BOOL)_reqAll
{
  static Class VarClass = Nil;
  NSString *newKey;
  id       newValue;
  BOOL     needNew;
  
  if (VarClass == Nil) VarClass = [EOQualifierVariable class];
  needNew = NO;

  if ([self->key class] == VarClass) {
    newKey = [_bindings objectForKey:[(EOQualifierVariable *)self->key key]];
    if (newKey == nil) {
      if (_reqAll)
        // throw exception
        ;
      else
        newKey = self->key;
    }
    else
      needNew = YES;
  }
  else
    newKey = self->key;

  if ([self->value class] == VarClass) {
    newValue = [_bindings objectForKey:[self->value key]];
    if (newValue == nil) {
      if (_reqAll)
        // throw exception
        ;
      else
        newValue = self->value;
    }
    else
      needNew = YES;
  }
  else
    newValue = self->value;

  if (!needNew)
    return self;

  return [[[[self class] alloc]
                         initWithKey:newKey
                         operatorSelector:self->operator
                         value:newValue] autorelease];
}

- (NSArray *)bindingKeys {
  static Class VarClass = Nil;
  Class keyClass, vClass;
  if (VarClass == Nil) VarClass = [EOQualifierVariable class];
  
  keyClass = [self->key   class];
  vClass   = [self->value class];
  
  if ((keyClass == VarClass) && (vClass == VarClass)) {
    id o[2];
    o[0] = [(EOQualifierVariable *)self->key   key];
    o[1] = [(EOQualifierVariable *)self->value key];
    return [NSArray arrayWithObjects:o count:2];
  }
  
  if (keyClass == VarClass)
    return [NSArray arrayWithObject:[(EOQualifierVariable *)self->key key]];
  if (vClass == VarClass)
    return [NSArray arrayWithObject:[(EOQualifierVariable *)self->value key]];
  
  return [NSArray array];
}

/* keys */

- (void)addQualifierKeysToSet:(NSMutableSet *)_keys {
  /* new in WO 4.5 */
  [_keys addObject:self->key];
}

/* evaluation */

- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx {
  id   lv, rv;
  BOOL (*m)(id, SEL, id);
  BOOL result;
  
  if (_ctx == nil)
    _ctx = [NSMutableDictionary dictionaryWithCapacity:16];
  
  if ((lv = [(NSDictionary *)_ctx objectForKey:self->key]) == nil) {
    lv = [_object valueForKeyPath:self->key];
    if (lv == nil) lv = null;
    [(NSMutableDictionary *)_ctx setObject:lv forKey:self->key];
  }
  
  rv = self->value != nil ? self->value : (id)null;
  
  if (debugEval) {
    NSLog(@"Eval: EOKeyValueQualifier:(%@ %@)\n"
          @"  compare %@<%@>\n  with %@<%@>",
          self->key, NSStringFromSelector(self->operator),
          lv, NSStringFromClass([lv class]),
          rv, NSStringFromClass([rv class]));
  }
  
  if ((m = (void *)[lv methodForSelector:self->operator]) == NULL) {
    /* no such operator method ! */
    [lv doesNotRecognizeSelector:self->operator];
    return NO;
  }
  
  result = m(lv, self->operator, rv);
  if (debugEval)
    NSLog(@"  %@", result ? @"MATCHES" : @"DOES NOT MATCH");
  return result;
}
- (BOOL)evaluateWithObject:(id)_object {
  return [self evaluateWithObject:_object inEvalContext:nil];
}

/* NSCoding */

- (void)encodeWithCoder:(NSCoder *)_coder {
  [_coder encodeObject:self->key];
  [_coder encodeObject:self->value];
  [_coder encodeValueOfObjCType:@encode(SEL) at:&(self->operator)];
}
- (id)initWithCoder:(NSCoder *)_coder {
  self->key   = [[_coder decodeObject] copyWithZone:[self zone]];
  self->value = [[_coder decodeObject] retain];
  [_coder decodeValueOfObjCType:@encode(SEL) at:&(self->operator)];

  if (self->operator == NULL) {
    NSLog(@"WARNING(%s): decoded no selector for kv qualifier (key=%@)", 
	  __PRETTY_FUNCTION__, self->key);
  }
  
  return self;
}

/* Comparing */

- (BOOL)isEqualToQualifier:(EOQualifier *)_qual {
  if (![self->key isEqual:[(EOKeyValueQualifier *)_qual key]])
    return NO;
  if (!((!self->value && ![(EOKeyValueQualifier *)_qual value]) || [self->value isEqual:[(EOKeyValueQualifier *)_qual value]]))
    return NO;
  if (sel_eq(self->operator, [(EOKeyValueQualifier *)_qual selector]))
    return YES;
  return NO;
}

/* remapping keys */

- (EOQualifier *)qualifierByApplyingTransformer:(id)_transformer
  inContext:(id)_ctx
{
  if ([_transformer respondsToSelector:
                      @selector(transformKeyValueQualifier:inContext:)]) {
    if (debugTransform)
      NSLog(@"transformer: %@\n  transform: %@", _transformer, self);
    return [_transformer transformKeyValueQualifier:self inContext:_ctx];
  }
  else {
    if (debugTransform)
      NSLog(@"EOKeyValueQualifier: not transforming using %@", _transformer);
    return [[self retain] autorelease];
  }
}

- (EOQualifier *)qualifierByApplyingKeyMap:(NSDictionary *)_map {
  EOKeyValueQualifier *kvq;
  NSString *k;
  
  k = [_map objectForKey:self->key];
  if (k == nil) k = self->key;
  
  kvq = [[EOKeyValueQualifier alloc] 
	  initWithKey:k operatorSelector:self->operator value:self->value];
  return [kvq autorelease];
}

/* key/value archiving */

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
  if ((self = [super initWithKeyValueUnarchiver:_unarchiver]) != nil) {
    NSString *s;
    
    self->key   = [[_unarchiver decodeObjectForKey:@"key"]   copy];
    self->value = [[_unarchiver decodeObjectForKey:@"value"] retain];
    
    if ((s = [_unarchiver decodeObjectForKey:@"selectorName"]) != nil) {
      if (![s hasSuffix:@":"]) s = [s stringByAppendingString:@":"];
      self->operator = NSSelectorFromString(s);
    }
    else if ((s = [_unarchiver decodeObjectForKey:@"selector"]) != nil)
      self->operator = NSSelectorFromString(s);
    else {
      NSLog(@"WARNING(%s): decoded no selector/selectorName for kv qualifier "
	    @"(key=%@)", 
	    __PRETTY_FUNCTION__, self->key);
      self->operator = EOQualifierOperatorEqual;
    }
    
    if (self->operator == NULL) {
      NSLog(@"WARNING(%s): decoded no selector for kv qualifier (key=%@)", 
	    __PRETTY_FUNCTION__, self->key);
      self->operator = EOQualifierOperatorEqual;
    }
  }
  return self;
}
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
  NSString *s;
  
  [super encodeWithKeyValueArchiver:_archiver];
  
  [_archiver encodeObject:[self key]   forKey:@"key"];
#ifdef WO_JAVA_COMPATIBILITY
  if (value != nil) // RuleEditor does it like this
#endif
      [_archiver encodeObject:[self value] forKey:@"value"];
  
  s = NSStringFromSelector([self selector]);
  if ([s hasSuffix:@":"]) s = [s substringToIndex:[s length] - 1];
  [_archiver encodeObject:s forKey:@"selectorName"];
}

/* description */

- (NSString *)description {
  NSMutableString *s;
  NSString *tmp;
  BOOL  parenthesized = [[self class] useParenthesesForComparisonQualifier];
  
  s = [NSMutableString stringWithCapacity:64];
  
  if (parenthesized)
      [s appendString:@"("];
  if (self->key != nil)
    [s appendString:self->key];
  else
    [s appendString:@"[NO KEY]"];
  
  [s appendString:@" "];
  
  if ((tmp = [EOQualifier stringForOperatorSelector:self->operator]) != nil)
    [s appendString:tmp];
  else if (self->operator != NULL)
    [s appendString:@"[NO STR OPERATOR]"];
  else
    [s appendString:@"[NO OPERATOR]"];
    
  [s appendString:@" "];
  
  if ((tmp = [self->value qualifierDescription]) != nil)
    [s appendString:tmp];
  else
#ifdef WO_JAVA_COMPATIBILITY
    [s appendString:@"null"];
#else
    [s appendString:@"nil"];
#endif
    if (parenthesized)
        [s appendString:@")"];
  return s;
}

@end /* EOKeyValueQualifier */
