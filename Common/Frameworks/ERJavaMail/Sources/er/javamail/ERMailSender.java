/*
  $Id$

  ERMailSender.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import er.extensions.*;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSArray;
import com.webobjects.appserver.WOApplication;

import javax.mail.*;
import javax.mail.internet.*;
import java.lang.reflect.*;

/** This class is used to send mails in a threaded way.<BR> This is
    needed in WebObjects because if sending 20 mails takes 40 seconds,
    then the user must wait 40 seconds before attempting to use
    the application.
    @author Camille Troillard <tuscland@mac.com> */
public class ERMailSender extends Thread {

    static ERXLogger log = ERXLogger.getERXLogger (ERMailSender.class);

    private static ERMailSender _sharedMailSender;

    private Stats stats;
    private MimeMessage message;
    private ERQueue messages = new ERQueue (50); // defaults with a limit of 50 messages

    // For thread management
    private boolean threadSuspended = false;
    private int milliSecondsWaitRunLoop = 5000;

    /**
     * Exception class for alerting about a stack overflow
     */
    public static class SizeOverflowException extends Exception  {
        public SizeOverflowException () { super (); }
    }
    
    private ERMailSender () {
        super ("ERMailSender");
        this.setPriority (Thread.MIN_PRIORITY);
        stats = new Stats ();

        if (WOApplication.application ().isDebuggingEnabled ())
            milliSecondsWaitRunLoop = 2000;
    }

    /** @return the shared instance of the singleton ERMailSender object */
    public static ERMailSender sharedMailSender () {
        if (_sharedMailSender == null)
            _sharedMailSender = new ERMailSender ();
        return _sharedMailSender;
    }

    /** @return the stats associated with this ERMailSender object */
    public Stats stats () {
        return stats;
    }

    /** Sends a message in a non-blocking way.<br> This means that the
	thread won't be blocked, but the message will be queued before
	being delivered. */
    public void sendMessageDeffered (ERMessage message) throws ERMailSender.SizeOverflowException {
        try {
	    log.debug ("Adding a message in the queue");
            messages.push (message);
        } catch (ERQueue.SizeOverflowException e) {
            throw new ERMailSender.SizeOverflowException ();
        }

        threadSuspended = false;

        // If we have not started to send mails, start the thread
        if (!this.isAlive ()) {
            this.start ();
            return;
        }
    }

    /** Sends a message immediately.<br>
	This means that the thread could be blocked if the message takes time to be delivered. */
    public void sendMessageNow (ERMessage message) {
	Transport transport = this._connectedTransportForSession (ERJavaMail.sharedInstance ().defaultSession ());

	try {
	    this._sendMessageNow (message, transport);
	} catch (MessagingException e) {
	    if (log.isDebugEnabled ())
		log.debug ("Caught exception when sending mail in a non-blocking manner.", e);
	    throw new NSForwardException (e);
	} finally {
	    // CHECKME (camille):
	    // Should we really close this default transport instance?
	    // I think there is no need to do so and that it should be closed
	    // when the ERMailSender is finalized
	    if (transport != null) {
		try {
		    transport.close ();
		} catch (MessagingException e) {
		    // Fatal exception ... we must at least notify the use
		    log.error ("Caught exception when closing transport.", e);
		    throw new RuntimeException ("Unable to open nor close the messaging transport channel.");
		}
	    }
	}
    }

    /** Common method used by 'sendMessageNow' and
	'sendMessageDeffered' (actully the 'run' method when the 
	thread is running) to send a message.<br> This method sends
	the message and increments the processed mail count.  If an
	exception occurs while sending the mail, and if a callback
	object has been given, the notifyInvalidEmails method is
	called.<br> If a MessagingException is thrown, then the
	exception is catched and rethrown immediately, thus letting us
	to process another callbacks or not.  For example, This is
	used when sendMessageNow is used, the MessagingException is
	encapsulated in a ERMailSender.ForwardException, and thrown to
	the user. */
    protected void _sendMessageNow (ERMessage message, Transport transport) throws MessagingException {
	MimeMessage aMessage  = message.mimeMessage ();
	Object callbackObject = message.callbackObject ();
	MessagingException exception = null;

	// Send the message
	try {
	    boolean debug = log.isDebugEnabled ();
	    if (debug) log.debug ("Sending a message ...");
	    transport.sendMessage (aMessage, aMessage.getAllRecipients ());
	    if (debug) log.debug ("Message sent.");
	} catch (SendFailedException e) {	
	    if (log.isDebugEnabled ())
		log.debug ("Failed to send message: " + e.getMessage ());
	    stats.incrementErrorCount ();

	    if (callbackObject != null) {
		SendFailedException sfex = (SendFailedException)e;
		NSArray invalidEmails = ERMailUtils.convertInternetAddressesToNSArray
		    ((InternetAddress [])sfex.getInvalidAddresses ());
		this.notifyInvalidEmails (callbackObject, invalidEmails);
	    }
	    
	    exception = e;
	} catch (MessagingException e) {
	    exception = e;
	} finally {
	    stats.incrementMailCount ();
	    if (exception != null)
		throw exception;
	}
    }

    /** Utility method that gets the SMTP Transport method for a session and
	connects the Transport before returning it. */
    protected Transport _connectedTransportForSession (javax.mail.Session session) {
	Transport transport = null;
	try {
	    transport = session.getTransport ("smtp");
	    if (!transport.isConnected())
		transport.connect();
	} catch (MessagingException e) {
	    log.error ("Unable to connect to SMTP Transport.", e);
	}

	return transport;
    }
	
    /** Don't call this method, this is the thread run loop
        and is automatically called. */
    public void run () {
        while (true) {
            try {
                if (threadSuspended) {
                    synchronized (this) {
                        while (threadSuspended)
                            this.wait (milliSecondsWaitRunLoop);
                    }
                }
            } catch (InterruptedException e) {
		log.warn ("ERMailSender thread has been interrupted.");
                threadSuspended = true;
                return;
            }

	    // If there are still messages pending ...
	    if (!messages.empty ()) {
		Session session     = null;
		Transport transport = null;
		session   = ERJavaMail.sharedInstance ().newSession ();
		transport = this._connectedTransportForSession (session);

		try {
		    if (!transport.isConnected ())
			transport.connect();
		} catch (MessagingException e) {
		    // Notify error in logs
		    log.error ("Unable to connect transport.", e);

		    // Exit run loop
		    throw new RuntimeException ("Unable to connect transport.");
		}

		while (!messages.empty ()) {
		    ERMessage message = (ERMessage)messages.pop();
		    try {
			this._sendMessageNow (message, transport);
		    } catch (MessagingException e) {
			// Here we get all the exceptions that are
			// not 'SendFailedException's.
			// All we can do is warn the admin.
			log.error ("Fatal Messaging Exception. Can't send the mail.", e);
		    }
		}

		try {
		    if (transport != null)
			transport.close ();
		} catch (MessagingException e) /* once again ... */ {
		    log.warn ("Unable to close transport.  Perhaps it has already been closed?", e);
		}
	    }

            threadSuspended = true;
        }
    }

    /** Executes the callback method to notify the calling application of
	any invalid emails. */
    protected void notifyInvalidEmails (Object callbackObject, NSArray invalidEmails) {
	// FIXME: We need to refactor this to use NSNotificationCenter !!!
	try {
	    Class c = Class.forName (ERMailDelivery.callBackClassName);
	    Class[] parameterTypes = new Class[] {callbackObject.getClass(), invalidEmails.getClass()};
	    Method m = c.getMethod (ERMailDelivery.callBackMethodName, parameterTypes);
	    Object[] args = new Object[] {callbackObject, invalidEmails};
	    m.invoke (c.newInstance(), args);
	} catch (ClassNotFoundException cnfe) {
	    log.error ("ERMailSender. Unable to find class: " + ERMailDelivery.callBackClassName);
	    throw new NSForwardException (cnfe);
	} catch (NoSuchMethodException nsme) {
	    log.error ("ERMailSender. Unable to find method: " + ERMailDelivery.callBackMethodName);
	    throw new NSForwardException (nsme);
	} catch (java.lang.Exception e) {
	    log.error ("Exception occured: " + e.getMessage(), e);
	    throw new NSForwardException (e);
	}
    }

    /** This class is about logging mail event for stats purposes.
        More stats to come in the future. */
    public class Stats {
        private int errorCount = 0;
        private int mailCount  = 0;

        /** @return the count of error that were encountered during mail seonding process */
        public synchronized int errorCount () {
	    return errorCount;
	}

        /** @return the total count of mails being sent.  This number does not take in
            accordance the number of errors.  To get the actual count of mail sent without
            error use 'errorCount - mailCount'. */
        public synchronized int mailCount () {
	    return mailCount;
	}

        /** @return the current queue size. This method is useful for simplistic
            load balancing between apps that are supposed to send mails */
        public synchronized int currentQueueSize () {
	    return messages.size ();
	}

        private void incrementErrorCount () {
	    errorCount++;
	}
        
	private void incrementMailCount () {
	    mailCount++;
	}
    }

}
