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
import er.extensions.ERXUtilities;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
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

@author Camille Troillard <tuscland@mac.com>
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
    protected NSMutableArray _attachments;

    /** NSArray of ERMailAttachment that must be binded to the message as INLINE. */
    protected NSMutableArray _inlineAttachments;

    /** Callback class name. Used to have ERMail call a method on a class
        after a message has been sent */
    public static String callBackClassName = null;
    public static String callBackMethodName = null;

    public static String DefaultCharset = "iso-8859-1";

    /** callbackObject to refer to in the calling program */
    public Object _callbackObject = null;

    /** Sets the callback class and method name */
    public static void setCallBackClassWithMethod (String className, String methodName) {
        callBackClassName = className;
        callBackMethodName = methodName;
    }

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
        this.setMimeMessage (new MimeMessage (this.session ()));
    }

    /** Default constructor */
    public ERMailDelivery () {
        this (ERJavaMail.sharedInstance ().defaultSession ());
    }

    /** Creates a new mail instance within ERMailDelivery */
    public void newMail () {
        this._attachments ().removeAllObjects ();
        this._inlineAttachments ().removeAllObjects ();
        this.setMimeMessage (new MimeMessage (this.session ()));
    }

    protected MimeMessage mimeMessage () {
        return _mimeMessage;
    }

    protected void setMimeMessage (MimeMessage message) {
        _mimeMessage = message;
    }

    public void addAttachment (ERMailAttachment attachment) {
        this._attachments ().addObject (attachment);
    }

    public void addInlineAttachment (ERMailAttachment attachment) {
        this._inlineAttachments ().addObject (attachment);
    }

    protected NSMutableArray _inlineAttachments () {
		if (_inlineAttachments == null)
			_inlineAttachments = new NSMutableArray ();
        return _inlineAttachments;
    }

    public NSArray inlineAttachments () {
        return this._inlineAttachments ();
    }

    protected NSMutableArray _attachments () {
		if (_attachments == null)
			_attachments = new NSMutableArray ();
        return _attachments;
    }

    public NSArray attachments () {
        return this._attachments ();
    }

    public void removeAttachment (ERMailAttachment attachment) {
        this._attachments ().removeObject (attachment);
        this._inlineAttachments ().removeObject (attachment);
    }

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

    /**
	 * Sets the X-Mailer header for the message. Useful for tracking
     * which mailers are sending messages.
     * @param xMailer value to set
     */
    public void setXMailerHeader (String xMailer) throws MessagingException {
        this.mimeMessage ().setHeader("X-Mailer", xMailer);
    }

    /**
	 * Gets the X-Mailer header set on the
     * MimeMessage.
     * @return X-Mailer header if it is set
     */
    public String xMailerHeader() throws MessagingException {
        String[] headers = this.mimeMessage ().getHeader ("X-Mailer");
        return headers.length > 0 ? headers[0] : null;
    }

    /**
	 * Builds an ERMessage for the current MimeMessage.
     * @return ERMessage for the current MimeMessage.
     */
    protected ERMessage buildMessage () {
        ERMessage message = new ERMessage ();
        message.setMimeMessage (this.mimeMessage ());
        message.setCallbackObject (this.callbackObject ());
        return message;
    }

    /**
	 * Sends the mail immediately.  The message is put in a FIFO queue managed
     * by a static threaded inner class
     */
    public void sendMail () {
        try {
            sendMail (false);
        } catch (ERMailSender.ForwardException e) {
            log.warn ("Sending mail in a non-blocking manner and a forward exception was thrown: "
                      + ERXUtilities.stackTrace (e));
        }
    }

    /**
	 * Method used to construct a MimeMessage and then send
     * it.  This method can be specified to block until the
     * message is sent or to add the message to a queue and have
     * a callback object handle any exceptions that happen.
     * If sending is blocking then any exception thrown will be
     * wrapped in a general {@link ERMailSender.ForwardException ForwardException}.
     * @param shouldBlock boolean to indicate if the message should be
     *		added to a queue or sent directly.
     */
    public void sendMail (boolean shouldBlock) throws ERMailSender.ForwardException {
		try {
			// add the current message to the message stack
			boolean mailAccepted = false;

			this.finishMessagePreparation ();

            while (!mailAccepted) {
				this.mimeMessage ().setSentDate (new Date ());

                try {
					ERMailSender.sharedMailSender ().sendMessage (this.buildMessage (), shouldBlock);
					mailAccepted = true;
				} catch (ERMailSender.SizeOverflowException e) {
                    // The mail sender is overflowed, we need to wait
                    try {
                        // Ask the current thread to stop computing for a little while
                        Thread.currentThread ().sleep (ERJavaMail.sharedInstance ().milliSecondsWaitIfSenderOverflowed ());
                    } catch (InterruptedException ie) {
                        log.warn ("Caught InterruptedException in ERMailDelivery:");
                        log.warn (ERXUtilities.stackTrace (ie));
                    }
                } catch (ERMailSender.Exception e) {
					log.warn ("ERMailSender.Exception exception caught, re-throwing exception.");
					throw new ERMailSender.ForwardException (e);
				}
			}
		} catch (MessagingException e) {
			log.warn ("MessagingException exception caught, re-throwing exception.");
			throw new ERMailSender.ForwardException (e);
		} finally {
			this.setMimeMessage (null);
		}
    }

    protected void finishMessagePreparation () throws MessagingException {
		DataHandler messageDataHandler = messageDataHandler = this.prepareMail ();

		// Add all the attachements to the javamail message
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

		// If the xMailer property has not been set, check if one has been provided
		// in the System properties
		if ((this.xMailerHeader () == null) &&
	        (ERJavaMail.sharedInstance ().defaultXMailerHeader () != null))
			this.setXMailerHeader (ERJavaMail.sharedInstance ().defaultXMailerHeader ());
			
		this.mimeMessage ().saveChanges ();
    }

    /** Sets addresses regarding their recipient type in current message */
    private void setAddresses (NSArray addressesArray, Message.RecipientType type)
        throws MessagingException, AddressException {
			InternetAddress [] addresses = null;

			if (!ERJavaMail.sharedInstance ().centralize ())
				addresses = ERMailUtils.convertNSArrayToInternetAddresses (addressesArray);
			else
				addresses = new InternetAddress [] { new InternetAddress (ERJavaMail.sharedInstance ().adminEmail ()) };

			this.mimeMessage ().setRecipients (type, addresses);
		}

    /**
	 * Abstract method called by subclasses for doing pre-processing before sending the mail.
     * @return the multipart used to put in the mail.
     */
    protected abstract DataHandler prepareMail () throws MessagingException;
}
