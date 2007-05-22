/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;

/**
 * Basic log4j EOF Appender<br>
 *	Very basic appender, useful for logging events to a database using EOF.
 *  Manditory Fields:<br>
 *	LoggingEntity - Entity for creating logging events.  The class mapped to this entity must
 *		implement the interface: {@link ERXEOFLogEntryInterface}
 *  Optional Fields:<br>
 *	BufferSize - Number of Events to catch before calling ec.saveChanges()
 */
public class ERXEOFAppender extends AppenderSkeleton {

    /** holds the logging entity name */
    protected String loggingEntity;

    /** holds a reference to the logging editing context */
    protected EOEditingContext ec;

    /** holds the buffer size, defaults to -1 */
    protected int bufferSize = -1;

    /** holds the flag if all the conditions for logging have been checked */
    protected boolean conditionsChecked = false;

    /** holds the current buffer size, defaults to 1 */
    protected int currentBufferSize = 1;
    
    /**
     * The EOF Appender does not require a layout
     * @return false
     */
    public boolean requiresLayout() { return false; }

    /**
     * Called to close the appender.
     */
    public synchronized void close() {
        if (!closed)
            closed = true;
    }

    /**
     * Gets the logging entity name.
     * @return logging entity name.
     */
    public String getLoggingEntity() { return loggingEntity; }

    /**
     * Sets the logging entity name.
     * @param name name of the logging entity
     */
    public void setLoggingEntity(String name) { loggingEntity = name; }

    /**
     * Determines if enough of the EOF stack has been setup
     * that it is safe to create an editing context and log
     * events to the database.
     * @return if any cooperating object stores have been created
     */
    // ENHANCEME: Should also check if the application has fully started up
    protected boolean safeToCreateEditingContext() {
        return EOObjectStoreCoordinator.defaultCoordinator().cooperatingObjectStores().count() > 0;
    }

    /**
     * Gets the editing context for logging events. Will
     * create one if one hasn't been created yet.
     * @return editing context to log events to the database
     */
    protected EOEditingContext editingContext() {
        if (ec == null) {
            if (safeToCreateEditingContext()) {
                ec = ERXEC.newEditingContext();
            }
        }
        return ec;
    }

    /**
     * Gets the buffer size.
     * @return current buffer size
     */
    public int getBufferSize() { return bufferSize; }

    /**
     * Sets the current buffer size. Must be set
     * to a value greater than zero.
     * @param bufferSize size of the buffer
     */
    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0)
            LogLog.warn("BufferSize must be greater than 0!  Attempted to set bufferSize to: " + bufferSize);
        else
            this.bufferSize = bufferSize;
    }

    /**
     * Used to determine if the system is ready to log
     * events to the database.
     * @return if all of the conditions are satisfied
     */
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

    /**
     * Entry point for logging an event.
     *
     * Reminder: the nesting of calls is:
     *
     *    doAppend()
     *      - check threshold
     *      - filter
     *      - append();
     *        - checkConditions();
     *        - subAppend();
     *
     * @param event current logging event
     */
    public void append(LoggingEvent event) {
        if (conditionsChecked || checkConditions()) {
            subAppend(event);
        } else {
            LogLog.warn("Unable to log event: " + event.getMessage());
        }
    }

    /**
     * This is where the real logging happens.
     * @param event current logging event
     */
    protected void subAppend(LoggingEvent event) {
        // Create Log Entry for event.
        if (editingContext() != null) {
            ERXEOFLogEntryInterface logEntry = (ERXEOFLogEntryInterface)ERXEOControlUtilities.createAndInsertObject(editingContext(), getLoggingEntity());
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
