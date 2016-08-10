/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEditingContextDelegate;
import er.extensions.logging.ERXMailAppender;

/**
 * Basic log4j Mail Message Appender<br>
 *	Used for logging log events to a database that will eventually be emailed
 *	out. Logs events using {@link ERCMailDelivery ERCMailDelivery}.
 *  @see er.extensions.logging.ERXMailAppender for more info.
 */
public class ERCMailMessageAppender extends ERXMailAppender {

    /** caches the no-op editing context delegate */
    protected static final ERXEditingContextDelegate _delegate=new ERXEditingContextDelegate();

    /** holds the editing context */
    protected EOEditingContext editingContext;    

    /**
     * Public constructor.
     */
    public ERCMailMessageAppender() {
        super();
    }

    /**
     * We want the ability to warn if we are going to be
     * creating the first cooperating object store. Not a bad
     * thing just a condition that might cause a strange EOF
     * issue if it occurs.
     * @return if the default object store coordinator has any
     *		cooperating object stores.
     */
    protected boolean hasCooperatingObjectStores() {
        return EOObjectStoreCoordinator.defaultCoordinator().cooperatingObjectStores().count() > 0;
    }

    /**
     * Gets the editing context to use for creating
     * mail messages in.
     * @return editing context with a no-op delegate
     *		set.
     */
    public EOEditingContext editingContext() {
        if (editingContext == null) {
            if (!hasCooperatingObjectStores()) {
                LogLog.warn("Creating editing context for the ERCMailMessageAppender before any cooperating object stores have been added.");
            }
            editingContext = new EOEditingContext();
            editingContext.setDelegate(_delegate);            
        }
        return editingContext;
    }

    /** Overridden because we want to use our own page */
    @Override
    public String getExceptionPageName() {
        String name = super.getExceptionPageName();
        if(name == null) {
            name = "ERCMailableExceptionPage";
        }
        return name;
    }

    /**
     * Overridden to add the Actor into the dictionary.
     * @param event logging event
     */
    @Override
    public NSMutableDictionary composeExceptionPageDictionary(LoggingEvent event) {
        NSMutableDictionary result = super.composeExceptionPageDictionary(event);
        result.setObjectForKey(ERCoreBusinessLogic.actor(),"actor");
        return result;
    }
        /**
     * Where the actual logging event is processed and a
     * mail message is generated.
     * @param event logging event
     */
    @Override
    public void subAppend(LoggingEvent event) {
        if (editingContext().hasChanges()) {
            LogLog.error("ERProblemMailMessageAppender: editingContext has changes -- infinite loop detected");
        } else {
            String title = composeTitle(event);
            String content = composeMessage(event);
            ERCMailMessage message = ERCMailDelivery.sharedInstance().composeEmail(computedFromAddress(),
                                                                                   toAddressesAsArray(),
                                                                                   toAddressesAsArray(),
                                                                                   bccAddressesAsArray(),
                                                                                   title,
                                                                                   content,
                                                                                   editingContext());
            if (getReplyTo() != null) {
                message.setReplyToAddress(getReplyTo());
            }
            try {
                editingContext().saveChanges();
            } catch (RuntimeException e) {
                LogLog.error("Caught exception when saving changes to mail context. Exception: "
                             + e.getMessage());
            } finally {
                editingContext().revert();
            }
        }
    }
}
