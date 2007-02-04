//
//  RMAppDelegate.m
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "RMAppDelegate.h"

#import "RMPreferencesWindow.h"
#import "RMModelGroup.h"

@implementation RMAppDelegate

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
            
            if (aDict) {
                NSDate  *aDate = [aDict objectForKey:NSFileModificationDate];
                
                if ([[aDocument fileModificationDate] compare:aDate] < 0) {
                    NSBeginAlertSheet(NSLocalizedString(@"Rule file changed on disk", @"Alert title"), 
                                      NSLocalizedString(@"Reload", @"Button title"), 
                                      NSLocalizedString(@"Keep Local Version", @"Button title"), 
                                      nil, 
                                      [[[aDocument windowControllers] lastObject] window], 
                                      self, 
                                      NULL, 
                                      @selector(sheetDidDismiss:returnCode:contextInfo:), 
                                      aDocument, 
                                      NSLocalizedString(@"File has been externally modified. You can reload rules from file or keep local version of rules, but you will need to save them, if you don't want to lose them.", @"Alert message"));
                }
            } else {
                [aDocument updateChangeCount:NSChangeReadOtherContents];
                NSBeginAlertSheet(NSLocalizedString(@"Rule file deleted on disk", @"Alert title"), 
                                  nil, 
                                  nil, 
                                  nil, 
                                  [[[aDocument windowControllers] lastObject] window], 
                                  nil, 
                                  NULL, 
                                  NULL, 
                                  NULL, 
                                  NSLocalizedString(@"File has been deleted from disk. You will need to save it again if you don't want to lose it.", @"Alert message"));
            }
        }
    }
}

- (IBAction)createNewModelGroup:(id)sender {
    NSError     *outError;
    NSDocument  *newDocument = [[NSDocumentController sharedDocumentController] makeUntitledDocumentOfType:RMModelGroupType error:&outError];
    
    if (newDocument) {
        [[NSDocumentController sharedDocumentController] addDocument:newDocument];
        [newDocument makeWindowControllers];
        [newDocument showWindows];
    } else {
        [[NSAlert alertWithError:outError] runModal];
    }
}

@end

