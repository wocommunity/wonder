/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.eocontrol.EOEventCenter;

/**
 * A concrete event recording handler object. This is useful for setting up custom event recording.
 * This recording handler will record everything.
 */
public class ERXEventRecordingDefaultHandler implements EOEventCenter.EventRecordingHandler {

    /**
     * Is always enabled by default.
     * @param flag determines if reciever should log events.
     * @param aClass reciever class.
     */
    public void setLoggingEnabled(boolean flag, Class aClass) {
    }
}
