/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOPageNotFoundException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;

/**
 * Utility class used for sending mails via the
 * ERCMailMessage database tables. Actual emails
 * are then sent using the ERMailer application.
 *
 * @property er.corebusinesslogic.ERCUseMailFacility
 */
public class ERCMailDelivery {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------

    /** logging supprt */
    public static final Logger log = Logger.getLogger(ERCMailDelivery.class);

    //	===========================================================================
    //	Class Variable(s)
    //	---------------------------------------------------------------------------

    /** holds a reference to the shared instance */
    protected static ERCMailDelivery _sharedInstance;

    //	===========================================================================
    //	Class Method(s)
    //	---------------------------------------------------------------------------

    /**
        * Gets the shared instance used to create
     * ERCMailMessages.
     * @return shared instance used to create
     *		mail messages.
     */
    public static ERCMailDelivery sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new ERCMailDelivery();
        return _sharedInstance;
    }

    /**
        * Utilitiy method used to break an array of email addresses
     * down into a comma separated list. Performs a bit of search
     * and replace to clean up the email addresses a bit.
     * @param a array of email addresses
     * @return comma separated and cleaned up list of email addresses
     */
    public static String commaSeparatedListFromArray(NSArray a) {
        StringBuilder result = new StringBuilder();
        if (a!=null) {
            for (Enumeration e=a.objectEnumerator(); e.hasMoreElements(); ) {
                String address=(String)e.nextElement();
                if (address.indexOf("\"")!=-1) {
                    address=address.replace('\"', '\'');
                }
                if (address.indexOf(",")!=-1) { // FIXME I am sure other characters than comma will cause problems
                    address=address.replace(',', ' ');
                    // address='\"'+address+'\"';
                }
                result.append(address);
                if (e.hasMoreElements()) {
                    result.append(',');
                    result.append(' ');
                }
            }
        }
        return result.toString();
    }

    /**
        * Is  Mail turned on
     *
     * @return if the Mail is turned on
     */
    public static boolean usesMail(){
        return ERXProperties.booleanForKey("er.corebusinesslogic.ERCUseMailFacility");
    }

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Composes a mail message.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param message text of the message
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail message for the given parameters
     */
    public ERCMailMessage composeEmail(String from,
                                       NSArray to,
                                       NSArray cc,
                                       NSArray bcc,
                                       String title,
                                       String message,
                                       EOEditingContext ec) {
        ERCMailMessage mailMessage = null;

        if (log.isDebugEnabled()) {
            log.debug("Sending email title \"" + title + "\" from \"" + from + "\" to \"" + to + "\" cc \""
                      + cc + "\" bcc \"" + bcc + "\"");
            log.debug("Email message: " + message);
        }
        if (usesMail()) {
            mailMessage = ERCMailMessage.mailMessageClazz().createAndInsertObject(ec);
            String safeTitle = title != null ? ( title.length() > 200 ? title.substring(0,198) : title ) : null;
            mailMessage.setTitle(safeTitle);
            mailMessage.setFromAddress(from);
            mailMessage.setToAddresses(commaSeparatedListFromArray(to));
            mailMessage.setCcAddresses(commaSeparatedListFromArray(cc));
            mailMessage.setBccAddresses(commaSeparatedListFromArray(bcc));
            mailMessage.setText(message);
        } else {
            throw new RuntimeException("The application doesn't use the ERCUseMailFacility."+
                                       "You can either set er.corebusinesslogic.ERCUseMailFacility in your properties or better check for that property before trying to compose the email");
        }

        return mailMessage;
    }

    /**
     * Composes a mail message with attachments.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param message text of the message
     * @param filePaths array of file paths to attach
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail message for the given parameters
     */
    public ERCMailMessage composeEmailWithAttachments (String from,
                                                       NSArray to,
                                                       NSArray cc,
                                                       NSArray bcc,
                                                       String title,
                                                       String message,
                                                       NSArray filePaths,
                                                       EOEditingContext ec) {
        ERCMailMessage mailMessage = composeEmail(from, to, cc, bcc, title, message, ec);

        for (Enumeration filePathEnumerator = filePaths.objectEnumerator();
             filePathEnumerator.hasMoreElements();) {
            String filePath = (String)filePathEnumerator.nextElement();
            ERCMessageAttachment attachment = ERCMessageAttachment.messageAttachmentClazz().createAndInsertObject(ec);
            attachment.setFilePath(filePath);
            mailMessage.addToBothSidesOfAttachments(attachment);
        }
        return mailMessage;
    }

    /**
     * Composes a mail message from a given component.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param component to render to get the message
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail message for the given parameters
     */
    public ERCMailMessage composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 WOComponent component,
                                                 EOEditingContext ec) {
        String message = null;
        if (component == null) {
            throw new IllegalStateException("Attempting to send a component email with a null component! From: "
                                            + title + " title: " + title);
        } else {
            WOContext context = component.context();
            // Emails should generate complete urls
            context.generateCompleteURLs();
            message = component.generateResponse().contentString();
        }
        return composeEmail(from, to, cc, bcc, title, message, ec);
    }

    /**
     * Composes a mail message from a given component.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param componentName name of the component to render
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail message for the given parameters
     */
    public ERCMailMessage composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 String componentName,
                                                 NSDictionary bindings,
                                                 EOEditingContext ec) {
        WOComponent component = ERXApplication.instantiatePage(componentName);
        if (component == null) {
            log.warn("Created null component for name \"" + componentName + "\"");
        } else if (log.isDebugEnabled()) {
            log.debug("Created component with name \"" + componentName + "\" class name \""
                      + component.getClass().getName() + "\"");
        }
        if (bindings != null && bindings.count() > 0){
            EOKeyValueCodingAdditions.DefaultImplementation.takeValuesFromDictionary(component, bindings);

        }
        return composeComponentEmail(from, to, cc, bcc, title, component, ec);
    }

    /**
     * Composes a mail message from a given component.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param componentName name of the component to render
     * @param plainTextComponentName name of the component to render
     * @param bindings bindings dictionary to use for the components that are instantiated
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail message for the given parameters
     */
    public ERCMailMessage composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 String componentName,
                                                 String plainTextComponentName,
                                                 NSDictionary bindings,
                                                 EOEditingContext ec) {
        ERCMailMessage result = composeComponentEmail(from, to , cc, bcc, title, componentName, bindings, ec);
        WOComponent plainTextComponent = ERXApplication.instantiatePage(plainTextComponentName);
        try{
            plainTextComponent = ERXApplication.instantiatePage(plainTextComponentName);
        }catch(WOPageNotFoundException exc){
            //Do nothing here since it is not mandatory to have a plain text version component
        }
        if(plainTextComponent!=null){
            EOKeyValueCodingAdditions.DefaultImplementation.takeValuesFromDictionary(plainTextComponent, bindings);
            WOContext context = plainTextComponent.context();
            context.generateCompleteURLs();
            result.setPlainText(plainTextComponent.generateResponse().contentString());
        }
        return result;
    }
    
    /**
     * Composes a mail message from previously instantiated components.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param component component to render
     * @param plainTextComponent plain-text component to render
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail message for the given parameters
     */
     public ERCMailMessage composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 WOComponent component,
                                                 WOComponent plainTextComponent,
                                                 EOEditingContext ec) {
        ERCMailMessage result = composeComponentEmail(from, to, cc, bcc, title, component, ec);
        
        if ( plainTextComponent != null ) {
            WOContext context = plainTextComponent.context();
            
            context.generateCompleteURLs();
            result.setPlainText(plainTextComponent.generateResponse().contentString());
        }
        
        return result;
    }
}
