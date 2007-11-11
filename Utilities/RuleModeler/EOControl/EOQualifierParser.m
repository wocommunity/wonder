/*
  Copyright (C) 2000-2007 SKYRIX Software AG
  Copyright (C) 2007      Helge Hess

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

#include <stdio.h>
#include "EOQualifier.h"
#include "EONull.h"
#include "common.h"
#ifdef WO_JAVA_COMPATIBILITY
#include "EOCustomClassWrapper.h"
#endif

//#define USE_DESCRIPTION_FOR_AT 1

static int qDebug = 0;
static NSMutableDictionary *EOQualifierParserTypeMappings = nil;

/* 
   The literals understood by the value parser.
   
   NOTE: Any literal used here can never be used as a key ! So add as little
   as possible.
*/
typedef struct {
  const unsigned char *token;
  id  value;
  int scase;
} EOQPTokEntry;

static EOQPTokEntry toks[] = {
  { (const unsigned char *)"NULL",  nil, 0 },
  { (const unsigned char *)"nil",   nil, 1 },
  { (const unsigned char *)"YES",   nil, 0 },
  { (const unsigned char *)"NO",    nil, 0 },
  { (const unsigned char *)"TRUE",  nil, 0 },
  { (const unsigned char *)"FALSE", nil, 0 },
  { (const unsigned char *)NULL,    nil, 0 }
};

static inline void _setupLiterals(void) {
  static BOOL didSetup = NO;
  if (didSetup) return;
  didSetup = YES;
  toks[0].value = [[NSNull null] retain];
  toks[1].value = toks[0].value;
  toks[2].value = [[NSNumber numberWithBool:YES] retain];
  toks[3].value = [[NSNumber numberWithBool:NO]  retain];
  toks[4].value = toks[2].value;
  toks[5].value = toks[3].value;
}

/* cache */
static Class  StringClass = Nil;
static Class  NumberClass = Nil;
static EONull *null       = nil;

/* parsing functions */

static EOQualifier *_parseCompoundQualifier(id _ctx, const char *_buf,
                                            unsigned _bufLen, unsigned *_qualLen);
static EOQualifier *_testOperator(id _ctx, const char *_buf,
                                  unsigned _bufLen, unsigned *_opLen,
                                  BOOL *_testAnd);
static EOQualifier *_parseQualifiers(id _ctx, const char *_buf,
                                     unsigned _bufLen, unsigned *_qualLen);
static EOQualifier *_parseParenthesisQualifier(id _ctx,
                                               const char *_buf, unsigned _bufLen,
                                               unsigned *_qualLen);
static EOQualifier *_parseNotQualifier(id _ctx, const char *_buf,
                                       unsigned _bufLen, unsigned *_qualLen);
static EOQualifier *_parseKeyCompQualifier(id _ctx, const char *_buf,
                                           unsigned _bufLen, unsigned *_qualLen);
static NSString *_parseKey(id _ctx, const char *_buf, unsigned _bufLen,
                           unsigned *_keyLen);
static id _parseValue(id _ctx, const char *_buf, unsigned _bufLen,
                      unsigned *_keyLen);
static inline unsigned _countWhiteSpaces(const char *_buf, unsigned _bufLen);
static NSString *_parseOp(const char *_buf, unsigned _bufLen,
                          unsigned *_opLen);

@interface EOQualifierParserContext : NSObject
{
  NSMapTable *qualifierCache;
}
- (NSDictionary *)resultForFunction:(NSString *)_fct atPos:(unsigned)_pos;
- (void)setResult:(NSDictionary *)_dict forFunction:(NSString *)_fct
            atPos:(unsigned)_pos;
- (id)getObjectFromStackFor:(char)_c;

/* factory */

- (EOQualifier *)keyComparisonQualifierWithLeftKey:(NSString *)_leftKey
  operatorSelector:(SEL)_sel
  rightKey:(NSString *)_rightKey;
- (EOQualifier *)keyValueQualifierWithKey:(NSString *)_key
  operatorSelector:(SEL)_sel
  value:(id)_value;
- (EOQualifier *)andQualifierWithArray:(NSArray *)_qualifiers;
- (EOQualifier *)orQualifierWithArray:(NSArray *)_qualifiers;
- (EOQualifier *)notQualifierWithQualifier:(EOQualifier *)_qualifier;

@end

@interface EOQualifierVAParserContext : EOQualifierParserContext
{
  va_list    *va;  
}
+ (id)contextWithVaList:(va_list *)_va;
- (id)initWithVaList:(va_list *)_va;
@end

@interface EOQualifierEnumeratorParserContext : EOQualifierParserContext
{
  NSEnumerator *enumerator;
}
+ (id)contextWithEnumerator:(NSEnumerator *)_enumerator;
- (id)initWithEnumerator:(NSEnumerator  *)_enumerator;
@end

@implementation EOQualifierVAParserContext

+ (id)contextWithVaList:(va_list *)_va {
  return [[[EOQualifierVAParserContext alloc] initWithVaList:_va] autorelease];
}

- (id)initWithVaList:(va_list *)_va {
  if ((self = [super init])) {
    self->va = _va;
  }
  return self;
}

- (id)getObjectFromStackFor:(char)_c {
  id obj = nil;

  if (StringClass == Nil) StringClass = [NSString class];
  if (NumberClass == Nil) NumberClass = [NSNumber class];
  if (null == nil)        null        = [EONull null];
  
  if (_c == 's') {
    char *str = va_arg(*self->va, char*);
    obj = [StringClass stringWithCString:str]; // Keep CString for %s
  }
  else if (_c == 'd') {
    int i= va_arg(*self->va, int);
    obj = [NumberClass numberWithInt:i];
  }
  else if (_c == 'f') {
    double d = va_arg(*self->va, double);
    obj = [NumberClass numberWithDouble:d];
  }
  else if (_c == '@') {
    id o = va_arg(*self->va, id);
#if USE_DESCRIPTION_FOR_AT
    obj = (o == nil) ? (id)null : (id)[o description];
#else
    obj = (o == nil) ? (id)null : (id)o;
#endif
  }
  else {
    [NSException raise:NSInvalidArgumentException
                format:@"unknown conversation char %c", _c];
  }
  return obj;
}

@end /* EOQualifierVAParserContext */

@implementation EOQualifierEnumeratorParserContext

+ (id)contextWithEnumerator:(NSEnumerator *)_enumerator {
  return [[[EOQualifierEnumeratorParserContext alloc]
                                      initWithEnumerator:_enumerator] autorelease];
}

- (id)initWithEnumerator:(NSEnumerator *)_enumerator {
  if ((self = [super init])) {
      self->enumerator = [_enumerator retain];
  }
  return self;
}

- (void)dealloc {
  [self->enumerator release];
  [super dealloc];;
}

- (id)getObjectFromStackFor:(char)_c {
  id o;

  if (NumberClass == Nil) NumberClass = [NSNumber class];

  o = [self->enumerator nextObject];
  switch (_c) {
    case '@':
#if USE_DESCRIPTION_FOR_AT
      return [o description];
#else
      return o;
#endif
    
    case 'f':
      return [NumberClass numberWithDouble:[o doubleValue]];
      
    case 'd':
      return [NumberClass numberWithInt:[o intValue]];
      
    case 's':
      // return [NSString stringWithCString:[o cString]];
      return [[o copy] autorelease];
      
    default:
      [NSException raise:NSInvalidArgumentException
                  format:@"unknown or not allowed conversation char %c", _c];
  }
  return nil;
}

@end /* EOQualifierEnumeratorParserContext */

@implementation EOQualifierParserContext

- (id)init {
  if (StringClass == Nil) StringClass = [NSString class];
  
  if ((self = [super init])) {
    self->qualifierCache = NSCreateMapTable(NSObjectMapKeyCallBacks,
                                            NSObjectMapValueCallBacks,
                                            200);
  }
  return self;
}

- (void)dealloc {
  if (self->qualifierCache) NSFreeMapTable(self->qualifierCache);
  [super dealloc];
}

- (NSDictionary *)resultForFunction:(NSString *)_fct atPos:(unsigned)_pos
{
  return NSMapGet(self->qualifierCache,
                  [StringClass stringWithFormat:@"%@_%d", _fct, _pos]);
}

- (void)setResult:(NSDictionary *)_dict forFunction:(NSString *)_fct
  atPos:(unsigned)_pos
{
  NSMapInsert(self->qualifierCache,
              [StringClass stringWithFormat:@"%@_%d", _fct, _pos],
              _dict);
}

- (id)getObjectFromStackFor:(char)_c {
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

/* factory */

- (EOQualifier *)keyComparisonQualifierWithLeftKey:(NSString *)_leftKey
  operatorSelector:(SEL)_sel
  rightKey:(NSString *)_rightKey
{
  static Class clazz = Nil;
  if (clazz == Nil) clazz = [EOKeyComparisonQualifier class];
  
  return [[[clazz alloc]
                  initWithLeftKey:_leftKey
                  operatorSelector:_sel
                  rightKey:_rightKey]
                  autorelease];
}
- (EOQualifier *)keyValueQualifierWithKey:(NSString *)_key
  operatorSelector:(SEL)_sel
  value:(id)_value
{
  static Class clazz = Nil;
  if (clazz == Nil) clazz = [EOKeyValueQualifier class];
  
  return [[[clazz alloc]
                  initWithKey:_key
                  operatorSelector:_sel
                  value:_value]
                  autorelease];
}
- (EOQualifier *)andQualifierWithArray:(NSArray *)_qualifiers {
  static Class clazz = Nil;
  if (clazz == Nil) clazz = [EOAndQualifier class];
  
  return [[[clazz alloc] initWithQualifierArray:_qualifiers] autorelease];
}
- (EOQualifier *)orQualifierWithArray:(NSArray *)_qualifiers {
  static Class clazz = Nil;
  if (clazz == Nil) clazz = [EOOrQualifier class];
  
  return [[[clazz alloc] initWithQualifierArray:_qualifiers] autorelease];
}

- (EOQualifier *)notQualifierWithQualifier:(EOQualifier *)_qualifier {
  static Class clazz = Nil;
  if (clazz == Nil) clazz = [EONotQualifier class];
  
  return [[[clazz alloc] initWithQualifier:_qualifier] autorelease];
}

- (EOQualifierVariable *)variableWithKey:(NSString *)_key {
  static Class clazz = Nil;
  if (clazz == Nil) clazz = [EOQualifierVariable class];

  return [clazz variableWithKey:_key];
}

@end /* EOQualifierParserContext */

@implementation EOQualifier(Parsing)

+ (void)registerValueClass:(Class)_valueClass forTypeName:(NSString *)_type {
  if (EOQualifierParserTypeMappings == nil)
    EOQualifierParserTypeMappings = [[NSMutableDictionary alloc] init];
  
  if (_type == nil) {
    NSLog(@"ERROR(%s): got passed no type name!", __PRETTY_FUNCTION__);
    return;
  }
  if (_valueClass == nil) {
    NSLog(@"ERROR(%s): got passed no value-class for type '%@'!",
          __PRETTY_FUNCTION__, _type);
    return;
  }
  
  [EOQualifierParserTypeMappings setObject:_valueClass forKey:_type];
}

+ (EOQualifier *)qualifierWithQualifierFormat:(NSString *)_qualifierFormat,... {
  va_list     va;
  EOQualifier *qualifier = nil;
  unsigned    length = 0;
  const char  *buf;
  unsigned    bufLen;

  _setupLiterals();
  qDebug = [EOQualifier isEvaluationDebuggingEnabled];
  if (StringClass == Nil) StringClass = [NSString class];
  
  buf = [_qualifierFormat UTF8String];
  bufLen = strlen(buf);
  
  va_start(va, _qualifierFormat);
  NS_DURING
    qualifier =
      _parseQualifiers([EOQualifierVAParserContext contextWithVaList:&va],
                       buf, bufLen, &length);
  NS_HANDLER
      va_end(va);
      [localException raise];
  NS_ENDHANDLER
  
  va_end(va);
  
  if (qualifier != nil) { /* check whether the rest of the string is OK */
    if (length < bufLen)
      length += _countWhiteSpaces(buf + length, bufLen - length);
    
    if (length < bufLen) {
      NSLog(@"WARNING(%s): unexpected chars at the end of the "
            @"string(class=%@,len=%i) '%@'",
            __PRETTY_FUNCTION__,
            [_qualifierFormat class],
            [_qualifierFormat length], _qualifierFormat);
      NSLog(@"  buf-length: %i", bufLen);
      NSLog(@"  length:     %i", length);
      NSLog(@"  char[length]: '%c' (%i) '%s'", buf[length], buf[length],
	    (buf+length));
      qualifier = nil;
      [NSException raise:NSInvalidArgumentException format:@"Unexpected chars '%@' at the end of the string '%@'", [[[StringClass alloc] initWithBytes:buf + length length:bufLen - length encoding:NSUTF8StringEncoding] autorelease], _qualifierFormat];
    }
    else if (length > bufLen) {
      NSLog(@"WARNING(%s): length should never be longer than bufLen ?, "
	    @"internal parsing error !",
	    __PRETTY_FUNCTION__);
        [NSException raise:NSInternalInconsistencyException format:@"%s: length should never be longer than bufLen ('%@')", __PRETTY_FUNCTION__, [[[StringClass alloc] initWithBytes:buf length:bufLen encoding:NSUTF8StringEncoding] autorelease]];
    }
  }
  // FIXME Else, should never happen: an exception should have been raised
  return qualifier;
}

+ (EOQualifier *)qualifierWithQualifierFormat:(NSString *)_qualifierFormat 
  arguments:(NSArray *)_arguments
{
  EOQualifier *qual  = nil;
  unsigned    length = 0;
  const char  *buf   = NULL;
  unsigned    bufLen = 0;
  EOQualifierEnumeratorParserContext *ctx;

  _setupLiterals();
  qDebug = [EOQualifier isEvaluationDebuggingEnabled];
  if (StringClass == Nil) StringClass = [NSString class];
  
  ctx = [EOQualifierEnumeratorParserContext contextWithEnumerator:
					      [_arguments objectEnumerator]];
  
  //NSLog(@"qclass: %@", [_qualifierFormat class]);
  buf = [_qualifierFormat UTF8String];
  bufLen = strlen(buf);
  qual   = _parseQualifiers(ctx, buf, bufLen, &length);
  
  if (qual != nil) { /* check whether the rest of the string is OK */
    if (length < bufLen) {
      length += _countWhiteSpaces(buf + length, bufLen - length);
    }
    if (length != bufLen) {
      NSLog(@"WARNING(%s): unexpected chars at the end of the string '%@'",
            __PRETTY_FUNCTION__, _qualifierFormat);
      qual = nil;
      [NSException raise:NSInvalidArgumentException format:@"Unexpected chars '%@' at the end of the string '%@'", [[[StringClass alloc] initWithBytes:buf + length length:bufLen - length encoding:NSUTF8StringEncoding] autorelease], _qualifierFormat];
    }
  }
  return qual;
}
 
@end /* EOQualifier(Parsing) */

static EOQualifier *_parseSingleQualifier(id _ctx, const char *_buf,
                                            unsigned _bufLen,
                                            unsigned *_qualLen)
{
  EOQualifier *res = nil;

  if ((res = _parseParenthesisQualifier(_ctx, _buf, _bufLen, _qualLen))  != nil) {
    if (qDebug)
      NSLog(@"_parseSingleQualifier return <%@> for <%s> ", res, _buf);

    return res;
  }
  if ((res = _parseNotQualifier(_ctx, _buf, _bufLen, _qualLen)) != nil) {
    if (qDebug)
      NSLog(@"_parseSingleQualifier return <%@> for <%s> ", res, _buf);

    return res;
  }
  if ((res = _parseKeyCompQualifier(_ctx, _buf, _bufLen, _qualLen)) != nil) {
    if (qDebug) {
      NSLog(@"_parseSingleQualifier return <%@> for <%s> length %d", 
	    res, _buf, *_qualLen);
    }
    return res;
  }
  return nil;
}

static EOQualifier *_parseQualifiers(id _ctx, const char *_buf, unsigned _bufLen,
                                     unsigned *_qualLen)
{
  EOQualifier *res = nil;


  if ((res = _parseCompoundQualifier(_ctx, _buf, _bufLen, _qualLen))) {
    if (qDebug)
      NSLog(@"_parseQualifiers return <%@> for <%s> ", res, _buf);
    return res;
  }

  if ((res = _parseSingleQualifier(_ctx, _buf, _bufLen, _qualLen))) {
    if (qDebug)
      NSLog(@"_parseQualifiers return <%@> for <%s> ", res, _buf);
    return res;
  }
  
  if (qDebug)
    NSLog(@"_parseQualifiers return nil for <%s> ", _buf);

  return nil;
}

static EOQualifier *_parseParenthesisQualifier(id _ctx, const char *_buf,
                                               unsigned _bufLen,
                                               unsigned *_qualLen)
{
  unsigned    pos     = 0;
  unsigned    qualLen = 0;
  EOQualifier *qual   = nil;

  pos = _countWhiteSpaces(_buf, _bufLen);

  if (_bufLen <= pos + 2) /* at least open and close parenthesis */ {
    if (qDebug)
      NSLog(@"1_parseParenthesisQualifier return nil for <%s> ", _buf);
 
    return nil;
  }
  if (_buf[pos] != '(') {
    if (qDebug)
      NSLog(@"2_parseParenthesisQualifier return nil for <%s> ", _buf);
    
    return nil;
  }
  pos++;
  if (!(qual = _parseQualifiers(_ctx, _buf + pos, _bufLen - pos,
                                &qualLen))) {
    if (qDebug)
      NSLog(@"3_parseParenthesisQualifier return nil for <%s> ", _buf);
    
    return nil;
  }
  
  pos += qualLen;
  if (_bufLen <= pos) {
    if (qDebug)
      NSLog(@"4_parseParenthesisQualifier return nil for <%s> qual[%@] %@ bufLen %d "
            @"pos %d", _buf, [qual class], qual, _bufLen, pos);
      [NSException raise:NSInvalidArgumentException format:@"Expected closing parenthesis"];

    return nil;
  }
  pos += _countWhiteSpaces(_buf + pos, _bufLen - pos);
  if (_buf[pos] != ')') {
    if (qDebug)
      NSLog(@"5_parseParenthesisQualifier return nil for <%s> [%s] ", _buf, _buf+pos);

    return nil;
  }
  if (qDebug)
    NSLog(@"6_parseParenthesisQualifier return <%@> for <%s> ", qual, _buf);
  
  *_qualLen = pos + 1; /* one step after the parenthesis */
  return qual;
}

static EOQualifier *_parseNotQualifier(id _ctx, const char *_buf,
                                       unsigned _bufLen, unsigned *_qualLen)
{
  unsigned    pos, len   = 0;
  char        c0, c1, c2 = 0;
  EOQualifier *qual      = nil;

  pos = _countWhiteSpaces(_buf, _bufLen);

  if (_bufLen - pos < 4) { /* at least 3 chars for 'NOT' */
    if (qDebug)
      NSLog(@"_parseNotQualifier return nil for <%s> ", _buf);
    
    return nil;
  }
  c0 = _buf[pos];
  c1 = _buf[pos + 1];
  c2 = _buf[pos + 2];
  if (!(((c0 == 'n') || (c0 == 'N')) &&
        ((c1 == 'o') || (c1 == 'O')) &&
        ((c2 == 't') || (c2 == 'T')))) {
    if (qDebug)
      NSLog(@"_parseNotQualifier return nil for <%s> ", _buf);
    return nil;
  }
  pos += 3;
  qual = _parseSingleQualifier(_ctx, _buf + pos, _bufLen - pos, &len);
  if (qual == nil) {
    if (qDebug)
      NSLog(@"_parseNotQualifier return nil for <%s> ", _buf);
    
    return nil;
  }
  *_qualLen = pos +len;
  if (qDebug)
    NSLog(@"_parseNotQualifier return %@ for <%s> ", qual, _buf);

  return [_ctx notQualifierWithQualifier:qual];
}

static EOQualifier *_parseKeyCompQualifier(id _ctx, const char *_buf,
                                           unsigned _bufLen, unsigned *_qualLen)
{
  NSString     *key       = nil;
  NSString     *op        = nil;
  NSString     *value     = nil;
  EOQualifier  *qual      = nil;
  NSDictionary *dict      = nil;
  SEL          sel        = NULL;
  unsigned     length     = 0;
  unsigned     pos        = 0;
  BOOL         valueIsKey = NO;

  dict = [_ctx resultForFunction:@"parseKeyCompQualifier" 
	       atPos:(unsigned long)_buf];
  if (dict != nil) {
    if (qDebug)
      NSLog(@"_parseKeyCompQual return <%@> [cached] for <%s> ", dict, _buf);
    
    *_qualLen = [[dict objectForKey:@"length"] unsignedIntValue];
    return [dict objectForKey:@"object"];
  }
  pos = _countWhiteSpaces(_buf, _bufLen);

  if ((key = _parseKey(_ctx , _buf + pos, _bufLen - pos, &length)) == nil) {
    if (qDebug)
      NSLog(@"_parseKeyCompQualifier return nil for <%s> ", _buf);
    
      // FIXME Raise?
    return nil;
  }
  pos += length;
  pos += _countWhiteSpaces(_buf + pos, _bufLen - pos);

  if (!(op = _parseOp(_buf + pos, _bufLen - pos, &length))) {
    if (qDebug)
      NSLog(@"_parseKeyCompQualifier return nil for <%s> ", _buf);
      [NSException raise:NSInvalidArgumentException format:@"Invalid qualifier format. Missing operator after '%@'", [[[StringClass alloc] initWithBytes:_buf length:pos encoding:NSUTF8StringEncoding] autorelease]];
    return nil;
  }
  sel = [EOQualifier operatorSelectorForString:op];
  if (sel == NULL) {
    NSLog(@"WARNING(%s): possible unknown operator <%@>", __PRETTY_FUNCTION__,
          op);
    if (qDebug)
      NSLog(@"_parseKeyCompQualifier return nil for <%s> ", _buf);
    return nil;
  }
  pos       +=length;
  pos       += _countWhiteSpaces(_buf + pos, _bufLen - pos);
  valueIsKey = NO;  
  
  value = _parseValue(_ctx, _buf + pos, _bufLen - pos, &length);
  if (value == nil) {
    value = _parseKey(_ctx, _buf + pos, _bufLen - pos, &length);
    if (value == nil) {
      if (qDebug)
	NSLog(@"_parseKeyCompQualifier return nil for <%s> ", _buf);
      [NSException raise:NSInvalidArgumentException format:@"Invalid qualifier format. Error after '%@'", [[[StringClass alloc] initWithBytes:_buf length:pos encoding:NSUTF8StringEncoding] autorelease]];
    }
    else
      valueIsKey = YES;
  }
  pos      +=length;  
  *_qualLen = pos;

  qual = (valueIsKey)
    ? [_ctx keyComparisonQualifierWithLeftKey:key
            operatorSelector:sel
            rightKey:value]
    : [_ctx keyValueQualifierWithKey:key
            operatorSelector:sel
            value:value];
  
  if (qDebug)
    NSLog(@"_parseKeyCompQualifier return <%@> for <%s> ", qual, _buf);

  if (qual != nil) {
    id keys[2], values[2];
    keys[0] = @"length"; values[0] = [NSNumber numberWithUnsignedInt:pos];
    keys[1] = @"object"; values[1] = qual;
    [_ctx setResult:
            [NSDictionary dictionaryWithObjects:values forKeys:keys count:2]
          forFunction:@"parseKeyCompQualifier"
          atPos:(unsigned long)_buf];
    *_qualLen = pos;
  }
  return qual;
}

static NSString *_parseOp(const char *_buf, unsigned _bufLen,
                          unsigned *_opLen)
{
  unsigned pos = 0;
  char     c0  = 0;
  char     c1  = 0;  

  if (_bufLen == 0) {
    if (qDebug)
      NSLog(@"_parseOp _bufLen == 0 --> return nil");
    return nil;
  }
  pos = _countWhiteSpaces(_buf, _bufLen);
  if (_bufLen - pos > 1) {/* at least an operation and a value */
    c0 = _buf[pos];
    c1 = _buf[pos+1];  

    if (((c0 >= '<') && (c0 <= '>')) || (c0 == '!')) {
      NSString *result;
      
      if ((c1 >= '<') && (c1 <= '>')) {
        *_opLen = 2;
        result = [[[StringClass alloc] initWithBytes:_buf + pos length:2 encoding:NSUTF8StringEncoding] autorelease];
	if (qDebug)
	  NSLog(@"_parseOp return <%@> for <%s> ", result, _buf);
      }
      else {
        *_opLen = 1;
        result = [[[StringClass alloc] initWithBytes:&c0 length:1 encoding:NSUTF8StringEncoding] autorelease];
	if (qDebug)
	  NSLog(@"_parseOp return <%@> for <%s> ", result, _buf);
      }
      return result;
    }
    else { /* string designator operator */
      unsigned opStart = pos;
      while (pos < _bufLen) {
        if (_buf[pos] == ' ')
          break;
        pos++;
      }
      if (pos >= _bufLen) {
        NSLog(@"WARNING(%s): found end of string during operator parsing",
              __PRETTY_FUNCTION__);
      }

      if (qDebug) {
	NSLog(@"%s: _parseOp return <%@> for <%s> ", __PRETTY_FUNCTION__,
          [[[StringClass alloc] initWithBytes:_buf + opStart length:pos - opStart encoding:NSUTF8StringEncoding] autorelease], _buf);
      }
      
      *_opLen = pos;
      return [[[StringClass alloc] initWithBytes:_buf + opStart length:pos - opStart encoding:NSUTF8StringEncoding] autorelease];
    }
  }
  if (qDebug)
    NSLog(@"_parseOp return nil for <%s> ", _buf);
  return nil;
}

static NSString *_parseKey(id _ctx, const char *_buf, unsigned _bufLen,
                           unsigned *_keyLen)
{ 
  id           result   = nil;
  NSDictionary *dict    = nil;
  unsigned     pos      = 0;
  unsigned     startKey = 0;
  char         c        = 0;

  if (_bufLen == 0) {
    if (qDebug) NSLog(@"%s: _bufLen == 0 --> return nil", __PRETTY_FUNCTION__);
    return nil;
  }
  dict = [_ctx resultForFunction:@"parseKey" atPos:(unsigned long)_buf];
  if (dict != nil) {
    if (qDebug) {
      NSLog(@"%s: return <%@> [cached] for <%s> ", __PRETTY_FUNCTION__,
	    dict, _buf);
    }
    *_keyLen = [[dict objectForKey:@"length"] unsignedIntValue];
    return [dict objectForKey:@"object"];
  }
  pos      = _countWhiteSpaces(_buf, _bufLen);
  startKey = pos;
  c        = _buf[pos];

  if (c == '%') {
    if (_bufLen - pos < 2) {
      if (qDebug) {
	NSLog(@"%s: [c==%%,bufLen-pos<2]: _parseValue return nil for <%s> ", 
	      __PRETTY_FUNCTION__, _buf);
      }
      return nil;
    }
    pos++;
    result = [_ctx getObjectFromStackFor:_buf[pos]];
    pos++;
  }
  else {
    /* '{' for namspaces */
    register BOOL isQuotedKey = NO;

    if (c == '"')
      isQuotedKey = YES;
    else if (!(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) ||
             c == '{')) {
      if (qDebug) {
	NSLog(@"%s: [c!=AZaz{]: _parseKey return nil for <%s> ", 
	      __PRETTY_FUNCTION__, _buf);
      }
      return nil;
    }
    
    pos++;
    while (pos < _bufLen) {
      c = _buf[pos];
      if (isQuotedKey && c == '"')
        break;
      else if
	  ((c == '<') || (c == '>') || (c == '=') || (c == '!') ||
           c == ')' || c == '(')
          break;
      else if (_countWhiteSpaces(_buf + pos, _bufLen - pos) != 0)
        break;
      pos++;    
    }
    if (isQuotedKey) {
      pos++; // skip quote
      result = [[[StringClass alloc] initWithBytes:(_buf + startKey + 1) 
                                            length:(pos - startKey - 2)
                                          encoding:NSUTF8StringEncoding] autorelease];
    }
    else {
      result = [[[StringClass alloc] initWithBytes:(_buf + startKey) 
                                            length:(pos - startKey)
                                          encoding:NSUTF8StringEncoding] autorelease];
    }
  }
  *_keyLen = pos;  
  if (qDebug)
    NSLog(@"%s: return <%@> for <%s> ", __PRETTY_FUNCTION__, result, _buf);
  
  if (result != nil) {
    id keys[2], values[2];
    
    keys[0] = @"length"; values[0] = [NSNumber numberWithUnsignedInt:pos];
    keys[1] = @"object"; values[1] = result;
    
    [_ctx setResult:
            [NSDictionary dictionaryWithObjects:values forKeys:keys count:2]
          forFunction:@"parseKey"
          atPos:(unsigned long)_buf];
    *_keyLen = pos;
  }
  return result;
}

static id _parseValue(id _ctx, const char *_buf, unsigned _bufLen,
                      unsigned *_keyLen)
{
  NSString     *cast = nil;
  NSDictionary *dict = nil;
  id           obj   = nil;
  unsigned     pos   = 0;
  char         c     = 0;
  
  if (NumberClass == Nil) NumberClass = [NSNumber class];
  if (null == nil) null = [[NSNull null] retain];
  
  if (_bufLen == 0) {
    if (qDebug) NSLog(@"_parseValue _bufLen == 0 --> return nil");
    return nil;
  }
  
  dict = [_ctx resultForFunction:@"parseValue" atPos:(unsigned long)_buf];
  if (dict != nil) {
    if (qDebug) {
      NSLog(@"_parseKeyCompQualifier return <%@> [cached] for <%s> ",
	    dict, _buf);
    }
    *_keyLen = [[dict objectForKey:@"length"] unsignedIntValue];
    return [dict objectForKey:@"object"];
  }
  
  pos = _countWhiteSpaces(_buf, _bufLen);
  c   = _buf[pos];
  
  if (c == '$') { /* found EOQualifierVariable */
    unsigned startVar = 0;
    NSString *varKey;
    
    pos++;
    startVar = pos;
    while (pos < _bufLen) {
      if ((_buf[pos] == ' ') || (_buf[pos] == ')'))
        break;
      pos++;
    }

    varKey = [[[StringClass alloc] initWithBytes:(_buf + startVar)
                                          length:pos - startVar
                                        encoding:NSUTF8StringEncoding] autorelease];
    obj = [_ctx variableWithKey:varKey];
  }
  else {
    /* first, check for CAST */
    BOOL parseComplexCast = NO;
    
    if (c == 'c' && _bufLen > 14) {
      if (strstr(_buf, "cast") == _buf && (isspace(_buf[4]) || _buf[4]=='(')) {
	/* for example: cast("1970-01-01T00:00:00Z" as 'dateTime') [min 15 #]*/
	pos += 4; /* skip 'cast' */
        while (isspace(_buf[pos])) /* skip spaces */
          pos++;
        if (_buf[pos] != '(') {
          NSLog(@"WARNING(%s): got unexpected cast string: '%s'",
                __PRETTY_FUNCTION__, _buf);
        }
        else
          pos++; /* skip opening bracket '(' */
        
	parseComplexCast = YES;
	c = _buf[pos];
      }
    }
    else if (c == '(') { /* starting with a cast */
      /* for example: (NSCalendarDate)"1999-12-12" [min 5 chars] */
      unsigned startCast = 0;
      
      pos++;
      startCast = pos;
      while (pos < _bufLen) {
        if (_buf[pos] == ')')
          break;
        pos++;
      }
      pos++;
      if (pos >= _bufLen) {
        NSLog(@"WARNING(%s): found end of string while reading a cast",
              __PRETTY_FUNCTION__);
        return nil;
      }
      c    = _buf[pos];
      cast = [[[StringClass alloc] initWithBytes:(_buf + startCast)
                                          length:(pos - 1 - startCast) 
                                        encoding:NSUTF8StringEncoding] autorelease];
      if (qDebug)
	NSLog(@"%s: got cast %@", __PRETTY_FUNCTION__, cast);
    }
    
    /* next, check for FORMAT SPECIFIER */
    if (c == '%') {
      if (_bufLen - pos < 2) {
	if (qDebug)
	  NSLog(@"_parseValue return nil for <%s> ", _buf);
	
        return nil;
      }
      pos++;
      obj = [_ctx getObjectFromStackFor:_buf[pos]];
      pos++;
    }
    
    /* next, check for A NUMBER */
    else if (((c >= '0') && (c <= '9')) || (c == '-') || (c == '.')) { /* got a number */
      unsigned startNumber;
      BOOL isFloat = (c == '.');

      startNumber = pos;
      pos++;
      while (pos < _bufLen) {
        c = _buf[pos];
          if (!((c >= '0') && (c <= '9'))) {
              if (c == '.') {
                  if (isFloat)
                      [NSException raise:NSInvalidArgumentException format:@"Invalid numerical value near %@", [[[NSString alloc] initWithBytes:_buf + startNumber length:_bufLen - startNumber encoding:NSUTF8StringEncoding] autorelease]];
                  else
                      isFloat = YES;
              }
              else
                  break;
          }
        pos++;
      }
      if(isFloat)
        obj = [NumberClass numberWithDouble:atof(_buf + startNumber)];
      else
        obj = [NumberClass numberWithInt:atoi(_buf + startNumber)];
    }

    /* check for some text literals */
    if ((obj == nil) && ((_bufLen - pos) > 1)) {
      unsigned char i;
      
      for (i = 0; i < 20 && (toks[i].token != NULL) && (obj == nil); i++) {
	const unsigned char *tok;
	unsigned char toklen;
	int rc;
	
	tok = toks[i].token;
	toklen = strlen((const char *)tok);
	if ((_bufLen - pos) < toklen)
	  /* remaining string not long enough */
	  continue;
	
	rc = toks[i].scase 
	  ? strncmp(&(_buf[pos]),     (const char *)tok, toklen)
	  : strncasecmp(&(_buf[pos]), (const char *)tok, toklen);
	if (rc != 0)
	  /* does not match */
	  continue;
	
	if (!(_buf[pos + toklen] == '\0' || isspace(_buf[pos + toklen]) ||
	      _buf[pos + toklen] == ')')) {
      /*
        Not at the string end or followed by a space or a right 
        parenthesis. The latter is required to correctly parse this:
          (not (attribute = nil) and
               attribute.className = 'com.webobjects.foundation.NSTimestamp')
      */
      continue;
    }

	/* wow, found the token */
	pos += toklen; /* skip it */
	obj = toks[i].value;
      }
    }
    
    /* next, check for STRING */
    if (obj == nil) {
      if ((c == '\'') || (c == '"')) {
	NSString *res                  = nil;
	char     string[_bufLen - pos];
	unsigned cnt                   = 0;
      
	pos++;
	while (pos < _bufLen) {
	  char ch = _buf[pos];
	  if (ch == c)
	    break;
	  if ((ch == '\\') && (_bufLen > (pos + 1))) {
	    if (_buf[pos + 1] == c) {
	      pos += 1;
	      ch = c;
	    }
	  }
	  string[cnt++] = ch;
	  pos++;
	}
	if (pos >= _bufLen) {
	  NSLog(@"WARNING(%s): found end of string before end of quoted text",
		__PRETTY_FUNCTION__);
        [NSException raise:NSInvalidArgumentException format:@"Found end of string before end of quoted text"];
	  return nil;
	}
	res = [[[StringClass alloc] initWithBytes:string length:cnt encoding:NSUTF8StringEncoding] autorelease];
	pos++; /* don`t forget quotations */
	if (qDebug) NSLog(@"_parseValue return <%@> for <%s> ", res, _buf);
	obj = res;
      }
    }
    
    /* complete parsing of cast */
    if (parseComplexCast && (pos + 6) < _bufLen) {
      /* now we need " as 'dateTime'" [min 7 #] */
      
      /* skip spaces */
      while (isspace(_buf[pos]) && pos < _bufLen) pos++;
      
      //printf("POS: '%s'\n", &(_buf[pos]));
      /* parse 'as' */
      if (_buf[pos] != 'a' && _buf[pos] != 'A')
	NSLog(@"%s: expecting 'AS' of complex cast ...", __PRETTY_FUNCTION__);
      else if (_buf[pos + 1] != 's' && _buf[pos + 1] != 'S')
	NSLog(@"%s: expecting 'AS' of complex cast ...", __PRETTY_FUNCTION__);
      else {
	/* skip AS */
	pos += 2;
	
	/* skip spaces */
	while (isspace(_buf[pos]) && pos < _bufLen) pos++;
	
	/* read cast type */
	if (_buf[pos] != '\'') {
	  NSLog(@"%s: expected type of complex cast ...", __PRETTY_FUNCTION__);
	}
	else {
	  const unsigned char *cs, *ce;
	  
	  //printf("POS: '%s'\n", &(_buf[pos]));
	  pos++;
	  cs = (const unsigned char *)&(_buf[pos]);
	  ce = (const unsigned char *)index((const char *)cs, '\'');
	  cast = [[[StringClass alloc] initWithBytes:(const char*)cs length:(ce - cs) encoding:NSUTF8StringEncoding] autorelease];
	  if (qDebug) {
	    NSLog(@"%s: parsed complex cast: '%@' to '%@'", 
		  __PRETTY_FUNCTION__, obj, cast);
	  }
	  pos += (ce - cs);
	  pos++; // skip '
	  pos++; // skip )
	  //printf("POS: '%s'\n", &(_buf[pos]));
	}
      }
    }
  }
  
  if (cast != nil && obj != nil) {
    Class class = Nil;
    id orig = obj;
    
    if ((class = [EOQualifierParserTypeMappings objectForKey:cast]) == nil) {
      /* no value explicitly mapped to class, try to construct class name... */
      NSString *className;

      className = cast;
      if ((class = NSClassFromString(className)) == Nil) {
        /* check some default cast types ... */
        className = [cast lowercaseString];
        
        if ([className isEqualToString:@"datetime"])
          class = [NSCalendarDate class];
        else if ([className isEqualToString:@"datetime.tz"])
          class = [NSCalendarDate class];
      }
    }
    if (class) {
      obj = [[[class alloc] initWithString:[orig description]] autorelease];
      
      if (obj == nil) {
	NSLog(@"%s: could not init object '%@' of cast class %@(%@) !",
	      __PRETTY_FUNCTION__, orig, class, cast);
	obj = null;
      }
    }
    else {
#ifdef WO_JAVA_COMPATIBILITY
        obj = [[[EOCustomClassWrapper alloc] initWithCustomClassName:cast value:[obj description]] autorelease];
#else
      NSLog(@"WARNING(%s): could not map cast '%@' to a class "
	    @"(returning null) !", 
	    __PRETTY_FUNCTION__, cast);
      obj = null;
#endif
    }
  }
  
  if (qDebug) {
    NSLog(@"%s: return <%@> for <%s> ", __PRETTY_FUNCTION__, 
	  obj != nil ? obj : (id)@"<nil>", _buf);
  }
  
  if (obj != nil) {
    NSDictionary *d;
    id keys[2], values[2];
    
    keys[0] = @"length"; values[0] = [NSNumber numberWithUnsignedInt:pos];
    keys[1] = @"object"; values[1] = obj;
    
    d = [[NSDictionary alloc] initWithObjects:values forKeys:keys count:2];
    [_ctx setResult:d forFunction:@"parseValue" atPos:(unsigned long)_buf];
    [d release];
    *_keyLen = pos;
  }
  return obj;
}

static EOQualifier *_testOperator(id _ctx, const char *_buf,
                                  unsigned _bufLen, unsigned *_opLen,
                                  BOOL *isAnd)
{
  EOQualifier *qual       = nil;
  char        c0, c1, c2  = 0;
  unsigned    pos, len    = 0;

  pos = _countWhiteSpaces(_buf, _bufLen);  
  
  if (_bufLen < 4) {/* at least OR or AND and something more */   
    if (qDebug)
      NSLog(@"_testOperator return nil for <%s> ", _buf);
    return nil;
  }
  c0 = _buf[pos + 0];
  c1 = _buf[pos + 1];
  c2 = _buf[pos + 2];
  
  if (((c0 == 'a') || (c0  == 'A')) &&
        ((c1 == 'n') || (c1  == 'N')) &&
        ((c2 == 'd') || (c2  == 'D'))) {
      pos    += 3;
      *isAnd  = YES;
  }
  else if (((c0 == 'o') || (c0  == 'O')) && ((c1 == 'r') || (c1  == 'R'))) {
      pos    += 2;
      *isAnd  = NO;
  }
  else{
      if (qDebug)
          NSLog(@"_testOperator got no AND nor OR for <%s> ", _buf);
      return nil; // No need to go further
  }

  pos += _countWhiteSpaces(_buf + pos, _bufLen - pos);
  qual = _parseSingleQualifier(_ctx, _buf + pos, _bufLen - pos, &len);
  if (!qual){
      [NSException raise:NSInvalidArgumentException 
                  format:@"Expected qualifier after %@", *isAnd ? @"AND" : @"OR"]; // TESTME!
  }
  *_opLen = pos + len;
  if (qDebug)
    NSLog(@"_testOperator return %@ for <%s> ", qual, _buf);
  
  return qual;
}

static EOQualifier *_parseCompoundQualifier(id _ctx, const char *_buf,
                                            unsigned _bufLen, unsigned *_qualLen)
{
  EOQualifier    *q0, *q1 = nil;
  NSMutableArray *array   = nil;
  unsigned       pos, len = 0;
  EOQualifier    *result;
  BOOL           isAnd;

  isAnd = YES;

  if ((q0 = _parseSingleQualifier(_ctx, _buf, _bufLen, &len)) == nil) {
    if (qDebug)
      NSLog(@"1_parseCompoundQualifier return nil for <%s> ", _buf);
    
    return nil;
  }
  pos = len;

  if (!(q1 = _testOperator(_ctx, _buf + pos, _bufLen - pos, &len, &isAnd))) {
    if (qDebug)
      NSLog(@"2_parseCompoundQualifier return nil for <%s> ", _buf);
    return nil;
  }
  pos  += len;
  array = [NSMutableArray arrayWithObjects:q0, q1, nil];
  
  while (YES) {
    BOOL newIsAnd;

    newIsAnd = YES;
    q0       = _testOperator(_ctx,  _buf + pos, _bufLen - pos, &len, &newIsAnd);

    if (!q0)
      break;
    
    if (newIsAnd != isAnd) {
      NSArray *a;

      a = [[array copy] autorelease];
      
      q1 = (isAnd)
        ? [_ctx andQualifierWithArray:a]
        : [_ctx orQualifierWithArray:a];

      [array removeAllObjects];
      [array addObject:q1];
      isAnd = newIsAnd;
    }
    [array addObject:q0];

    pos += len;
  }

  *_qualLen = pos;
  result = (isAnd)
    ? [_ctx andQualifierWithArray:array]
    : [_ctx orQualifierWithArray:array];
  
  if (qDebug)
    NSLog(@"3_parseCompoundQualifier return <%@> for <%s> ", result, _buf);

  return result;
}

static inline unsigned _countWhiteSpaces(const char *_buf, unsigned _bufLen) {
  unsigned cnt = 0;
  
  if (_bufLen == 0) {
    if (qDebug)
      NSLog(@"_parseString _bufLen == 0 --> return nil");
    return 0;
  }
  
  while (_buf[cnt] == ' ' || _buf[cnt] == '\t' || 
	 _buf[cnt] == '\n' || _buf[cnt] == '\r') {
    cnt++;
    if (cnt == _bufLen)
      break;
  }
  return cnt;
}
