/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;
import org.apache.log4j.helpers.LogLog;
import er.extensions.*;

/**
 * Basic log4j Mail Message Appender<br>
 *	Used for logging log events to a database that will eventually be emailed
 *	out. Logs events using {@link ERCMailDelivery ERCMailDelivery}.
 *  Manditory Fields:<br>
 *      TOAddresses - coma seperated list of email addresses to send the log event
 *		message to.
 *	FromAddress - Who the message is from, if left blank then DomainName is a
 *		manditory field.
 *  Optional Fields:<br>
 *	BCCAddresses - coma separated list of email address to bcc on the email
 *	CCAddresses - coma separated list of email address to cc on the email
 *	DomainName - When generating a from email address, used for the bit after the
 *		@, ie fo@bar.com, the domain name is 'bar.com'.
 *	HostName - When generating an email address from, defaults to name of the
 *		localhost.
 *	Title - Title of the email messages, if not specified the title will be a
 *		truncated version of the log message.
 */
public class ERCMailMessageAppender extends AppenderSkeleton {

    /** caches the no-op editing context delegate */
    protected static ERXEditingContextDelegate _delegate=new ERXEditingContextDelegate();

    /** holds the from address */
    protected String fromAddress;

    /** holds the computed from address */
    protected String computedFromAddress;
    
    /** holds the to addresses */
    protected String toAddresses;

    /** holds the cc addresses */
    protected String ccAddresses;

    /** holds the bcc addresses */
    protected String bccAddresses;
    
    /** holds the domain */
    protected String domainName;
    
    /** holds the title */
    protected String title;
    
    /** holds the editing context */
    protected EOEditingContext editingContext;    

    /** holds the host name */
    protected String hostName;

    /** holds the flag if all the conditions for logging have been checked */
    protected boolean conditionsChecked = false;
    
    /**
     * Public constructor.
     */
    public ERCMailMessageAppender() {
        closed = false;
    }

    /**
     * The mail message appender does require
     * a layout.
     * @return true.
     */
    public boolean requiresLayout() { return true; }

    /**
     * When closed set the state to closed. Closes
     * the current appender.
     */
    public void close() { closed = true; }

    /**
     * Gets the from address set by the user.
     * @return the from address.
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * Sets the from address.
     * @param fromAddress to use when generating emails.
     */
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    /**
     * Gets the from address for the appender.
     * If a from address is not specified then
     * an address is constructed in the form:
     * applicationName-hostName@domainName.
     * @return the from address.     
     */
    public String computedFromAddress() {
        if (computedFromAddress == null) {
            if (getFromAddress() != null) {
                computedFromAddress = getFromAddress();
            } else {
                computedFromAddress = WOApplication.application().name()+"-"+getHostName()+"@" + getDomainName();
            }
        }
        return computedFromAddress;
    }
    
    /**
     * Gets the to addresses as a string.
     * @return to addresses as a string
     */
    public String getToAddresses() {
        return toAddresses;
    }

    /**
     * Sets the to addresses as a string.
     * @param toAddresses comma separated array of email addresses
     */
    public void setToAddresses(String toAddresses) {
        this.toAddresses = toAddresses;
    }

    /**
     * Gets the to addresses as an array.
     * @return the to addresses as an array.
     */
    public NSArray toAddressesAsArray() {
        return toAddresses != null ? NSArray.componentsSeparatedByString(toAddresses, ",") : NSArray.EmptyArray;
    }

    /**
     * Sets the to addresses as a string.
     * @param ccAddresses comma separated array of email addresses
     */
    public void setCcAddresses(String ccAddresses) {
        this.ccAddresses = ccAddresses;
    }

    /**
     * Gets the cc addresses as a String.
     * @return cc addresses as a string
     */
    public String ccAddresses() {
        return ccAddresses;
    }

    /**
     * Gets the cc addresses as an array.
     * @return the cc addresses as an array.
     */
    public NSArray ccAddressesAsArray() {
        return ccAddresses != null ? NSArray.componentsSeparatedByString(ccAddresses, ",") : NSArray.EmptyArray;
    }
    
    /**
     * Sets the bcc addresses as a string.
     * @param bccAddresses comma separated array of email addresses
     */
    public void setBccAddresses(String bccAddresses) {
        this.bccAddresses = bccAddresses;
    }

    /**
     * Gets the bcc addresses as a String.
     * @return bcc addresses as a string
     */
    public String bccAddresses() {
        return bccAddresses;
    }

    /**
     * Gets the bcc addresses as an array.
     * @return the bcc addresses as an array.
     */
    public NSArray bccAddressesAsArray() {
        return bccAddresses != null ? NSArray.componentsSeparatedByString(bccAddresses, ",") : NSArray.EmptyArray;
    }    
    
    /**
     * Gets the title.
     * @return title.
     */
    public String getTitle() { return title; }

    /**
     * Sets the title.
     * @param title of the mail message
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the host name. If one is not specified then the host
     * name of the machine will be used.
     * @return host name to use when constructing the 'from' email
     *		address.
     */
    public String getHostName() {
        if (hostName == null)
            hostName = ERXConfigurationManager.defaultManager().hostName();
        return hostName;
    }

    /**
     * Sets the host name to use for the given appender.
     * @param hostName for the appender.
     */
    public void setHostName(String hostName) { this.hostName = hostName; }

    /**
     * Gets the domain name.
     * @return domain name.
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Sets the domain name.
     * @param domainName new domain name
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
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

    /**
     * Used to determine if the system is ready to log
     * events with MERCMailDelivery.
     * @return true if all of the conditions are satisfied
     */
    protected boolean checkConditions() {
        if (getFromAddress() == null && getDomainName() == null) {
            LogLog.error("Attempting to log an event with a null domain name and a null from address!");
        } else if (toAddressesAsArray().count() == 0) {
            LogLog.error("Attempting to log with an empty array of toAddresses");
        } else if (this.layout == null) {
                LogLog.warn("Attempting to log an event to an ERCMailMessageAppender without a layout specified.");
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
     * Where the actual logging event is processed and a
     * mail message is generated.
     * @param event logging event
     */
    public void subAppend(LoggingEvent event) {
        if (editingContext().hasChanges()) {
            LogLog.error("ERProblemMailMessageAppender: editingContext has changes -- infinite loop detected");
        } else {
            String title;
            if (getTitle() != null) {
                title = getTitle();
            } else {
                title =  event.getLevel().toString() + ": " + WOApplication.application().name() + ": " +
                event.getRenderedMessage();
            }
            
            ERCMailDelivery.sharedInstance().composeEmail(computedFromAddress(),
                                                          toAddressesAsArray(),
                                                          ccAddressesAsArray(),
                                                          bccAddressesAsArray(),
                                                          title,
                                                          this.layout.format(event),
                                                          editingContext());
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
