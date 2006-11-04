//
//  RMModelGroup.m
//  RuleModeler
//
//  Created by Dave Lopper on 8/14/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

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

- (void)makeWindowControllers {
    RMModelGroupEditor *editor = [[RMModelGroupEditor alloc] init];
    
    [self addWindowController:editor];
    [editor release];
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
        
        return (aDict != nil);
    }
    else{
        *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:NSLocalizedString(@"Unknown file type", @"Error message") forKey:NSLocalizedDescriptionKey]];
    }
    
    return NO;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    NSArray         *removedRules = [change objectForKey:NSKeyValueChangeOldKey];
    NSArray         *addedRules = [change objectForKey:NSKeyValueChangeNewKey];
    NSMutableArray  *rules = [self mutableArrayValueForKey:@"rules"]; // We may not modify the _rules directly, else observers won't notice modifications - see Cocoa Bindings / Troubleshooting Cocoa Bindings

    if (removedRules) {
        NSEnumerator    *ruleEnum = [removedRules objectEnumerator];
        id              eachRule;

        // We can't remove them in batch, because there might be duplicate rules; we can't use isEqual:.
        while (eachRule = [ruleEnum nextObject]) {
            [rules removeObjectIdenticalTo:eachRule];
        }        
    }
    
    if (addedRules) {
        [rules addObjectsFromArray:addedRules];    
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
    NSMutableArray          *docsToClose = [NSMutableArray arrayWithArray:[self valueForKeyPath:@"modelContainers.model"]];
    
    while (eachDoc = [docEnum nextObject]) {
        if (eachDoc != self) {
            if ([eachDoc isKindOfClass:[RMModelGroup class]]) {
                if ([[eachDoc valueForKeyPath:@"modelContainers.model"] containsObject:self]) {
                    [docsToClose removeObject:eachDoc];
                }
            } else if ([eachDoc isKindOfClass:[RMModel class]]) {
                if ([[eachDoc windowControllers] count]) {
                    [docsToClose removeObject:eachDoc];
                }
            }
        }
    }
    
    [super close];

    // The following code allows us to avoid adding models to recent list,
    // when models where only referenced in group document.
    [dc setDontAddRecentDocument:YES];
    [docsToClose makeObjectsPerformSelector:@selector(close)];
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

- (void)close {
    // We don't want that model documents which are still referenced by model group documents
    // be really closed. Closing it would remove it from documentController's list.
//    NSLog(@"Should close %@?", [self fileName]);
    NSEnumerator    *docEnum = [[[NSDocumentController sharedDocumentController] documents] objectEnumerator];
    id              eachDoc;
    BOOL            doClose = YES;
    
    while (eachDoc = [docEnum nextObject]) {
        if ([eachDoc isKindOfClass:[RMModelGroup class]]) {
            if ([[eachDoc valueForKeyPath:@"modelContainers.model"] containsObject:self]) {
                doClose = NO;
                break;
            }
        }
    }
    
    if (doClose) {
//        NSLog(@"YES");
        [super close];
    } else {
//        NSLog(@"NO");
    }
}

@end
