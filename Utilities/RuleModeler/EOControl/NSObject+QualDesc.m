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

#include "EONull.h"
#include "common.h"

@interface NSString(QualifierDescription)
- (NSString *)qualifierDescription;
@end

@implementation NSString(QualifierDescription)

- (NSString *)qualifierDescription {
  // Escape ' with backslash
  NSMutableString *qualifierDescription = [self mutableCopy];
  int             i = 0, count = [qualifierDescription length];
    
  for (; i < count; i++) {
    unichar aChar = [qualifierDescription characterAtIndex:i];
        
    if (aChar == '\'') {
      [qualifierDescription insertString:@"\\" atIndex:i];
      i += 1;
      count += 1;
    }
  }
    
  return [NSString stringWithFormat:@"'%@'", [qualifierDescription autorelease]];
}

@end /* NSString(QualifierDescription) */

@implementation NSObject(QualifierDescription)

- (NSString *)qualifierDescription {
  // Display using cast notation
  return [NSString stringWithFormat:@"(%@)%@", NSStringFromClass([self class]), [[self description] qualifierDescription]];
}

@end /* NSObject(QualifierDescription) */

@implementation EONull(QualifierDescription)

- (NSString *)qualifierDescription {
#ifdef WO_JAVA_COMPATIBILITY
  return @"null";
#else
  return @"nil";
#endif
}

@end /* EONull(QualifierDescription) */

#ifdef WO_JAVA_COMPATIBILITY
@implementation NSDecimalNumber(QualifierDescription)

- (NSString *)qualifierDescription {
  return [NSString stringWithFormat:@"(java.math.BigDecimal)'%@'", [self description]];
}

@end /* NSDecimalNumber(QualifierDescription) */
#endif

@implementation NSNumber(QualifierDescription)

- (NSString *)qualifierDescription {
  return [self description];
}

@end /* NSNumber(QualifierDescription) */
