/*
 RMPreferencesWindow.m
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

#import "RMPreferencesWindow.h"

#import "DMToolbarUtils.h"

@implementation RMPreferencesWindow

- (id)init {
    if (self = [self initWithWindowNibName:@"RMPreferencesWindow"]) {
        d2wclientConfigurationPaths = [[[NSUserDefaults standardUserDefaults] arrayForKey:@"d2wclientConfigurationPaths"] mutableCopy];
    }
    
    return self;
}

- (void)awakeFromNib {
    [[self window] useOptimizedDrawing:YES];
    
    preferencePanes = [[NSMutableDictionary dictionary] retain];
    toolbarItems = [[NSMutableDictionary dictionary] retain];
    toolbarIdentifiers = [[NSMutableArray array] retain];
    
 
    [self addPreferencePane:generalPane identifier:@"GeneralPane" icon:[NSImage imageNamed:@"NSApplicationIcon"] label:NSLocalizedString(@"General", @"Toolbar item label") toolTip:NSLocalizedString(@"General Preferences", @"Toolbar item tooltip") target:self action:@selector(toolbarAction:) menu:NULL];
    [self addPreferencePane:documentPane identifier:@"DocumentPane" icon:[NSImage imageNamed:@"DocumentPane"] label:NSLocalizedString(@"Document", @"Toolbar item label") toolTip:NSLocalizedString(@"Document Preferences", @"Toolbar item tooltip") target:self action:@selector(toolbarAction:) menu:NULL];
    [self addPreferencePane:colorPane identifier:@"ColorPane" icon:[NSImage imageNamed:@"ColorPane"] label:NSLocalizedString(@"Colors", @"Toolbar item label") toolTip:NSLocalizedString(@"Color Preferences", @"Toolbar item tooltip") target:self action:@selector(toolbarAction:) menu:NULL];
    
    [self prepareToolbar];
    
    [self setPrefPane:@"GeneralPane"];
}

- (void)prepareToolbar {
    NSToolbar *toolbar = [[[NSToolbar alloc] initWithIdentifier:@"DMWindowToolbar"] autorelease];
    
    [toolbar setDelegate:self];
    [toolbar setAllowsUserCustomization:NO];
    [toolbar setAutosavesConfiguration:NO];
    [toolbar setDisplayMode:NSToolbarDisplayModeDefault];
    [toolbar setSizeMode:NSToolbarSizeModeDefault];
    
    [[self window] setToolbar:toolbar];
}

- (void)addPreferencePane:(NSView *)paneView identifier:(NSString *)identifier icon:(NSImage *)icon label:(NSString *)label toolTip:(NSString *)toolTip target:(id)target action:(SEL)action menu:(NSMenu *)menu {
    [preferencePanes setObject:paneView forKey:identifier];
    
    addToolbarItem(toolbarItems, identifier, label, label, toolTip, target, @selector(setImage:), icon, action, menu);
    
    [toolbarIdentifiers addObject:identifier];
}

#pragma mark NSToolbar Delegate Methods

- (NSToolbarItem *)toolbar:(NSToolbar *)toolbar itemForItemIdentifier:(NSString *)itemIdentifier willBeInsertedIntoToolbar:(BOOL)flag {
    NSToolbarItem *newItem = [[[NSToolbarItem alloc] initWithItemIdentifier:itemIdentifier] autorelease];
    NSToolbarItem *item = [toolbarItems objectForKey:itemIdentifier];
    
    [newItem setLabel:[item label]];
    [newItem setPaletteLabel:[item paletteLabel]];
    
    if ([item view] != NULL) {
        [newItem setView:[item view]];
        
        [newItem setMinSize:[[item view] bounds].size];
        [newItem setMaxSize:[[item view] bounds].size];
    } else {
        [newItem setImage:[item image]];
    }
    
    [newItem setToolTip:[item toolTip]];
    [newItem setTarget:[item target]];
    [newItem setAction:[item action]];
    [newItem setMenuFormRepresentation:[item menuFormRepresentation]];
    
    return newItem;
}

- (NSArray *)toolbarDefaultItemIdentifiers:(NSToolbar *)toolbar {
    return toolbarIdentifiers;
}

- (NSArray *)toolbarAllowedItemIdentifiers:(NSToolbar *)toolbar {
    return toolbarIdentifiers;
}

- (NSArray *)toolbarSelectableItemIdentifiers:(NSToolbar *)toolbar {
    return toolbarIdentifiers;
}

#pragma mark Actions

- (void)toolbarAction:(id)sender {
    NSString *identifier = [sender itemIdentifier];
    
    if (![identifier isEqual:currentIdentifier]) {
	[self setPrefPane:identifier];
    }
}

- (void)setPrefPane:(NSString *)identifier {
    NSWindow *window = [self window];
    
    NSView *previousPane = [window contentView];
    NSView *prefPane = [preferencePanes objectForKey:identifier];
    
    NSRect previousRect = [previousPane frame];
    NSRect prefRect = [prefPane frame];
    NSRect windowRect = [window frame];
    
    float heightDiff = NSHeight(previousRect) - NSHeight(prefRect);
    
    NSRect newFrame = [window frameRectForContentRect:prefRect];
    newFrame.origin.y = windowRect.origin.y + heightDiff;
    newFrame.origin.x = windowRect.origin.x;
    
    [currentPref removeFromSuperview];
    
    [window setFrame:newFrame display:YES animate:YES];
    [[window contentView] addSubview:prefPane];
    [[window contentView] setAutoresizesSubviews:YES]; // For some reason, it is not the case in the nib
    
    currentPref = prefPane;
    
    [currentIdentifier release];
    currentIdentifier = [[NSString stringWithString:identifier] retain];
    [[[self window] toolbar] setSelectedItemIdentifier:identifier];
}

- (IBAction)addPath:(id)sender {
    int aRow = [d2wclientConfigurationPathTableView selectedRow];
    
    if(aRow == -1)
        aRow = [d2wclientConfigurationPaths count];
    [d2wclientConfigurationPaths insertObject:[NSString string] atIndex:aRow];
    [d2wclientConfigurationPathTableView reloadData];
    [d2wclientConfigurationPathTableView selectRow:aRow byExtendingSelection:NO];
    [d2wclientConfigurationPathTableView editColumn:0 row:aRow withEvent:nil select:YES];
    [[NSUserDefaults standardUserDefaults] setObject:d2wclientConfigurationPaths forKey:@"d2wclientConfigurationPaths"];
}

- (IBAction)removePath:(id)sender {
    int aRow = [d2wclientConfigurationPathTableView selectedRow];
    
    [d2wclientConfigurationPaths removeObjectAtIndex:aRow];
    [d2wclientConfigurationPathTableView reloadData];
    [[NSUserDefaults standardUserDefaults] setObject:d2wclientConfigurationPaths forKey:@"d2wclientConfigurationPaths"];
}

#pragma mark NSTableView DataSource and Delegate Methods

- (int)numberOfRowsInTableView:(NSTableView *)aTableView {
    return [d2wclientConfigurationPaths count];
}

- (id)tableView:(NSTableView *)aTableView objectValueForTableColumn:(NSTableColumn *)aTableColumn row:(int)rowIndex {
    return [d2wclientConfigurationPaths objectAtIndex:rowIndex];
}

- (void)tableView:(NSTableView *)aTableView setObjectValue:(id)anObject forTableColumn:(NSTableColumn *)aTableColumn row:(int)rowIndex
{
    [d2wclientConfigurationPaths replaceObjectAtIndex:rowIndex withObject:anObject];
    [[NSUserDefaults standardUserDefaults] setObject:d2wclientConfigurationPaths forKey:@"d2wclientConfigurationPaths"];
}

- (void)tableViewSelectionDidChange:(NSNotification *)aNotification {
    [removePathButtonCell setEnabled:([d2wclientConfigurationPathTableView numberOfSelectedRows] > 0)];
}

@end
