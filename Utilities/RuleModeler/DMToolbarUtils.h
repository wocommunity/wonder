/*
 *  DMToolbarUtils.h
 *  Deimos
 *
 *  Created by King Chung Huang on Mon Nov 10 2003.
 *  Copyright (c) 2003 King Chung Huang. All rights reserved.
 *
 */

#include <AppKit/AppKit.h>

extern NSToolbarItem *addToolbarItem(NSMutableDictionary *theDict, NSString *identifier, NSString *label, NSString *paletteLabel, NSString *toolTip, id target, SEL settingSelector, id itemContent, SEL action, NSMenu *menu);
