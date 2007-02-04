/*
 EOKeyValueArchiverTest.m
 RuleModeler

 Created by davelopper on 1/13/07.


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

#import "EOKeyValueArchiverTest.h"
#import "Rule.h"
#import "Assignment.h"
#import "NSPropertyListSerializationAdditions.h"


// We implement the following -isEqual: methods only for the tests
// Do not implement them for normal use, because these objects are mutable,
// and we would need to reimplement -hash

@implementation Rule(Testing)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[self class]]) {
        return [self isEqualToRule:anObject];
    } else {
        return NO;
    }
}

@end

@implementation Assignment(Testing)

- (BOOL)isEqual:(id)anObject {
    if ([anObject isKindOfClass:[self class]]) {
        return [self isEqualToAssignment:anObject];
    } else {
        return NO;
    }
}

@end

@implementation EOKeyValueArchiverTest

- (NSArray *) rulesFromFile:(NSString *)path
{
    NSData          *data = [NSData dataWithContentsOfFile:path];
    NSString        *error = nil;
    NSDictionary    *plist = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListMutableContainersAndLeaves format:NULL errorDescription:&error];
    
    STAssertNil(error, @"Invalid property list '%@'", path);
    
    return [Rule rulesFromMutablePropertyList:plist];
}

- (void) testArchiveUnarchive:(NSString *)path
{
    NSArray         *originalRules = [self rulesFromFile:path];
    NSEnumerator    *ruleEnum = [originalRules objectEnumerator];
    Rule            *eachRule;
    
    while(eachRule = [ruleEnum nextObject]){
        EOKeyValueArchiver      *archiver = [[EOKeyValueArchiver alloc] init];    
        
        [archiver encodeObject:[NSArray arrayWithObject:eachRule] forKey:@"rules"];
        
        NSDictionary            *plist = [archiver dictionary];
        Rule                    *unarchivedRule = [[Rule rulesFromMutablePropertyList:plist] lastObject];
        
        [archiver release];
        STAssertTrue([eachRule isEqualToRule:unarchivedRule], @"Following rules are not equal (original/restored):\n%@\n%@", eachRule, unarchivedRule);
    }
}

- (void) testArchiveUnarchive
{
    [self testArchiveUnarchive:@"/System/Library/Frameworks/JavaDirectToWeb.framework/Resources/d2w.d2wmodel"];
    [self testArchiveUnarchive:@"/Developer/Examples/JavaWebObjects/JCDiscussionBoard/d2w.d2wmodel"];
}

- (void) testUnarchiveArchive:(NSString *)path
{
    NSData          *data = [NSData dataWithContentsOfFile:path];
    NSString        *error = nil;
    NSDictionary    *originalPlist = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListMutableContainersAndLeaves format:NULL errorDescription:&error];

    STAssertNil(error, @"Invalid property list '%@'", path);

    NSArray             *originalRules = [self rulesFromFile:path];
    EOKeyValueArchiver  *archiver = [[EOKeyValueArchiver alloc] init];
    
    [archiver encodeObject:originalRules forKey:@"rules"];
    
    NSDictionary    *archivedPlist = [archiver dictionary];
    NSSet           *originalRuleSet = [NSSet setWithArray:[originalPlist objectForKey:@"rules"]];
    NSSet           *archivedRuleSet = [NSSet setWithArray:[archivedPlist objectForKey:@"rules"]];
    BOOL            equals = [originalRuleSet isEqual:archivedRuleSet];
    
    STAssertTrue(equals, @"Saved content is not equal to loaded content '%@'", path);
    
    [archiver release];
}

- (void) testUnarchiveArchive
{
    [self testUnarchiveArchive:@"/System/Library/Frameworks/JavaDirectToWeb.framework/Resources/d2w.d2wmodel"];
    [self testUnarchiveArchive:@"/Developer/Examples/JavaWebObjects/JCDiscussionBoard/d2w.d2wmodel"];
    // TODO Create custom D2W model allowing more tests
}

@end
