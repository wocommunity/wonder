/*
 * WOEventSetupPage.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class WOEventSetupPage extends WOEventPage {
    public Class	currentClass;
    public String	currentEventDescription;
    public int		currentIndex;

    protected static _ClassNameComparator _classNameAscendingComparator = new _ClassNameComparator(EOSortOrdering.CompareAscending);
    
    public WOEventSetupPage(WOContext aContext) {
        super(aContext);
    }

    public NSArray registeredEventClasses() {
        NSMutableArray	classes;


        classes = new NSMutableArray();
        classes.setArray(EOEventCenter.registeredEventClasses());
        
        try {
            classes.sortUsingComparator(_classNameAscendingComparator);
        } catch (NSComparator.ComparisonException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }

        return classes;
    }

    public boolean isClassRegisteredForLogging() {
         return EOEventCenter.recordsEventsForClass(currentClass);
    }

    public void setIsClassRegisteredForLogging(boolean yn) {
         EOEventCenter.setRecordsEvents(yn, currentClass);
    }

    protected void _setAllRegisteredEvents(boolean tf) {
        NSArray	registered;
        int i, n;
        Class c;

        registered = EOEventCenter.registeredEventClasses();
        int count = registered.count();
        for (i = 0, n = count; i < n; i++) {
            c = (Class)registered.objectAtIndex(i);
            EOEventCenter.setRecordsEvents(tf, c);
        }
    }
    
    public WOComponent selectAll() {
        _setAllRegisteredEvents(true);
        return null;
    }

    public WOComponent clearAll() {
        _setAllRegisteredEvents(false);
        return null;
    }
    
    public NSArray currentEventDescriptions() {
        NSMutableArray	descs;
        NSDictionary	map;

        map = EOEvent.eventTypeDescriptions(currentClass);

        descs = new NSMutableArray();
        descs.setArray(map.allValues());
        descs.removeObject(map.objectForKey(EOEvent.EventGroupName));
        try {
            descs.sortUsingComparator(NSComparator.AscendingStringComparator);
        } catch (NSComparator.ComparisonException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        descs.insertObjectAtIndex(map.objectForKey(EOEvent.EventGroupName), 0);

        return descs;
    }

    public boolean isClassName() {
        return (currentIndex == 0);
    }

}
