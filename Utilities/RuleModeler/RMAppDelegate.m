//
//  RMAppDelegate.m
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "RMAppDelegate.h"

#import "RMPreferencesWindow.h"

@implementation RMAppDelegate

- (IBAction)showPreferences:(id)sender {
    if (preferencesWindow == nil) {
		preferencesWindow = [[RMPreferencesWindow alloc] init];
    }
    
    [[preferencesWindow window] center];
    [preferencesWindow showWindow:self];
}

@end
