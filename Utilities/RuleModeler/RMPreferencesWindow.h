//
//  RMPreferencesWindow.h
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <AppKit/AppKit.h>


@interface RMPreferencesWindow : NSWindowController {
    
    IBOutlet NSUserDefaultsController   *userDefaultsController;
    
    IBOutlet NSView                     *generalPane;
    IBOutlet NSView			*importingPane;
    IBOutlet NSView                     *sharingPane;
    IBOutlet NSView			*coresPane;
    
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
