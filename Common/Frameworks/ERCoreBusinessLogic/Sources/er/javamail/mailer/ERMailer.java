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
import er.extensions.ERXJobLoadBalancer.*;

import er.javamail.*;
import er.corebusinesslogic.*;

import java.util.Enumeration;
import java.io.File;
import javax.mail.*;

/**
 * Mailer bridge class. Used to pull mail out of the
 * ERMailMessage entity and send it via the ERJavaMail
 * framework for sending mail.
 */
public class ERMailer {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------    

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERMailer.class);
    public final static String MAIL_WORKER_IDENTIFICATION = "ERMailer";

    //	===========================================================================
    //	Class Variable(s)
    //	---------------------------------------------------------------------------
    
    /** holds a reference to the shared instance */
    protected static ERMailer instance;

    protected static Factory factory;

    private static final boolean _warnOnGeneralAdaptorExceptionLockingMessage =
        ERXProperties.booleanForKeyWithDefault("er.javamail.mailer.ERMailer.WarnOnGeneralAdaptorExceptionLockingMessage", true);
    
    //	===========================================================================
    //	Class Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Gets the current factory.  If the factory is unset, sets the factory to the default
     * factory.
     *
     * @return the factory
     */
    public static Factory factory() {
        if ( factory == null )
            factory = new DefaultFactory();

        return factory;
    }

    /**
     * Sets the factory.
     *
     * @param value new factory value
     */
    public static void setFactory(Factory value) {
        factory = value;
    }

    /**
     * Instantiates a new mailer instance using the factory and returns it.
     *
     * @return a new mailer instance.
     */
    public static ERMailer newMailer() {
        return factory().newMailer();
    }

    protected static boolean shouldDeleteSentMail() {
        return ERXProperties.booleanForKeyWithDefault("er.javamail.mailer.ERMailer.ShouldDeleteSentMail", true);
    }
    
    /**
     * Gets the shared mailer instance.
     * @return mailer singleton
     */
    public static ERMailer instance() {
        if ( instance == null )
            instance = newMailer();

        return instance;
    }

    //	===========================================================================
    //	Instance Variable(s)
    //	---------------------------------------------------------------------------

    /** Caches the message title prefix */
    protected String messageTitlePrefix;
    protected ERXJobLoadBalancer.WorkerIdentification workerIdentification;
    
    //  ===========================================================================
    //  Constructor(s)
    //  ---------------------------------------------------------------------------    
    
    public ERMailer() {
        workerIdentification=new ERXJobLoadBalancer.WorkerIdentification(MAIL_WORKER_IDENTIFICATION, 
                ERXJobLoadBalancer.jobLoadBalancer().workerInstanceIdentification());
        int ttl=ERXProperties.intForKeyWithDefault("er.javamail.mailer.ERMailer.WorkerTimeOut", 300000);
        ERXJobLoadBalancer.jobLoadBalancer().setTtlForWorkerType(MAIL_WORKER_IDENTIFICATION, ttl); // 5mns
    }
    


    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------    
    
    /**
     * Fetches all mail that is ready to
     * be sent from the ERMailMessage table
     * and sends the message using the
     * ERJavaMail framework for sending
     * messages.
     */

    	public boolean useJobLoadBalancer() {
    		return ERXProperties.booleanForKeyWithDefault("er.javamail.mailer.useERXJobLoadBalancer", false);
    	}
    
    public void processOutgoingMail() {
        if (log.isDebugEnabled())
            log.debug("Starting outgoing mail processing.");
        ERXFetchSpecificationBatchIterator iterator;
        if (useJobLoadBalancer()) {
    			JobSet idSpace=ERXJobLoadBalancer.jobLoadBalancer().idSpace(workerIdentification);
    			log.debug("Fetching messages "+idSpace);
    			iterator = ERCMailMessage.mailMessageClazz().batchIteratorForUnsentMessages(idSpace);
        } else {            
            iterator = ERCMailMessage.mailMessageClazz().batchIteratorForUnsentMessages();
        } 

        EOEditingContext ec = ERXEC.newEditingContext();
        iterator.setEditingContext(ec);
        ec.lock();
        try {
            iterator.batchCount();
        } finally {
            ec.unlock();
        }
        ec.dispose();
        while (iterator.hasNextBatch()) {
            EOEditingContext temp = ERXEC.newEditingContext();
            temp.lock();
            try {
                iterator.setEditingContext(temp);
                sendMailMessages(iterator.nextBatch());
            } finally {
                temp.unlock();
            }
            temp.dispose();
        }
        if (log.isDebugEnabled())
            log.debug("Done outgoing mail processing.");
    }
    
    /**
     * Sends an array of ERCMailMessage objects.
     * @param mailMessages array of messages to send
     */
    public void sendMailMessages(NSArray mailMessages) {
        if (mailMessages.count() > 0) {
            log.info("Sending " + mailMessages.count() + " mail message(s).");
            for (Enumeration messageEnumerator = mailMessages.objectEnumerator();
                 messageEnumerator.hasMoreElements();) {
                ERCMailMessage mailMessage = (ERCMailMessage)messageEnumerator.nextElement();

                if( !mailMessage.isReadyToSendState() ) { //due to the operation of the batch iterator, we may pull records that have already been sent
                    continue;
                }

                if (log.isDebugEnabled())
                    log.debug("Sending mail message: " + mailMessage);

                try {
                    ERMailDelivery delivery = createMailDeliveryForMailMessage(mailMessage);

                    if (delivery != null) {
                        mailMessage.setState(ERCMailState.PROCESSING_STATE);
                        mailMessage.editingContext().saveChanges(); // This will throw if optimistic locking occurs
                        delivery.sendMail(true);

                        mailMessage.setState(ERCMailState.SENT_STATE);
                        mailMessage.setDateSent(new NSTimestamp());                            
                        
                        if (shouldDeleteSentMail()) {
                            if (mailMessage.shouldArchiveSentMailAsBoolean()) {
                                mailMessage.archive();
                            }
                            // FIXME: Nasty stack overflow bug
                            if (!mailMessage.hasAttachments()) {
                                mailMessage.editingContext().deleteObject(mailMessage);                                
                            }
                        }
                        if (useJobLoadBalancer()) ERXJobLoadBalancer.jobLoadBalancer().heartbeat(workerIdentification);
                    } else {
                        log.warn("Unable to create mail delivery for mail message: " + mailMessage);
                    }
                } catch (EOGeneralAdaptorException ge) {
                    if ( _warnOnGeneralAdaptorExceptionLockingMessage )
                        log.warn("Caught general adaptor exception, reverting context. Might be running multiple mailers", ge);
                    mailMessage.editingContext().revert();
                } catch (Throwable e) {
                    if (e instanceof NSForwardException)
                        e = ((NSForwardException)e).originalException();
                    log.warn("Caught exception when sending mail: " + ERXUtilities.stackTrace(e));
                    log.warn("Message trying to send: " + mailMessage + " pk: " + mailMessage.primaryKey());
                    
                    // ENHANCEME: Need to implement a waiting state to retry sending mails.
                    mailMessage.setState(ERCMailState.EXCEPTION_STATE);
                    mailMessage.setExceptionReason(e.getMessage());
                    
                    // Report the mailing error
                    ERCoreBusinessLogic.sharedInstance().reportException(e, new NSDictionary(mailMessage.snapshot(),
                                                                                    "Mail Message Snapshot"));
                } finally {
                    // The editingcontext will not have any changes if an optimistic error occurred
                    if (mailMessage.editingContext().hasChanges()) {
                        try {
                            mailMessage.editingContext().saveChanges();
                        } catch (RuntimeException runtime) {
                            log.error("RuntimeException during save changes!", runtime);
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
    // ENHANCEME: Not handling double byte (Japanese) language
    public ERMailDelivery createMailDeliveryForMailMessage(ERCMailMessage message) throws MessagingException {
        ERMailDeliveryHTML mail = ERMailDeliveryHTML.newMailDelivery();

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
        mail.setSubject(messageTitlePrefix() + message.title());
        mail.setHTMLContent(message.text());
        
        if (message.plainText() != null)
            mail.setHiddenPlainTextContent(message.plainText());

        if (message.hasAttachments()) {
            for (Enumeration attachmentEnumerator = message.attachments().objectEnumerator();
                 attachmentEnumerator.hasMoreElements();) {
                File fileAttachment = ((ERCMessageAttachment)attachmentEnumerator.nextElement()).file();
                mail.addAttachment(new ERMailFileAttachment(fileAttachment.getName(), null, fileAttachment));
            }
        }
        return mail;
    }

    /**
     * The message title prefix is used to distiguish emails generated in different environments.
     * @return message title prefix
     */
    public String messageTitlePrefix() {
        if (messageTitlePrefix == null) {
            messageTitlePrefix = ERCoreBusinessLogic.staticStoredValueForKey("ERMailTitleEnvironmentPrefix");
            if (messageTitlePrefix == null) {
                messageTitlePrefix = "";
            }
        }
        return messageTitlePrefix;
    }

    //	===========================================================================
    //	Factory-related things
    //	---------------------------------------------------------------------------

    public static interface Factory {
        /**
         * Vends new instances of a mailer.  This is primarily used to set the static instance
         * of ERMailer.
         *
         * @return A new instance of an ERMailer or a subclass.
         */
        public ERMailer newMailer();
    }

    /**
     * Default factory.  Just vends back an ERMailer instance.
     */
    public static class DefaultFactory implements Factory {
        public ERMailer newMailer() {
            return new ERMailer();
        }
    }
}
