//
//  PBXAppleScriptMenuPlugin.m
//  CMMJavaMenu
//
//  Created by Anjo Krank on Mon Mar 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

#import "PBXAppleScriptMenuPlugin.h"
#import "AppleScriptItem.h"
#import "PBXTextViewPoser.h"

static NSMutableArray *availableItems;

/* this is grabbed directly from
   http://developer.apple.com/qa/qa2001/qa1070.html
   I'm not sure if it is really needed, but who knows...
*/

void EnableScriptingAdditions() {
    OSErr err;
    AppleEvent e, r;
    ProcessSerialNumber selfPSN = { 0, kCurrentProcess };
    void (*f)();
    CFBundleRef b;
    b = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.openscripting"));
    if (b != NULL) {
        f = (void (*)()) CFBundleGetFunctionPointerForName(b, CFSTR("OSAInstallStandardHandlers"));
        if (f != NULL) (*f)();
    }
    err = AEBuildAppleEvent(kASAppleScriptSuite, kGetAEUT,
			    typeProcessSerialNumber, &selfPSN, sizeof(selfPSN),
			    kAutoGenerateReturnID, kAnyTransactionID, &e, NULL, "");
    if (err == noErr) {
        AESend(&e, &r, kAEWaitReply, kAENormalPriority, kAEDefaultTimeout, NULL, NULL);
        AEDisposeDesc(&e);
        AEDisposeDesc(&r);
    }
}

@implementation PBXAppleScriptMenuPlugin
+ (NSArray *)availableItems {
    return availableItems;
}

+ (void)scanDirectoryAtPath:(NSString *)path {
    NSString *file;
    NSDirectoryEnumerator *enumerator = [[NSFileManager defaultManager] enumeratorAtPath:path];
    while (file = [enumerator nextObject]) {
	AppleScriptItem *item = [AppleScriptItem scriptItemWithPath:[NSString stringWithFormat:@"%@/%@", path, file ] action:PATCH_SELECTOR];
	if (item != nil)
            [availableItems addObject:item];
    }
}

+ (void)pluginDidLoad:fp12 {
    EnableScriptingAdditions();

    // we don't need to release as we only get unloaded when we quit
    
    availableItems = [[NSMutableArray alloc] init];

    [self scanDirectoryAtPath:@"/Library/Contextual Menu Scripts"];
    [self scanDirectoryAtPath:[@"~/Library/Contextual Menu Scripts" stringByExpandingTildeInPath]];

    [PBXTextViewPoser poseAsClass:[PBXTextView class]];
}
@end
