//  Copyright (c) 2004 King Chung Huang
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

//  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

//
//  RMModelEditor.m
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import "RMModelEditor.h"

#import "RMModel.h"
#import "Rule.h"
#import "Assignment.h"
#import "DMToolbarUtils.m"

@implementation RMModelEditor

- (id)init {
    if (self = [self initWithWindowNibName:@"RMModelEditor"]) {
	
    }
    
    return self;
}

- (void)setDocument:(NSDocument *)document {
    // We need to do that in order to avoid a KVO warning when document closes
    // Only that binding is problematic
    if(document == nil)
        [rulesController unbind:@"contentArray"];
    [super setDocument:document];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:NSViewFrameDidChangeNotification object:rhsValueHelpField];
	[_assignmentClassNames autorelease];
	[_rhsKeyNames autorelease];
	[toolbarItems autorelease];
    [rulesController removeObserver:self forKeyPath:@"selection.rhs.toolTip"];	
	[super dealloc]; // Will release all nib top-level objects
}

- (void)updateHelpViewSize {
    NSString    *toolTip = [rulesController valueForKeyPath:@"selection.rhs.toolTip"];
    NSRect      rhsValueTextViewFrame = [[rhsValueTextView enclosingScrollView] frame];
    NSRect      rhsValueHelpFieldFrame = [rhsValueHelpField frame];
    double      helpHeightIncrease;
    double      margin = 0;
    NSRect      invalidRect = NSUnionRect(rhsValueTextViewFrame, rhsValueHelpFieldFrame);
    
    if([toolTip isKindOfClass:[NSString class]]){
        NSRect  idealRect = rhsValueHelpFieldFrame;
        
        idealRect.size.height = 10000;
        idealRect.size = [[rhsValueHelpField cell] cellSizeForBounds:idealRect];
        
        helpHeightIncrease = idealRect.size.height - rhsValueHelpFieldFrame.size.height;
        if(rhsValueTextViewFrame.size.height - helpHeightIncrease < 56){
            helpHeightIncrease = rhsValueTextViewFrame.size.height - 56;
        }
        if(rhsValueHelpFieldFrame.size.height <= 2){
            margin = 9;
        }
    }
    else{
        if(rhsValueHelpFieldFrame.size.height > 2){
            margin = -9;
        }
        helpHeightIncrease = -rhsValueHelpFieldFrame.size.height + 1;
    }
    rhsValueTextViewFrame.origin.y += helpHeightIncrease + margin;
    rhsValueTextViewFrame.size.height -= helpHeightIncrease + margin;
    rhsValueHelpFieldFrame.size.height += helpHeightIncrease;
    [[rhsValueTextView enclosingScrollView] setFrame:rhsValueTextViewFrame];
    [rhsValueHelpField setPostsFrameChangedNotifications:NO];
    [rhsValueHelpField setFrame:rhsValueHelpFieldFrame];
    [rhsValueHelpField setPostsFrameChangedNotifications:YES];
    [[rhsValueHelpField superview] setNeedsDisplayInRect:invalidRect];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    [self updateHelpViewSize];
}

- (void)helpViewFrameDidChange:(NSNotification *)notif {
    [self updateHelpViewSize];
}

- (void)refreshAssignmentClassNamesComboBoxContents {
    NSArray         *assignmentClassNames = [[[self model] rules] valueForKeyPath:@"rhs.assignmentClass"];
    NSMutableSet    *assignmentClassNamesSet = [NSMutableSet setWithArray:assignmentClassNames];
    
    [assignmentClassNamesSet addObjectsFromArray:[NSArray arrayWithObjects:@"com.webobjects.directtoweb.Assignment", @"com.webobjects.directtoweb.KeyValueAssignment", @"com.webobjects.directtoweb.BooleanAssignment", @"com.webobjects.directtoweb.DefaultAssignment", nil]];
    [_assignmentClassNames autorelease];
    _assignmentClassNames = [[assignmentClassNamesSet allObjects] mutableCopy];
    [_assignmentClassNames removeObject:[NSNull null]];
    [_assignmentClassNames sortUsingSelector:@selector(caseInsensitiveCompare:)];
    [assignmentClassNamesComboBox reloadData];
}

- (void)refreshRhsKeyNamesComboBoxContents {
    NSArray *rhsKeys = [[[self model] rules] valueForKeyPath:@"rhs.keyPath"];
    NSSet   *rhsKeysSet = [NSSet setWithArray:rhsKeys];
    
    [_rhsKeyNames autorelease];
    _rhsKeyNames = [[rhsKeysSet allObjects] mutableCopy];
    [_rhsKeyNames removeObject:[NSNull null]];
    [_rhsKeyNames sortUsingSelector:@selector(caseInsensitiveCompare:)];
    [rhsKeyNamesComboBox reloadData];
}

- (void)awakeFromNib {
    [[self window] useOptimizedDrawing:YES];
    
    [self refreshAssignmentClassNamesComboBoxContents];
    [self refreshRhsKeyNamesComboBoxContents];
    
    [sourceDrawer setContentSize:NSMakeSize(0.0, 120.0)];
    
    [rhsValueTextView setFieldEditor:YES];
    [rhsValueTextView setFocusRingType:NSFocusRingTypeExterior];
    [[rhsValueTextView enclosingScrollView] setFocusRingType:NSFocusRingTypeExterior];
    [self updateHelpViewSize];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(helpViewFrameDidChange:) name:NSViewFrameDidChangeNotification object:rhsValueHelpField];
    [rulesController addObserver:self forKeyPath:@"selection.rhs.toolTip" options:0 context:NULL];
    [rulesController setSortDescriptors:[NSArray arrayWithObjects:[[[NSSortDescriptor alloc] initWithKey:@"author" ascending:YES] autorelease], [[[NSSortDescriptor alloc] initWithKey:@"lhsDescription" ascending:YES] autorelease], [[[NSSortDescriptor alloc] initWithKey:@"rhs.keyPath" ascending:YES] autorelease], nil]];
    [rulesTableView setAutosaveTableColumns:YES];
    
    toolbarItems = [[NSMutableDictionary dictionary] retain];
    
    addToolbarItem(toolbarItems, @"NewRule", @"New", @"New Rule", @"Add a new rule", rulesController, @selector(setImage:), [NSImage imageNamed:@"new.tif"], @selector(add:), nil);
    addToolbarItem(toolbarItems, @"DuplicateRule", @"Duplicate", @"Duplicate Rule", @"Duplicate rules", self, @selector(setImage:), [NSImage imageNamed:@"duplicate.tif"], @selector(duplicate:), nil);
    addToolbarItem(toolbarItems, @"RemoveRule", @"Remove", @"Remove Rule", @"Remove rules", rulesController, @selector(setImage:), [NSImage imageNamed:@"remove.tif"], @selector(remove:), nil);
    addToolbarItem(toolbarItems, @"Filter", @"Filter", @"Filter Rules", @"Enter a term or EOQualifier format", rulesController, @selector(setView:), filterView, @selector(search:), nil);
    addToolbarItem(toolbarItems, @"PreviousRule", @"Previous", @"Previous Rule", @"Select previous rule", rulesController, @selector(setImage:), [NSImage imageNamed:@"previous.tif"], @selector(selectPrevious:), nil);
    addToolbarItem(toolbarItems, @"NextRule", @"Next", @"Next Rule", @"Select next rule", rulesController, @selector(setImage:), [NSImage imageNamed:@"next.tif"], @selector(selectNext:), nil);
    addToolbarItem(toolbarItems, @"PreviewRule", @"Preview", @"Preview Rule", @"Toggle the source preview drawer", sourceDrawer, @selector(setImage:), [NSImage imageNamed:@"preview.tif"], @selector(toggle:), nil);
    
    [self prepareToolbar];
}

#pragma mark NSToolbar Methods

- (void)prepareToolbar {
    NSToolbar *toolbar = [[[NSToolbar alloc] initWithIdentifier:@"DMWindowToolbar"] autorelease];
    
    // create the NSToolbarItems for the NSToolbar
    /*
     addToolbarItem(toolbarItems,	// the dictionary of "master" NSToolbarItems
                    @"TestItem",	// an identifier for the item
                    @"Test Label",	// the label
                    @"Palette Label",	// the palette label
                    @"My Tooltip",	// the tooltip
                    self,		// the target
                    @selector(setView:),// settingSelector (@selector(setView:) or @selector(setImage:)
                    testView,		// the content for the above selector
                    NULL,		// action
                    NULL);		// menu
     */
    
    [toolbar setDelegate:self];
    [toolbar setAllowsUserCustomization:YES];
    [toolbar setAutosavesConfiguration:YES];
    [toolbar setDisplayMode:NSToolbarDisplayModeDefault];
    [toolbar setSizeMode:NSToolbarSizeModeDefault];
    
    [[self window] setToolbar:toolbar];
}

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
    static NSArray *dii = nil;
    
    if (dii == nil) {
	dii = [[NSArray arrayWithObjects:@"NewRule", @"DuplicateRule", @"RemoveRule", NSToolbarSpaceItemIdentifier, @"PreviousRule", @"NextRule", NSToolbarSpaceItemIdentifier, @"PreviewRule", NSToolbarFlexibleSpaceItemIdentifier, @"Filter", nil] retain];
    }
    
    return dii;
}

- (NSArray *)toolbarAllowedItemIdentifiers:(NSToolbar *)toolbar {
    static NSArray *aii = nil;
    
    if (aii == nil) {
	aii = [[NSArray arrayWithObjects:@"NewRule", @"DuplicateRule", @"RemoveRule", @"PreviousRule", @"NextRule", @"PreviewRule", @"Filter", NSToolbarCustomizeToolbarItemIdentifier, NSToolbarFlexibleSpaceItemIdentifier, NSToolbarSpaceItemIdentifier, NSToolbarSeparatorItemIdentifier, nil] retain];
    }
    
    return aii;
}

/*
 - (NSArray *)toolbarSelectableItemIdentifiers:(NSToolbar *)toolbar {
     return nil;
 }
 */

- (void)toolbarAction:(id)sender {
    NSString *identifier = [sender itemIdentifier];
    
    NSLog(@"identifier:%@", identifier);
}

#pragma mark Stuff

- (RMModel *)model {
    return [self document];
}

- (NSArray *)assignmentClassNames {
    return _assignmentClassNames;
}

- (IBAction)rhsComboBoxAction:(id)sender {
    NSString *name = [sender stringValue];
    NSMutableArray *values;
    
    if (sender == assignmentClassNamesComboBox) {
	values = _assignmentClassNames;
    } else if (sender == rhsKeyNamesComboBox) {
	values = _rhsKeyNames;
    } else {
	return;
    }
    
    if (![values containsObject:name]) {
	[values addObject:name];
	
	[sender reloadData];
    }
}

- (IBAction)copy:(id)sender {
    NSResponder *firstResponder = [[self window] firstResponder];
    if (firstResponder == rulesTableView) {
		if ([rulesTableView numberOfSelectedRows] > 0) {
			NSIndexSet *rowIdx = [rulesTableView selectedRowIndexes];
			NSMutableArray *rows = [NSMutableArray arrayWithCapacity:[rulesTableView numberOfSelectedRows]];
			
			NSArray *rules = [rulesController arrangedObjects];
			Rule *rule;
			
			unsigned int idx = [rowIdx firstIndex];
			
			while (idx != NSNotFound) {
				rule = [rules objectAtIndex:idx];
				[rows addObject:rule];
				
				idx = [rowIdx indexGreaterThanIndex:idx];
			}
			
			EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
			
			[archiver encodeObject:rows forKey:@"rules"];
			
			NSDictionary *plist = [archiver dictionary];
			
			NSPasteboard *pb = [NSPasteboard generalPasteboard];
			
			[pb declareTypes:[NSArray arrayWithObjects:@"D2WRules", NSStringPboardType, nil] owner:self];
			[pb setPropertyList:plist forType:@"D2WRules"];
			[pb setString:[plist description] forType:NSStringPboardType];
            [archiver release];
		}
    }
}

- (IBAction)cut:(id)sender {
    [self copy:sender];
    [self remove:sender];
}

- (IBAction)paste:(id)sender {
    NSResponder *firstResponder = [[self window] firstResponder];
    
    if (firstResponder == rulesTableView) {
	NSPasteboard *pb = [NSPasteboard generalPasteboard];
	NSString *type = [pb availableTypeFromArray:[NSArray arrayWithObject:@"D2WRules"]];
	
	if (type) {
	    NSDictionary *plist = [pb propertyListForType:@"D2WRules"];	    
        NSArray *rules = [Rule rulesFromMutablePropertyList:plist];
	    
	    [rulesController addObjects:rules];
	}
    }
}

- (IBAction)duplicate:(id)sender {
    NSResponder *firstResponder = [[self window] firstResponder];
    
    if (firstResponder == rulesTableView) {
	NSIndexSet *rowIdx = [rulesTableView selectedRowIndexes];
	NSMutableArray *rows = [NSMutableArray arrayWithCapacity:[rulesTableView numberOfSelectedRows]];
	
	NSArray *rules = [[self model] rules];
	Rule *rule;
	
	unsigned int idx = [rowIdx firstIndex];
	
	while (idx != NSNotFound) {
	    rule = [rules objectAtIndex:idx];
	    [rows addObject:[rule copy]];
	    
	    idx = [rowIdx indexGreaterThanIndex:idx];
	}
	
	[rulesController addObjects:rows];
    }
}

- (IBAction)add:(id)sender {
    [rulesController add:sender];
}

- (IBAction)remove:(id)sender {
    [rulesController remove:sender];
}

- (IBAction)selectNext:(id)sender {
    [rulesController selectNext:sender];
}

- (IBAction)selectPrevious:(id)sender {
    [rulesController selectPrevious:sender];
}

- (IBAction)centerSelectionInVisibleArea:(id)sender {
    [self showSelectedRule:sender];
}

- (IBAction)showSelectedRule:(id)sender {
    [rulesTableView scrollRowToVisible:[rulesTableView selectedRow]];
}

- (BOOL)validateMenuItem:(NSMenuItem *)item {
    NSString *title = [item title];
    
    if (([title isEqualToString:@"Copy"] || [title isEqualToString:@"Cut"] || [[item title] isEqualToString:@"Duplicate"]) && [rulesTableView numberOfSelectedRows] == 0) {
	return NO;
    } else if ([title isEqualToString:@"Paste"]) {
	NSPasteboard *pb = [NSPasteboard generalPasteboard];
	NSString *type = [pb availableTypeFromArray:[NSArray arrayWithObject:@"D2WRules"]];
	
	if (!type) {
	    return NO;
	}
    } else if ([title isEqualToString:@"Duplicate Rule"] && ([[self window] firstResponder] != rulesTableView || [rulesTableView numberOfSelectedRows] == 0)) {
	return NO;
    } else if ([title isEqualToString:@"Remove Rule"]) {
	return [rulesController canRemove];
    } else if ([title isEqualToString:@"Select Next"]) {
	return [rulesController canSelectNext];
    } else if ([title isEqualToString:@"Select Previous"]) {
	return [rulesController canSelectPrevious];
    }
    
    return YES;
}

- (IBAction)focusFilter:(id)sender {
    [[[self window] toolbar] setVisible:YES];
    
    [[self window] makeFirstResponder:filterField];
}

- (IBAction)toggleSourceDrawer:(id)sender {
    [sourceDrawer toggle:sender];
}

- (IBAction)removeDuplicateRules:(id)sender {
    NSMutableIndexSet   *removedRuleIndexes = [[NSMutableIndexSet alloc] init];
    NSArray             *allRules = [rulesController content];    
    unsigned int        i, j, count = [allRules count];
    
    for (i = 0; i < count; i++) {
        if (![removedRuleIndexes containsIndex:i]) {
            Rule    *eachRule = [allRules objectAtIndex:i];
            
            for (j = i + 1; j < count; j++) {
                Rule    *anotherRule = [allRules objectAtIndex:j];
                
                if ([eachRule isEqual:anotherRule]) {
                    [removedRuleIndexes addIndex:j];
                }
            }
        }
    }
    
    if ([removedRuleIndexes count] > 0) {
        // Test is necessary, else the arrayController would always add an entry to the undo stack, even when doing nothing!
        [rulesController removeObjectsAtArrangedObjectIndexes:removedRuleIndexes];
    }
    [removedRuleIndexes release];
}

#pragma mark Splitview Delegate Methods

- (float)splitView:(NSSplitView *)sender constrainMinCoordinate:(float)proposedMin ofSubviewAt:(int)offset {
    if (sender == masterSplitView || sender == detailSplitView) {
	return 130.0;
    } else if (sender == lhsSplitView) {
	return 90.0;
    }
    
    return proposedMin;
}

- (float)splitView:(NSSplitView *)sender constrainMaxCoordinate:(float)proposedMax ofSubviewAt:(int)offset {
    if (sender == masterSplitView) {
	return proposedMax - 200.0;
    } else if (sender == lhsSplitView) {
	return proposedMax - 25.0;
    } else if (sender == detailSplitView) {
	return proposedMax - 180.0;
    }
    
    return proposedMax;
}

- (BOOL)splitView:(NSSplitView *)sender canCollapseSubview:(NSView *)subview {
    return YES;
}

- (void)splitViewDidResizeSubviews:(NSNotification *)aNotification {
    [self updateHelpViewSize];
}

#pragma mark Combobox datasource methods

- (int)numberOfItemsInComboBox:(NSComboBox *)combobox {
    if (combobox == assignmentClassNamesComboBox) {
	return [_assignmentClassNames count];
    } else if (combobox == rhsKeyNamesComboBox) {
	return [_rhsKeyNames count];
    }
    
    return 0;
}

- (id)comboBox:(NSComboBox *)combobox objectValueForItemAtIndex:(int)index {
    if (combobox == assignmentClassNamesComboBox) {
	return [_assignmentClassNames objectAtIndex:index];
    } else if (combobox == rhsKeyNamesComboBox) {
	return [_rhsKeyNames objectAtIndex:index];
    }
    
    return nil;
}

- (NSString *)comboBox:(NSComboBox *)combobox completedString:(NSString *)string {
    NSArray *data;
    
    if (combobox == assignmentClassNamesComboBox) {
	data = _assignmentClassNames;
    } else if (combobox == rhsKeyNamesComboBox) {
	data = _rhsKeyNames;
    } else {
	return string;
    }
    
    int i, count = [data count];
    NSString *complete;
    
    for (i = 0; i < count; i++) {
	complete = [data objectAtIndex:i];
	
	if ([complete hasPrefix:string]) {
	    return complete;
	}
    }
    
    return string;
}

- (unsigned int)comboBox:(NSComboBox *)combobox indexOfItemWithStringValue:(NSString *)string {
    NSArray *data;
    
    if (combobox == assignmentClassNamesComboBox) {
	data = _assignmentClassNames;
    } else if (combobox == rhsKeyNamesComboBox) {
	data = _rhsKeyNames;
    } else {
	return NSNotFound;
    }
    
    return [data indexOfObject:string];
}

@end
