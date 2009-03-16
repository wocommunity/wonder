/*
 RMTableView.m
 RuleModeler

 Created by davelopper on 11/5/06.


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

#import "RMTableView.h"
#import "NSIndexSetAdditions.h"

/*
 * Catches delete and backspace keys to perform deletion
 * Catches return key to perform addition
 * Provides invertSelection: action
 * See http://rentzsch.com/cocoa/NSTableViewCocoaBindingsDeleteKey
 */
@implementation RMTableView

- (NSArrayController *) tableContentController {
	return [[self infoForBinding:@"content"] objectForKey:NSObservedObjectKey];
}

- (void)keyDown:(NSEvent *)event {
    NSString *chars = [event characters];
    
    if([chars length] > 0){
        unichar            key = [chars characterAtIndex:0];
        NSArrayController  *tableContentController = [self tableContentController];
        
        if (key == NSDeleteCharacter || key == NSDeleteFunctionKey || key == NSDeleteCharFunctionKey) { 
            if ([self isEnabled] && [tableContentController canRemove]) {
                [tableContentController remove:self];
            }
            else
                NSBeep();
            return;
        }
        else if (key == NSCarriageReturnCharacter) { 
            if ([self isEnabled] && [tableContentController canAdd]) {
                if ([[self delegate] respondsToSelector:@selector(addToTableView:)])
                    [[self delegate] addToTableView:self];
                else
                    [tableContentController add:self];
            }
            else
                NSBeep();
            return;
        }
    }
    [super keyDown:event]; // let somebody else handle the event 
}

- (IBAction)invertSelection:(id)sender {
	NSArrayController   *tableContentController = [self tableContentController];
    unsigned            maxIndex = [[tableContentController arrangedObjects] count] - 1;
    NSIndexSet          *inverseIndexSet = [[tableContentController selectionIndexes] inverseIndexWithMaxIndex:maxIndex];
    
    [tableContentController setSelectionIndexes:inverseIndexSet];
}

@end
