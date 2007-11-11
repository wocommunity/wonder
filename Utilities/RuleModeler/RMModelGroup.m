/*
 RMModelGroup.m
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

#import "RMModelGroup.h"
#import "RMModelGroupEditor.h"
#import "RMModel.h"
#import "RMDocumentController.h"
#import "Rule.h"
#import "NSStringAdditions.h"


NSString *RMModelGroupType = @"Rule Model Group";


// We need that (inner) class only to support undo/redo of the modification
// of the 'relative' info.
@interface RMModelGroupContainer : NSObject {
    RMModel         *_model;
    BOOL            _relative;
    RMModelGroup    *_group;
}

- (id)initWithModel:(RMModel *)model relative:(BOOL)relative group:(RMModelGroup *)group;

@end

@implementation RMModelGroupContainer

- (id)initWithModel:(RMModel *)model relative:(BOOL)relative group:(RMModelGroup *)group {
    [super init];
    _model = [model retain];
    _relative = relative;
    _group = group;
    
    return self;
}

- (void)dealloc {
    [_model release];
    [super dealloc];
}

- (void)setRelative:(BOOL)relative {
    if (_relative != relative) {
        [[[_group undoManager] prepareWithInvocationTarget:self] setRelative:_relative];
        [[_group undoManager] setActionName:((relative != [[_group undoManager] isUndoing]) ? NSLocalizedString(@"Set Relative Path", @"Undo-redo action name") : NSLocalizedString(@"Set Absolute Path", @"Undo-redo action name"))];
        _relative = relative;
    }
}

- (BOOL)relative {
    return _relative;
}

@end

@implementation RMModelGroup

- (id)init {
    self = [super init];
    if (self != nil) {
        _modelContainers = [[NSMutableArray alloc] init];
        _rules = [[NSMutableArray alloc] init];
    }
    return self;
}

- (void)dealloc {
    while([[self modelContainers] count] > 0){
        [self removeObjectFromModelContainersAtIndex:0]; // Necessary, to ensure controllers will unregister as observers
    }
    [_rules release];
    [_modelContainers release];
    [_futureURL release];
    [super dealloc];
}

- (id)makeNewWindowController {
    RMModelGroupEditor *editor = [[RMModelGroupEditor alloc] init];
    
    [self addWindowController:editor];
    [editor release];
    
    return editor;
}

- (void)makeWindowControllers {
    [self makeNewWindowController];
}

- (BOOL)saveToURL:(NSURL *)absoluteURL ofType:(NSString *)typeName forSaveOperation:(NSSaveOperationType)saveOperation error:(NSError **)outError {
    // Overloading that method allows us to get the path at which the group 
    // document will be saved; we need that information in dataOfType:error:,
    // and fileURL has not yet been updated at the time that method is invoked.
    [_futureURL release];
    _futureURL = [absoluteURL retain];
    return [super saveToURL:absoluteURL ofType:typeName forSaveOperation:saveOperation error:outError];
}

- (NSData *)dataOfType:(NSString *)typeName error:(NSError **)outError {
    if ([typeName isEqualToString:RMModelGroupType]) {
        NSArray         *relatives = [_modelContainers valueForKey:@"relative"];
        NSMutableArray  *savedPaths = [[_modelContainers valueForKeyPath:@"model.fileURL.path"] mutableCopy];
        int             i, aCount = [savedPaths count];
        NSString        *myPath = [_futureURL path];
        
        for (i = 0; i < aCount; i++) {
            if ([[relatives objectAtIndex:i] boolValue]) {
                [savedPaths replaceObjectAtIndex:i withObject:[[savedPaths objectAtIndex:i] pathRelativeToPath:myPath]];
            }
        }
        
        NSString    *anErrorMessage;
        NSData      *data = [NSPropertyListSerialization dataFromPropertyList:[NSDictionary dictionaryWithObject:savedPaths forKey:@"modelPaths"] format:NSPropertyListXMLFormat_v1_0 errorDescription:&anErrorMessage];
        
        [savedPaths release];
        if (!data) {
            if (outError) {
                *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:anErrorMessage forKey:NSLocalizedDescriptionKey]];
            }
        }
        
        return data;
    }
    else{
        if (outError) {
            *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:NSLocalizedString(@"Unknown file type", @"Error message") forKey:NSLocalizedDescriptionKey]];
        }
    }
    
    return nil;
}

- (IBAction)saveDocument:(id)sender {
    // Fixes bug where if you edit a field and choose to save file, your modification is not taken in account,
    // if you didn't leave the field.
    if ([[NSApp mainWindow] makeFirstResponder:[NSApp mainWindow]]) {
        /* All fields are now valid; it’s safe to use fieldEditor:forObject:
        to claim the field editor. */
    }
    else {
        /* Force first responder to resign. */
        [[NSApp mainWindow] endEditingFor:nil];
    }
    [super saveDocument:sender];
}

- (NSArray *)modelContainers {
    return _modelContainers;
}

- (void)setModelContainers:(NSArray *)modelContainers {
    [[[self undoManager] prepareWithInvocationTarget:self] setModelContainers:[NSArray arrayWithArray:_modelContainers]];
    [_modelContainers setArray:modelContainers];
}

- (BOOL)readFromData:(NSData *)data ofType:(NSString *)typeName error:(NSError **)outError {
    if ([typeName isEqualToString:RMModelGroupType]) {
        NSString        *anErrorMessage = nil;
        NSDictionary    *aDict = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListImmutable format:NULL errorDescription:&anErrorMessage];
        
        if(anErrorMessage){
            if (outError) {
                *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:anErrorMessage forKey:NSLocalizedDescriptionKey]];
            }
            
            return NO;
        }

        NSEnumerator    *pathEnum = [[aDict objectForKey:@"modelPaths"] objectEnumerator];
        NSString        *aPath;
        NSString        *basePath = [[[self fileURL] path] stringByDeletingLastPathComponent];
        BOOL            returnValue;
        
        [[self undoManager] disableUndoRegistration];
        [self setModelContainers:[NSArray array]];
        while (aPath = [pathEnum nextObject]) {
            BOOL    isRelativePath = ![aPath isAbsolutePath];
            
            if (isRelativePath) {
                aPath = [basePath stringByAppendingPathComponent:aPath];
            }
            
            NSError *docError;
            RMModel *aModel = [self addModelWithURL:[NSURL fileURLWithPath:aPath] relativePath:isRelativePath error:&docError];
            
            if (!aModel) {
                // TODO Handle nil -> ask user where's model; maybe delayed - currently, model is silently removed from group
            }
        }
        [[self undoManager] enableUndoRegistration];
        
        returnValue = (aDict != nil);
        
        if (returnValue) {
            [[self windowControllers] makeObjectsPerformSelector:@selector(unfocus:) withObject:self]; // Reset focus (else could contain invalid rules - though we might clear them out)
        }
        
        return returnValue;
    }
    else{
        *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:NSLocalizedString(@"Unknown file type", @"Error message") forKey:NSLocalizedDescriptionKey]];
    }
    
    return NO;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    NSArray         *removedRules = [change objectForKey:NSKeyValueChangeOldKey];
    NSArray         *addedRules = [change objectForKey:NSKeyValueChangeNewKey];
//    NSMutableArray  *rules = [self mutableArrayValueForKey:@"rules"]; // We may not modify the _rules directly, else observers won't notice modifications - see Cocoa Bindings / Troubleshooting Cocoa Bindings

    if (removedRules) {
        // Batch removal
        NSEnumerator        *ruleEnum = [_rules objectEnumerator];
        Rule                *eachRule;
        NSMutableIndexSet   *removedRulesIndexSet = [[NSMutableIndexSet alloc] init];

        while (eachRule = [ruleEnum nextObject]) {
            if ([removedRules containsObject:eachRule]) {
                [removedRulesIndexSet addIndex:[_rules indexOfObjectIdenticalTo:eachRule]];
            }
        }
        [self willChange:NSKeyValueChangeRemoval valuesAtIndexes:removedRulesIndexSet forKey:@"rules"];
        [_rules removeObjectsAtIndexes:removedRulesIndexSet];
        [self didChange:NSKeyValueChangeRemoval valuesAtIndexes:removedRulesIndexSet forKey:@"rules"];
        [removedRulesIndexSet release];
    }
    
    if (addedRules) {
        // Batch addition
        NSIndexSet   *addedRulesIndexSet = [[NSIndexSet alloc] initWithIndexesInRange:NSMakeRange([_rules count], [addedRules count])];
        
        [self willChange:NSKeyValueChangeInsertion valuesAtIndexes:addedRulesIndexSet forKey:@"rules"];
        [_rules insertObjects:addedRules atIndexes:addedRulesIndexSet];
        [self didChange:NSKeyValueChangeInsertion valuesAtIndexes:addedRulesIndexSet forKey:@"rules"];
        [addedRulesIndexSet release];
    }
}

- (void)insertObject:(id)modelContainer inModelContainersAtIndex:(unsigned)index {
    RMModel     *aModel = [modelContainer valueForKey:@"model"];
    unsigned    oldRuleCount = [_rules count];
    unsigned    newRulesCount = [[aModel rules] count];
    NSIndexSet  *newRulesIndexSet = [[NSIndexSet alloc] initWithIndexesInRange:NSMakeRange(oldRuleCount, newRulesCount)];
    
    [_modelContainers insertObject:modelContainer atIndex:index];
    // Performance optimization: we no longer modify the mutableArrayValueForKey:@"rules" result,
    // because it will add objects one-by-one, thus triggering modification observation in all observers
    // E.g. the tableView will reorder its content after each inserted object!
    // By performing manual KVO notifications, we can work in batch: observers are notified only once
    [self willChange:NSKeyValueChangeInsertion valuesAtIndexes:newRulesIndexSet forKey:@"rules"];
    [_rules addObjectsFromArray:[aModel rules]];
    [self didChange:NSKeyValueChangeInsertion valuesAtIndexes:newRulesIndexSet forKey:@"rules"];
    [aModel addObserver:self forKeyPath:@"rules" options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld) context:NULL];
    [newRulesIndexSet release];
}

- (RMModel *)addModelWithURL:(NSURL *)url relativePath:(BOOL)relativePath error:(NSError **)outError {
    RMDocumentController    *dc = [NSDocumentController sharedDocumentController];
    RMModel                 *aModel;
    
    [dc setDontAddRecentDocument:YES];
    aModel = [dc openDocumentWithContentsOfURL:url display:NO error:outError];
    [dc setDontAddRecentDocument:NO];
    
    if (aModel != nil && ![[_modelContainers valueForKey:@"model"] containsObject:aModel]) {
        [[[self undoManager] prepareWithInvocationTarget:self] removeModelWithURL:url relativePath:relativePath error:NULL];
        [self insertObject:[[[RMModelGroupContainer alloc] initWithModel:aModel relative:relativePath group:self] autorelease] inModelContainersAtIndex:[_modelContainers count]];
    }

    return aModel;
}

- (void)addModelsWithURLs:(NSArray *)urls relativePaths:(BOOL)relativePaths error:(NSError **)outError {
    NSEnumerator    *urlEnum = [urls objectEnumerator];
    NSURL           *aURL;
    
    while (aURL = [urlEnum nextObject]) {
        NSError *anError = nil;
        
        [self addModelWithURL:aURL relativePath:relativePaths error:&anError];
        if (anError && outError) {
            if (*outError) {
                // Coalesce errors
            } else {
                *outError = anError;
            }
        }
        // TODO Handle error -> ask user where's model; maybe delayed - use NSError recovery; currently, we silently ignore errors and remove models from group
    }
    [[self undoManager] setActionName:NSLocalizedString(@"Add Models", @"Undo-redo action name")]; // We can't say for sure how many models we added (due to potential removed duplicates, or unfound models)
}

- (NSArray *)rules
{
    return _rules;
}

- (unsigned)countOfRules
{
    return [_rules count];
}

- (Rule *)objectInRulesAtIndex:(unsigned)theIndex
{
    return [_rules objectAtIndex:theIndex];
}

- (void)getRules:(Rule **)objsPtr range:(NSRange)range {
    [_rules getObjects:objsPtr range:range];
}

- (void)insertObject:(Rule *)obj inRulesAtIndex:(unsigned)theIndex
{
    [_rules insertObject:obj atIndex:theIndex];
}

- (void)removeObjectFromRulesAtIndex:(unsigned)theIndex
{
    [_rules removeObjectAtIndex:theIndex];
}

- (void)replaceObjectInRulesAtIndex:(unsigned)theIndex withObject:(Rule *)obj
{
    [_rules replaceObjectAtIndex:theIndex withObject:obj];
}

- (void)removeObjectFromModelContainersAtIndex:(unsigned)index {
    id                  aModelContainer = [_modelContainers objectAtIndex:index];
    RMModel             *aModel = [aModelContainer valueForKey:@"model"];
    NSEnumerator        *ruleEnum = [_rules objectEnumerator];
    Rule                *eachRule;
    NSMutableIndexSet   *removedRulesIndexSet = [[NSMutableIndexSet alloc] init];
    
    [aModel removeObserver:self forKeyPath:@"rules"];
    // Performance optimization: we no longer modify the mutableArrayValueForKey:@"rules" result,
    // because it will remove objects one-by-one, thus triggering modification observation in all observers
    // E.g. the tableView will reorder its content after each deletion!
    // By performing manual KVO notifications, we can work in batch: observers are notified only once
    while (eachRule = [ruleEnum nextObject]) {
        if ([eachRule model] == aModel) {
            [removedRulesIndexSet addIndex:[_rules indexOfObjectIdenticalTo:eachRule]]; // We can't remove them in batch, because there might be duplicate rules in other models; we can't use isEqual:.
        }
    }
    [self willChange:NSKeyValueChangeRemoval valuesAtIndexes:removedRulesIndexSet forKey:@"rules"];
    [_rules removeObjectsAtIndexes:removedRulesIndexSet];
    [self didChange:NSKeyValueChangeRemoval valuesAtIndexes:removedRulesIndexSet forKey:@"rules"];
    [_modelContainers removeObjectAtIndex:index];
    [removedRulesIndexSet release];
}

- (void)removeModelWithURL:(NSURL *)url relativePath:(BOOL)relativePath error:(NSError **)outError {
    int anIndex;
    
    for (anIndex = [_modelContainers count] - 1; anIndex >= 0; anIndex--) {
        RMModel *eachModel = [[_modelContainers objectAtIndex:anIndex] valueForKey:@"model"];
        
        if ([[eachModel fileURL] isEqual:url]) {
            break;
        }
    }
    if (anIndex >= 0) {
        [[[self undoManager] prepareWithInvocationTarget:self] addModelWithURL:url relativePath:relativePath error:NULL]; // FIXME error:NULL - currently we silently remove models we can't reload
        [self removeObjectFromModelContainersAtIndex:anIndex];
    }
}

- (void)removeModels:(NSArray *)models {
    NSEnumerator    *modelEnum = [models objectEnumerator];
    RMModel         *aModel;
    
    while (aModel = [modelEnum nextObject]) {
        [self removeModelWithURL:[aModel fileURL] relativePath:YES error:NULL];
    }
    [[self undoManager] setActionName:[NSString stringWithFormat:([models count] > 1 ? NSLocalizedString(@"Remove %i Model(s)", @"Undo-redo action name") : NSLocalizedString(@"Remove %i Model", @"Undo-redo action name")), [models count]]];
}

- (void)close {
    // When closing group document, we also close referenced model documents,
    // except when model documents are either open, or referenced in other
    // group documents.
    RMDocumentController    *dc = [NSDocumentController sharedDocumentController];
    NSEnumerator            *docEnum = [[dc documents] objectEnumerator];
    id                      eachDoc;
    NSMutableArray          *modelsToClose = [NSMutableArray arrayWithArray:[self valueForKeyPath:@"modelContainers.model"]];
    
    while (eachDoc = [docEnum nextObject]) {
        if (eachDoc != self) {
            if ([eachDoc isKindOfClass:[RMModelGroup class]]) { // Don't close models referenced by other model groups
                [modelsToClose removeObjectsInArray:[eachDoc valueForKeyPath:@"modelContainers.model"]];
            } else if ([eachDoc isKindOfClass:[RMModel class]]) { // Don't close an opened model, i.e. a model which has a GUI
                if ([[eachDoc windowControllers] count]) {
                    [modelsToClose removeObject:eachDoc];
                }
            }
        }
    }
    
    [super close];

    // The following code allows us to avoid adding models to recent list,
    // when models where only referenced in group document.
    [dc setDontAddRecentDocument:YES];
    [modelsToClose makeObjectsPerformSelector:@selector(close)];
    [dc setDontAddRecentDocument:NO];
}

@end

@implementation RMModel (RMModelGroupSupport)

- (void)showWindows {
    // A model document could have been opened without UI, when referenced
    // in model group only. This overload ensures that controller has been
    // created before asking for window.
    if ([[self windowControllers] count] == 0)
        [self makeWindowControllers];
    [super showWindows];
}

- (BOOL)isReferencedInSomeModelGroup {
    NSEnumerator    *docEnum = [[[NSDocumentController sharedDocumentController] documents] objectEnumerator];
    id              eachDoc;
    
    while (eachDoc = [docEnum nextObject]) {
        if ([eachDoc isKindOfClass:[RMModelGroup class]]) {
            if ([[eachDoc valueForKeyPath:@"modelContainers.model"] containsObject:self]) {
                return YES;
            }
        }
    }
    
    return NO;
}

- (void)close {
    // We don't want that model documents which are still referenced by model group documents
    // be really closed. Closing it would remove it from documentController's list.
    if (![self isReferencedInSomeModelGroup]) {
        [super close];
    }
}

@end
