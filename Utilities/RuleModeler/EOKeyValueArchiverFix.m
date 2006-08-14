//
//  EOKeyValueArchiverFix.m
//  RuleModeler
//

#import "EOKeyValueArchiverFix.h"


@implementation EOKeyValueArchiverFix

- (void)encodeObject:fp8 forKey:fp12 {
    // There is a bug in the EOKeyValueArchiver: it cannot encode empty dictionaries!
    if([fp8 isEqual:[NSDictionary dictionary]]){
        [_propertyList setObject:fp8 forKey:fp12];
    }
    else
        [super encodeObject:fp8 forKey:fp12];
}

@end
