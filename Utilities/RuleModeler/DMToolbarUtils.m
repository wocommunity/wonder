/*
 *  DMToolbarUtils.c
 *  Deimos
 *
 *  Created by King Chung Huang on Mon Nov 10 2003.
 *  Copyright (c) 2003 King Chung Huang. All rights reserved.
 *
 */

#include "DMToolbarUtils.h"

// addToolbarItem from "ToolbarSample - Controller.m" in ADC Sample Code - Cocoa
NSToolbarItem *addToolbarItem(NSMutableDictionary *theDict, NSString *identifier, NSString *label, NSString *paletteLabel, NSString *toolTip, id target, SEL settingSelector, id itemContent, SEL action, NSMenu *menu) {
    NSMenuItem *mItem;
    
    // here we create the NSToolbarItem and setup its attributes in line with the parameters
    NSToolbarItem *item = [[[NSToolbarItem alloc] initWithItemIdentifier:identifier] autorelease];
    [item setLabel:label];
    [item setPaletteLabel:paletteLabel];
    [item setToolTip:toolTip];
    [item setTarget:target];
    
    // The settingSelector parameter can either be @selector(setView:) or @selector(setImage:). Pass in the right
    // one depending upon whether your NSToolbarItem will have a custom view or an image, respectively
    // (in the itemContent parameter). Then this next line will do the right thing automatically.
    [item performSelector:settingSelector withObject:itemContent];
    
    if (settingSelector == @selector(setView:)) {
        [item setMinSize:[itemContent bounds].size];
        [item setMaxSize:[itemContent bounds].size];
    }
    
    [item setAction:action];
    
    // If this NSToolbarItem is supposed to have a menu "form representation" associated with it (for text-only
    // mode), we set it up here. Actually, you have to hand an NSMenuItem (not a complete NSMenu to the toolbar
    // item, so we create a dummy NSMenuItem that has our real menu as a submenu.
    if (menu != NULL) {
        // construct an NSMenuItem
        mItem = [[[NSMenuItem alloc] init] autorelease];
        [mItem setSubmenu:menu];
        [mItem setTitle:[menu title]];
        
        [item setMenuFormRepresentation:mItem];
    }
    
    // Now that we've setup all the settings for this new toolbar item, we add it to the dictionary.
    // The dictionary retains the toolbar item for us, which is why we could autorelease it when we created
    // it (above).
    [theDict setObject:item forKey:identifier];
    
    return item;
}
