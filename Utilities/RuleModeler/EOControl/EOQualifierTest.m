//
//  EOQualifierTest.m
//  RuleModeler
//
//  Created by Dave Lopper on 1/21/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

#import "EOQualifierTest.h"
#import "EOQualifier.h"


@implementation EOQualifierTest

+ (void) initialize {
    [[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObject:@"YES" forKey:@"EOQualifierDebugEvaluation"]];
//    [EOQualifier registerValueClass:[NSDecimalNumber class] forTypeName:@"java.math.BigDecimal"];
}

- (void) setUp
{
    [super setUp];
    qualifierDictionary = [[NSDictionary dictionaryWithContentsOfFile:[[NSBundle bundleForClass:[self class]] pathForResource:@"EOQualifierTest" ofType:@"plist"]] retain];
}

- (void) tearDown
{
    [qualifierDictionary release];
    qualifierDictionary = nil;
    
    [super tearDown];
}

- (void) testValidFormatParsing
{
    NSEnumerator    *anEnum = [[qualifierDictionary objectForKey:@"validFormats"] objectEnumerator];
    NSString        *eachFormat;
    
    while(eachFormat = [anEnum nextObject]){
        EOQualifier *aQualifier;
        
        STAssertNoThrow(aQualifier = [EOQualifier qualifierWithQualifierFormat:eachFormat], @"Invalid qualifier format: '%@'", eachFormat);
        STAssertNotNil(aQualifier, @"Invalid qualifier format: '%@'. Invalid implementation, because should have raised instead of returning nil.", eachFormat);
    }
}

- (void) testInvalidFormatParsing
{
    NSEnumerator    *anEnum = [[qualifierDictionary objectForKey:@"invalidFormats"] objectEnumerator];
    NSString        *eachFormat;
    
    while(eachFormat = [anEnum nextObject]){
        EOQualifier *aQualifier;
        
        STAssertThrows(aQualifier = [EOQualifier qualifierWithQualifierFormat:eachFormat], @"Qualifier format should be invalid, but no exception thrown: '%@'", eachFormat);
    }
}

- (void) testFormat2Archive
{
    NSDictionary    *testDict = [qualifierDictionary objectForKey:@"format2Archive"];
    NSEnumerator    *anEnum = [testDict keyEnumerator];
    NSString        *eachFormat;
    
    while(eachFormat = [anEnum nextObject]){
        EOQualifier *aQualifier;
        
        STAssertNoThrow(aQualifier = [EOQualifier qualifierWithQualifierFormat:eachFormat], @"Invalid qualifier format: '%@'", eachFormat);
        STAssertNotNil(aQualifier, @"Invalid qualifier format: '%@'. Invalid implementation, because should have raised instead of returning nil.", eachFormat);
        
        EOKeyValueArchiver  *archiver = [[EOKeyValueArchiver alloc] init];
        NSDictionary        *archive;
        NSDictionary        *expectedArchive = [testDict objectForKey:eachFormat];
        
        [aQualifier encodeWithKeyValueArchiver:archiver];
        archive = [archiver dictionary];
        STAssertEqualObjects(archive, expectedArchive, @"Format not resulting in correct qualifier");
    }
}

@end
