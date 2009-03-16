/*
 NSIndexSetAdditions.m
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

#import "NSIndexSetAdditions.h"


@implementation NSIndexSet(RuleModelerAdditions)

- (id)inverseIndexWithMaxIndex:(unsigned)maxIndex {
    unsigned            indexCount = [self count];
    NSMutableIndexSet   *inverseIndexSet = [[NSMutableIndexSet alloc] init];
    
    maxIndex++;
    if (indexCount == 0) {
        [inverseIndexSet addIndexesInRange:NSMakeRange(0, maxIndex)];
    } 
    else if (indexCount != maxIndex) {
        unsigned    firstIndex = [self firstIndex];
        NSRange     aRange = NSMakeRange(firstIndex, [self lastIndex] - firstIndex + 1);
        unsigned    *indexes = NSZoneMalloc(NSDefaultMallocZone(), indexCount * sizeof(unsigned));
        unsigned    i;
        unsigned    previousIndexPlusOne = firstIndex + 1;
        
        (void)[self getIndexes:indexes maxCount:indexCount inIndexRange:&aRange];
        if (firstIndex != 0)
            [inverseIndexSet addIndexesInRange:NSMakeRange(0, firstIndex)];
        for (i = 1; i < indexCount; i++) {
            unsigned    eachIndex = indexes[i];
            
            if (eachIndex != previousIndexPlusOne)
                [inverseIndexSet addIndexesInRange:NSMakeRange(previousIndexPlusOne, eachIndex - previousIndexPlusOne)];
            previousIndexPlusOne = eachIndex + 1;
        }
        if (previousIndexPlusOne != maxIndex)
            [inverseIndexSet addIndexesInRange:NSMakeRange(previousIndexPlusOne, maxIndex - previousIndexPlusOne)];
        NSZoneFree(NSDefaultMallocZone(), indexes);
    }
    
    return [inverseIndexSet autorelease];
}

@end
