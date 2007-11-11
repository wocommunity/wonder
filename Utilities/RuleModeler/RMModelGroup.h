/*
 RMModelGroup.h
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

#import <Cocoa/Cocoa.h>


extern NSString *RMModelGroupType;


@class RMModel;
@class Rule;


@interface RMModelGroup : NSDocument {
    NSMutableArray  *_rules;
    NSMutableArray  *_modelContainers;
    NSURL           *_futureURL;
}

- (RMModel *)addModelWithURL:(NSURL *)url relativePath:(BOOL)relativePath error:(NSError **)outError;
- (void)addModelsWithURLs:(NSArray *)urls relativePaths:(BOOL)relativePaths error:(NSError **)outError;

- (void)removeModels:(NSArray *)models;
- (void)removeObjectFromModelContainersAtIndex:(unsigned)index;
- (void)removeModelWithURL:(NSURL *)url relativePath:(BOOL)relativePath error:(NSError **)outError;

- (NSArray *)rules;
- (unsigned)countOfRules;
- (Rule *)objectInRulesAtIndex:(unsigned)theIndex;
- (void)getRules:(Rule **)objsPtr range:(NSRange)range;
- (void)insertObject:(Rule *)obj inRulesAtIndex:(unsigned)theIndex;
- (void)removeObjectFromRulesAtIndex:(unsigned)theIndex;
- (void)replaceObjectInRulesAtIndex:(unsigned)theIndex withObject:(Rule *)obj;

- (NSArray *) modelContainers;
- (void) insertObject:(id)obj inModelContainersAtIndex:(unsigned)theIndex;
- (void) removeObjectFromModelContainersAtIndex:(unsigned)theIndex;

- (id)makeNewWindowController;

@end
