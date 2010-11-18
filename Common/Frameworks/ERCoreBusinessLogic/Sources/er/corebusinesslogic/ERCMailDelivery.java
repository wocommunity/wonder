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
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;

/**
* Utility class used for sending mails via the
 * ERCMailMessage database tables. Actual emails
 * are then sent using the ERMailer application.
 */
public class ERCMailDelivery {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------

    /** logging supprt */
    public static final Logger log = Logger.getLogger(ERCMailDelivery.class);

    private static final int MAX_TO_ADDRESS_SIZE = 1000;

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
        StringBuffer result=new StringBuffer();
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
     * Utility method to break down an array of addresses into an
     * array of several strings, each one with several addresses.
     * Each string length is lower than {@link #MAX_TO_ADDRESS_SIZE}.
     * 
     * @param a array of addresses
     * @return array of comma separated and cleaned up lists of email addresses
     */
    public static NSArray commaSeparatedListArrayFromArray(NSArray a) {
        int currentSize = 0;
        final int maxSizePerArray = MAX_TO_ADDRESS_SIZE; //FIXME Find this by peeking into model
        NSMutableArray partialArray = new NSMutableArray();
        NSMutableArray result = new NSMutableArray();
      
        for( Enumeration addressEnumerator = a.objectEnumerator(); addressEnumerator.hasMoreElements(); ) {
            String address = (String) addressEnumerator.nextElement();
            if( address.length() > maxSizePerArray ) {
                throw new RuntimeException( "Address " + address + " is too long." );
            }
            
            if( currentSize + address.length() > maxSizePerArray ) {
                result.addObject( commaSeparatedListFromArray(partialArray) );
                partialArray = new NSMutableArray();
                currentSize = 0;
            }
            
            partialArray.addObject( address );
            currentSize += address.length() + 2; // Consider the address and ", "
        }
        
        if( partialArray.count() > 0 ) {
          result.addObject( commaSeparatedListFromArray(partialArray) );
        }
        
        return result;
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
     * Composes a mail messages.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param message text of the message
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeEmail(String from,
                                       NSArray to,
                                       NSArray cc,
                                       NSArray bcc,
                                       String title,
                                       String message,
                                       EOEditingContext ec) {
    	return composeEmail(null, from, to, cc, bcc, title, message, ec);
    }
    
    /**
     * Composes mail messages.
     *
     * @param contextString the message context 
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param message text of the message
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeEmail(String contextString,
    		                           String from,
                                       NSArray to,
                                       NSArray cc,
                                       NSArray bcc,
                                       String title,
                                       String message,
                                       EOEditingContext ec) {
      
        NSMutableArray messages = new NSMutableArray( to.count() );
      
        if (log.isDebugEnabled()) {
            log.debug("Sending email title \"" + title + "\" from \"" + from + "\" to \"" + to + "\" cc \""
                      + cc + "\" bcc \"" + bcc + "\"");
            log.debug("Email message: " + message);
        }
        if (usesMail()) {
            boolean firstMessage = true;
            String safeTitle = title != null ? ( title.length() > 200 ? title.substring(0,198) : title ) : null;
            NSArray toLists = commaSeparatedListArrayFromArray( to );
            
            for( Enumeration toListsEnumerator = toLists.objectEnumerator(); toListsEnumerator.hasMoreElements(); ) {
                String toAddresses = (String) toListsEnumerator.nextElement();
                ERCMailMessage mailMessage = (ERCMailMessage)ERCMailMessage.mailMessageClazz().createAndInsertObject(ec);
                mailMessage.setTitle(safeTitle);
                mailMessage.setFromAddress(from);
                mailMessage.setToAddresses(toAddresses);
                mailMessage.setText(message);
                
                // CCers and BCCers should receive only one copy of the email, so we include them only on one of the messages
                if( firstMessage ) {
                    mailMessage.setCcAddresses(commaSeparatedListFromArray(cc));
                    mailMessage.setBccAddresses(commaSeparatedListFromArray(bcc));
                    firstMessage = false;
                }
                
                messages.addObject( mailMessage );
                
                if( toLists.count() > 1 ) {
                    mailMessage.setHasClones( true );
                }
            }
        } else {
            throw new RuntimeException("The application doesn't use the ERCUseMailFacility."+
                                       "You can either set er.corebusinesslogic.ERCUseMailFacility in your properties or better check for that property before trying to compose the email");
        }

        return messages;
    }

    /**
     * Composes mail messages with attachments.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param message text of the message
     * @param filePaths array of file paths to attach
     * @param deleteOnSent should the attachment files be deleted when the message is sent
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeEmailWithAttachments (String from,
                                                       NSArray to,
                                                       NSArray cc,
                                                       NSArray bcc,
                                                       String title,
                                                       String message,
                                                       NSArray filePaths,
                                                       boolean deleteOnSent,
                                                       EOEditingContext ec) {
    	return composeEmailWithAttachments(null, from, to, cc, bcc, title, message, filePaths, deleteOnSent, ec);
    }
    
    /**
     * Composes mail messages with attachments.
     *
     * @param contextString the message context 
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param message text of the message
     * @param filePaths array of file paths to attach
     * @param deleteOnSent should the attachment files be deleted when the message is sent
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeEmailWithAttachments (String contextString,
                                                       String from,
                                                       NSArray to,
                                                       NSArray cc,
                                                       NSArray bcc,
                                                       String title,
                                                       String message,
                                                       NSArray filePaths,
                                                       boolean deleteOnSent,
                                                       EOEditingContext ec) {
        NSArray messages = this.composeEmail(contextString, from, to, cc, bcc, title, message, ec);

        for(Enumeration messageEnumerator = messages.objectEnumerator(); messageEnumerator.hasMoreElements(); ) {
            ERCMailMessage mailMessage = (ERCMailMessage) messageEnumerator.nextElement();
            
            for (Enumeration filePathEnumerator = filePaths.objectEnumerator();
              filePathEnumerator.hasMoreElements();) {
                String filePath = (String)filePathEnumerator.nextElement();
                ERCMessageAttachment attachment = (ERCMessageAttachment)ERCMessageAttachment.messageAttachmentClazz().createAndInsertObject(ec);
                attachment.setFilePath(filePath);
                attachment.setDeleteOnSent(Boolean.valueOf(deleteOnSent));
                mailMessage.addToBothSidesOfAttachments(attachment);
            }
        }
      
        return messages;
    }

    /**
     * Composes mail messages from a given component.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param component to render to get the message
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 WOComponent component,
                                                 EOEditingContext ec) {
    	return composeComponentEmail(null, from, to, cc, bcc, title, component, ec);
    }
    
    /**
     * Composes mail messages from a given component.
     *
     * @param contextString the message context 
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param component to render to get the message
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeComponentEmail (String contextString,
                                                 String from,
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
            context._generateCompleteURLs ();
            message = component.generateResponse().contentString();
        }
        return composeEmail(contextString, from, to, cc, bcc, title, message, ec);
    }

    /**
     * Composes mail messages from a given component.
     *
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param componentName name of the component to render
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 String componentName,
                                                 NSDictionary bindings,
                                                 EOEditingContext ec) {
    	return composeComponentEmail(null, from, to, cc, bcc, title, componentName, bindings, ec);
    }
    
    /**
     * Composes mail messages from a given component.
     *
     * @param contextString the message context 
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param componentName name of the component to render
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
    public NSArray composeComponentEmail (String contextString,
                                                 String from,
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
        return composeComponentEmail(contextString, from, to, cc, bcc, title, component, ec);
    }

    /**
     * Composes mail messages from a given component.
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
     * @return created mail messages for the given parameters
     */
    public NSArray composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 String componentName,
                                                 String plainTextComponentName,
                                                 NSDictionary bindings,
                                                 EOEditingContext ec) {
    	return composeComponentEmail(null, from, to, cc, bcc, title, componentName, plainTextComponentName, bindings, ec);
    }
    
    /**
     * Composes mail messages from a given component.
     *
     * @param contextString the message context 
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
     * @return created mail messages for the given parameters
     */
    public NSArray composeComponentEmail (String contextString,
                                                 String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 String componentName,
                                                 String plainTextComponentName,
                                                 NSDictionary bindings,
                                                 EOEditingContext ec) {
        NSArray result = composeComponentEmail(contextString, from, to, cc, bcc, title, componentName, bindings, ec);
        WOComponent plainTextComponent = ERXApplication.instantiatePage(plainTextComponentName);
        try{
            plainTextComponent = ERXApplication.instantiatePage(plainTextComponentName);
        }catch(WOPageNotFoundException exc){
            //Do nothing here since it is not mandatory to have a plain text version component
        }
        if(plainTextComponent!=null){
            EOKeyValueCodingAdditions.DefaultImplementation.takeValuesFromDictionary(plainTextComponent, bindings);
            WOContext context = plainTextComponent.context();
            context._generateCompleteURLs ();
            for ( Enumeration messageEnumerator = result.objectEnumerator(); messageEnumerator.hasMoreElements(); ) {
                ERCMailMessage message = (ERCMailMessage) messageEnumerator.nextElement();
                message.setPlainText(plainTextComponent.generateResponse().contentString());
            }
            
        }
        return result;
    }
    
    /**
     * Composes mail message from previously instantiated components.
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
     * @return created mail messages for the given parameters
     */
     public NSArray composeComponentEmail (String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 WOComponent component,
                                                 WOComponent plainTextComponent,
                                                 EOEditingContext ec) {
    	 return composeComponentEmail(null, from, to, cc, bcc, title, component, plainTextComponent, ec);
     }
     
    /**
     * Composes mail message from previously instantiated components.
     *
     * @param contextString the message context 
     * @param from email address
     * @param to email addresses
     * @param cc email addresses
     * @param bcc email addresses
     * @param title of the message
     * @param component component to render
     * @param plainTextComponent plain-text component to render
     * @param ec editing context to create the mail
     *		message in.
     * @return created mail messages for the given parameters
     */
     public NSArray composeComponentEmail (String contextString,
                                                 String from,
                                                 NSArray to,
                                                 NSArray cc,
                                                 NSArray bcc,
                                                 String title,
                                                 WOComponent component,
                                                 WOComponent plainTextComponent,
                                                 EOEditingContext ec) {
        NSArray result = composeComponentEmail(contextString, from, to, cc, bcc, title, component, ec);
        
        if ( plainTextComponent != null ) {
            WOContext context = plainTextComponent.context();
            
            context._generateCompleteURLs();
            for ( Enumeration messageEnumerator = result.objectEnumerator(); messageEnumerator.hasMoreElements(); ) {
              ERCMailMessage message = (ERCMailMessage) messageEnumerator.nextElement();
              message.setPlainText(plainTextComponent.generateResponse().contentString());
            }
        }
        
        return result;
    }
}
