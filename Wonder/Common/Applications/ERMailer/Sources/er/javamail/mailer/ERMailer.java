//
// ERMailer.java
// Project ERMailer
//
// Created by max on Tue Oct 22 2002
//
package er.javamail.mailer;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

import er.extensions.*;

import er.javamail.*;
import er.corebusinesslogic.*;

import java.util.Enumeration;

/**
 * Mailer bridge class. Used to pull mail out of the
 * ERMailMessage entity and send it via the ERJavaMail
 * framework for sending mail.
 */
public class ERMailer {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERMailer.class);
    
    /** holds a reference to the shared instance */
    protected static ERMailer instance;

    /**
     * Gets the shared mailer instance.
     * @return mailer singleton
     */
    public static ERMailer instance() {
        if (instance == null)
            instance = new ERMailer();
        return instance;
    }

    /**
     * Fetches all mail that is ready to
     * be sent from the ERMailMessage table
     * and sends the message using the
     * ERJavaMail framework for sending
     * messages.
     */
    public void processOutgoingMail() {
        log.debug("Starting outgoing mail processing.");
        EOEditingContext ec = ERXExtensions.newEditingContext();

        NSArray unsentMessages = ERCMailMessage.mailMessageClazz().messagesToBeSent(ec);

        sendMailMessages(unsentMessages);

        ec.dispose();
        
        log.debug("Done outgoing mail processing.");
    }
    
    /**
     * Sends an array of ERCMailMessage objects.
     * @param mailMessages array of messages to send
     */
    public void sendMailMessages(NSArray mailMessages) {
        for (Enumeration messageEnumerator = mailMessages.objectEnumerator(); messageEnumerator.hasMoreElements();) {
            ERCMailMessage mailMessage = (ERCMailMessage)messageEnumerator.nextElement();
            ERMailDelivery delivery = createMailDeliveryForMailMessage(mailMessage);

            if (log.isDebugEnabled())
                log.debug("Sending mail message: " + mailMessage);
            
            if (delivery != null) {
                try {
                    mailMessage.setState(ERCMailState.PROCESSING_STATE);
                    mailMessage.editingContext().saveChanges(); // This will throw if optimistic locking occurs
                    delivery.sendMail(true);
                    mailMessage.setState(ERCMailState.SENT_STATE);
                    mailMessage.setDateSent(new NSTimestamp());
                } catch (EOGeneralAdaptorException ge) {
                    log.warn("Caught general adaptor exception, reverting context : " + ge);
                    mailMessage.editingContext().revert();
                } catch (NSForwardException e) {
                    log.warn("Caught exception when sending mail: " + ERXUtilities.stackTrace(e.originalException()));
                    // ENHANCEME: Need to implement a waiting state to retry sending mails.
                    mailMessage.setState(ERCMailState.EXCEPTION_STATE);
                    mailMessage.setExceptionReason(e.originalException().getMessage());
                    // Report the mailing error
                    ERCoreBusinessLogic.sharedInstance().reportException(e.originalException(),
                                                                         new NSDictionary(mailMessage.snapshot(),
                                                                                          "Mail Message Snapshot"));
                } finally {
                    // The editingcontext will not have any changes if an optimistic error occurred
                    if (mailMessage.editingContext().hasChanges()) {
                        try {
                            mailMessage.editingContext().saveChanges();
                        } catch (RuntimeException runtime) {
                            log.error("RuntimeException during save changes! Exception: " + runtime.getMessage());
                            throw runtime;
                        }                        
                    }
                }
            }
        }        
    }
    
    /**
     * Creates a ERMailDelivery for a given
     * MailMessage.
     * @param message mail message
     * @return a mail delevery object
     */
    // ENHANCEME: Not handling hidden text, plain text, double byte (Japanese) language or file attachments.
    public ERMailDelivery createMailDeliveryForMailMessage(ERCMailMessage message) {
        ERMailDeliveryHTML mail = new ERMailDeliveryHTML();

        try {
            // Add all of the addresses
            mail.setFromAddress(message.fromAddress());
            if (message.replyToAddress() != null)
                mail.setReplyToAddress(message.replyToAddress());
            mail.setToAddresses(message.toAddressesAsArray());
            if (message.ccAddressesAsArray().count() > 0)
                mail.setCCAddresses(message.ccAddressesAsArray());
            if (message.bccAddressesAsArray().count() > 0)
                mail.setBCCAddresses(message.bccAddressesAsArray());

            // Set the xMailer if one is specified
            // Note (tuscland): setXMailerHeader has a higher precedence over
            // System property er.javamail.XMailerHeader
            if (message.xMailer() != null)
                mail.setXMailerHeader(message.xMailer());

            // Set the content
            mail.setSubject(message.title());
            mail.setHTMLContent(message.text());
        } catch (Exception e) {
            log.error("Caught exception constructing mail to delevery: " + ERXUtilities.stackTrace(e));
            mail = null;
        }
        return mail;
    }
}
