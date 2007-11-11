/*
 RMFilteringArrayController.h
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

#import <Foundation/Foundation.h>

@interface RMFilteringArrayController : NSArrayController {
    NSArray  *focussedObjects;
    NSImage  *focusImage;
    NSImage  *emptyImage;
    IBOutlet NSSearchField   *searchField;
    BOOL    searchIsCaseSensitive;
    BOOL    searchMatchesAnyWord;
    BOOL    textualSearchOnly;
    NSArray *searchWords;
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
- (IBAction)changeParsingOption:(id)sender;

- (BOOL)searchIsCaseSensitive;
- (NSArray *)searchWords;
- (BOOL)textualSearchOnly;

@end
