//  Copyright (c) 2004 King Chung Huang
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

//  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

//
//  RMModelEditor.h
//  RuleModeler
//
//  Created by King Chung Huang on Thu Jan 29 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <AppKit/AppKit.h>
#import "EOControl.h"

@class RMModel;

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
    
    IBOutlet NSArrayController  *rulesController;
    
    @private
    NSMutableArray		*_assignmentClassNames;
    NSMutableArray		*_rhsKeyNames;
    
    NSMutableDictionary		*toolbarItems;
    NSMutableArray		*toolbarIdentifiers;
    
}

- (void)prepareToolbar;

- (RMModel *)model;

- (IBAction)showSelectedRule:(id)sender;

- (NSArray *)assignmentClassNames;
- (IBAction)rhsComboBoxAction:(id)sender;

@end
