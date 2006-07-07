/*
 $Id$

 ERMailDelivery.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

import com.webobjects.foundation.*;

import er.extensions.*;

/** 
 * This is the main class for sending mail with the JavaMail API.
 * You typically don't create instances of this class since it is abstract. Instead, you should create instances of its 
 * subclasses that fitted with specifical use cases.<BR>
 * Here is an example of its usage.
 
 <PRE><code>
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
   </code></PRE>

@author Camille Troillard <tuscland@mac.com>
@author ak fixes
*/
public abstract class ERMailDelivery {

    private static Logger log = Logger.getLogger (ERMailDelivery.class);

    /** JavaMail session */
    private javax.mail.Session _session;

    /** Content of sent mail.  In one instance of ERMailDelivery, when creating multiple mails,
        you must be sure to call newMail () method before send a new mail in order to have a
        cleared fresh mail */
    protected MimeMessage _mimeMessage;

    /** NSArray of ERMailAttachment that must be binded to the message as ATTACHEMENT. */
    protected NSMutableArray _attachments;

    /** NSArray of ERMailAttachment that must be binded to the message as INLINE. */
    protected NSMutableArray _inlineAttachments;

    public static String DefaultCharset = System.getProperty ("er.javamail.defaultEncoding");
    public String _charset = DefaultCharset;

    public String charset () {
        return _charset;
    }

    public void setCharset (String charset) {
        _charset = charset;
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

    protected InternetAddress internetAddressWithEmailAndPersonal (String email, String personal) throws AddressException {
        InternetAddress address = null;

        if (personal != null) {
            address = new InternetAddress ();
            address.setAddress (email);

            try {
                address.setPersonal (personal, this.charset ());
            } catch (java.io.UnsupportedEncodingException ex) {
                // set the string anyway.
                try {
                    address.setPersonal (personal);
                } catch (Exception e) {
                    // give up ...
                }
            }
        } else {
            address = new InternetAddress (email);
        }

        return address;
    }

    /** Sets the from address for the current message instance */
    public void setFromAddress (String fromAddress) throws MessagingException, AddressException {
        setFromAddress(fromAddress, null);
    }

    /** Sets the from address for the current message instance using an email and the personal name. */
    public void setFromAddress (String fromAddress, String personalName) throws MessagingException, AddressException {
        InternetAddress address = this.internetAddressWithEmailAndPersonal (fromAddress, personalName);
        this.mimeMessage ().setFrom (address);
    }

    public void setToAddress (String toAddress) throws MessagingException, AddressException {
        this.setToAddress (toAddress, null);
    }

    /** Sets the to address for the current message instance using an email and the personal name. */
    public void setToAddress (String toAddress, String personalName) throws MessagingException, AddressException {
        InternetAddress address = this.internetAddressWithEmailAndPersonal (toAddress, personalName);
        setInternetAddresses (new NSArray (address), Message.RecipientType.TO);
    }

    /** Sets the to-addresses array for the current message instance */
    public void setToAddresses (NSArray toAddresses) throws MessagingException, AddressException {
        setAddresses (toAddresses, Message.RecipientType.TO, true);
    }

    /** Sets the reply-to address for the current message instance */
    public void setReplyToAddress (String toAddress) throws MessagingException, AddressException {
        setReplyToAddress(toAddress, null);
    }

    /** Sets the reply-to address for the current message instance */
    public void setReplyToAddress (String toAddress, String personalName) throws MessagingException, AddressException {
        InternetAddress addresses[] = new InternetAddress[] {this.internetAddressWithEmailAndPersonal (toAddress, personalName)};
        this.mimeMessage ().setReplyTo  (addresses);
    }

    /** Sets the cc-addresses array for the current message instance */
    public void setCCAddresses (NSArray ccAddresses) throws MessagingException, AddressException {
        setAddresses (ccAddresses, Message.RecipientType.CC, true);
    }

    /** Sets the bcc-addresses array for the current message instance */
    public void setBCCAddresses (NSArray bccAddresses) throws MessagingException, AddressException {
        setAddresses (bccAddresses, Message.RecipientType.BCC, true);
    }

    /** Sets the subject for the current message instance */
    public void setSubject (String subject) throws MessagingException {
        this.mimeMessage ().setSubject (ERMailUtils.encodeString (subject, this.charset ()));
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
        return headers != null && headers.length > 0 ? headers[0] : null;
    }

    /**
     * Builds an ERMessage for the current MimeMessage.
     * @return ERMessage for the current MimeMessage.
     */
    protected ERMessage buildMessage () {
        ERMessage message = new ERMessage ();
        message.setMimeMessage (this.mimeMessage ());
        return message;
    }

    /**
     * Sends the mail immediately.  The message is put in a FIFO queue managed
     * by a static threaded inner class
     */
    public void sendMail () {
        try {
            sendMail (false);
        } catch (NSForwardException e) {
            log.warn ("Sending mail in a non-blocking manner and a forward exception was thrown.", e);
        }
    }

    /**
     * Method used to construct a MimeMessage and then send
     * it.  This method can be specified to block until the
     * message is sent or to add the message to a queue and have
     * a callback object handle any exceptions that happen.
     * If sending is blocking then any exception thrown will be
     * wrapped in a general {@link NSForwardException}.
     * @param shouldBlock boolean to indicate if the message should be
     *		added to a queue or sent directly.
     */
    public void sendMail (boolean shouldBlock) {
        try {
            if (ERJavaMail.sharedInstance ().centralize ()) {
                if(ERJavaMail.sharedInstance ().adminEmail () == null) {
                    throw new IllegalArgumentException("When setting 'er.javamail.centralize=true' (which means you just test sending mails), you must also give a valid 'er.javamail.adminEmail=foo@bar.com' to which the mails are sent.");
                }
                InternetAddress [] addresses = new InternetAddress [] { new InternetAddress (ERJavaMail.sharedInstance ().adminEmail ()) };
                mimeMessage ().setRecipients (Message.RecipientType.TO, addresses);
                mimeMessage ().setRecipients (Message.RecipientType.CC, new InternetAddress []{});
                mimeMessage ().setRecipients (Message.RecipientType.BCC, new InternetAddress []{});
            }
            if(mimeMessage().getAllRecipients().length == 0) {
                return;
            }

             this.finishMessagePreparation ();
             ERMailSender sender = ERMailSender.sharedMailSender ();
             ERMessage message    = this.buildMessage ();
             
            if (shouldBlock)
                sender.sendMessageNow (message);
            else {
                // add the current message to the message stack
                boolean mailAccepted = false;
                while (!mailAccepted) {
                    try {
                        sender.sendMessageDeffered (message);
                        mailAccepted = true;                       
                    } catch (ERMailSender.SizeOverflowException e) {
                        // The mail sender is overflowed, we need to wait
                        try {
                            // Ask the current thread to stop
                            // computing for a little while.
                            // Here, we make the assumption that
                            // the current thread is the one that
                            // feeds the ERMailSender.
                            Thread.sleep
                                (ERJavaMail.sharedInstance ().milliSecondsWaitIfSenderOverflowed ());
                        } catch (InterruptedException ie) {
                            log.warn ("Caught InterruptedException.", ie);
                        }
                    }
                }
            }
        } catch (MessagingException e) {
            log.warn ("MessagingException exception caught, re-throwing exception.", e);
            throw new NSForwardException (e);
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
            (ERJavaMail.sharedInstance ().defaultXMailerHeader () != null)) {
            this.setXMailerHeader (ERJavaMail.sharedInstance ().defaultXMailerHeader ());
        }

        this.mimeMessage ().setSentDate (new Date ());
        this.mimeMessage ().saveChanges ();
    }

    /**
     * Sets addresses using an NSArray of InternetAddress objects.
     */
    public void setInternetAddresses (NSArray addresses,
                                      Message.RecipientType type) throws MessagingException {
        if ((type == null) || (addresses == null) || (addresses.count () == 0)) {
            // don't do anything.
            return;
        }

        InternetAddress [] internetAddresses = new InternetAddress [addresses.count ()];
        for (int i=0 ; i<addresses.count () ; i++) {
            internetAddresses [i] = (InternetAddress) addresses.objectAtIndex (i);
        }

        this.mimeMessage ().setRecipients (type, internetAddresses);
    }

    /**
     * Sets addresses regarding their recipient type in the current message.
     * Has the option to filter the address list based on the white and black
     * lists.
     */
    private void setAddresses (NSArray addressesArray,
            Message.RecipientType type,
            boolean filterAddresses)
    throws MessagingException, AddressException {
        if (filterAddresses) {
            addressesArray = ERJavaMail.sharedInstance().filterEmailAddresses(addressesArray);
        }
        if (addressesArray.count () == 0) {
            // don't do anything.
            return;
        }
        InternetAddress [] addresses = ERMailUtils.convertNSArrayToInternetAddresses (addressesArray);
        this.mimeMessage ().setRecipients (type, addresses);
    }

    /**
     * Abstract method called by subclasses for doing pre-processing before sending the mail.
     * @return the multipart used to put in the mail.
     */
    protected abstract DataHandler prepareMail () throws MessagingException;
    
    /** Callback class name. Used to have ERMail call a method on a class
     after a message has been sent 
     @deprecated*/
    public static String callBackClassName = null;
    /** @deprecated */
    public static String callBackMethodName = null;
    /** callbackObject to refer to in the calling program 
     @deprecated */
    public Object _callbackObject = null;
    
    /** Sets the callback class and method name
     @deprecated*/
    public static void setCallBackClassWithMethod (String className, String methodName) {
        callBackClassName = className;
        callBackMethodName = methodName;
    }
    
    
    /** Sets the object for the message. This is the identifying object from the calling program.
     @deprecated */
    public void setCallbackObject (Object obj) {
        _callbackObject = obj;
    }
    /** @deprecated */
    public Object callbackObject () {
        return _callbackObject;
    }
    
}


