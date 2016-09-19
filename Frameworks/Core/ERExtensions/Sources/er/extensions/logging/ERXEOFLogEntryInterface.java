/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.logging;

/**
 * Interface implemented by EnterpriseObjects to initialize with
 * a logging event and optionally a layout. This is used in conjunction
 * with the {@link ERXEOFAppender}.
 */
public interface ERXEOFLogEntryInterface {

    /**
     * This method is called on an enterprise object after
     * it has been created to initialize the log entry with
     * the current log event.
     * @param event current logging event
     * @param layout current layout for the appender
     */
    public void intializeWithLoggingEvent(org.apache.log4j.spi.LoggingEvent event, org.apache.log4j.Layout layout);
}
