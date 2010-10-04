/*
 RMModel.m
 RuleModeler

 Created by King Chung Huang on 1/29/04.


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

#import "RMModel.h"
#import "RMModelEditor.h"
#import "Rule.h"
#import "Assignment.h"
#import "NSPlistDescriptions.h"

#import "EOControl.h"

#import "NSStringAdditions.h"

static NSString *ruleModelType = @"Apple D2WModel File";

@interface NSArray(RuleModelerDescription)
- (NSString *)_rm_descriptionWithLocale:(id)locale indent:(NSUInteger)level;
- (NSString *)_rm_description;
@end

@implementation RMModel

- (id)init {
    if (self = [super init]) {
		[self setRules:[NSArray array]];
    }
    
    return self;
}

- (void)dealloc {
    [_rules release];
    [super dealloc];
}

- (id)makeNewWindowController {
    RMModelEditor *editor = [[RMModelEditor alloc] init];
    
    [self addWindowController:editor];
    [editor release];
    
    return editor;
}

- (void)makeWindowControllers {
    [self makeNewWindowController];
}

static NSArray * _sortDescriptors = nil;

+ (NSArray *) sortDescriptors {
	if (_sortDescriptors == nil) {
        // We need to define criteria which always returns the rules in the same order
        // We choose to base our order on priority, then description length, etc.
        if (![[NSUserDefaults standardUserDefaults] boolForKey:@"useRuleEditorRuleOrdering"]) {
            NSSortDescriptor    *descriptor;
            NSMutableArray      *sortDescriptors = [[NSMutableArray alloc] initWithCapacity:5];
            
            descriptor = [[NSSortDescriptor alloc] initWithKey:@"author" ascending:YES];
            [sortDescriptors addObject:descriptor];
            [descriptor release];
            descriptor = [[NSSortDescriptor alloc] initWithKey:@"description.length" ascending:YES];
            [sortDescriptors addObject:descriptor];
            [descriptor release];
            descriptor = [[NSSortDescriptor alloc] initWithKey:@"lhsDescription.length" ascending:YES];
            [sortDescriptors addObject:descriptor];
            [descriptor release];
            descriptor = [[NSSortDescriptor alloc] initWithKey:@"lhsDescription" ascending:YES];
            [sortDescriptors addObject:descriptor];
            [descriptor release];
            descriptor = [[NSSortDescriptor alloc] initWithKey:@"rhs.keyPath" ascending:YES];
            [sortDescriptors addObject:descriptor];
            [descriptor release];
            _sortDescriptors = sortDescriptors;
        }
        else{
            // dscheck: Change the sorting to match what was in the old RuleEditor.
            NSSortDescriptor *rhsKeyPathDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"rhs.keyPath" ascending:YES] autorelease];
            NSSortDescriptor *authorDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"author" ascending:NO] autorelease];
            NSSortDescriptor *hasLHSDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"hasLHS" ascending:NO] autorelease];
            NSSortDescriptor *lhsDescriptionDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"lhsDescription" ascending:YES] autorelease];

            _sortDescriptors = [[NSArray arrayWithObjects:rhsKeyPathDescriptor, authorDescriptor, hasLHSDescriptor, lhsDescriptionDescriptor, nil] retain];
        }
	}
    
	return _sortDescriptors;
}

+ (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if ([keyPath isEqualToString:@"values.useRuleEditorRuleOrdering"]) {
        [_sortDescriptors release];
        _sortDescriptors = nil;
    }
}

- (NSData *)dataOfType:(NSString *)typeName error:(NSError **)outError {
    if ([typeName isEqualToString:ruleModelType]) {
        
        EOKeyValueArchiver *archiver = [[EOKeyValueArchiver alloc] init];
        NSMutableArray *rules = [[self rules] mutableCopy];
        
		// dscheck: call the sortDescriptor method
        [rules sortUsingDescriptors:[RMModel sortDescriptors]];                  
            
        [archiver encodeObject:rules forKey:@"rules"];
        
        NSDictionary *plist = [archiver dictionary];
        NSString *errorDesc = nil;
        BOOL prettyPrint = ![[NSUserDefaults standardUserDefaults] boolForKey:@"saveRulesInSingleRows"];
        NSData *data = [[NSPropertyListSerialization openStepFormatStringFromPropertyList:plist level:(prettyPrint ? INT_MAX:2) escapeNonASCII:YES errorDescription:&errorDesc] dataUsingEncoding:NSUTF8StringEncoding];
        
        if (errorDesc) {
            NSLog(@"%@", errorDesc);
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:errorDesc forKey:NSLocalizedDescriptionKey]];
        }
        
        [archiver release];
        [rules release];
        
        return data;
    }
    else{
        *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:NSLocalizedString(@"Unknown file type", @"Error message") forKey:NSLocalizedDescriptionKey]];
    }
    
    return nil;
}
- (BOOL)saveToURL:(NSURL *)absoluteURL ofType:(NSString *)typeName forSaveOperation:(NSSaveOperationType)saveOperation error:(NSError **)outError {
    BOOL result = [super saveToURL:absoluteURL ofType:typeName forSaveOperation:saveOperation error:outError];
    if(result) {
        NSMutableArray *rules = [[[self rules] mutableCopy] autorelease];
        
        [rules sortUsingDescriptors:[RMModel sortDescriptors]];
        NSError *errorDesc = nil;
        NSString *description = [rules _rm_description];
        NSURL *url = absoluteURL;
        url = (url == nil ? [self fileURL] : url);
        if(url != nil) {
			NSString *urlPath = [url path];
			NSString *wolipsPassword = [[NSUserDefaults standardUserDefaults] stringForKey:@"wolipsPassword"];
			if (wolipsPassword != nil) {
				int wolipsPort = [[NSUserDefaults standardUserDefaults] integerForKey:@"wolipsPort"];
				if (wolipsPort == 0) {
					wolipsPort = 9485;
				}
				
				NSString *wolipsUrlStr = [NSString stringWithFormat:@"http://localhost:%d/refresh?pw=%@&path=%@", wolipsPort, [wolipsPassword encodePercentEscapes], [urlPath encodePercentEscapes]];
				NSURL *wolipsUrl = [NSURL URLWithString:wolipsUrlStr];
				[wolipsUrl resourceDataUsingCache:NO];
			}
										
            NSString *path = [urlPath stringByAppendingPathExtension:@"txt"];
            result = [description writeToFile:path atomically:YES encoding:NSUTF8StringEncoding error:outError];
            if(!result) {
                NSLog(@"Save failed: %@ %@", path, outError);
            }
        }
    }
    return result;
}

- (BOOL)readFromData:(NSData *)data ofType:(NSString *)typeName error:(NSError **)outError {
    if([typeName isEqualToString:ruleModelType]){
        NSString        *error = nil;
        NSDictionary    *plist = [NSPropertyListSerialization propertyListFromData:data mutabilityOption:NSPropertyListMutableContainersAndLeaves format:NULL errorDescription:&error];
        
        if(error){
            if (outError) {
                *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:error forKey:NSLocalizedDescriptionKey]];
            }
            
            return NO;
        }
        
        [[self undoManager] disableUndoRegistration];
        NS_DURING
            [self setRules:[Rule rulesFromMutablePropertyList:plist]];
        NS_HANDLER
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:[localException reason] forKey:NSLocalizedDescriptionKey]];
            return NO;
        NS_ENDHANDLER
        
		[[self undoManager] enableUndoRegistration];
        if([self rules] == nil) {
            *outError = [NSError errorWithDomain:@"RuleModeler" code:0 userInfo:[NSDictionary dictionaryWithObject:NSLocalizedString(@"No 'rules' key-value pair", @"Error message") forKey:NSLocalizedDescriptionKey]];
            return NO;
        }

        [[self windowControllers] makeObjectsPerformSelector:@selector(unfocus:) withObject:self]; // Reset focus (else could contain invalid rules - though we might clear them out)
        
        return YES;
    }
    else{
        *outError = [NSError errorWithDomain:@"RuleModeler" code:NSFileReadInvalidFileNameError userInfo:[NSDictionary dictionaryWithObject:NSLocalizedString(@"Unknown file type", @"Error message") forKey:NSLocalizedDescriptionKey]];
    }
    
    return NO;
}

- (IBAction)saveDocument:(id)sender {
    // Fixes bug where if you edit a field and choose to save file, your modification is not taken in account,
    // if you didn't leave the field.
    if ([[NSApp mainWindow] makeFirstResponder:[NSApp mainWindow]]) {
        /* All fields are now valid; it's safe to use fieldEditor:forObject:
        to claim the field editor. */
        [super saveDocument:sender];
    }
    // FIXME Do the same to handle a Quit event (from menu, or from system shutdown) - see NSApp delegate method -applicationShouldTerminate:
}

- (NSArray *)rules {
    return _rules;
}

- (void)setRules:(NSArray *)newRules {
    [self willChangeValueForKey:@"rules"];
	[_rules makeObjectsPerformSelector:@selector(setModel:) withObject:nil];
	[_rules autorelease];
	
	_rules = [newRules mutableCopy];
	[_rules makeObjectsPerformSelector:@selector(setModel:) withObject:self];
    [self didChangeValueForKey:@"rules"];
}

- (unsigned)countOfRules {
    return [_rules count];
}

- (id)objectInRulesAtIndex:(unsigned)theIndex {
    return [_rules objectAtIndex:theIndex];
}

- (void)getRules:(id *)objsPtr range:(NSRange)range {
    [_rules getObjects:objsPtr range:range];
}

- (void)insertObject:(id)obj inRulesAtIndex:(unsigned)theIndex {
    [_rules insertObject:obj atIndex:theIndex];
	[obj setModel:self];
}
/*
- (void)updateChangeCount:(NSDocumentChangeType)changeType {
    NSLog(@"updateChangeCount:%d", changeType);
    [super updateChangeCount:changeType];
}
*/
- (void)removeObjectFromRulesAtIndex:(unsigned)theIndex {
    [_rules removeObjectAtIndex:theIndex];
}

- (void)replaceObjectInRulesAtIndex:(unsigned)theIndex withObject:(id)obj {
    [_rules replaceObjectAtIndex:theIndex withObject:obj];
	[obj setModel:self];
}

- (void)insertRules:(NSArray *)rules atIndexes:(NSIndexSet *)indexes {
    // This is a KVC-compliant method
    [_rules insertObjects:rules atIndexes:indexes];
	[_rules makeObjectsPerformSelector:@selector(setModel:) withObject:self];
}

- (void)removeRulesAtIndexes:(NSIndexSet *)indexes { 
    // This is a KVC-compliant method
    [_rules removeObjectsAtIndexes:indexes];
}

- (NSString *)description {
    return [[self rules] description];
}

+ (void)initialize {
    // Do not call super - see +initialize documentation
    [self setKeys:[NSArray arrayWithObject:@"fileURL"] triggerChangeNotificationsForDependentKey:@"displayName"];
    [[NSUserDefaultsController sharedUserDefaultsController] addObserver:self forKeyPath:@"values.useRuleEditorRuleOrdering" options:0 context:NULL];
}

- (NSString *)displayName {
    // Returns filename (except if "d2w.d2wmodel") and a path component: 
    // the last path component which is neither Resources, nor Contents, 
    // nor Versions, nor in a Versions/A folder.
    // That's because normally all D2W models are named d2w.d2wmodel, and in 
    // Wonder frameworks/apps the are located in a Resources folder, and when
    // installed they are located in a (Contents/)Resources or Versions/*/Resources folder.
    if ([self fileURL]) {
        static NSArray  *ignoredPathComponentNames = nil;
        
        if (ignoredPathComponentNames == nil) {
            ignoredPathComponentNames = [[NSArray alloc] initWithObjects:@"Resources", @"Contents", @"Versions", @"A", nil];
        }
        NSString    *aPath = [[self fileURL] path];
        NSString    *aName = [aPath lastPathComponent];
        NSString    *aPathComponent;
        
        if ([[aName pathExtension] isEqualToString:@"d2wmodel"]) {
            aName = [aName stringByDeletingPathExtension];
        }
        do {
            aPath = [aPath stringByDeletingLastPathComponent];
            aPathComponent = [aPath lastPathComponent];
        } while ([ignoredPathComponentNames containsObject:aPathComponent]);
        
        if ([aName isEqualToString:@"d2w"]) {
            return aPathComponent;
        } else {
            return [NSString stringWithFormat:@"%@ - %@", aName, aPathComponent];
        }
    }
    return [super displayName];
}

- (void)setDisplayName:(NSString *)ignored {
    // This method is implemented just to avoid KVO warning due to model list tableView
}

@end

@implementation NSArray(RuleModelerDescription)

- (NSString *)_rm_descriptionWithLocale:(id)locale indent:(NSUInteger)level
{
    NSMutableArray *descriptionArray = [NSMutableArray arrayWithCapacity:[self count]];
    for(id obj in self) {
        NSString *description;
        if([obj isKindOfClass:[NSString class]]) {
            // HACK: This is WRONG.  Even before Snow Leopard the value would be quoted if needed.
            // Fortunately this code path won't be reached unless we start replacing more of the
            // plist generation (descriptionWithLocale:indent:) code.
            // Unfortunately none of the methods to properly quote a string for a plist are
            // public.  Bummer huh?
            description = obj;
        }
        else if([obj isKindOfClass:[NSArray class]] || [obj isKindOfClass:[NSDictionary class]]) {
            // This is basically right although this code path itself won't be reached.
            if([obj respondsToSelector:@selector(_rm_descriptionWithLocale:indent:)])
                description = [obj _rm_descriptionWithLocale:locale indent:level+1];
            else
                description = [obj descriptionWithLocale:locale indent:level+1];
        }
        else if([obj isKindOfClass:[NSData class]]) {
            description = [obj description];
        }
        else {
            // This is the critcial difference between our description and the Snow Leopard description.
            // On SL the string is quoted if it contains anything which should be quoted.  I believe
            // the change was made to guarantee the output could always be read back in as a plist
            // but when you are describing non-primitive objects (such as in our case a Rule object)
            // there is some argument to be made that the pre-Snow behavior of allowing the object
            // to write its own format, without extra quoting, was a better API.
            description = [obj description];
        }
        [descriptionArray addObject:description];
    }
    
    NSMutableString *indentString = [NSMutableString stringWithCapacity:4 * level];
    NSUInteger levelI;
    for(levelI = 0 ; levelI < level; ++levelI)
        [indentString appendString:@"    "];
    
    NSMutableString *result = [NSMutableString string];
    [result appendString:indentString];
    [result appendString:@"(\n"];
    
    NSUInteger i = 0;
    NSUInteger count = [self count];
    for(NSString *description in descriptionArray) {
        [result appendString:indentString];
        [result appendString:@"    "];
        [result appendString:description];
        
        // If we are outputting something other than the last item we need a comma.
        if(i < count - 1)
            [result appendString:@",\n"];
        else
            [result appendString:@"\n"];

        ++i;
    }
    [result appendString:indentString];
    [result appendString:@")"];
    return result;
}

- (NSString *)_rm_description
{
    return [self _rm_descriptionWithLocale:nil indent:0];
}

@end
