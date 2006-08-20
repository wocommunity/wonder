//
//  RMModelGroupEditor.m
//  RuleModeler
//
//  Created by Dave Lopper on 8/14/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

#import "RMModelGroupEditor.h"
#import "RMModelGroup.h"
#import "Rule.h"
#import "DMToolbarUtils.m"


//#define EDITABLE_MODELS

@implementation RMModelGroupEditor

- (id)init {
    if (self = [self initWithWindowNibName:@"RMModelGroupEditor"]) {
        
    }
    
    return self;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
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
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(windowWillClose:) name:NSWindowWillCloseNotification object:nil];
        [super awakeFromNib]; // Must be called after loading second nib, else toolbar not initialized correctly
    }
}

- (void) windowWillClose:(NSNotification *)notification {
    // In case a referenced document is closed without being saved, we need to
    // revert the document to its saved state.
    id  aController = [[notification object] delegate];
    
    if ([aController isKindOfClass:[NSWindowController class]] && [[aController document] isKindOfClass:[RMModel class]]) {
        RMModel *aModel = [aController document];
        
        if ([[[modelController content] valueForKey:@"model"] containsObject:aModel]) {
            if ([aModel isDocumentEdited]) {
                NSError *outError;
                
                if (![aModel revertToContentsOfURL:[aModel fileURL] ofType:[aModel fileType] error:&outError]) {
                    // TODO Maybe offer user to remove or change reference to model - currently we silently remove our reference to it
                    [[NSAlert alertWithError:outError] beginSheetModalForWindow:[self window] modalDelegate:nil didEndSelector:NULL contextInfo:NULL];
                    [(RMModelGroup *)[self document] removeModels:[NSArray arrayWithObject:aModel]];
                }
            }
        }
    }
}

- (void)addToolbarItems {
#ifdef EDITABLE_MODELS
    [super addToolbarItems];
#else
    addToolbarItem(toolbarItems, @"Filter", @"Filter", @"Filter Rules", @"Enter a term or EOQualifier format", rulesController, @selector(setView:), filterView, @selector(search:), nil);
    addToolbarItem(toolbarItems, @"PreviousRule", @"Previous", @"Previous Rule", @"Select previous rule", rulesController, @selector(setImage:), [NSImage imageNamed:@"previous.tif"], @selector(selectPrevious:), nil);
    addToolbarItem(toolbarItems, @"NextRule", @"Next", @"Next Rule", @"Select next rule", rulesController, @selector(setImage:), [NSImage imageNamed:@"next.tif"], @selector(selectNext:), nil);
    addToolbarItem(toolbarItems, @"PreviewRule", @"Preview", @"Preview Rule", @"Toggle the source preview drawer", sourceDrawer, @selector(setImage:), [NSImage imageNamed:@"preview.tif"], @selector(toggle:), nil);
#endif
    addToolbarItem(toolbarItems, @"ToggleModels", @"Models", @"Toggle Models", @"Toggle the model list drawer", modelListDrawer, @selector(setImage:), [NSImage imageNamed:@"models.tiff"], @selector(toggle:), nil);
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
    [[NSOpenPanel openPanel] beginSheetForDirectory:nil file:nil types:[NSArray arrayWithObject:@"d2wmodel"] modalForWindow:[self window] modalDelegate:self didEndSelector:@selector(openPanelDidEnd:returnCode: contextInfo:) contextInfo:NULL];
}

- (IBAction)showModel:(id)sender {
    // Open model document
    [[modelController valueForKeyPath:@"selectedObjects.model"] makeObjectsPerformSelector:@selector(showWindows)];
}

- (IBAction)removeModels:(id)sender {
    [(RMModelGroup *)[self document] removeModels:[modelController valueForKeyPath:@"selectedObjects.model"]];
}

- (IBAction)showRuleModel:(id)sender {
    // Opens rule model documents, reset's rule model filter, and selects rules
    NSArray         *selectedRules = [rulesController selectedObjects];
    NSArray         *models = [selectedRules valueForKeyPath:@"@distinctUnionOfObjects.model"];
    NSEnumerator    *modelEnum = [models objectEnumerator];
    RMModel         *eachModel;
    
    while (eachModel = [modelEnum nextObject]) {
        NSEnumerator    *ruleEnum = [selectedRules objectEnumerator];
        Rule            *eachRule;
        NSMutableArray  *rules = [NSMutableArray arrayWithCapacity:[selectedRules count]];
        
        while (eachRule = [ruleEnum nextObject]) {
            if([eachRule model] == eachModel)
                [rules addObject:eachRule];
        }
        [eachModel showWindows];
        [[[[eachModel windowControllers] lastObject] rulesController] setValue:nil forKey:@"filterPredicate"];
        [[[[eachModel windowControllers] lastObject] rulesController] setValue:rules forKey:@"selectedObjects"];
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
    } else {
        return [super validateAction:action];
    }
}
#endif

- (void) removeDuplicateRulesAtIndexes:(NSIndexSet *)indexes {
    // Performs search though all models. On results, show matching model 
    // documents to user, to allow him to undo, per model.
    NSMutableArray  *modelRules = [[self rules] mutableCopy];
    NSArray         *removedRules = [modelRules objectsAtIndexes:indexes];
    NSArray         *targetModels = [removedRules valueForKeyPath:@"@distinctUnionOfObjects.model"];
    NSEnumerator    *modelEnum = [targetModels objectEnumerator];
    RMModel         *eachModel;
    
    while (eachModel = [modelEnum nextObject]) {
        NSEnumerator        *ruleEnum = [removedRules objectEnumerator];
        Rule                *eachRule;
        NSMutableIndexSet   *ruleIndexes = [NSMutableIndexSet indexSet];
        RMModelEditor       *anEditor;
        
        while (eachRule = [ruleEnum nextObject]) {
            if ([eachRule model] == eachModel) {
                unsigned    anIndex = [[eachModel rules] indexOfObjectIdenticalTo:eachRule];
                
                [ruleIndexes addIndex:anIndex];
            }
        }
        [eachModel showWindows];        
        anEditor = [[eachModel windowControllers] lastObject];
        [anEditor removeDuplicateRulesAtIndexes:ruleIndexes];
    }
    [modelRules release];
}

- (NSSize)drawerWillResizeContents:(NSDrawer *)sender toSize:(NSSize)contentSize {
    if (sender == modelListDrawer)
        [[NSUserDefaults standardUserDefaults] setFloat:contentSize.width forKey:@"modelListDrawerWidth"];
    
    return contentSize;
}

@end
