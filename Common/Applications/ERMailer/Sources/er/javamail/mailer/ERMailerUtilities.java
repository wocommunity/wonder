//
// ERMailerUtilities.java
// Project ERMailer
//
// Created by Max Muller on Tue Oct 22 2002
//
package er.javamail.mailer;

import com.webobjects.foundation.*;

import er.extensions.*;
import er.corebusinesslogic.*;
import er.javamail.*;

/**
 * Collection of utilities methods used to bridge the
 * worlds of {@link er.corebusinesslogic.ERCMailMessage ERCMailMessage}
 * and {@link er.javamail.ERMailDelevery ERMailDelevery}.
 */
public class ERMailerUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERMailerUtilities.class);

    /**
     * Creates a ERMailDelivery for a given
     * MailMessage.
     * @param message mail message
     * @return a mail delevery object
     */
    // ENHANCEME: Not handling hidden text, plain text, double byte (Japanese) language or file attachments.
    public static ERMailDelivery createMailDeliveryForMailMessage(ERCMailMessage message) {
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

    // IMPLEMENTME
    public static void reportExceptionForMessage(Exception exception, ERCMailMessage mailMessage) {
    }    
}
