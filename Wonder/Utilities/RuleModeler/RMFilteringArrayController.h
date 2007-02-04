//
//  RMFilteringArrayController.h
//  RuleModeler
//
//  Created by King Chung Huang on Fri Jan 30 2004.
//  Copyright (c) 2004 King Chung Huang. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface RMFilteringArrayController : NSArrayController {
    NSArray  *focussedObjects;
    NSImage  *focusImage;
    NSImage  *emptyImage;
    IBOutlet NSSearchField   *searchField;
    BOOL    searchIsCaseSensitive;
    BOOL    searchMatchesAnyWord;
}

- (void)search:(id)sender;

- (IBAction)focus:(id)sender;
- (IBAction)unfocus:(id)sender;

- (BOOL)focussing;
- (BOOL)canFocus;
- (BOOL)canUnfocus;

- (NSSearchField *) searchField;
- (void) setSearchField:(NSSearchField *)value;

- (IBAction)changeCaseSensitivityOption:(id)sender;
- (IBAction)changeWordMatchingOption:(id)sender;

@end
