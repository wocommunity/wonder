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

#ifndef __EOQualifier_h__
#define __EOQualifier_h__

#import <Foundation/NSObject.h>
//#include <EOControl/EOKeyValueArchiver.h>
#include "EOKeyValueArchiver.h"

/*
  EOQualifier
  
  EOQualifier is the superclass of all the concrete qualifier classes which
  are used to build up a qualification object hierarchy (aka a SQL where
  statement).

  Subclasses:
    EOAndQualifier
    EOOrQualifier
    EONotQualifier
    EOKeyValueQualifier
    EOKeyComparisonQualifier

  EOQualifierVariable

  EOQualifierVariable defers the evaluation of some qualification value to
  runtime. It's comparable to SQL late-binding variables (aka "a=$value").
  
  Also provided are some categories on NSObject and NSArray to filter an
  in-memory object tree.
*/

@class NSDictionary, NSArray, NSSet, NSMutableSet;

@protocol EOQualifierEvaluation
- (BOOL)evaluateWithObject:(id)_object;
@end

@interface EOQualifier : NSObject < NSCopying, EOKeyValueArchiving, EOQualifierEvaluation >

+ (EOQualifier *)qualifierToMatchAnyValue:(NSDictionary *)_values;
+ (EOQualifier *)qualifierToMatchAllValues:(NSDictionary *)_values;

+ (SEL)operatorSelectorForString:(NSString *)_str;
+ (NSString *)stringForOperatorSelector:(SEL)_sel;

+ (void)setUseParenthesesForComparisonQualifier:(BOOL)flag;
+ (BOOL)useParenthesesForComparisonQualifier;

    /* bindings */

- (EOQualifier *)qualifierWithBindings:(NSDictionary *)_bindings
  requiresAllVariables:(BOOL)_reqAll;
- (NSArray *)bindingKeys;

/* keys (new in WO 4.5) */

- (NSSet *)allQualifierKeys;
- (void)addQualifierKeysToSet:(NSMutableSet *)_keys;

/* comparing */

- (BOOL)isEqual:(id)_obj;
- (BOOL)isEqualToQualifier:(EOQualifier *)_qual;

/* remapping keys */

- (EOQualifier *)qualifierByApplyingTransformer:(id)_t inContext:(id)_ctx;
- (EOQualifier *)qualifierByApplyingKeyMap:(NSDictionary *)_map;

/* BDControl additions */

- (unsigned int)count;
- (NSArray *)subqualifiers;

/* debugging */

+ (BOOL)isEvaluationDebuggingEnabled;

@end /* EOQualifier */

@interface EOQualifier(Parsing)

+ (EOQualifier *)qualifierWithQualifierFormat:(NSString *)_qualifierFormat, ...;
+ (EOQualifier *)qualifierWithQualifierFormat:(NSString *)_qualifierFormat 
  arguments:(NSArray *)_arguments;

/* this is used in "cast (xxx as mytypename)" expressions */
+ (void)registerValueClass:(Class)_valueClass forTypeName:(NSString *)_type;

@end

@interface EOAndQualifier : EOQualifier < EOQualifierEvaluation, NSCoding >
{
  NSArray  *qualifiers;
  unsigned count;
}

- (id)initWithQualifierArray:(NSArray *)_qualifiers;
- (id)initWithQualifiers:(EOQualifier *)_qual1, ...;
- (NSArray *)qualifiers;

@end /* EOAndQualifier */

@interface EOOrQualifier : EOQualifier < EOQualifierEvaluation, NSCoding >
{
  NSArray  *qualifiers;
  unsigned count;
}

- (id)initWithQualifierArray:(NSArray *)_qualifiers; /* designated init */
- (id)initWithQualifiers:(EOQualifier *)_qual1, ...;
- (NSArray *)qualifiers;

@end /* EOOrQualifier */

@interface EONotQualifier : EOQualifier < EOQualifierEvaluation, NSCoding >
{
  EOQualifier *qualifier;
}

- (id)initWithQualifier:(EOQualifier *)_qualifier; /* designated init */
- (EOQualifier *)qualifier;

@end /* EONotQualifier */

extern SEL EOQualifierOperatorEqual;
extern SEL EOQualifierOperatorNotEqual;
extern SEL EOQualifierOperatorLessThan;
extern SEL EOQualifierOperatorGreaterThan;
extern SEL EOQualifierOperatorLessThanOrEqualTo;
extern SEL EOQualifierOperatorGreaterThanOrEqualTo;
extern SEL EOQualifierOperatorContains;
extern SEL EOQualifierOperatorLike;
extern SEL EOQualifierOperatorCaseInsensitiveLike;

@interface EOKeyValueQualifier : EOQualifier < EOQualifierEvaluation, NSCoding >
{
  /* this is a '%A selector %@' qualifier */
  NSString *key;
  id       value;
  SEL      operator;
}

- (id)initWithKey:(NSString *)_key
  operatorSelector:(SEL)_selector
  value:(id)_value;

- (NSString *)key;
- (SEL)selector;
- (id)value;

@end

@interface EOKeyComparisonQualifier : EOQualifier
  < EOQualifierEvaluation, NSCoding >
{
  /* this is a '%A selector %A' qualifier */
  NSString *leftKey;
  NSString *rightKey;
  SEL      operator;
}

- (id)initWithLeftKey:(NSString *)_leftKey
  operatorSelector:(SEL)_selector
  rightKey:(NSString *)_rightKey;

- (NSString *)leftKey;
- (NSString *)rightKey;
- (SEL)selector;

@end

/* operators */

#define EOQualifierOperatorEqual                @selector(isEqualTo:)
#define EOQualifierOperatorNotEqual             @selector(isNotEqualTo:)
#define EOQualifierOperatorLessThan             @selector(isLessThan:)
#define EOQualifierOperatorGreaterThan          @selector(isGreaterThan:)
#define EOQualifierOperatorLessThanOrEqualTo    @selector(isLessThanOrEqualTo:)
#define EOQualifierOperatorGreaterThanOrEqualTo @selector(isGreaterThanOrEqualTo:)
#define EOQualifierOperatorContains             @selector(doesContain:)
#define EOQualifierOperatorLike                 @selector(isLike:)
#define EOQualifierOperatorCaseInsensitiveLike  @selector(isCaseInsensitiveLike:)

/* variable qualifier content */

@interface EOQualifierVariable : NSObject < NSCoding, EOKeyValueArchiving >
{
  NSString *varKey;
}

+ (id)variableWithKey:(NSString *)_key;
- (id)initWithKey:(NSString *)_key;

- (NSString *)key;

/* Comparing */

- (BOOL)isEqual:(id)_obj;
- (BOOL)isEqualToQualifierVariable:(EOQualifierVariable *)_obj;

@end

/* define the appropriate selectors */

@interface NSObject(QualifierComparisions)
- (BOOL)isEqualTo:(id)_object;
- (BOOL)isNotEqualTo:(id)_object;
- (BOOL)isLessThan:(id)_object;
- (BOOL)isGreaterThan:(id)_object;
- (BOOL)isLessThanOrEqualTo:(id)_object;
- (BOOL)isGreaterThanOrEqualTo:(id)_object;
- (BOOL)doesContain:(id)_object;
- (BOOL)isLike:(NSString *)_object;
- (BOOL)isCaseInsensitiveLike:(NSString *)_object;
@end

@interface NSObject(EOQualifierTransformer)

- (EOQualifier *)transformQualifier:(EOQualifier *)_q       inContext:(id)_ctx;
- (EOQualifier *)transformAndQualifier:(EOAndQualifier *)_q inContext:(id)_ctx;
- (EOQualifier *)transformOrQualifier:(EOOrQualifier *)_q   inContext:(id)_ctx;
- (EOQualifier *)transformNotQualifier:(EONotQualifier *)_q inContext:(id)_ctx;

- (EOQualifier *)transformKeyValueQualifier:(EOKeyValueQualifier *)_q 
  inContext:(id)_ctx;
- (EOQualifier *)transformKeyComparisonQualifier:(EOKeyComparisonQualifier *)q 
  inContext:(id)_ctx;

@end

/* array qualification */

#import <Foundation/NSArray.h>

@interface NSArray(Qualification)
- (NSArray *)filteredArrayUsingQualifier:(EOQualifier *)_qualifier;
@end

#endif /* __EOQualifier_h__ */
