//
// ERMailer.java
// Project ERMailer
//
// Created by max on Tue Oct 22 2002
//
package er.javamail.mailer;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

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
        
        log.debug("Done outgoing mail processing.");
    }
    
    /**
     * Sends an array of ERCMailMessage objects.
     * @param mailMessages array of messages to send
     */
    public void sendMailMessages(NSArray mailMessages) {
        for (Enumeration messageEnumerator = mailMessages.objectEnumerator(); messageEnumerator.hasMoreElements();) {
            ERCMailMessage mailMessage = (ERCMailMessage)messageEnumerator.nextElement();
            ERMailDelivery delivery = ERMailerUtilities.createMailDeliveryForMailMessage(mailMessage);

            if (log.isDebugEnabled())
                log.debug("Sending mail message: " + mailMessage);
            
            if (delivery != null) {
                try {
                    delivery.sendMail(true);
                    mailMessage.setState(ERCMailState.SENT_STATE);
                    mailMessage.setDateSent(new NSTimestamp());
                } catch (ERMailSender.ForwardException e) {
                    log.warn("Caught exception when sending mail: " + ERXUtilities.stackTrace(e.forwardException()));
                    // ENHANCEME: Need to implement a waiting state to retry sending mails.
                    mailMessage.setState(ERCMailState.EXCEPTION_STATE);
                    mailMessage.setExceptionReason(e.forwardException().getMessage());
                    // Report the mailing error
                    ERMailerUtilities.reportExceptionForMessage(e.forwardException(), mailMessage);
                } finally {
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
