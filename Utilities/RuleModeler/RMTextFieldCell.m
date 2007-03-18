/*
 RMTextFieldCell.m
 RuleModeler

 Created by davelopper on 2/25/07.


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

#import "RMTextFieldCell.h"


@implementation RMTextFieldCell

- (void)dealloc {
    [self unbind:@"highlightedWords"];
    [self unbind:@"caseSensitivity"];
    [self unbind:@"highlightColor"];
    [self unbind:@"highlightsMatchingWords"];
    [highlightedWords release];
    [highlightColor release];
    
    [super dealloc];
}

- (void)highlightMatchingWords {
    NSString                    *stringValue = [self stringValue];
    NSRange                     searchRange = NSMakeRange(0, [stringValue length]);
    // Unlike for other classes, we can't copy the attributedStringValue, 
    // because when cell is in tableView, textColor is not correct when table selection changes.
//    NSMutableAttributedString   *attributedString = [[self attributedStringValue] mutableCopy];
    NSMutableAttributedString   *attributedString = [[NSMutableAttributedString alloc] initWithString:stringValue];
    NSMutableParagraphStyle     *paragraphStyle = [[NSParagraphStyle defaultParagraphStyle] mutableCopy];
  
//    [attributedString removeAttribute:NSBackgroundColorAttributeName range:searchRange];
    [attributedString setAttributes:[NSDictionary dictionaryWithObject:[NSFont systemFontOfSize:[NSFont smallSystemFontSize]] forKey:NSFontAttributeName] range:searchRange];

    // Change paragraph style to clip long lines, instead of showing only full words
    [paragraphStyle setLineBreakMode:NSLineBreakByClipping];
    [attributedString addAttribute:NSParagraphStyleAttributeName value:paragraphStyle range:searchRange];
    [paragraphStyle release];

    if ([self highlightsMatchingWords] && [[self highlightedWords] count] > 0) {
        NSEnumerator    *wordEnum = [[self highlightedWords] objectEnumerator];
        NSString        *eachWord;
        unsigned        options = [self caseSensitivity] ? 0:NSCaseInsensitiveSearch;
        NSColor         *aColor = [self highlightColor];
        
        while (eachWord = [wordEnum nextObject]) {
            NSRange matchRange;
            
            do {
                matchRange = [stringValue rangeOfString:eachWord options:options range:searchRange];
                
                if (matchRange.location != NSNotFound) {
                    [attributedString addAttribute:NSBackgroundColorAttributeName value:aColor range:matchRange];
                    searchRange.length = NSMaxRange(searchRange) - NSMaxRange(matchRange);
                    searchRange.location = NSMaxRange(matchRange);
                }
            } while(matchRange.length > 0);
        }
    }
    [self setAttributedStringValue:attributedString];
    [attributedString release];
    [[self controlView] setNeedsDisplay:YES];
}

- (NSArray *)highlightedWords {
    return highlightedWords;
}

- (void)setHighlightedWords:(NSArray *)newHighlightedWords {
    if (highlightedWords != newHighlightedWords) {
        NSArray *oldHighlightedWords = highlightedWords;
        
        highlightedWords = [newHighlightedWords copy];
        [oldHighlightedWords release];
        if ([self highlightsMatchingWords])
            [self highlightMatchingWords];
    }
}

- (BOOL)caseSensitivity {
    return caseSensitivity;
}

- (void)setCaseSensitivity:(BOOL)newCaseSensitivity {
    if (caseSensitivity != newCaseSensitivity) {
        caseSensitivity = newCaseSensitivity;
        if ([self highlightsMatchingWords])
            [self highlightMatchingWords];
    }
}

- (void)drawWithFrame:(NSRect)cellFrame inView:(NSView *)controlView {
    if ([self highlightsMatchingWords])
        [self highlightMatchingWords];
    [super drawWithFrame:cellFrame inView:controlView];
}

- (NSColor *)highlightColor
{
    return highlightColor;
}

- (void)setHighlightColor:(NSColor *)value
{
    if(highlightColor != value){
        NSColor *oldValue = highlightColor;
        
        highlightColor = (value != nil ? [value copy] : nil);
        if(oldValue != nil)
            [oldValue release];
        if ([self highlightsMatchingWords])
            [self highlightMatchingWords];
    }
}

- (id)copyWithZone:(NSZone *)zone {
    // Super's implementation copies raw memory, and doesn't take care about retain/release for our new ivars.
    RMTextFieldCell  *result = [super copyWithZone:zone];
    
    result->highlightedWords = [highlightedWords copy];
    result->highlightColor = [highlightColor copy];
    if ([result highlightsMatchingWords])
        [result highlightMatchingWords];
    
    return result;
}

- (BOOL)highlightsMatchingWords {
    return highlightsMatchingWords;
}

- (void)setHighlightsMatchingWords:(BOOL)newHighlightsMatchingWords {
    if (highlightsMatchingWords != newHighlightsMatchingWords) {
        highlightsMatchingWords = newHighlightsMatchingWords;
        [self highlightMatchingWords];
    }
}

@end
