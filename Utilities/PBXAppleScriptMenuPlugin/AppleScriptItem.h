//
//  AppleScriptItem.h
//  CMMJavaMenu
//
//  Created by Anjo Krank on Mon Mar 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Carbon/Carbon.h>
#import <AppKit/AppKit.h>


@interface AppleScriptItem : NSMenuItem {
    NSString *scriptText;
    NSString *path;
    NSDate *lastDate;
    ComponentInstance theComponent;
    OSAID contextID;
    AppleEvent theEvent;
}
+ scriptItemWithPath:(NSString *)path action:(SEL)aSelector;
- initScriptItemWithPath:(NSString *)path action:(SEL)aSelector;
- (BOOL)shouldShowWithString:(NSString *)selection;
- (NSString *)invokeWithString:(NSString *)selection;
@end
