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
    
    public boolean requiresLayout() { return false; }
    public synchronized void close() {
        if (!closed)
            closed = true;
    }

    protected String loggingEntity;
    public String getLoggingEntity() { return loggingEntity; }
    public void setLoggingEntity(String loggingEntity) { loggingEntity = loggingEntity; }
    
    protected EOEditingContext ec;
    public void objectStoreWasAdded(NSNotification n) {
        ec = ERXExtensions.newEditingContext();
    }

    // ENHANCEME: Should also check if the application has fully started up
    protected boolean safeToCreateEditingContext() {
        return EOObjectStoreCoordinator.defaultCoordinator().cooperatingObjectStores().count() > 0;
    }

    protected EOEditingContext editingContext() {
        if (ec == null) {
            if (safeToCreateEditingContext()) {
                ec = ERXExtensions.newEditingContext();
            }
        }
        return ec;
    }
    
    protected int bufferSize = -1;
    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0)
            LogLog.warn("BufferSize must be greater than 0!  Attempted to set bufferSize to: " + bufferSize);
        else
            this.bufferSize = bufferSize;
    }
    
    protected boolean conditionsChecked = false;
    protected boolean checkConditions() {
        if (getLoggingEntity() == null) {
            LogLog.warn("Attempting to log an event with a null LoggingEntity specified.");
        } else if (!safeToCreateEditingContext()) {
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
        if (editingContext() != null) {
            ERXEOFLogEntryInterface logEntry = (ERXEOFLogEntryInterface)ERXUtilities.createEO(getLoggingEntity(),
                                                                                              editingContext());
            // Note that layout is not required and can be null.
            logEntry.intializeWithLoggingEvent(event, layout);
            if (getBufferSize() == -1 || currentBufferSize == getBufferSize()) {
                editingContext().saveChanges();
                // Clean out the ec.
                editingContext().revert();
                currentBufferSize = 1;
            } else {
                currentBufferSize++;
            }
        }
    }
}
