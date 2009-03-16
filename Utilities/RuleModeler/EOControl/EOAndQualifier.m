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
#include "common.h"

@interface EOQualifier(EvalContext)
- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx;
@end

@implementation EOAndQualifier

static BOOL debugEval      = NO;
static BOOL debugTransform = NO;

+ (void)initialize {
  debugEval = [EOQualifier isEvaluationDebuggingEnabled];
}

- (id)initWithQualifierArray:(NSArray *)_qualifiers {
  self->count      = [_qualifiers count];
  self->qualifiers = [_qualifiers copyWithZone:[self zone]];
  return self;
}

- (id)initWithQualifiers:(EOQualifier *)_qual1, ... {
  va_list     va;
  EOQualifier *q;
  id       *qs;
  unsigned c;
  NSArray  *a;
  
  va_start(va, _qual1);
  for (c = 0, q = _qual1; q != nil; q = va_arg(va, id), c++)
    ;
  va_end(va);

  if (c == 0)
    return [self initWithQualifierArray:nil];

  qs = objc_calloc(c, sizeof(id));
  
  va_start(va, _qual1);
  for (c = 0, q = _qual1; q != nil; q = va_arg(va, id), c++) {
    qs[c] = q;
  }
  va_end(va);

  a = [NSArray arrayWithObjects:qs count:c];
  if (qs) objc_free(qs);

  return [self initWithQualifierArray:a];
}

- (void)dealloc {
  [self->qualifiers release];
  [super dealloc];
}

- (NSArray *)qualifiers {
  return self->qualifiers;
}
- (NSArray *)subqualifiers {
  return [self qualifiers];
}

/* bindings */

- (EOQualifier *)qualifierWithBindings:(NSDictionary *)_bindings
  requiresAllVariables:(BOOL)_reqAll
{
  NSArray  *array;
  id       objects[self->count + 1];
  unsigned i;
  id (*objAtIdx)(id,SEL,unsigned);
  
  objAtIdx = (void *)
    [self->qualifiers methodForSelector:@selector(objectAtIndex:)];
  
  for (i = 0; i < self->count; i++) {
    id q, newq;

    q = objAtIdx(self->qualifiers, @selector(objectAtIndex:), i);
    newq = [q qualifierWithBindings:_bindings requiresAllVariables:_reqAll];
    if (newq == nil) newq = q;
    
    objects[i] = newq;
  }

  array = [NSArray arrayWithObjects:objects count:self->count];
  return [[[[self class] alloc] initWithQualifierArray:array] autorelease];
}

- (NSArray *)bindingKeys {
  NSMutableSet *keys = nil;
  unsigned i;
  IMP objAtIdx;

  objAtIdx = [self->qualifiers methodForSelector:@selector(objectAtIndex:)];
  
  for (i = 0; i < self->count; i++) {
    NSArray *qb;
    id q;

    q = objAtIdx(self->qualifiers, @selector(objectAtIndex:), i);
    qb = [q bindingKeys];

    if ([qb count] > 0) {
      if (keys == nil) keys = [NSMutableSet setWithCapacity:16];
      [keys addObjectsFromArray:qb];
    }
  }
  
  return keys ? [keys allObjects] : (NSArray *)[NSArray array];
}

/* keys */

- (void)addQualifierKeysToSet:(NSMutableSet *)_keys {
  /* new in WO 4.5 */
  [self->qualifiers makeObjectsPerformSelector:_cmd withObject:_keys];
}

/* evaluation */

- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx {
  unsigned i;
  IMP objAtIdx;
  
  if ((_ctx == nil) && (self->count > 1))
    _ctx = [NSMutableDictionary dictionaryWithCapacity:16];
  
  objAtIdx = [self->qualifiers methodForSelector:@selector(objectAtIndex:)];
  
  for (i = 0; i < self->count; i++) {
    id q;
    
    q = objAtIdx(self->qualifiers, @selector(objectAtIndex:), i);
    
    if (![q evaluateWithObject:_object inEvalContext:_ctx]) {
      if (debugEval) {
        NSLog(@"Eval: EOAndQualifier '%@':\n  qualifier[%i]: '%@'\n"
              @"  failed on object '%@'",
              self, i+1, q, _object);
      }
      return NO;
    }
  }
  if (debugEval) {
    NSLog(@"Eval: EOAndQualifier '%@':\n  true on object '%@'",
          self, _object);
  }
  return YES;
}
- (BOOL)evaluateWithObject:(id)_object {
  return [self evaluateWithObject:_object inEvalContext:nil];
}

/* NSCoding */

- (void)encodeWithCoder:(NSCoder *)_coder {
  [_coder encodeObject:self->qualifiers];
}
- (id)initWithCoder:(NSCoder *)_coder {
  self->qualifiers = [[_coder decodeObject] retain];
  return self;
}

/* Comparing */

- (BOOL)isEqualToQualifier:(EOQualifier *)_qual {
  return [self->qualifiers isEqualToArray:[(EOAndQualifier *)_qual qualifiers]]; // In D2W context, we should ignore order in our case, shouldn't we?
}

/* remapping keys */

- (EOQualifier *)qualifierByApplyingTransformer:(id)_transformer
  inContext:(id)_ctx
{
  if ([_transformer respondsToSelector:
                      @selector(transformAndQualifier:inContext:)]) {
    if (debugTransform)
      NSLog(@"transformer: %@\n  transform: %@", _transformer, self);
    return [_transformer transformAndQualifier:self inContext:_ctx];
  }
  else {
    EOAndQualifier *aq;
    NSArray  *a;
    id       *qs;
    unsigned i;
    BOOL     didTransform = NO;
    
    if (debugTransform) {
      NSLog(@"EOAndQualifier: transform %i using %@ ...", 
            self->count, _transformer);
    }
    
    qs = objc_calloc(self->count, sizeof(id));
    for (i = 0; i < self->count; i++) {
      EOQualifier *q;
      
      q     = [self->qualifiers objectAtIndex:i];
      qs[i] = [q qualifierByApplyingTransformer:_transformer inContext:_ctx];
      if (qs[i] == nil) 
        qs[i] = q;
      else if (qs[i] != q) {
        if (debugTransform)
          NSLog(@"EOAndQualifier:   subqualifier %i did transform", i);
        didTransform = YES;
      }
      else if (debugTransform)
        NSLog(@"EOAndQualifier:   subqualifier %i did not transform", i);
    }
    if (didTransform) {
      a = [[NSArray alloc] initWithObjects:qs count:self->count];
      if (qs) objc_free(qs);
      aq = [[EOAndQualifier alloc] initWithQualifierArray:a];
      [a release];
      return [aq autorelease];
    }
    else {
      if (qs) objc_free(qs);
      return [[self retain] autorelease];
    }
  }
}

- (EOQualifier *)qualifierByApplyingKeyMap:(NSDictionary *)_map {
  EOAndQualifier *aq;
  NSArray  *a;
  id       *qs;
  unsigned i;
  
  qs = objc_calloc(self->count + 1, sizeof(id));
  for (i = 0; i < self->count; i++) {
    EOQualifier *q;
    
    q     = [self->qualifiers objectAtIndex:i];
    qs[i] = [q qualifierByApplyingKeyMap:_map];
    if (qs[i] == nil) qs[i] = q;
  }
  a = [[NSArray alloc] initWithObjects:qs count:self->count];
  if (qs) objc_free(qs);
  aq = [[EOAndQualifier alloc] initWithQualifierArray:a];
  [a release];
  return [aq autorelease];
}

/* key/value archiving */

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
  if ((self = [super initWithKeyValueUnarchiver:_unarchiver]) != nil) {
    self->qualifiers = [[_unarchiver decodeObjectForKey:@"qualifiers"] copy];
    self->count      = [self->qualifiers count];
  }
  return self;
}
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
  [super encodeWithKeyValueArchiver:_archiver];
  [_archiver encodeObject:[self qualifiers] forKey:@"qualifiers"];
}

/* description */

- (NSString *)description {
  NSMutableString *ms;
  NSArray  *sd;
  unsigned i, len;
  
  sd = [self->qualifiers valueForKey:@"qualifierDescription"];
  if ((len = [sd count]) == 0)
    return nil;
  if (len == 1)
    return [sd objectAtIndex:0];
  
  ms = [NSMutableString stringWithCapacity:(len * 16)];
  [ms appendString:@"("];
  for (i = 0; i < len; i++) {
#ifdef WO_JAVA_COMPATIBILITY
    if (i != 0) [ms appendString:@" and "];
#else
    if (i != 0) [ms appendString:@" AND "];
#endif
    [ms appendString:[sd objectAtIndex:i]];
  }
  [ms appendString:@")"];
  return ms;
}

@end /* EOAndQualifier */
