//
//  AppleScriptItem.m
//  CMMJavaMenu
//
//  Created by Anjo Krank on Mon Mar 25 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//

#import "AppleScriptItem.h"
#import <Foundation/NSString.h>


@interface AppleScriptItem(Private)
- (void)refresh;
- (BOOL)compile;
- (NSString *)execute;
- (void)prepare:(NSString *)selector context:(NSString *)context;
@end

@interface NSString(OSAAdditions)
+ (NSString *)stringWithAEDesc:(AEDesc *)desc;
@end

@implementation NSString(OSAAdditions)
+ (NSString *)stringWithAEDesc:(AEDesc *)desc {
    NSString *result = nil;
    long size = AEGetDescDataSize(desc);
    
    if(desc->descriptorType != typeNull) {
	Ptr bytes = NewPtr(size+1);
	OSErr err1 = AEGetDescData(desc, bytes, size);
	bytes[size] = 0;
	if(err1 == noErr && strcmp(bytes, "null") != 0) {
	    result = [NSString stringWithCString:bytes];
	}
	if(bytes != NULL)
	    DisposePtr(bytes);
    }
    return result;
}
@end


@implementation AppleScriptItem
+ (AppleScriptItem *)scriptItemWithPath:(NSString *)scriptPath action:(SEL)aSelector {
    return [[[AppleScriptItem alloc] initScriptItemWithPath:scriptPath action:aSelector] autorelease];
}

- (AppleScriptItem *)initScriptItemWithPath:(NSString *)scriptPath action:(SEL)aSelector {
    self = [super initWithTitle:@"NONE" action:aSelector keyEquivalent:@""];

    theComponent = NULL;
    contextID = kOSANullScript;
    path = [scriptPath copy];

    [self refresh];
    return self;
}

- (void)refresh {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSDate *currentDate = [[fileManager fileAttributesAtPath:path traverseLink:NO] fileModificationDate];
    if(lastDate == nil || [lastDate compare:currentDate] != NSOrderedSame) {
	[scriptText release];
	scriptText = [[NSString alloc] initWithContentsOfFile:path];

	if(![self compile])
	    NSLog( @"Script error");
	else
	    NSLog( @"Compile OK");

	[lastDate release];
	lastDate = [currentDate copy];
    }
}

- (void)dealloc {
    if (contextID != kOSANullScript) OSADispose(theComponent, contextID);
    if (theComponent != NULL) CloseComponent(theComponent);
    [scriptText release];
    [path release];
    NSLog(@"release of AppleScriptItem");
    [super dealloc];
}

- (BOOL)shouldShowWithString:(NSString *)selection {
    NSString *label;
    [self prepare:@"createmenu" context:selection];
    label = [self execute];
    if(label != nil) {
	[self setTitle:label];
	return YES;
    }
    return NO;
}

- (NSString *)invokeWithString:(NSString *)selection {
    [self prepare:@"handlemenu" context:selection];
    return [self execute];
}

- (void) reportErrorWithTitle:(NSString *)title {
    AEDesc resultData;
    NSString *result = nil;

    AECreateDesc(typeNull, NULL, 0, &resultData);
    OSAScriptError(theComponent, kOSAErrorMessage, typeChar, &resultData);
    result = [NSString stringWithAEDesc:&resultData];
    AEDisposeDesc(&resultData);
    NSRunAlertPanel(title, result, @"OK", nil, nil, nil);
}

- (BOOL)compile {
    AEDesc scriptTextDesc;
    OSStatus err;

    if (contextID != kOSANullScript) OSADispose(theComponent, contextID);
    contextID = kOSANullScript;
    /* set up locals to a known state */
    AECreateDesc(typeNull, NULL, 0, &scriptTextDesc);

    /* open the scripting component */
    theComponent = OpenDefaultComponent(kOSAComponentType, typeAppleScript);
    if (theComponent != NULL) {
	err = AECreateDesc(typeChar, [scriptText cString], [scriptText length], &scriptTextDesc);
	if (err == noErr) {
	    err = OSACompile(theComponent, &scriptTextDesc,  kOSAModeCompileIntoContext, &contextID);
	    AEDisposeDesc(&scriptTextDesc);
	    if (err == errOSAScriptError) {
		[self reportErrorWithTitle:@"Script Compilation error"];
	    }
	}
    } else {
	err = paramErr;
    }

    return (err == noErr);
}

- (NSString *)execute {
    AEDesc resultData;
    OSStatus err;
    NSString *result = nil;
    OSAID resultID = kOSANullScript;

    [self refresh];

    AECreateDesc(typeNull, NULL, 0, &resultData);

    err = OSAExecuteEvent( theComponent, &theEvent,  contextID, kOSAModeNull, &resultID);
    if (err == errOSAScriptError) {
	[self reportErrorWithTitle:@"Execution error"];
    } else if (err == noErr && resultID != kOSANullScript) {
	//  OSAGetSource(theComponent, resultID, kOSAScriptBestType, &resultData);
	OSADisplay(theComponent, resultID, typeChar, kOSAModeDisplayForHumans, &resultData);
	result = [NSString stringWithAEDesc:&resultData];
    }

    AEDisposeDesc(&resultData);

    if (resultID != kOSANullScript) OSADispose(theComponent, resultID);

    return result;
}

- (void)prepare:(NSString *)selector context:(NSString *)context {
    OSStatus err;
    ProcessSerialNumber PSN;

    PSN.highLongOfPSN = 0;
    PSN.lowLongOfPSN = kCurrentProcess;

    /* create the container list */
    err = AEBuildAppleEvent('ascr', kASSubroutineEvent, typeProcessSerialNumber, (Ptr) &PSN, sizeof(PSN),
			    kAutoGenerateReturnID, kAnyTransactionID, &theEvent, NULL,
			    "'----':[TEXT(@)],"
			    "'snam':TEXT(@)", [context cString], [selector cString]);
    if(err != noErr) {
	NSLog(@"prepare: %d", err);
    }
}

@end
