/*
 $Id$
 
 ERMailDelivery.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;

import er.extensions.ERXLogger;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/** This is the main class for sending mail with the JavaMail API.
You typically don't create instances of this class since it is abstract.
You should create instances of its subclasses that fitted with specifical
use cases.<BR>

Here is an example of its usage.

<PRE>
ERMailDeliveryHTML mail = new ERMailDeliveryHTML ();
mail.setWOComponentContent (mailPage);

try {
    mail.newMail ();
    mail.setFromAddress    (emailFrom);
    mail.setReplyToAddress (emailReplyTo);
    mail.setSubject 	   (emailSubject);
    mail.setToAddresses    (new NSArray (toEmailAddresses));

    // Send the mail
    mail.sendMail ();
} catch (Exception e) {
    // do something ...
}
</PRE>

@author Camille Troillard <camille@odaiko.com>
*/
public abstract class ERMailDelivery {

	static ERXLogger log = ERXLogger.getERXLogger (ERMailDelivery.class);

    /** JavaMail session */
    private javax.mail.Session _session;

    /** Content of sent mail.  In one instance of OMailDelivery, when creating multiple mails,
        you must be sure to call newMail () method before send a new mail in order to have a
        cleared fresh mail */
    protected MimeMessage _mimeMessage;

    /** NSArray of ERMailAttachment that must be binded to the message as ATTACHEMENT. */
    protected NSMutableArray attachments = new NSMutableArray ();

    /** NSArray of ERMailAttachment that must be binded to the message as INLINE. */
    protected NSMutableArray inlineAttachments = new NSMutableArray ();

    /** Callback class name. Used to have ERMail call a method on a class
        after a message has been sent */
    public static String callBackClassName = null;
    public static String callBackMethodName = null;

    /** callbackObject to refer to in the calling program */
    public Object _callbackObject = null;

	public static String DefaultCharset = "iso-8859-1";

	
	protected javax.mail.Session session () {
		return _session;
    }
	protected void setSession (javax.mail.Session aSession) {
		_session = aSession;
    }

	/** Designated constructor */
    public ERMailDelivery (javax.mail.Session session) {
        super ();
		this.setSession (session);
    }

    /** Default constructor */
    public ERMailDelivery () {
        this (ERJavaMail.sharedInstance ().defaultSession ());
    }

    /** Creates a new mail instance within ERMailDelivery */
    public void newMail () {
        attachments.removeAllObjects ();
        inlineAttachments.removeAllObjects ();
        this.setMimeMessage (new MimeMessage (this.session ()));
    }

	protected MimeMessage mimeMessage () {
		return _mimeMessage;
	}
	protected void setMimeMessage (MimeMessage message) {
		_mimeMessage = message;
	}

    public void addAttachment (ERMailAttachment attachment) {
        attachments.addObject (attachment);
    }

    public void addInlineAttachment (ERMailAttachment attachment) {
        inlineAttachments.addObject (attachment);
    }

    public NSArray inlineAttachments () {
        return inlineAttachments;
    }

    public NSArray attachments () {
        return attachments;
    }

    public void removeAttachment (ERMailAttachment attachment) {
        attachments.removeObject (attachment);
        inlineAttachments.removeObject (attachment);
    }

    /** Sets the callback class and method name */
    public static void setCallBackClassWithMethod (String className, String methodName) {
        callBackClassName = className;
        callBackMethodName = methodName;
    }
    
    // </STATIC METHODS> --------------------------------------------------

    /** Sets the from address for the current message instance */
    public void setFromAddress (String fromAddress) throws MessagingException, AddressException {
        this.mimeMessage ().setFrom (new InternetAddress (fromAddress));
    }

    /** Sets the reply-to address for the current message instance */
    public void setReplyToAddress (String replyToAddress) throws MessagingException, AddressException {
        this.mimeMessage ().setReplyTo  (new InternetAddress [] {
            new InternetAddress (replyToAddress)
        });
    }

    /** Sets the to-addresses array for the current message instance */
    public void setToAddresses (NSArray toAddresses) throws MessagingException, AddressException {
        setAddresses (toAddresses, Message.RecipientType.TO);
    }

    public void setToAddress (String toAddress) throws MessagingException, AddressException {
        this.setToAddresses (new NSArray (toAddress));
    }

    /** Sets the cc-addresses array for the current message instance */
    public void setCCAddresses (NSArray ccAddresses) throws MessagingException, AddressException {
        setAddresses (ccAddresses, Message.RecipientType.CC);
    }

    /** Sets the bcc-addresses array for the current message instance */
    public void setBCCAddresses (NSArray bccAddresses) throws MessagingException, AddressException {
        setAddresses (bccAddresses, Message.RecipientType.BCC);
    }

    /** Sets the object for the message. This is the identifying object from the calling program. */
    public void setCallbackObject (Object obj) {
        _callbackObject = obj;
    }
    public Object callbackObject () {
		return _callbackObject;
	}

    /** Sets the subject for the current message instance */
    public void setSubject (String subject) throws MessagingException {
        String encoded = null;
        try {
            encoded = MimeUtility.encodeText (subject, DefaultCharset, null);
        } catch (Exception e) {
            encoded = subject;
        }
        this.mimeMessage ().setSubject (encoded);
    }

	protected ERMessage buildMessage () {
		ERMessage message = new ERMessage ();
		message.setMimeMessage (this.mimeMessage ());
		message.setCallbackObject (this.callbackObject ());
		return message;
	}

    /** Sends the mail immediately.  The message is put in a FIFO queue managed
        by a static threaded inner class */
    public void sendMail () {
        DataHandler messageDataHandler = this.prepareMail ();

        try {
            if (this.attachments ().count () > 0) {
                // Create a Multipart that will hold the prepared multipart and the attachments
                MimeMultipart multipart = new MimeMultipart ();

                // Create the main body part
                BodyPart mainBodyPart = new MimeBodyPart ();
                mainBodyPart.setDataHandler (messageDataHandler);

                // add the main body part to the content of the message
                multipart.addBodyPart (mainBodyPart);

                // add each attachments to the former multipart
                Enumeration en = this.attachments ().objectEnumerator ();
                while (en.hasMoreElements ()) {
                    ERMailAttachment attachment = (ERMailAttachment)en.nextElement ();
                    BodyPart bp = attachment.getBodyPart ();
                    bp.setDisposition (Part.ATTACHMENT);
                    multipart.addBodyPart (bp);
                }

                this.mimeMessage ().setContent (multipart);
            } else {
                this.mimeMessage ().setDataHandler (messageDataHandler);
            }

            this.mimeMessage ().saveChanges ();
        } catch (Exception e) {
            e.printStackTrace ();
        }
        
        // add the current message to the message stack
		boolean mailAccepted = false;
		try {
			while (!mailAccepted) {
				try {
					this.mimeMessage ().setSentDate (new Date ());
				} catch (MessagingException e) {
					if (log.isDebugEnabled ()) {
						log.debug ("Unable to set the date while sending a message.");
						e.printStackTrace ();
					}
				}

				try {
					ERMailSender.sharedMailSender ().sendMessage (this.buildMessage ());
					mailAccepted = true;
				} catch (ERMailSender.SizeOverflowException e) {
					// The mail sender is overflowed, we need to wait
					try {
						// Ask the current thread to stop computing for a little while
						Thread.currentThread ().sleep (
									 ERJavaMail.sharedInstance ().milliSecondsWaitIfSenderOverflowed ());
					} catch (InterruptedException ie) {
						log.warn ("Caught InterruptedException in ERMailDelivery:");
						ie.printStackTrace ();
					}
				} catch (ERMailSender.Exception e) {
					// Handle another class of exception
					// Because there is no other class of exception, ignore this one.
					break;
				}
			}
		} finally {
			this.setMimeMessage (null);
		}
    }

    /** Called by subclasses for doing pre-processing before sending the mail.
        @return the multipart used to put in the mail. */
    protected abstract DataHandler prepareMail ();

    /** Sets addresses regarding their recipient type in current message */
    private void setAddresses (NSArray addressesArray, Message.RecipientType type)
    throws MessagingException, AddressException {
        InternetAddress [] addresses = null;

        if (!ERJavaMail.sharedInstance ().centralize ())
            addresses = _nsarrayToInternetAddresses (addressesArray);
        else
            addresses = new InternetAddress [] { new InternetAddress (ERJavaMail.sharedInstance ().adminEmail ()) };

        this.mimeMessage ().setRecipients (type, addresses);
    }

    /** Private method that converts NSArray of String emails to InternetAddress []. */
    private InternetAddress [] _nsarrayToInternetAddresses (NSArray addressesArray) throws AddressException {
        InternetAddress [] addresses = new InternetAddress [addressesArray.count ()];

        Enumeration en = addressesArray.objectEnumerator ();
        for (int i=0 ; en.hasMoreElements () ; i++){
            String anAddress = (String)en.nextElement ();
            addresses [i] = new InternetAddress (anAddress);
        }

        return addresses;
    }

}
