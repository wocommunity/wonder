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

@interface PBXBookmarkMenuItemHack : NSMenuItem {
    id _bookmark;
    id _opener;
}
@end

@implementation PBXBookmarkMenuItemHack

- initWithBookmark:(id)bookmark opener:(id)opener {
    [super initWithTitle:[bookmark name] action:@selector(open:) keyEquivalent:@""];
    _bookmark = [bookmark retain];
    _opener = [opener retain];
    [self setTarget:self];
    return self;
}

- (NSString *)title {
    return [_bookmark name];
}

- (void) open:(id)sender {
    [_bookmark fileDocumentLoadIfNeeded:nil];
    [_opener openBookmark:_bookmark inSeparateWindow:NO allowExternalEditor:NO];
}

- (void)dealloc {
    [_bookmark release];
    [_opener release];
    [super dealloc];
}
@end
static int compareBookmarks(id o1, id o2, void *dummy) {
    return [[o1 name] compare:[o2 name]];
}
static int compareProjects(id o1, id o2, void *dummy) {
    return [[[o1 projectDocument] displayName] compare:[[o2 projectDocument] displayName]];
}
@implementation PBXTextViewPoser

+ (NSMenu *)defaultMenu {
    NSMenu *contextMenu = [super defaultMenu];
    return contextMenu;
}

- (NSMenu *)fileMenuWithNavigator:(id)navigator opener:(id) opener {
    id object;
    NSArray *arr = [navigator _historyBookmarks];
    NSEnumerator *enumerator;
    NSMenu *newMenu = [[NSMenu alloc] initWithTitle:[[navigator projectDocument] displayName]];

    arr = [arr sortedArrayUsingFunction:compareBookmarks context:nil];
    enumerator = [arr objectEnumerator];
    while (object = [enumerator nextObject]) {
        id item = [[[PBXBookmarkMenuItemHack alloc] initWithBookmark:object opener:opener] autorelease];
        [newMenu addItem:item];
    }
    return newMenu;
}

- (NSMenu *)projectFileMenu:(id)contextMenu {
    id navigator;
    id object;
    id workspaceModule = [NSClassFromString(@"PBXWorkspaceModule") topMostWorkspaceModule];
    id bookmarksModule = [workspaceModule bookmarksModule];
    id opener = [bookmarksModule valueForKey:@"fileOpener"];
    NSEnumerator *enumerator;
    NSArray *arr;
    enumerator = [[contextMenu itemArray] objectEnumerator];
    while (object = [enumerator nextObject]) {
        // we get some strange error if we call [-title] directly?
        NSString * title = [object valueForKey:@"title"];
        if(title == nil)
            continue;
        if([title hasSuffix:@"pbproj"]) {
            [contextMenu removeItem:object];
        }
        if([title isEqualToString:@"Speech"] || [title isEqualToString:@"Spelling"]) {
            [contextMenu removeItem:object];
        }
    }
    arr = [NSClassFromString(@"PBXFileNavigator") allNavigators];
    arr = [arr sortedArrayUsingFunction:compareProjects context:nil];
    
    enumerator = [arr objectEnumerator];
    while (navigator = [enumerator nextObject]) {
        NSMenu *submenu = [self fileMenuWithNavigator:navigator opener:opener];
        id item = [contextMenu addItemWithTitle:[submenu title] action:nil keyEquivalent:@""];
        [contextMenu setSubmenu:submenu forItem:item];
    }
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
    [self projectFileMenu:contextMenu];
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
