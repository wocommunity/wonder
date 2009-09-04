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

#include "EOKeyValueArchiver.h"
#include "common.h"
#ifdef WO_JAVA_COMPATIBILITY
#include "EOCustomClassWrapper.h"
#endif

@implementation EOKeyValueArchiver

- (id)init {
  if ((self = [super init])) {
    self->plist = [[NSMutableDictionary alloc] init];
  }
  return self;
}

- (void)dealloc {
  [self->plist release];
  [super dealloc];
}

/* coding */

static BOOL isPListObject(id _obj) {
  if ([_obj isKindOfClass:[NSString class]])
    return YES;
  if ([_obj isKindOfClass:[NSData class]])
    return YES;
  if ([_obj isKindOfClass:[NSArray class]])
    return YES;
  if ([_obj isKindOfClass:[NSDictionary class]])
      return YES;
  return NO;
}

- (void)encodeObject:(id)_obj forKey:(NSString *)_key {
    NSParameterAssert(_key != nil);
    if (_obj == nil)
        _obj = [NSNull null];
    
    if ([_obj isKindOfClass:[NSString class]]) {
        id c;
        c = [_obj copy]; // Is it really necessary to make a copy?
        [self->plist setObject:c forKey:_key];
        [c release];
    }
    else if ([_obj isKindOfClass:[NSData class]]) {
        id c;
        c = [_obj copy]; // Is it really necessary to make a copy?
        [self->plist setObject:c forKey:_key];
        [c release];
    }
    else if ([_obj isKindOfClass:[NSArray class]]) {
        NSMutableDictionary *oldPlist;
        int i, count = [_obj count];
        NSMutableArray  *newArray = [NSMutableArray arrayWithCapacity:count];

        oldPlist = self->plist;
        for (i = 0; i < count; i++) {
            id  e = [_obj objectAtIndex:i];
            id  k = [NSString stringWithFormat:@"%i", i];
            self->plist = [[NSMutableDictionary alloc] initWithCapacity:count];
            [self encodeObject:e forKey:k];
            e = [self->plist objectForKey:k];
            [newArray addObject:e];
            [self->plist release];
        }
        self->plist = oldPlist;
        [self->plist setObject:newArray forKey:_key];
    }
    else if ([_obj isKindOfClass:[NSDictionary class]]) {
        NSMutableDictionary *oldPlist;
        int i, count = [_obj count];
        NSArray *keys = [_obj allKeys];
        NSMutableDictionary *newDict = [NSMutableDictionary dictionaryWithCapacity:count];
        
        oldPlist = self->plist;
        self->plist = [[NSMutableDictionary alloc] initWithCapacity:count];
        for (i = 0; i < count; i++) {
            id  k = [keys objectAtIndex:i];
            id  e = [_obj objectForKey:k];
            id ek;
            [self encodeObject:k forKey:k];
            ek = [self->plist objectForKey:k];
            [self encodeObject:e forKey:k];
            e = [self->plist objectForKey:k];
            [newDict setObject:e forKey:ek];
        }
        [self->plist release];
        self->plist = oldPlist;
        [self->plist setObject:newDict forKey:_key];
    }
    else {
        NSMutableDictionary *oldPlist;
        oldPlist = self->plist;
        self->plist = [[NSMutableDictionary alloc] init];
        /* store class name */
        [self->plist setObject:NSStringFromClass([_obj class]) forKey:@"class"];
        [_obj encodeWithKeyValueArchiver:self];
        [oldPlist setObject:self->plist forKey:_key];
        [self->plist release];
        self->plist = oldPlist;
    }
}

- (void)encodeReferenceToObject:(id)_obj forKey:(NSString *)_key {
  NSParameterAssert(_key != nil);
  if ([self->delegate respondsToSelector:
           @selector(archiver:referenceToEncodeForObject:)])
    _obj = [self->delegate archiver:self referenceToEncodeForObject:_obj];

  /* if _obj wasn't replaced by the delegate, encode the object in place .. */
  [self encodeObject:_obj forKey:_key];
}

- (void)encodeBool:(BOOL)_flag forKey:(NSString *)_key {
  NSParameterAssert(_key != nil);
  /* NO values are not archived .. */
  if (_flag) {
    [self->plist setObject:@"YES" forKey:_key];
  }
}
- (void)encodeInt:(int)_value forKey:(NSString *)_key {
  NSParameterAssert(_key != nil);
  [self->plist setObject:[NSString stringWithFormat:@"%i", _value] forKey:_key];
}

- (NSDictionary *)dictionary {
  return [NSDictionary dictionaryWithDictionary:self->plist];
}

/* delegate */

- (void)setDelegate:(id)_delegate {
  self->delegate = _delegate;
}
- (id)delegate {
  return self->delegate;
}

@end /* EOKeyValueArchiver */


@implementation EOKeyValueUnarchiver

- (id)initWithDictionary:(NSDictionary *)_dict {
  self->plist = [[NSDictionary alloc] initWithDictionary:_dict];
  self->unarchivedObjects = [[NSMutableArray alloc] initWithCapacity:16];
  // should be a hashtable
  self->awakeObjects = [[NSMutableSet alloc] initWithCapacity:16];
  return self;
}
- (id)init {
  [self release];
  return nil;
}

- (void)dealloc {
  [self->awakeObjects      release];
  [self->unarchivedObjects release];
  [self->plist             release];
  [super dealloc];
}

/* class handling */

- (Class)classForName:(NSString *)_name {
  /*
    This method maps class names. It is intended for archives which are
    written by the Java bridge and therefore use fully qualified Java
    package names.
    
    The mapping is hardcoded for now, this could be extended to use a
    dictionary if considered necessary.
  */
  NSString *lastComponent = nil;
  Class   clazz;
  NSRange r;

  if (_name == nil)
    return nil;
  
  if ((clazz = NSClassFromString(_name)) != Nil)
    return clazz;
  
  /* check for Java like . names (eg com.webobjects.directtoweb.Assignment) */
  
  r = [_name rangeOfString:@"." options:NSBackwardsSearch];
  if (r.length > 0 && r.location != [_name length] - 1) {
    lastComponent = [_name substringFromIndex:(r.location + r.length)];
    
    /* first check whether the last name directly matches a class */
    if ((clazz = NSClassFromString(lastComponent)) != Nil)
      return clazz;
    
    /* then check some hardcoded prefixes */
    
    if ([_name hasPrefix:@"com.webobjects.directtoweb"]) {
      NSString *s;
      
      s = [@"D2W" stringByAppendingString:lastComponent];
      if ((clazz = NSClassFromString(s)) != Nil)
	return clazz;
    }
#ifdef WO_JAVA_COMPATIBILITY
    // TODO Create plist for mapping
    if ([_name isEqualToString:@"com.webobjects.foundation.NSKeyValueCoding$Null"]) {
        return [NSNull class];
    }
    if ([_name isEqualToString:@"java.lang.Boolean"]) {
        return [NSNumber class];
    }
  
    return [EOCustomClassWrapper class];
#else
    NSLog(@"WARNING(%s): could not map Java class in unarchiver: '%@'",
	  __PRETTY_FUNCTION__, _name);
#endif
  }
  
  return Nil;
}

/* decoding */

- (id)_decodeCurrentPlist {
  NSString *className;
  Class    clazz;
  id       obj;

  if ([self->plist isKindOfClass:[NSArray class]]) {
      unsigned count;
      
      if ((count = [self->plist count]) == 0)
	obj = [NSArray array];
      else {
	unsigned i;
	id *objs;
	
	objs = objc_malloc(count * sizeof(id));
	for (i = 0; i < count; i++)
	  objs[i] = [self decodeObjectAtIndex:i];
	
	obj = [NSArray arrayWithObjects:objs count:count];
	objc_free(objs);
      }
      return obj;
  }
  
  if (![self->plist isKindOfClass:[NSDictionary class]])
    return [[self->plist copy] autorelease];
  
  /* handle dictionary */
  
  if ((className = [self->plist objectForKey:@"class"]) == nil) {
      /* treat as plain dictionary */
      NSMutableDictionary   *aDict = [NSMutableDictionary dictionaryWithCapacity:[self->plist count]];
      NSEnumerator          *keyEnum = [self->plist keyEnumerator];
      id                    eachKey;
      
      while (eachKey = [keyEnum nextObject]) {
          id    decodedKey;

          /* push */
          id lastParent   = self->parent;
          self->parent = self->plist;
          self->plist  = eachKey;
          
          decodedKey = [self _decodeCurrentPlist];
          
          /* pop */
          self->plist  = self->parent;
          self->parent = lastParent;

          /* push */
          lastParent   = self->parent;
          self->parent = self->plist;
          self->plist  = [self->plist objectForKey:eachKey];
          
          obj = [self _decodeCurrentPlist];
          [aDict setObject:obj forKey:decodedKey];
          
          /* pop */
          self->plist  = self->parent;
          self->parent = lastParent;
      }
      
      return aDict;
  }
  
  if ([className isEqualToString:@""])
      return nil;
  
  if ((clazz = [self classForName:className]) == nil) {
    NSLog(@"WARNING(%s): did not find class specified in archive '%@': %@",
	  __PRETTY_FUNCTION__, className, self->plist);
    return nil;
  }
  
  /* create custom object */
  
  obj = [clazz alloc];
  obj = [obj initWithKeyValueUnarchiver:self];
    
  if (obj != nil)
    [self->unarchivedObjects addObject:obj];
  else {
    NSLog(@"WARNING(%s): could not unarchive object %@",
	  __PRETTY_FUNCTION__, self->plist);
  }
  if (self->unarchivedObjects != nil)
    [obj release];
  else
    [obj autorelease];
  
  return obj;
}

- (id)decodeObjectAtIndex:(unsigned)_idx {
  NSDictionary *lastParent;
  id obj;
  
  /* push */
  lastParent   = self->parent;
  self->parent = self->plist;
  self->plist  = [(NSArray *)self->parent objectAtIndex:_idx];
  
  obj = [self _decodeCurrentPlist];

  /* pop */
  self->plist  = self->parent;
  self->parent = lastParent;
  
  return obj != nil ? obj : (id)[NSNull null];
}

- (id)decodeObjectForKey:(NSString *)_key {
  NSDictionary *lastParent;
  id obj;

  /* push */
  lastParent   = self->parent;
  self->parent = self->plist;
  self->plist  = [(NSDictionary *)self->parent objectForKey:_key];

  obj = [self _decodeCurrentPlist];
  
  /* pop */
  self->plist  = self->parent;
  self->parent = lastParent;
  
  return obj;
}
- (id)decodeObjectReferenceForKey:(NSString *)_key {
  id refObj, obj;

  refObj = [self decodeObjectForKey:_key];

  if ([self->delegate respondsToSelector:
           @selector(unarchiver:objectForReference:)]) {
    obj = [self->delegate unarchiver:self objectForReference:refObj];
    
    if (obj != nil && ![self->unarchivedObjects containsObject:obj])
      [self->unarchivedObjects addObject:obj];
  }
  else {
    /* if delegate does not dereference, pass back the reference object */
    // FIXME Why not added to unarchivedObjects?
    obj = refObj;
  }
  return obj;
}

- (BOOL)decodeBoolForKey:(NSString *)_key {
  id v;
  
  if ((v = [self->plist objectForKey:_key]) == nil)
    return NO;
  
  if ([v isKindOfClass:[NSString class]]) {
    unsigned l = [v length];
    
    if (l == 4 && [v isEqualToString:@"true"])   return YES;
    if (l == 5 && [v isEqualToString:@"false"])  return NO;
    if (l == 3 && [v isEqualToString:@"YES"])    return YES;
    if (l == 2 && [v isEqualToString:@"NO"])     return NO;
    if (l == 1 && [v characterAtIndex:0] == '1') return YES;
    if (l == 1 && [v characterAtIndex:0] == '0') return NO;
  }
  
  return [v boolValue];
}
- (int)decodeIntForKey:(NSString *)_key {
  return [[self->plist objectForKey:_key] intValue];
}

/* operations */

- (void)ensureObjectAwake:(id)_object {
  if (![self->awakeObjects containsObject:_object]) {
    if ([_object respondsToSelector:@selector(awakeFromKeyValueUnarchiver:)]) {
      [_object awakeFromKeyValueUnarchiver:self];
    }
    [self->awakeObjects addObject:_object];
  }
}
- (void)awakeObjects {
  NSEnumerator *e;
  id obj;

  e = [self->unarchivedObjects objectEnumerator];
  while ((obj = [e nextObject]) != nil)
    [self ensureObjectAwake:obj];
}

- (void)finishInitializationOfObjects {
  NSEnumerator *e;
  id obj;

  e = [self->unarchivedObjects objectEnumerator];
  while ((obj = [e nextObject]) != nil) {
    if ([obj respondsToSelector:
               @selector(finishInitializationWithKeyValueUnarchiver:)])
      [obj finishInitializationWithKeyValueUnarchiver:self];
  }
}

- (id)parent {
  return self->parent;
}

/* delegate */

- (void)setDelegate:(id)_delegate {
  self->delegate = _delegate;
}
- (id)delegate {
  return self->delegate;
}

@end /* EOKeyValueUnarchiver */

#ifndef WO_JAVA_COMPATIBILITY
// Not supported by WO: no available constructor with String arg
@implementation NSCalendarDate (EOKeyValueArchiving)

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
  [archiver encodeObject:@"NSCalendarDate" forKey:@"class"];
  [archiver encodeObject:[self description] forKey:@"value"];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
  return [self initWithString:[unarchiver decodeObjectForKey:@"value"]];
}

@end
#endif

@implementation NSNumber (EOKeyValueArchiving)

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
#ifdef WO_JAVA_COMPATIBILITY
  // MS: RuleModeler change!
  BOOL useRuleEditorRuleOrdering = [[NSUserDefaults standardUserDefaults] boolForKey:@"useRuleEditorRuleOrdering"];
  if (useRuleEditorRuleOrdering) {
    [archiver encodeObject:@"NSNumber" forKey:@"class"];
  } else {
    [archiver encodeObject:@"java.lang.Number" forKey:@"class"];
  }
#endif
  [archiver encodeObject:[self description] forKey:@"value"];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
  NSString    *aString = [unarchiver decodeObjectForKey:@"value"];
    
  // We need to create a new NSNumber
  [self release];
  if([aString rangeOfString:@"."].location != NSNotFound)
    return [[NSNumber alloc] initWithDouble:[aString doubleValue]];
  else if([aString caseInsensitiveCompare:@"true"] == NSOrderedSame)
    return [[NSNumber alloc] initWithBool:YES];
  else if([aString caseInsensitiveCompare:@"false"] == NSOrderedSame)
    return [[NSNumber alloc] initWithBool:NO];
  else
    return [[NSNumber alloc] initWithInt:[aString intValue]];
}

@end

@implementation NSDecimalNumber (EOKeyValueArchiving)

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
#ifdef WO_JAVA_COMPATIBILITY
  [archiver encodeObject:@"java.math.BigDecimal" forKey:@"class"];
#endif
  [archiver encodeObject:[self description] forKey:@"value"];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
  NSString    *aString = [unarchiver decodeObjectForKey:@"value"];
    
  return [self initWithString:aString];
}

@end

@implementation NSNull (EOKeyValueArchiving)

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)archiver {
#ifdef WO_JAVA_COMPATIBILITY
  // MS: RuleModeler change!
  BOOL useRuleEditorRuleOrdering = [[NSUserDefaults standardUserDefaults] boolForKey:@"useRuleEditorRuleOrdering"];
  if (useRuleEditorRuleOrdering) {
    [archiver encodeObject:@"EONull" forKey:@"class"];
  } else {
    [archiver encodeObject:@"com.webobjects.foundation.NSKeyValueCoding$Null" forKey:@"class"];
  }
#endif
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)unarchiver {
  return self;
}

@end

