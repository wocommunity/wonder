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
#include "EONull.h"
#include "common.h"

static EONull *null = nil;

/* values */

@interface NSObject(CompareIFace)
- (NSComparisonResult)compare:(id)_object;
@end

@implementation NSObject(ImplementedQualifierComparisons)

- (BOOL)isEqualTo:(id)_object {
  return [self isEqual:_object];
}
- (BOOL)isNotEqualTo:(id)_object {
  return ![self isEqualTo:_object];
}

- (BOOL)isLessThan:(id)_object {
  return [self compare:_object] < 0 ? YES : NO;
}
- (BOOL)isGreaterThan:(id)_object {
  return [self compare:_object] > 0 ? YES : NO;
}
- (BOOL)isLessThanOrEqualTo:(id)_object {
  return [self compare:_object] <= 0 ? YES : NO;
}
- (BOOL)isGreaterThanOrEqualTo:(id)_object {
  return [self compare:_object] >= 0 ? YES : NO;
}

- (BOOL)doesContain:(id)_object {
  return NO;
}

- (BOOL)isLike:(NSString *)_object {
  return NO;
}
- (BOOL)isCaseInsensitiveLike:(NSString *)_object {
  return NO;
}

@end /* NSObject(ImplementedQualifierComparisons) */

@implementation NSArray(ImplementedQualifierComparisons)

- (BOOL)doesContain:(id)_object {
  return [self containsObject:_object];
}

@end /* NSArray(ImplementedQualifierComparisons) */

@implementation NSString(ImplementedQualifierComparisons)

- (BOOL)isLike:(NSString *)_pattern {
  NSArray  *cs;
  unsigned count;
#if 0  
  NSString *first, *last;
#endif  
  
  if (null == nil) null = [[EONull null] retain];

  if ((id)_pattern == (id)null)
    return NO;
  
  if ([_pattern isEqual:@"*"])
    /* all match */
    return YES;
  
  cs    = [_pattern componentsSeparatedByString:@"*"];
  count = [cs count];

  if (count == 0)
    return [self isEqual:_pattern];
  
  if (count == 1)
    return [self isEqual:_pattern];
  
  if (count == 2) {
    if ([_pattern hasPrefix:@"*"])
      return [self hasSuffix:[cs objectAtIndex:1]];
    if ([_pattern hasSuffix:@"*"])
      return [self hasPrefix:[cs objectAtIndex:0]];
  }
  if (count == 3) {
    if ([_pattern hasPrefix:@"*"] && [_pattern hasSuffix:@"*"])
      return [self rangeOfString:[cs objectAtIndex:1]].length == 0
        ? NO : YES;
  }
#if 1
  {
    NSEnumerator *enumerator;
    int          idx;
    int          len;
    NSString     *str;

    idx        = 0;
    len        = [self length];
    enumerator = [cs objectEnumerator];
    
    while ((str = [enumerator nextObject]) && idx < len) {
      NSRange r;
      
      if ([str length] == 0)
        continue;
      
      r = NSMakeRange(idx, ([self length] - idx));
      r = [self rangeOfString:str options:0 range:r];
      if (r.length == 0)
        return NO;
      
      idx += r.length;
    }
    return [enumerator nextObject] ? NO : YES;
  }
#else
  first = [cs objectAtIndex:0];
  last  = [cs lastObject];

  if (![self hasPrefix:first])
    return NO;
  if (![self hasSuffix:last])
    return NO;

  /* to be completed (match interior stuff, match '?') */
  
  return YES;
#endif
  return NO;
}

- (BOOL)isCaseInsensitiveLike:(NSString *)_pattern {
  return [[self lowercaseString] isLike:[_pattern lowercaseString]];
}

@end /* NSString(ImplementedQualifierComparisons) */

@implementation NSNumber(ImplementedQualifierComparisons)

- (BOOL)isEqualTo:(id)_object {
  if (_object == nil) return NO;
  if (_object == self) return YES;
  return [self isEqual:_object];
}
- (BOOL)isNotEqualTo:(id)_object {
  if (_object == nil) return YES;
  return ![self isEqualTo:_object];
}

- (BOOL)isLessThan:(id)_object {
  if (_object == nil)  return YES;
  if (_object == self) return NO;
  return [self compare:_object] < 0 ? YES : NO;
}
- (BOOL)isGreaterThan:(id)_object {
  if (_object == nil) return NO;
  if (_object == self) return NO;
  return [self compare:_object] > 0 ? YES : NO;
}
- (BOOL)isLessThanOrEqualTo:(id)_object {
  if (_object == nil)  return YES;
  if (_object == self) return YES;
  return [self compare:_object] <= 0 ? YES : NO;
}
- (BOOL)isGreaterThanOrEqualTo:(id)_object {
  if (_object == nil)  return NO;
  if (_object == self) return YES;
  return [self compare:_object] >= 0 ? YES : NO;
}

@end /* NSNumber(ImplementedQualifierComparisons) */

@implementation NSDate(ImplementedQualifierComparisons)

#define CHECK_NULL(__VAL__, __RES__) \
  {if (null == nil) null = [[EONull null] retain];} \
  if (__VAL__ == nil || __VAL__ == null) return __RES__

- (BOOL)isLessThan:(id)_object {
  CHECK_NULL(_object, NO);
  return [self compare:_object] < 0 ? YES : NO;
}
- (BOOL)isGreaterThan:(id)_object {
  CHECK_NULL(_object, YES);
  return [self compare:_object] > 0 ? YES : NO;
}

- (BOOL)isLessThanOrEqualTo:(id)_object {
  CHECK_NULL(_object, NO);
  return [self compare:_object] <= 0 ? YES : NO;
}
- (BOOL)isGreaterThanOrEqualTo:(id)_object {
  CHECK_NULL(_object, YES);
  return [self compare:_object] >= 0 ? YES : NO;
}

@end /* NSDate(ImplementedQualifierComparisons) */

@implementation NSNull(ImplementedQualifierComparisons)

- (NSComparisonResult)compare:(id)_object
{
    return self == _object ? NSOrderedSame : NSOrderedDescending;
}

@end

