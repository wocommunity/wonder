//
//  RMAppDelegate.h
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RMPreferencesWindow;

@interface RMAppDelegate : NSObject {

    @private
    RMPreferencesWindow     *preferencesWindow;
    
}

- (IBAction)showPreferences:(id)sender;
- (IBAction)createNewModelGroup:(id)sender;

@end
