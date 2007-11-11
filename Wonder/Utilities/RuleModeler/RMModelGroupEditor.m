/*
 RMModelGroupEditor.m
 RuleModeler

 Created by davelopper on 8/14/06.


 Copyright (c) 2004-2007, Project WONDER <http://wonder.sourceforge.net/>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  * Neither the name of the Project WONDER nor the names of its contributors may
    be used to endorse or promote products derived from this software without
    specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#import "RMModelGroupEditor.h"
#import "RMModelGroup.h"
#import "Rule.h"
#import "DMToolbarUtils.h"


//#define EDITABLE_MODELS

@implementation RMModelGroupEditor

- (id)init {
    if (self = [self initWithWindowNibName:@"RMModelGroupEditor"]) {
        
    }
    
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[modelListDrawer contentView] release]; // Top-level object!
    [modelListDrawer release];
    [modelController release];

    [super dealloc];
}

- (void)setDocument:(NSDocument *)document {
    // We need to do that in order to avoid a KVO warning when document closes
    // Only that binding is problematic
    if(document == nil)
        [modelController unbind:@"contentArray"];
    [super setDocument:document];
}

- (void)awakeFromNib {
    if (!modelListDrawer) { // This method is invoked twice, once for each nib
        [NSBundle loadNibNamed:@"RMModelList" owner:self];
        [modelListDrawer setParentWindow:[self window]];
        NSSize  aSize = NSMakeSize(200, 0);
        aSize.width = [[NSUserDefaults standardUserDefaults] floatForKey:@"modelListDrawerWidth"];
        [modelListDrawer setContentSize:aSize];
        [modelListDrawer toggle:nil];
        [modelTableView setDoubleAction:@selector(showModel:)];
        [modelTableView setTarget:self];
#ifndef EDITABLE_MODELS
        [rulesTableView setDoubleAction:@selector(showRuleModel:)];
        [rulesTableView setTarget:self];
#endif        
        [rulesTableView setEnabled:NO];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(windowWillClose:) name:NSWindowWillCloseNotification object:nil];
        [super awakeFromNib]; // Must be called after loading second nib, else toolbar not initialized correctly
        // TODO Use RBSplitView to allow programmatically collapsing splitviews (http://www.brockerhoff.net/src/rbs.html)
    }
}

- (void)windowWillClose:(NSNotification *)notification {
    // In case a referenced document is closed without being saved, we need to
    // revert the document to its saved state.
    // When method is invoked, the relationship between model and its windowcontroller(s)
    // has already been broken, thus we simply check all our models that have no windowcontroller
    // and when one of them has modifications, we revert it.
    NSEnumerator    *modelEnum = [[[modelController content] valueForKey:@"model"] objectEnumerator];
    RMModel         *eachModel;
    
    while (eachModel = [modelEnum nextObject]) {
        if (![[eachModel windowControllers] count] && [eachModel isDocumentEdited]) {
            NSError *outError;
            
            // FIXME When terminating, we could avoid reverting
            if (![eachModel revertToContentsOfURL:[eachModel fileURL] ofType:[eachModel fileType] error:&outError]) {
                // TODO Maybe offer user to remove or change reference to model - currently we silently remove our reference to it
                [[NSAlert alertWithError:outError] beginSheetModalForWindow:[self window] modalDelegate:nil didEndSelector:NULL contextInfo:NULL];
                [(RMModelGroup *)[self document] removeModels:[NSArray arrayWithObject:eachModel]];
            }
        }
    }    
}

- (void)addToolbarItems {
#ifdef EDITABLE_MODELS
    [super addToolbarItems];
#else
    NSToolbarItem   *anItem;
    
    anItem = addToolbarItem(toolbarItems, @"Filter", 
                            NSLocalizedString(@"Filter", @"Toolbar item label"),
                            NSLocalizedString(@"Filter Rules", @"Toolbar item palette label"), 
                            NSLocalizedString(@"Enter a term or EOQualifier format", @"Toolbar item tooltip"), 
                            rulesController, @selector(setView:), filterView, @selector(search:), nil);
    [anItem setMaxSize:NSMakeSize(1000., [anItem maxSize].height)];
    [anItem setVisibilityPriority:NSToolbarItemVisibilityPriorityHigh];
    addToolbarItem(toolbarItems, @"PreviousRule", 
                   NSLocalizedString(@"Previous", @"Toolbar item label"), 
                   NSLocalizedString(@"Previous Rule", @"Toolbar item palette label"), 
                   NSLocalizedString(@"Select previous rule", @"Toolbar item tooltip"), 
                   rulesController, @selector(setImage:), [NSImage imageNamed:@"previous"], @selector(selectPrevious:), nil);
    addToolbarItem(toolbarItems, @"NextRule", 
                   NSLocalizedString(@"Next", @"Toolbar item label"), 
                   NSLocalizedString(@"Next Rule", @"Toolbar item palette label"), 
                   NSLocalizedString(@"Select next rule", @"Toolbar item tooltip"), 
                   rulesController, @selector(setImage:), [NSImage imageNamed:@"next"], @selector(selectNext:), nil);
    addToolbarItem(toolbarItems, @"PreviewRule", 
                   NSLocalizedString(@"Preview", @"Toolbar item label"), 
                   NSLocalizedString(@"Preview Rule", @"Toolbar item palette label"), 
                   NSLocalizedString(@"Toggle the source preview drawer", @"Toolbar item tooltip"), 
                   sourceDrawer, @selector(setImage:), [NSImage imageNamed:@"preview"], @selector(toggle:), nil);
#endif
    addToolbarItem(toolbarItems, @"ToggleModels", 
                   NSLocalizedString(@"Models", @"Toolbar item label"), 
                   NSLocalizedString(@"Toggle Models", @"Toolbar item palette label"), 
                   NSLocalizedString(@"Toggle the model list drawer", @"Toolbar item tooltip"), 
                   modelListDrawer, @selector(setImage:), [NSImage imageNamed:@"models"], @selector(toggle:), nil);
}

- (NSString *)toolbarIdentifier {
    return @"RMModelGroupWindowToolbar";
}

- (NSArray *)toolbarDefaultItemIdentifiers:(NSToolbar *)toolbar {
    static NSArray *dii = nil;
    
    if (dii == nil) {
#ifdef EDITABLE_MODELS
        dii = [[NSArray arrayWithObjects:@"ToggleModels", NSToolbarSeparatorItemIdentifier, @"NewRule", @"DuplicateRule", @"RemoveRule", NSToolbarSpaceItemIdentifier, @"PreviousRule", @"NextRule", NSToolbarSpaceItemIdentifier, @"PreviewRule", NSToolbarFlexibleSpaceItemIdentifier, @"Filter", nil] retain];
#else
        dii = [[NSArray arrayWithObjects:@"ToggleModels", NSToolbarSeparatorItemIdentifier, @"PreviousRule", @"NextRule", NSToolbarSpaceItemIdentifier, @"PreviewRule", NSToolbarFlexibleSpaceItemIdentifier, @"Filter", nil] retain];
#endif
    }
    
    return dii;
}

- (NSArray *)toolbarAllowedItemIdentifiers:(NSToolbar *)toolbar {
    static NSArray *aii = nil;
    
    if (aii == nil) {
#ifdef EDITABLE_MODELS
        aii = [[NSArray arrayWithObjects:@"ToggleModels", @"NewRule", @"DuplicateRule", @"RemoveRule", @"PreviousRule", @"NextRule", @"PreviewRule", @"Filter", NSToolbarCustomizeToolbarItemIdentifier, NSToolbarFlexibleSpaceItemIdentifier, NSToolbarSpaceItemIdentifier, NSToolbarSeparatorItemIdentifier, nil] retain];
#else
        aii = [[NSArray arrayWithObjects:@"ToggleModels", @"PreviousRule", @"NextRule", @"PreviewRule", @"Filter", NSToolbarCustomizeToolbarItemIdentifier, NSToolbarFlexibleSpaceItemIdentifier, NSToolbarSpaceItemIdentifier, NSToolbarSeparatorItemIdentifier, nil] retain];
#endif
    }
    
    return aii;
}

- (void)openPanelDidEnd:(NSOpenPanel *)panel returnCode:(int)returnCode  contextInfo:(void  *)contextInfo {
    if (returnCode == NSOKButton) {
        NSError *anError = nil;
        
        [(RMModelGroup *)[self document] addModelsWithURLs:[panel URLs] relativePaths:YES error:&anError];
        if (anError) {
            [[NSAlert alertWithError:anError] beginSheetModalForWindow:[self window] modalDelegate:nil didEndSelector:NULL contextInfo:NULL];
        }
    }
}

- (IBAction)addModels:(id)sender {
    NSOpenPanel *openPanel = [NSOpenPanel openPanel];
    
    [openPanel setCanSelectHiddenExtension:YES];
    [openPanel setTreatsFilePackagesAsDirectories:YES];
    [openPanel setAllowsMultipleSelection:YES];
    [openPanel beginSheetForDirectory:nil file:nil types:[NSArray arrayWithObject:@"d2wmodel"] modalForWindow:[self window] modalDelegate:self didEndSelector:@selector(openPanelDidEnd:returnCode: contextInfo:) contextInfo:NULL];
}

- (IBAction)showModel:(id)sender {
    // Open model document
    [[modelController valueForKeyPath:@"selectedObjects.model"] makeObjectsPerformSelector:@selector(showWindows)];
}

- (IBAction)removeModels:(id)sender {
    [(RMModelGroup *)[self document] removeModels:[modelController valueForKeyPath:@"selectedObjects.model"]];
}

- (IBAction)showRuleModel:(id)sender {
    // Opens rule model documents, resets rule model filter, and selects rules
    NSArray         *selectedRules = [rulesController selectedObjects];
    NSArray         *models = [selectedRules valueForKeyPath:@"@distinctUnionOfObjects.model"];
    NSEnumerator    *modelEnum = [models objectEnumerator];
    RMModel         *eachModel;
    RMWindowPart    aPart = -1;
    
    // In case of double-click action, we get a clicked row/col, and only one row is selected.
    // We set focus in rule model window, according to which column was clicked.
    if ([rulesTableView clickedRow] != -1) {
        int clickedColumn = [rulesTableView clickedColumn];
        
        if(clickedColumn != -1){
            NSTableColumn   *aCol = [[rulesTableView tableColumns] objectAtIndex:clickedColumn];
            NSString        *anIdentifier = [aCol identifier];
            
            if([anIdentifier isEqualToString:@"priority"])
                aPart = RMWindowPriorityPart;
            else if([anIdentifier isEqualToString:@"lhs"])
                aPart = RMWindowLHSPart;
            else if([anIdentifier isEqualToString:@"rhs.keyPath"])
                aPart = RMWindowRHSKeyPathPart;
            else if([anIdentifier isEqualToString:@"rhs.value"])
                aPart = RMWindowRHSValuePart;
            else if([anIdentifier isEqualToString:@"assignment"])
                aPart = RMWindowRHSClassPart;
        }
    }
    
    while (eachModel = [modelEnum nextObject]) {
        NSEnumerator    *ruleEnum = [selectedRules objectEnumerator];
        Rule            *eachRule;
        NSMutableArray  *rules = [NSMutableArray arrayWithCapacity:[selectedRules count]];
        RMModelEditor   *editor;
        
        while (eachRule = [ruleEnum nextObject]) {
            if([eachRule model] == eachModel)
                [rules addObject:eachRule];
        }
        [eachModel showWindows];
        editor = [[eachModel windowControllers] lastObject]; // Call that AFTER having called showWindows
        [[editor rulesController] setValue:nil forKey:@"filterPredicate"];
        [[editor rulesController] setValue:rules forKey:@"selectedObjects"];
        [editor unfocus:sender];
        if(aPart != -1)
            [editor setFirstResponderInPart:aPart];
    }
}

- (NSString *)tableView:(NSTableView *)aTableView toolTipForCell:(NSCell *)aCell rect:(NSRectPointer)rect tableColumn:(NSTableColumn *)aTableColumn row:(int)row mouseLocation:(NSPoint)mouseLocation {
    // Tooltips work more or less... Sometimes user needs to resize drawer to 
    // force display of tooltips on next hovering.
    if ([[aTableColumn identifier] isEqualToString:@"displayName"]) {
        return [[[modelController arrangedObjects] objectAtIndex:row] valueForKeyPath:@"model.fileURL.path"];
    } else {
        return nil;
    }
}

#ifndef EDITABLE_MODELS
- (BOOL)validateAction:(SEL)action {
    if (action == @selector(cut:) || action == @selector(paste:) || action == @selector(duplicate:) || action == @selector(add:) || action == @selector(remove:)) {
        return NO;
    } else if (action == @selector(showRuleModel:)) {
            return [rulesTableView numberOfSelectedRows] > 0;
    } else {
        return [super validateAction:action];
    }
}
#endif

- (NSSize)drawerWillResizeContents:(NSDrawer *)sender toSize:(NSSize)contentSize {
    if (sender == modelListDrawer)
        [[NSUserDefaults standardUserDefaults] setFloat:contentSize.width forKey:@"modelListDrawerWidth"];
    
    return contentSize;
}

- (BOOL)observesRules {
    return NO;
}

@end
