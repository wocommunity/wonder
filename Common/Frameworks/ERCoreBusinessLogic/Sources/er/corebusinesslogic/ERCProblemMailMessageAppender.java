/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;
import org.apache.log4j.helpers.LogLog;
import er.extensions.*;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Basic Appender that uses ERCMailDelivery to log events.
////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class ERCProblemMailMessageAppender extends AppenderSkeleton {

    public ERCProblemMailMessageAppender() {
        closed = false;
        ERXRetainer.retain(this);
        NSNotificationCenter.defaultCenter().addObserver(this,
                                                         new NSSelector("objectStoreWasAdded",
                                                                        ERXConstant.NotificationClassArray),
                                                         EOObjectStoreCoordinator.CooperatingObjectStoreWasAddedNotification,
                                                         null);
    }
    
    public boolean requiresLayout() { return true; }
    public void close() { closed = true; }

    protected EOEditingContext ec;    

    protected String fromAddress;
    public String getFromAddress() {
        if (fromAddress == null)
            fromAddress = WOApplication.application().name()+"-"+hostName+"@netstruxr.com";
        return fromAddress;
    }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    protected String title;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    protected String hostName;
    public String getHostName() {
        if (hostName == null)
            hostName = ERCoreBusinessLogic.defaultHostName();
        return hostName;
    }
    public void setHostName(String hostName) { this.hostName = hostName; }
        
    protected boolean objectStoreWasAdded = false;
    protected static ERXEditingContextDelegate _delegate=new ERXEditingContextDelegate();
    public void objectStoreWasAdded(NSNotification n) {
        objectStoreWasAdded = true;
        ec = new EOEditingContext();
        ec.setDelegate(_delegate);
        NSNotificationCenter.defaultCenter().removeObserver(this);
        ERXRetainer.release(this);
    }
    
    public void append(LoggingEvent event) {
        if (!objectStoreWasAdded) {
            LogLog.warn("Attempting to log an event to an ERCProblemMailMessageAppender before an ObjectStoreCoordinator has been added: " +
                        event.getRenderedMessage());  
        } else if (this.layout == null) {
            LogLog.warn("Attempting to log an event to an ERCProblemMailMessageAppender without a layout specified.");
        } else if (ec.hasChanges()) {
            WOApplication.application().logString("***************** ERProblemMailMessageAppender: editingContext has changes -- infinite loop detected");
        } else if (ERCoreBusinessLogic.emailsForProblemRecipients() != null && ERCoreBusinessLogic.emailsForProblemRecipients().count() > 0) {
            ERCMailDelivery.sharedInstance().composeEmail(WOApplication.application().name()+"-"+getHostName()+"@netstruxr.com",
                                                          ERCoreBusinessLogic.emailsForProblemRecipients(),
                                                          null,
                                                          null,
                                                          event.priority.toString() + ": " + WOApplication.application().name() + ": " +
                                                          event.getRenderedMessage(),
                                                          this.layout.format(event),
                                                          ec);
            ec.saveChanges();
        } else {
            LogLog.warn("Attmepting to append event when recipient emails is null or count is zero.");
        }
    }
}
