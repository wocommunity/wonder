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

@implementation EOKeyComparisonQualifier

static EONull *null = nil;

+ (void)initialize {
  if (null == nil)
    null = [[EONull null] retain];
}

- (id)initWithLeftKey:(NSString *)_leftKey
  operatorSelector:(SEL)_selector
  rightKey:(NSString *)_rightKey;
{
  self->leftKey  = [_leftKey  copyWithZone:NULL];
  self->rightKey = [_rightKey copyWithZone:NULL];
  self->operator = _selector;
  return self;
}

- (void)dealloc {
  [self->leftKey  release];
  [self->rightKey release];
  [super dealloc];
}

/* accessors */

- (NSString *)leftKey {
  return self->leftKey;
}
- (NSString *)rightKey {
  return self->rightKey;
}
- (SEL)selector {
  return self->operator;
}

/* bindings */

- (EOQualifier *)qualifierWithBindings:(NSDictionary *)_bindings
  requiresAllVariables:(BOOL)_reqAll
{
  static Class VarClass = Nil;
  NSString *newLeftKey;
  id       newRightKey;
  BOOL     needNew;
  
  if (VarClass == Nil) VarClass = [EOQualifierVariable class];
  needNew = NO;

  if ([self->leftKey class] == VarClass) {
    newLeftKey =
      [_bindings objectForKey:[(EOQualifierVariable *)self->leftKey key]];
    if (newLeftKey == nil) {
      if (_reqAll)
        // throw exception
        ;
      else
        newLeftKey = self->leftKey;
    }
    else
      needNew = YES;
  }
  else
    newLeftKey = self->leftKey;

  if ([self->rightKey class] == VarClass) {
    newRightKey =
      [_bindings objectForKey:[(EOQualifierVariable *)self->rightKey key]];
    if (newRightKey == nil) {
      if (_reqAll)
        // throw exception
        ;
      else
        newRightKey = self->rightKey;
    }
    else
      needNew = YES;
  }
  else
    newRightKey = self->rightKey;

  if (!needNew)
    return self;

  return [[[[self class] alloc]
                         initWithLeftKey:newLeftKey
                         operatorSelector:self->operator
                         rightKey:newRightKey]
                         autorelease];
}

- (NSArray *)bindingKeys {
  static Class VarClass = Nil;
  Class lkClass, rkClass;
  if (VarClass == Nil) VarClass = [EOQualifierVariable class];
  
  lkClass = [self->leftKey  class];
  rkClass = [self->rightKey class];
  
  if ((lkClass == VarClass) && (rkClass == VarClass)) {
    id o[2];
    o[0] = [(EOQualifierVariable *)self->leftKey  key];
    o[1] = [(EOQualifierVariable *)self->rightKey key];
    return [NSArray arrayWithObjects:o count:2];
  }
  
  if (lkClass == VarClass)
    return [NSArray arrayWithObject:[(EOQualifierVariable *)self->leftKey key]];
  if (rkClass == VarClass) {
    return [NSArray arrayWithObject:
                      [(EOQualifierVariable *)self->rightKey key]];
  }
  return [NSArray array];
}

/* keys */

- (void)addQualifierKeysToSet:(NSMutableSet *)_keys {
  /* new in WO 4.5 */
  [_keys addObject:self->leftKey];
  [_keys addObject:self->rightKey];
}

/* evaluation */

- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx {
  id   lv, rv;
  BOOL (*m)(id, SEL, id);
  
  if (_ctx == nil)
    _ctx = [NSMutableDictionary dictionaryWithCapacity:16];
  
  if ((lv = [(NSDictionary *)_ctx objectForKey:self->leftKey]) == nil) {
    lv = [_object valueForKeyPath:self->leftKey];
    if (lv == nil) lv = null;
    [(NSMutableDictionary *)_ctx setObject:lv forKey:self->leftKey];
  }
  if ((rv = [(NSDictionary *)_ctx objectForKey:self->rightKey]) == nil) {
    rv = [_object valueForKeyPath:self->rightKey];
    if (rv == nil) rv = null;
    [(NSMutableDictionary *)_ctx setObject:rv forKey:self->rightKey];
  }
  
  if ((m = (void *)[lv methodForSelector:self->operator]) == NULL) {
    /* no such operator method ! */
    [lv doesNotRecognizeSelector:self->operator];
    return NO;
  }

  return m(lv, self->operator, rv);
}
- (BOOL)evaluateWithObject:(id)_object {
  return [self evaluateWithObject:_object inEvalContext:nil];
}

/* NSCoding */

- (void)encodeWithCoder:(NSCoder *)_coder {
  [_coder encodeObject:self->leftKey];
  [_coder encodeObject:self->rightKey];
  [_coder encodeValueOfObjCType:@encode(SEL) at:&(self->operator)];
}
- (id)initWithCoder:(NSCoder *)_coder {
  self->leftKey  = [[_coder decodeObject] copyWithZone:[self zone]];
  self->rightKey = [[_coder decodeObject] copyWithZone:[self zone]];
  [_coder decodeValueOfObjCType:@encode(SEL) at:&(self->operator)];
  return self;
}

/* Comparing */

- (BOOL)isEqualToQualifier:(EOQualifier *)_qual {
  if (![self->leftKey isEqual:[(EOKeyComparisonQualifier *)_qual leftKey]])
    return NO;
  if (![self->rightKey isEqual:[(EOKeyComparisonQualifier *)_qual rightKey]])
    return NO;
  if (sel_eq(self->operator, [(EOKeyComparisonQualifier *)_qual selector]))
    return YES;
  return NO;
}

/* remapping keys */

- (EOQualifier *)qualifierByApplyingTransformer:(id)_transformer
  inContext:(id)_ctx
{
  if ([_transformer respondsToSelector:
                      @selector(transformKeyComparisonQualifier:inContext:)]) {
    return [_transformer transformKeyComparisonQualifier:self inContext:_ctx];
  }
  else
    return [[self retain] autorelease];
}

- (EOQualifier *)qualifierByApplyingKeyMap:(NSDictionary *)_map {
  EOKeyComparisonQualifier *kcq;
  NSString *l, *r;
  
  l = [_map objectForKey:self->leftKey];
  if (l == nil) l = self->leftKey;
  r = [_map objectForKey:self->rightKey];
  if (r == nil) r = self->rightKey;
  
  kcq = [[EOKeyComparisonQualifier alloc] 
	  initWithLeftKey:l operatorSelector:self->operator rightKey:r];
  return [kcq autorelease];
}

/* key/value archiving */

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
  if ((self = [super initWithKeyValueUnarchiver:_unarchiver]) != nil) {
    NSString *s;
    
    self->leftKey  = [[_unarchiver decodeObjectForKey:@"leftKey"]  retain];
    self->rightKey = [[_unarchiver decodeObjectForKey:@"rightKey"] retain];

    if ((s = [_unarchiver decodeObjectForKey:@"selectorName"]) != nil) {
      if (![s hasSuffix:@":"]) s = [s stringByAppendingString:@":"];
      self->operator = NSSelectorFromString(s);
    }
    else if ((s = [_unarchiver decodeObjectForKey:@"selector"]) != nil)
      self->operator = NSSelectorFromString(s);
  }
  return self;
}
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
  NSString *s;
  
  [super encodeWithKeyValueArchiver:_archiver];
  
  [_archiver encodeObject:[self leftKey]  forKey:@"leftKey"];
  [_archiver encodeObject:[self rightKey] forKey:@"rightKey"];

  s = NSStringFromSelector([self selector]);
  if ([s hasSuffix:@":"]) s = [s substringToIndex:[s length] - 1];
  [_archiver encodeObject:s forKey:@"selectorName"];
}

/* description */

- (NSString *)description {
  NSMutableString *s;
  BOOL  parenthesized = [[self class] useParenthesesForComparisonQualifier];
  
  s = [NSMutableString stringWithCapacity:64];
  if (parenthesized)
    [s appendString:@"("];
  [s appendString:self->leftKey];
  [s appendString:@" "];
  [s appendString:[EOQualifier stringForOperatorSelector:self->operator]];
  [s appendString:@" "];
  [s appendString:self->rightKey];
  if (parenthesized)
    [s appendString:@")"];
  return s;
}

@end /* EOKeyComparisonQualifier */
