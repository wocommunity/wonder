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
    NSMutableAttributedString   *attributedString = [[self attributedStringValue] mutableCopy];

    // There is no API that can tell us when highlight color should NOT be used:
    // When rule is disabled, even when selected, we need to use textColor which defaults to [NSColor disabledControlTextColor]
    if ([self isHighlighted] && !_cFlags.needsHighlightedText)
        [attributedString addAttribute:NSForegroundColorAttributeName value:[NSColor selectedControlTextColor] range:searchRange];
    else
        [attributedString addAttribute:NSForegroundColorAttributeName value:[self textColor] range:searchRange];

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
}

- (NSArray *)highlightedWords {
    return highlightedWords;
}

- (void)setHighlightedWords:(NSArray *)newHighlightedWords {
    if (highlightedWords != newHighlightedWords) {
        NSArray *oldHighlightedWords = highlightedWords;
        
        highlightedWords = [newHighlightedWords copy];
        [oldHighlightedWords release];
        if ([self highlightsMatchingWords]){
            [self highlightMatchingWords];
            [(NSControl *)[self controlView] updateCell:self];
        }
    }
}

- (BOOL)caseSensitivity {
    return caseSensitivity;
}

- (void)setCaseSensitivity:(BOOL)newCaseSensitivity {
    if (caseSensitivity != newCaseSensitivity) {
        caseSensitivity = newCaseSensitivity;
        if ([self highlightsMatchingWords]){
            [self highlightMatchingWords];
            [(NSControl *)[self controlView] updateCell:self];
        }
    }
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
        if ([self highlightsMatchingWords]){
            [self highlightMatchingWords];
            [(NSControl *)[self controlView] updateCell:self];
        }
    }
}

- (id)copyWithZone:(NSZone *)zone {
    // Super's implementation copies raw memory, and doesn't take care about retain/release for our new ivars.
    RMTextFieldCell  *result = [super copyWithZone:zone];
    
    result->highlightedWords = [highlightedWords copy];
    result->highlightColor = [highlightColor copy];
    
    return result;
}

- (BOOL)highlightsMatchingWords {
    return highlightsMatchingWords;
}

- (void)setHighlightsMatchingWords:(BOOL)newHighlightsMatchingWords {
    if (highlightsMatchingWords != newHighlightsMatchingWords) {
        highlightsMatchingWords = newHighlightsMatchingWords;
        [self highlightMatchingWords];
        [(NSControl *)[self controlView] updateCell:self];
    }
}

@end
