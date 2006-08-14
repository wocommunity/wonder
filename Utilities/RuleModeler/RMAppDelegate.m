//
//  RMAppDelegate.m
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "RMAppDelegate.h"

#import "RMPreferencesWindow.h"
#import "EOKeyValueArchiverFix.h"

@implementation RMAppDelegate

+ (void)initialize {
    [EOKeyValueArchiverFix poseAsClass:[EOKeyValueArchiver class]];
}

- (id)init {
    self = [super init];
    if (self != nil) {
        [[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"Defaults" ofType:@"plist"]]];
    }
    return self;
}

- (IBAction)showPreferences:(id)sender {
    if (preferencesWindow == nil) {
		preferencesWindow = [[RMPreferencesWindow alloc] init];
    }
    
    [[preferencesWindow window] center];
    [preferencesWindow showWindow: self];
}

- (void)dealloc {
    [preferencesWindow release];

    [super dealloc];
}

- (BOOL)applicationShouldOpenUntitledFile:(NSApplication *)sender {
    return [[NSUserDefaults standardUserDefaults] boolForKey:@"openNewDocumentAtStartup"];
}

- (NSError *)application:(NSApplication *)application willPresentError:(NSError *)error {
    if (![[error userInfo] objectForKey:NSRecoveryAttempterErrorKey]) {
        NSError *underlyingError = [[error userInfo] objectForKey:NSUnderlyingErrorKey];
        
        if (underlyingError) {
            NSMutableDictionary *aDict = [[error userInfo] mutableCopy];
            
            [aDict setObject:[NSString stringWithFormat:@"%@\n\n%@", [aDict objectForKey:NSLocalizedDescriptionKey], [[underlyingError userInfo] objectForKey:NSLocalizedDescriptionKey]] forKey:NSLocalizedDescriptionKey];
            error = [NSError errorWithDomain:[error domain] code:[error code] userInfo:aDict];
            [aDict release];
        }
    }
    
    return error;
}

- (void)sheetDidDismiss:(NSWindow *)sheet returnCode:(int)returnCode contextInfo:(void  *)contextInfo {
    NSDocument  *aDoc = (NSDocument *)contextInfo;
    
    [aDoc updateChangeCount:NSChangeReadOtherContents];    
    if (returnCode == NSAlertDefaultReturn) {
        // Reload
        [aDoc performSelector:@selector(revertDocumentToSaved:) withObject:nil afterDelay:0];
    }
}

- (void)applicationDidBecomeActive:(NSNotification *)aNotification {
    NSEnumerator    *documentEnum = [[[NSDocumentController sharedDocumentController] documents] objectEnumerator];
    NSDocument      *aDocument;
    
    while (aDocument = [documentEnum nextObject]) {
        NSURL   *aURL = [aDocument fileURL];
        
        if (aURL) {
            NSDictionary    *aDict = [[NSFileManager defaultManager] fileAttributesAtPath:[aURL path] traverseLink:YES];
            NSDate          *aDate = [aDict objectForKey:NSFileModificationDate];
            
            if ([[aDocument fileModificationDate] compare:aDate] < 0) {
                NSBeginAlertSheet(@"Rule file changed on disk", @"Reload", @"Keep Local Version", nil, [[[aDocument windowControllers] lastObject] window], self, NULL, @selector(sheetDidDismiss:returnCode:contextInfo:), aDocument, @"File has been externally modified. You can reload rules from file or keep local version of rules, but you will need to save them, if you don't want to lose them.");
            }
        }
    }
}

@end

