/*
 EOCustomClassWrapper.m
 RuleModeler
 
 Created by davelopper on 1/21/07.
 
 
 Copyright (c) 2004-2007, Project WONDER <http://wonder.sourceforge.net/>
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  * Neither the name of the Project WONDER nor the names of its contributors may
    be used to endorse or promote products derived from this software without
    specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#import "EOCustomClassWrapper.h"
#include "EOKeyValueArchiver.h"


@interface NSString(EOCustomClassWrapper)
- (NSString *)qualifierDescription;
@end

@implementation EOCustomClassWrapper

- (id) initWithCustomClassName:(NSString *)_customClassName value:(NSString *)_value {
    NSParameterAssert(_customClassName != nil && _value != nil);
    
    if(self = [super init]) {
        customClassName = [_customClassName copy];
        value = [_value copy];
    }
    
    return self;
}

- (void) dealloc {
    [customClassName release];
    [value release];
    
    [super dealloc];
}

- (id)initWithKeyValueUnarchiver:(EOKeyValueUnarchiver *)_unarchiver {
    customClassName = [[_unarchiver decodeObjectForKey:@"class"] retain];
    value = [[_unarchiver decodeObjectForKey:@"value"] retain];
    return self;
}

- (void)encodeWithKeyValueArchiver:(EOKeyValueArchiver *)_archiver {
    [_archiver encodeObject:customClassName forKey:@"class"];
    [_archiver encodeObject:value forKey:@"value"];
}

- (NSString *)qualifierDescription {
    // Display using cast notation
    return [NSString stringWithFormat:@"(%@)%@", customClassName, [value qualifierDescription]];
}

@end
