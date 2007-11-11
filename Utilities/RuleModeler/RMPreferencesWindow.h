/*
 RMPreferencesWindow.h
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

#import <AppKit/AppKit.h>


@interface RMPreferencesWindow : NSWindowController {
    
    IBOutlet NSUserDefaultsController   *userDefaultsController;
    
    IBOutlet NSView                     *generalPane;
    IBOutlet NSView                     *colorPane;
    IBOutlet NSView                     *documentPane;
    
    NSMutableDictionary                 *toolbarItems;
    NSMutableDictionary			*preferencePanes;
    
    NSMutableArray			*toolbarIdentifiers;
    
    NSString				*currentIdentifier;
    NSView				*currentPref;
    NSMutableArray      *d2wclientConfigurationPaths;
    IBOutlet NSTableView    *d2wclientConfigurationPathTableView;
    IBOutlet NSButtonCell   *removePathButtonCell;
}

- (void)addPreferencePane:(NSView *)paneView identifier:(NSString *)identifier icon:(NSImage *)icon label:(NSString *)label toolTip:(NSString *)toolTip target:(id)target action:(SEL)action menu:(NSMenu *)menu;
- (void)prepareToolbar;

- (void)setPrefPane:(NSString *)identifier;

- (IBAction)addPath:(id)sender;
- (IBAction)removePath:(id)sender;

@end
