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

@interface NSObject(QualifierDescription)
- (NSString *)qualifierDescription;
@end

@interface EOQualifier(EvalContext)
- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx;
@end

@implementation EONotQualifier

- (id)initWithQualifier:(EOQualifier *)_qualifier {
  self->qualifier = [_qualifier retain];
  return self;
}

- (void)dealloc {
  [self->qualifier release];
  [super dealloc];
}

/* accessors */

- (EOQualifier *)qualifier {
  return self->qualifier;
}

- (unsigned int)count {
  return self->qualifier ? 1 : 0;
}
- (NSArray *)subqualifiers {
  return self->qualifier ? [NSArray arrayWithObject:self->qualifier] : nil;
}

/* bindings */

- (EOQualifier *)qualifierWithBindings:(NSDictionary *)_bindings
  requiresAllVariables:(BOOL)_reqAll
{
  EOQualifier *nq;

  nq = [self->qualifier qualifierWithBindings:_bindings
                        requiresAllVariables:_reqAll];
  if (nq == nil)
    return self;

  if (nq == self->qualifier)
    return self;
  
  return [[[[self class] alloc] initWithQualifier:nq] autorelease];
}

- (NSArray *)bindingKeys {
  return [self->qualifier bindingKeys];
}

/* keys */

- (void)addQualifierKeysToSet:(NSMutableSet *)_keys {
  /* new in WO 4.5 */
  [self->qualifier addQualifierKeysToSet:_keys];
}

/* evaluation */

- (BOOL)evaluateWithObject:(id)_object inEvalContext:(id)_ctx {
  return
    [self->qualifier evaluateWithObject:_object inEvalContext:_ctx]
    ? NO : YES;
}
- (BOOL)evaluateWithObject:(id)_object {
  return [self evaluateWithObject:_object inEvalContext:nil];
}

/* NSCoding */

- (void)encodeWithCoder:(NSCoder *)_coder {
  [_coder encodeObject:self->qualifier];
}

- (id)initWithCoder:(NSCoder *)_coder {
  self->qualifier = [[_coder decodeObject] retain];
  return self;
}

/* Comparing */

- (BOOL)isEqualToQualifier:(EOQualifier *)_qual {
  return [self->qualifier isEqual:[(EONotQualifier *)_qual qualifier]];
}

/* remapping keys */

- (EOQualifier *)qualifierByApplyingTransformer:(id)_transformer
  inContext:(id)_ctx
{
  if ([_transformer respondsToSelector:
                      @selector(transformNotQualifier:inContext:)]) {
    return [_transformer transformNotQualifier:self inContext:_ctx];
  }
  else {
    EONotQualifier *nq;
    EOQualifier *q;
    
    q = [self->qualifier
             qualifierByApplyingTransformer:_transformer inContext:_ctx];
    nq = [[EONotQualifier alloc] initWithQualifier:(q ? q : self->qualifier)];
    return [nq autorelease];
  }
}

- (EOQualifier *)qualifierByApplyingKeyMap:(NSDictionary *)_map {
  EONotQualifier *nq;
  EOQualifier *q;
  
  q = [self->qualifier qualifierByApplyingKeyMap:_map];
  nq = [[EONotQualifier alloc] initWithQualifier:(q ? q : self->qualifier)];
  return [nq autorelease];
}

/* key/value archiving */

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
  if ((self = [super initWithKeyValueUnarchiver:_unarchiver]) != nil) {
    self->qualifier = [[_unarchiver decodeObjectForKey:@"qualifier"] copy];
  }
  return self;
}
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
  [super encodeWithKeyValueArchiver:_archiver];
  [_archiver encodeObject:[self qualifier] forKey:@"qualifier"];
}

/* description */

- (NSString *)description {
  NSString *qd;
  
  qd = [self->qualifier qualifierDescription];
  
#ifdef WO_JAVA_COMPATIBILITY
  if (([self->qualifier isKindOfClass:[EOKeyValueQualifier class]] || [self->qualifier isKindOfClass:[EOKeyComparisonQualifier class]]) && ![[self class] useParenthesesForComparisonQualifier])
    return [[@"not (" stringByAppendingString:qd] stringByAppendingString:@")"];
  else
    return [@"not " stringByAppendingString:qd];
#else
  return [[@"NOT (" stringByAppendingString:qd] stringByAppendingString:@")"];
#endif
}

@end /* EONotQualifier */
