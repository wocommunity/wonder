/*
 $Id$
 
 ERMailDeliveryHTML.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;

/** This ERMailDelivery subclass is specifically crafted for HTML messages
    using a WOComponent as redering device.
    @author Camille Troillard <tuscland@mac.com> */
public class ERMailDeliveryHTML extends ERMailDelivery {

    /** WOComponent used to render the HTML message. */
    private WOComponent mailComponent;

    /** Plain text preamble set in top of HTML source so that non-HTML compliant mail readers
        can at least display this message. */
    private String hiddenPlainTextContent = "";

    /** True if this the current message has a plain text preamble. */
    private boolean hasHiddenPlainTextContent = false;

    /** Sets the WOComponent used to render the HTML message. */
    public void setWOComponentContent (WOComponent component) {
        mailComponent = component;
    }

    /** Sets the Plain text preamble that will be displayed set in top of HTML source.
        Non-HTML compliant mail readers can at least display this message. */
    public void setHiddenPlainTextContent (String content) {
        hiddenPlainTextContent = content + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
        hasHiddenPlainTextContent = true;
    }

    /** Creates a new mail instance within OMailDelivery.  Sets hasHiddenPlainTextContent to false. */
    public void newMail () {
        super.newMail ();
        hasHiddenPlainTextContent = false;
    }

    /** Pre-processes the mail before it gets sent.
        @see ERMailDelivery.prepareMail */
    protected DataHandler prepareMail () {
        MimeMultipart multipart = null;
        MimeBodyPart textPart = null;
        MimeBodyPart htmlPart = null;

        try {
            this.mimeMessage ().setSentDate (new Date ());
            multipart = new MimeMultipart ("alternative");
            
            // set the plain text part
            if (hasHiddenPlainTextContent) {
                textPart = new MimeBodyPart ();
                textPart.setText (hiddenPlainTextContent, ERMailDelivery.DefaultCharset);
                multipart.addBodyPart (textPart);
            }

            // create and fill the html message part
            htmlPart = new MimeBodyPart ();
			WOContext context = mailComponent.context ();
			context._generateCompleteURLs ();
            WOMessage response = mailComponent.generateResponse ();
            String messageString = response.contentString ();

            // Set the content of the html part
            htmlPart.setContent (messageString, "text/html");
            multipart.addBodyPart (htmlPart);

            // Inline attachements
            if (this.inlineAttachments ().count () > 0) {
                // Create a "related" MimeMultipart
                MimeMultipart relatedMultiparts = new MimeMultipart ("related");
                
                // add each inline attachments to the message
                Enumeration en = this.inlineAttachments ().objectEnumerator ();
                while (en.hasMoreElements ()) {
                    ERMailAttachment attachment = (ERMailAttachment)en.nextElement ();
                    BodyPart bp = attachment.getBodyPart ();
                    relatedMultiparts.addBodyPart (bp);
                }

                // Add this multipart to the main multipart as a compound BodyPart
                BodyPart relatedAttachmentsBodyPart = new MimeBodyPart ();
                relatedAttachmentsBodyPart.setDataHandler (new DataHandler (relatedMultiparts, relatedMultiparts.getContentType ()));
                multipart.addBodyPart (relatedAttachmentsBodyPart);
            }
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
        
        return new DataHandler (multipart, multipart.getContentType ());
    }

}
