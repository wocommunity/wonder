/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;
import org.apache.log4j.helpers.LogLog;

/////////////////////////////////////////////////////////////////////////////////////////////
//  Basic log4j EOF Appender
//	Very basic appender, useful for logging events to a database using EOF.			
//  Manditory Fields
//	LogEntity - Entity for creating logging events.  The class mapped to this entity must
//		implement the interface: ERXEOFLogEntryInterface
//  Optional Fields
//	BufferSize - Number of Events to catch before calling ec.saveChanges()
/////////////////////////////////////////////////////////////////////////////////////////////
public class ERXEOFAppender extends AppenderSkeleton {

    // FIXME:  Here we assume that the Log4j system is configured before the first CooperatingObjectStore
    //		is created.  We need a way to determine if it is ok to create an editing context and insert
    //		objects.
    //	Max: Should use EOCooperatingObjectStore.defaultCoordinator().cooperatingObjectStores().count()
    
    public ERXEOFAppender() {
        ERXRetainer.retain(this);
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("objectStoreWasAdded",
                                                                        ERXConstant.NotificationClassArray),
                                                         EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification,
                                                         null);
    }

    public boolean requiresLayout() { return false; }
    public synchronized void close() {
        if (!closed)
            closed = true;
    }

    protected String loggingEntity;
    public String getLoggingEntity() { return loggingEntity; }
    public void setLoggingEntity(String loggingEntity) { loggingEntity = loggingEntity; }
    
    protected EOEditingContext ec;
    protected boolean objectStoreWasAdded = false;
    public void objectStoreWasAdded(NSNotification n) {
        objectStoreWasAdded = true;
        ec = ERXExtensions.newEditingContext();
        NSNotificationCenter.defaultCenter().removeObserver(this);
        ERXRetainer.release(this);
    }

    protected int bufferSize = -1;
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0)
            LogLog.warn("BufferSize must be greater than 0!  Attempted to set bufferSize to: " + bufferSize);
        else
            bufferSize = bufferSize;
    }
    
    protected boolean conditionsChecked = false;
    protected boolean checkConditions() {
        if (getLoggingEntity() == null) {
            LogLog.warn("Attempting to log an event with a null LoggingEntity specified.");
        } else if (!objectStoreWasAdded) {
            LogLog.warn("Attempting to log an event to an EREOFAppender before an ObjectStoreCoordinator has been added.");
        } else {
            conditionsChecked = true;
        }
        return conditionsChecked;
    }

    // Reminder: the nesting of calls is:
    //
    //    doAppend()
    //      - check threshold
    //      - filter
    //      - append();
    //        - checkConditions();
    //        - subAppend();

    public void append(LoggingEvent event) {
        if (conditionsChecked || checkConditions()) {
            subAppend(event);
        } else {
            LogLog.warn("Unable to log event: " + event.getMessage());
        }
    }

    protected int currentBufferSize = 1;
    protected void subAppend(LoggingEvent event) {
        // Create Log Entry for event.
        ERXEOFLogEntryInterface logEntry = (ERXEOFLogEntryInterface)ERXUtilities.createEO(getLoggingEntity(), ec);
        // Note that layout is not required and can be null.
        logEntry.intializeWithLoggingEvent(event, layout);
        if (getBufferSize() == -1 || currentBufferSize == getBufferSize()) {
            ec.saveChanges();
        } else {
            currentBufferSize++;
        }
    }
}
