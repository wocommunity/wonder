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

#include "EOQualifier.h"
#include "common.h"

@implementation EOQualifierVariable

+ (id)variableWithKey:(NSString *)_key {
  return [[[self alloc] initWithKey:_key] autorelease];
}

- (id)initWithKey:(NSString *)_key {
  self->varKey = [_key copyWithZone:[self zone]];
  return self;
}
- (id)init {
  return [self initWithKey:nil];
}

- (void)dealloc {
  [self->varKey release];
  [super dealloc];
}

/* accessors */

- (NSString *)key {
  return self->varKey;
}

/* NSCoding */

- (void)encodeWithCoder:(NSCoder *)_coder {
  [_coder encodeObject:self->varKey];
}
- (id)initWithCoder:(NSCoder *)_coder {
  self->varKey = [[_coder decodeObject] copyWithZone:[self zone]];
  return self;
}

/* Comparing */

- (BOOL)isEqual:(id)_obj {
  if ([_obj isKindOfClass:[self class]])
    return [self isEqualToQualifierVariable:(EOQualifierVariable *)_obj];
  
  return NO;
}

- (BOOL)isEqualToQualifierVariable:(EOQualifierVariable *)_obj {
  return [self->varKey isEqual:[_obj key]];
}

/* key/value archiving */

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
  if ((self = [super init]) != nil) {
    self->varKey = [[_unarchiver decodeObjectForKey:@"key"] copy];
  }
  return self;
}
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
  [_archiver encodeObject:[self key] forKey:@"key"];
}

/* description */

- (NSString *)qualifierDescription {
  return [@"$" stringByAppendingString:[self key]];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<%@[0x%p]: variable=%@>",
                     NSStringFromClass([self class]), self,
                     [self key]];
}

@end /* EOQualifierVariable */
