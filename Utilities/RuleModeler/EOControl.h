//
//  Rule.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>

#define EOQualifierOperatorEqual @selector(isEqual:)
#define EOQualifierOperatorCaseInsensitiveLike @selector(caseInsensitiveLike:)

@interface EONull:NSObject <NSCopying, NSCoding>
{
}

+ allocWithZone:(struct _NSZone *)fp8;
+ null;
- copy;
- copyWithZone:(struct _NSZone *)fp8;
- (unsigned int)hash;
- retain;
- (void)release;
- autorelease;
- (unsigned int)retainCount;
- (void)dealloc;
- description;
- replacementObjectForPortCoder:fp8;
- (void)encodeWithCoder:fp8;
- initWithCoder:fp8;

@end

@interface EOQualifier:NSObject <NSCopying>
{
}

+ (void)initialize;
+ qualifierWithQualifierFormat:fp8;
+ qualifierWithQualifierFormat:fp8 varargList:(STR)fp12;
+ qualifierWithQualifierFormat:fp8 arguments:fp12;
+ _qualifierArrayFromDictionary:fp8;
+ qualifierToMatchAllValues:fp8;
+ qualifierToMatchAnyValue:fp8;
+ allQualifierOperators;
+ relationalQualifierOperators;
+ stringForOperatorSelector:(SEL)fp8;
+ (SEL)operatorSelectorForString:fp8;
- (void)dealloc;
- copyWithZone:(struct _NSZone *)fp8;
- validateKeysWithRootClassDescription:fp8;
- (char)evaluateWithObject:fp8;
- allQualifierKeys;
- (void)addQualifierKeysToSet:fp8;
- qualifierWithBindings:fp8 requiresAllVariables:(char)fp12;
- (void)_addBindingsToDictionary:fp8;
- bindingKeys;
- keyPathForBindingKey:fp8;

@end
@interface EOKeyValueUnarchiver:NSObject
{
    NSDictionary *_propertyList;
    id _parent;
    id _nextParent;
    NSMutableArray *_allUnarchivedObjects;
    id _delegate;
    struct _NSHashTable *_awakenedObjects;
}

- initWithDictionary:fp8;
- (void)setDelegate:fp8;
- delegate;
- parent;
- _objectForPropertyList:fp8;
- _objectsForPropertyList:fp8;
- _dictionaryForPropertyList:fp8;
- _findTypeForPropertyListDecoding:fp8;
- decodeObjectForKey:fp8;
- decodeObjectReferenceForKey:fp8;
- (char)decodeBoolForKey:fp8;
- (int)decodeIntForKey:fp8;
- (void)finishInitializationOfObjects;
- (void)ensureObjectAwake:fp8;
- (void)awakeObjects;
- (void)dealloc;

@end

@interface EOKeyValueArchiver:NSObject
{
    NSMutableDictionary *_propertyList;
    id _delegate;
}

- init;
- (void)setDelegate:fp8;
- delegate;
- (void)_encodeValue:fp8 forKey:fp12;
- (void)_encodeObjects:fp8 forKey:fp12;
- (void)_encodeDictionary:fp8 forKey:fp12;
- (void)encodeObject:fp8 forKey:fp12;
- (void)encodeReferenceToObject:fp8 forKey:fp12;
- (void)encodeBool:(char)fp8 forKey:fp12;
- (void)encodeInt:(int)fp8 forKey:fp12;
- dictionary;
- (void)dealloc;

@end
@protocol EOQualifierEvaluation
- (char)evaluateWithObject:fp8;
@end
@interface EONotQualifier:EOQualifier <EOQualifierEvaluation, NSCoding>
{
    EOQualifier *_qualifier;
}

- initWithQualifier:fp8;
- qualifier;
- (void)dealloc;
- description;
- (char)evaluateWithObject:fp8;
- validateKeysWithRootClassDescription:fp8;
- replacementObjectForPortCoder:fp8;
- initWithCoder:fp8;
- (void)encodeWithCoder:fp8;
- (void)encodeWithKeyValueArchiver:fp8;
- initWithKeyValueUnarchiver:fp8;
- qualifierWithBindings:fp8 requiresAllVariables:(char)fp12;
- (void)_addBindingsToDictionary:fp8;
- (void)addQualifierKeysToSet:fp8;

@end

@interface EOOrQualifier:EOQualifier <EOQualifierEvaluation, NSCoding>
{
    NSArray *_qualifiers;
}

- initWithQualifierArray:fp8;
- initWithQualifiers:fp8;
- qualifiers;
- (void)dealloc;
- description;
- (char)evaluateWithObject:fp8;
- validateKeysWithRootClassDescription:fp8;
- replacementObjectForPortCoder:fp8;
- initWithCoder:fp8;
- (void)encodeWithCoder:fp8;
- (void)encodeWithKeyValueArchiver:fp8;
- initWithKeyValueUnarchiver:fp8;
- qualifierWithBindings:fp8 requiresAllVariables:(char)fp12;
- (void)_addBindingsToDictionary:fp8;
- (void)addQualifierKeysToSet:fp8;

@end

@interface EOAndQualifier:EOQualifier <EOQualifierEvaluation, NSCoding>
{
    NSArray *_qualifiers;
}

- initWithQualifierArray:fp8;
- initWithQualifiers: (EOQualifier*)qualifiers,...;
- qualifiers;
- (void)dealloc;
- description;
- (char)evaluateWithObject:fp8;
- validateKeysWithRootClassDescription:fp8;
- replacementObjectForPortCoder:fp8;
- initWithCoder:fp8;
- (void)encodeWithCoder:fp8;
- (void)encodeWithKeyValueArchiver:fp8;
- initWithKeyValueUnarchiver:fp8;
- qualifierWithBindings:fp8 requiresAllVariables:(char)fp12;
- (void)_addBindingsToDictionary:fp8;
- (void)addQualifierKeysToSet:fp8;

@end

@interface EOKeyComparisonQualifier:EOQualifier <EOQualifierEvaluation, NSCoding>
{
    SEL _selector;
    NSString *_leftKey;
    NSString *_rightKey;
}

- initWithLeftKey:fp8 operatorSelector:(SEL)fp12 rightKey:fp16;
- leftKey;
- rightKey;
- (SEL)selector;
- (void)dealloc;
- description;
- (char)evaluateWithObject:fp8;
- validateKeysWithRootClassDescription:fp8;
- replacementObjectForPortCoder:fp8;
- initWithCoder:fp8;
- (void)encodeWithCoder:fp8;
- (void)encodeWithKeyValueArchiver:fp8;
- initWithKeyValueUnarchiver:fp8;
- qualifierWithBindings:fp8 requiresAllVariables:(char)fp12;
- (void)_addBindingsToDictionary:fp8;
- (void)addQualifierKeysToSet:fp8;

@end

@interface EOKeyValueQualifier:EOQualifier <EOQualifierEvaluation, NSCoding>
{
    SEL _selector;
    NSString *_key;
    id _value;
    id _lowercaseCache;
}

- initWithKey:fp8 operatorSelector:(SEL)fp12 value:fp16;
- key;
- value;
- (SEL)selector;
- (void)dealloc;
- description;
- (char)evaluateWithObject:fp8;
- validateKeysWithRootClassDescription:fp8;
- replacementObjectForPortCoder:fp8;
- initWithCoder:fp8;
- (void)encodeWithCoder:fp8;
- (void)encodeWithKeyValueArchiver:fp8;
- initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)fp8;
- qualifierWithBindings:fp8 requiresAllVariables:(char)fp12;
- (void)_addBindingsToDictionary:fp8;
- (void)addQualifierKeysToSet:fp8;

@end

@interface NSString (EOPrefixAndSuffixMethods)
+ (id)_stringWithStrings:(id)fp8;
+ (id)_stringWithUnsigned:(unsigned int)fp8;
- (BOOL)containsOnlyWhiteSpaceAndNewLines;
- (id)stringByDeletingSuffixWithDelimiter:(id)fp8;
- (id)prefixWithDelimiter:(id)fp8;
- (id)suffixWithDelimiter:(id)fp8;
- (id)stringMarkingUpcaseTransitionsWithDelimiter:(id)fp8;
- (id)quotedStringWithQuote:(id)fp8;
- (id)stringRepeatedTimes:(unsigned int)fp8;
- (id)_getBracketedStringFromBuffer:(struct _NSStringBuffer *)fp8;
- (BOOL)_matchesCharacter:(unsigned short)fp8;
- (BOOL)matchesPattern:(id)fp8 caseInsensitive:(BOOL)fp12;
- (BOOL)matchesPattern:(id)fp8;
- (BOOL)isLike:(id)fp8;
- (BOOL)isCaseInsensitiveLike:(id)fp8;
@end

