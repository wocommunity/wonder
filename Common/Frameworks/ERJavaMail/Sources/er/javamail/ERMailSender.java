/*
 $Id$
 
 ERMailSender.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import er.extensions.*;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.appserver.WOApplication;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Vector;
import java.lang.reflect.*;

/** This class is used to send mails in a threaded way.<BR>
    This is needed in WebObjects because if sending 20 mails takes 40 seconds,
    then the user must wait 40 seconds before attempting to use the application.
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

	public static class Exception extends java.lang.Exception {
		public Exception () { super (); }
	}

	public static class SizeOverflowException extends ERMailSender.Exception {
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
    
    /** Puts a JavaMail MimeMessage in the message queue and sends it ASAP. */
    public void sendMessage (ERMessage message) throws ERMailSender.Exception {
		try {
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

    /** Don't call this method, this is the thread run loop
        and is automatically called. */
    public void run () {
        Address[] invalidEmails = null;
        Vector invalidEmailsVector = new Vector();
        while (true) {
            try {                
                if (threadSuspended) {
                    synchronized (this) {
                        while (threadSuspended)
                            this.wait (milliSecondsWaitRunLoop);
                    }
                }
            } catch (InterruptedException e) {
            }

            Object callbackObject = null;

            // If there are still messages pending ...
			Session session = null;
			Transport transport = null;
			if (!messages.empty ()) {
				session = ERJavaMail.sharedInstance ().newSession ();
				try {
					transport = session.getTransport ("smtp");
					transport.connect ();
				} catch (java.lang.Exception e) {
					// FIXME: handle this exception
					e.printStackTrace ();
				}
			}

            while (!messages.empty ()) {
                ERMessage message = (ERMessage)messages.pop();
                MimeMessage aMessage = message.mimeMessage();
                callbackObject = message.callbackObject();

				// Send the message
				try {
					transport.sendMessage (aMessage, aMessage.getAllRecipients());
                } catch (java.lang.Exception e) {
                    stats.incrementErrorCount ();
                    if (e instanceof javax.mail.SendFailedException) {
                        SendFailedException sfex = (SendFailedException)e;
                        invalidEmails = sfex.getInvalidAddresses();
                        if (invalidEmails != null) {
                            for (int i = 0; i < invalidEmails.length; i++) {
                                invalidEmailsVector.addElement(invalidEmails[i] + "");
                            }
                        }
                        
                    }

                    e.printStackTrace ();
                } finally {
                    stats.incrementMailCount ();
                }
            }

			if (transport != null) {
				try {
					transport.close ();
				} catch (java.lang.Exception e) {
					e.printStackTrace ();
					// FIXME: handle this exception
				}
			}

            /** Execute the callback method to notify the calling application of
               any invalid emails. anObject is an object that comes from the calling
               application. It's a way to relate any status conditions in ERJavaMail
               with the calling application. For example, if you have your own "Message"
               class, you can pass it along with the list of e-mails to send to
               ERJavaMail. If an error occurs, you can set an error state on your
               own Message object. Or if the e-mails are succcessfully sent, you can
               update your Message object with a success state. */
            
            if (callbackObject != null) {
                try {
                    Class c = Class.forName (ERMailDelivery.callBackClassName);  
                    Class[] parameterTypes = new Class[] {callbackObject.getClass(), invalidEmailsVector.getClass()};
                    Method m = c.getMethod (ERMailDelivery.callBackMethodName, parameterTypes);
                    Object[] args = new Object[] {callbackObject, invalidEmailsVector};
                    m.invoke(c.newInstance(), args);
                } catch (ClassNotFoundException cnfe) {
                    log.error ("ERMailSender. Unable to find class: " + ERMailDelivery.callBackClassName);
					throw new NSForwardException (cnfe);
                } catch (NoSuchMethodException nsme) {
                    log.error ("ERMailSender. Unable to find method: " + ERMailDelivery.callBackMethodName);
					throw new NSForwardException (nsme);
                } catch (IllegalAccessException iae) {
                    log.error ("ERMailSender. IllegalAccessException: " + iae.getMessage());
					throw new NSForwardException (iae);
                } catch (InvocationTargetException ite) {
                    log.error ("ERMailSender. InvocationTargetException: " + ite.getMessage());
					throw new NSForwardException (ite);
                } catch (InstantiationException ie) {
                    log.error ("ERMailSender. InstantiationException: " + ie.getMessage());
					throw new NSForwardException (ie);
                }
            }        

            threadSuspended = true;
        }
    }
    
    /** This class is about logging mail event for stats purposes.
        More stats to come in the future. */
    public class Stats {
        private int errorCount = 0;
        private int mailCount  = 0;

        /** @return the count of error that were encountered during mail seonding process */
        public synchronized int errorCount () 		{	return errorCount;	}

        /** @return the total count of mails being sent.  This number does not take in
            accordance the number of errors.  To get the actual count of mail sent without
            error use 'errorCount - mailCount'. */
        public synchronized int mailCount ()		{	return mailCount;	}

        /** @return the current queue size. This method is useful for simplistic
            load balancing between apps that are supposed to send mails */
        public synchronized int currentQueueSize ()	{	return messages.size ();	}

        private void incrementErrorCount () {	errorCount++;		}
        private void incrementMailCount ()  {	mailCount++;		}
    }

}
