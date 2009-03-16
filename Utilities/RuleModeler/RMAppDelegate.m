/*
 RMAppDelegate.h
 RuleModeler

 Created by King Chung Huang on 1/30/04.


 Copyright (c) 2004 King Chung Huang

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 of the Software, and to permit persons to whom the Software is furnished to do
 so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
*/

#import "RMAppDelegate.h"

#import "RMPreferencesWindow.h"
#import "RMModelGroup.h"
#import "Assignment.h"
#import "RMModel.h"
#import "Rule.h"
#import "EOControl.h"
#import <Carbon/Carbon.h> // Necessary only for GetCurrentEventKeyModifiers()

@implementation RMAppDelegate

- (id)init {
    self = [super init];
    if (self != nil) {
        [[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithContentsOfFile:[[NSBundle mainBundle] pathForResource:@"Defaults" ofType:@"plist"]]];
        [[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObject:[NSArchiver archivedDataWithRootObject:[NSColor colorWithCalibratedRed:212/255. green:212/255. blue:255/255. alpha:1.]] forKey:@"searchFilterHighlightColor"]];
        [[NSUserDefaults standardUserDefaults] registerDefaults:[NSDictionary dictionaryWithObject:[NSNumber numberWithBool:YES] forKey:@"highlightSearchFilterOccurences"]];
        
        [[NSUserDefaultsController sharedUserDefaultsController] addObserver:self forKeyPath:@"values.d2wclientConfigurationPaths" options:0 context:NULL];
        [[NSUserDefaultsController sharedUserDefaultsController] addObserver:self forKeyPath:@"values.useParenthesesForComparisonQualifier" options:0 context:NULL];
        [[NSUserDefaultsController sharedUserDefaultsController] addObserver:self forKeyPath:@"values.defaultRulePriority" options:0 context:NULL];
        
        [Assignment setD2wclientConfigurationPaths:[[NSUserDefaults standardUserDefaults] arrayForKey:@"d2wclientConfigurationPaths"]];
        [EOQualifier setUseParenthesesForComparisonQualifier:[[NSUserDefaults standardUserDefaults]boolForKey:@"useParenthesesForComparisonQualifier"]];
        [Rule setDefaultRulePriority:[[NSUserDefaults standardUserDefaults] integerForKey:@"defaultRulePriority"]];
    }
    return self;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if ([keyPath isEqualToString:@"values.d2wclientConfigurationPaths"]) {
        [Assignment setD2wclientConfigurationPaths:[[NSUserDefaults standardUserDefaults] arrayForKey:@"d2wclientConfigurationPaths"]];
        [Assignment refreshToolTipDictionary];
    }
    else if ([keyPath isEqualToString:@"values.useParenthesesForComparisonQualifier"]) {
        [EOQualifier setUseParenthesesForComparisonQualifier:[[NSUserDefaults standardUserDefaults] boolForKey:@"useParenthesesForComparisonQualifier"]];
    }
    else if ([keyPath isEqualToString:@"values.defaultRulePriority"])
        [Rule setDefaultRulePriority:[[NSUserDefaults standardUserDefaults] integerForKey:@"defaultRulePriority"]];
}

- (IBAction)showPreferences:(id)sender {
    if (preferencesWindow == nil) {
		preferencesWindow = [[RMPreferencesWindow alloc] init];
    }
    
//    [[preferencesWindow window] center];
    [preferencesWindow showWindow: self];
}

- (void)dealloc {
    [[NSUserDefaultsController sharedUserDefaultsController] removeObserver:self forKeyPath:@"values.d2wclientConfigurationPaths"];
    [[NSUserDefaultsController sharedUserDefaultsController] removeObserver:self forKeyPath:@"values.useParenthesesForComparisonQualifier"];
    [[NSUserDefaultsController sharedUserDefaultsController] removeObserver:self forKeyPath:@"values.defaultRulePriority"];
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

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    // -[NSApplication currentEvent] returns nil; the only way to get the Shift info is to use a Carbon call
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"restoreOpenDocuments"] && (GetCurrentEventKeyModifiers() & shiftKey) != shiftKey) {
        NSEnumerator    *anEnum = [[[NSUserDefaults standardUserDefaults] arrayForKey:@"restoredDocumentURLs"] objectEnumerator];
        NSString        *eachURLString;
        
        while (eachURLString = [anEnum nextObject]) {
            NSURL   *anURL = [NSURL URLWithString:eachURLString];
            
            if (anURL != nil) {
                [[NSDocumentController sharedDocumentController] openDocumentWithContentsOfURL:anURL display:YES error:NULL];
            }
        }
    }
}

- (void)applicationWillTerminate:(NSNotification *)aNotification
{
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"restoreOpenDocuments"]) {
        NSEnumerator    *anEnum = [[[NSDocumentController sharedDocumentController] documents] objectEnumerator];
        NSDocument      *eachDocument;
        NSMutableArray  *paths = [NSMutableArray array];
        
        while (eachDocument = [anEnum nextObject]) {
            if ([[eachDocument windowControllers] count] > 0 && [eachDocument fileURL] != nil) {
                [paths addObject:[[[eachDocument fileURL] standardizedURL] description]];
            }
        }
        [[NSUserDefaults standardUserDefaults] setObject:paths forKey:@"restoredDocumentURLs"];
    }
    else {
        [[NSUserDefaults standardUserDefaults] removeObjectForKey:@"restoredDocumentURLs"];
    }
}

@end

