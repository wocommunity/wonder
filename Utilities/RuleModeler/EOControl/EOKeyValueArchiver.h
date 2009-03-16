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

#ifndef __EOControl_EOKeyValueArchiver_H__
#define __EOControl_EOKeyValueArchiver_H__

#import <Foundation/NSObject.h>

@class NSString, NSDictionary, NSMutableArray, NSMutableDictionary;
@class NSMutableSet;

@interface EOKeyValueArchiver : NSObject
{
  NSMutableDictionary *plist;
  id delegate; // non-retained
}

/* coding */

- (void)encodeObject:(id)_obj            forKey:(NSString *)_key;
- (void)encodeReferenceToObject:(id)_obj forKey:(NSString *)_key;
- (void)encodeBool:(BOOL)_flag           forKey:(NSString *)_key;
- (void)encodeInt:(int)_value            forKey:(NSString *)_key;

- (NSDictionary *)dictionary;

/* delegate */

- (void)setDelegate:(id)_delegate;
- (id)delegate;

@end

@interface EOKeyValueUnarchiver : NSObject
{
  NSDictionary   *plist;
  NSMutableArray *unarchivedObjects;
  NSMutableSet   *awakeObjects;
  id parent;
  
  id delegate; // non-retained (eg a WOComponent)
}

- (id)initWithDictionary:(NSDictionary *)_dict;

/* decoding */

- (id)decodeObjectForKey:(NSString *)_key;
- (id)decodeObjectReferenceForKey:(NSString *)_key; /* ask delegate for obj */
- (BOOL)decodeBoolForKey:(NSString *)_key;
- (int)decodeIntForKey:(NSString *)_key;

- (id)decodeObjectAtIndex:(unsigned)_idx;

/* operations */

- (void)ensureObjectAwake:(id)_object;
- (void)finishInitializationOfObjects;
- (void)awakeObjects;
- (id)parent;

/* delegate */

- (void)setDelegate:(id)_delegate;
- (id)delegate;

@end

@protocol EOKeyValueArchiving

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver;
- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver;

@end

@interface NSObject(EOKeyValueArchivingAwakeMethods)

- (void)finishInitializationWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_un;
- (void)awakeFromKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver;

@end

/* delegates */

@interface NSObject(KVCArchiverDelegates)

- (id)archiver:(EOKeyValueArchiver *)_archiver
  referenceToEncodeForObject:(id)_obj;

@end

@interface NSObject(KVCUnarchiverDelegates)

- (id)unarchiver:(EOKeyValueUnarchiver *)_unarchiver
  objectForReference:(id)_obj;

@end

#endif /* __EOControl_EOKeyValueArchiver_H__ */
