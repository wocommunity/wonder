//
//  PBXTextViewPoser.m
//  CMMJavaMenu
//
//  Created by Anjo Krank on Mon Mar 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

#import "PBXTextViewPoser.h"
#import "AppleScriptItem.h"
#import "PBXAppleScriptMenuPlugin.h"

@implementation PBXTextViewPoser

+ (NSMenu *)defaultMenu {
    NSMenu *contextMenu = [super defaultMenu];
    return contextMenu;
}

- (NSMenu *)menuForEvent:(NSEvent *)event {
    NSMenu *contextMenu = [super menuForEvent:event];
    NSRange selectedRange = [self selectedRange];
    id object;
    NSEnumerator *enumerator = [[PBXAppleScriptMenuPlugin availableItems] objectEnumerator];

    // First, remove all previous menus
    while (object = [enumerator nextObject]) {
	if([object menu] != nil) {
	    [[object menu] removeItem:object];
	}
    }

    // then, if we have a selection, add every item that returns a valid menu label
    if(selectedRange.length > 0) {
	NSString *selection = [[self string] substringWithRange:selectedRange];
	NSEnumerator *enumerator = [[PBXAppleScriptMenuPlugin availableItems] objectEnumerator];
	AppleScriptItem *item;

	while (item = (AppleScriptItem *)[enumerator nextObject]) {
	    if([item shouldShowWithString:selection]) {
		[contextMenu insertItem:item atIndex:[[contextMenu itemArray] count]];
	    }
	}
    }
    return contextMenu;
}

- (id)AS_handleMenu:(id)sender {
    NSRange selectedRange = [self selectedRange];
    NSString *selection = [[self string] substringWithRange:selectedRange];
    NSString *result = [sender invokeWithString:selection];

    if(result != nil) {
	if(1) {
	    NSRange undoRange = selectedRange;

	    undoRange.length = [result length];
	    [[[self undoManager] prepareWithInvocationTarget:self] replaceCharactersInRange:undoRange withString:selection];
	    [self replaceCharactersInRange:selectedRange withString:result];
	} else {
	    /* I wonder why this doesn't work?? */
	    [[NSPasteboard generalPasteboard] setString:result forType:NSStringPboardType];
	}
    }
    return nil;
}
@end
