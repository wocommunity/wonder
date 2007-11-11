/*
 RMModelEditor.h
 RuleModeler

 Created by King Chung Huang on 1/29/04.


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

#import <AppKit/AppKit.h>


@class RMModel;
@class RMFilteringArrayController;


typedef enum {
    RMWindowPriorityPart,
    RMWindowLHSPart,
    RMWindowRHSClassPart,
    RMWindowRHSKeyPathPart,
    RMWindowRHSValuePart
} RMWindowPart;


@interface RMModelEditor : NSWindowController {
    
    IBOutlet NSComboBox		*assignmentClassNamesComboBox;
    IBOutlet NSComboBox		*rhsKeyNamesComboBox;
    
    IBOutlet NSTableView	*rulesTableView;
    
    IBOutlet NSSplitView	*masterSplitView;
    IBOutlet NSSplitView	*detailSplitView;
    IBOutlet NSSplitView	*lhsSplitView;

    IBOutlet NSView		*filterView;
    IBOutlet NSSearchField      *filterField;
    
    IBOutlet NSDrawer		*sourceDrawer;
    
    IBOutlet RMFilteringArrayController  *rulesController;
    IBOutlet NSTextField    *lhsValueTextField;
    IBOutlet NSButton       *lhsFormatCheckbox;
    IBOutlet NSTextView     *rhsValueTextView;
    IBOutlet NSTextField    *rhsValueHelpField;
    
    NSMutableDictionary		*toolbarItems;
    IBOutlet NSView         *cornerView;
}

- (void)prepareToolbar;

- (RMModel *)model;

- (BOOL)observesRules;

- (IBAction)showSelectedRule:(id)sender;

- (IBAction)rhsComboBoxAction:(id)sender;

- (IBAction)remove:(id)sender;

- (IBAction)removeDuplicateRules:(id)sender;
- (IBAction)showDuplicateRules:(id)sender;
- (void) removeDuplicateRulesAtIndexes:(NSIndexSet *)indexes;

- (NSString *)toolbarIdentifier;
- (void)addToolbarItems;

- (RMFilteringArrayController *) rulesController;
- (NSArray *)rules;
- (BOOL)validateAction:(SEL)action;

- (IBAction) focus:(id)sender;
- (IBAction) unfocus:(id)sender;
- (void) showRules:(NSArray *)rules;

- (void) setFirstResponderInPart:(RMWindowPart)part;

- (IBAction)openInNewWindow:(id)sender;

@end
