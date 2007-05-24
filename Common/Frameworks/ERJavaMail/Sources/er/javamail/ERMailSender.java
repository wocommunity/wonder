/*
 $Id$

 ERMailSender.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.net.ConnectException;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXUnitAwareDecimalFormat;

/**
 * This class is used to send mails in a threaded way.<BR>
 * This is needed in WebObjects because if sending 20 mails takes 40 seconds, then the user must wait 40 seconds before
 * attempting to use the application.
 * 
 * @author Camille Troillard <tuscland@mac.com>
 * @author Tatsuya Kawano <tatsuyak@mac.com>
 * @author Max Muller <maxmuller@mac.com>
 */
public class ERMailSender implements Runnable {

	public static final String InvalidEmailNotification = "InvalidEmailNotification";

	static Logger log = Logger.getLogger(ERMailSender.class);

	private static ERMailSender _sharedMailSender;

	private Stats stats;

	// Holds sending messages. The queue size can be set by
	// er.javamail.senderQueue.size property
	private ERQueue messages;
	// For thread management

	private int milliSecondsWaitRunLoop = 30000;
	
	private Thread _senderThread;

	/**
	 * Exception class for alerting about a stack overflow
	 */
	public static class SizeOverflowException extends Exception {
		public SizeOverflowException(Exception e) {
			super(e);
		}
	}

	private ERMailSender() {
		stats = new Stats();
		messages = new ERQueue(ERJavaMail.sharedInstance().senderQueueSize());

		if (log.isDebugEnabled()) {
			log.debug("ERMailSender initialized (JVM heap size: " + stats.formattedUsedMemory() + ")");
		}
	}

	/** @return the shared instance of the singleton ERMailSender object */
	public static synchronized ERMailSender sharedMailSender() {
		if (_sharedMailSender == null) {
			_sharedMailSender = new ERMailSender();
		}
		return _sharedMailSender;
	}

	/** @return the stats associated with this ERMailSender object */
	public Stats stats() {
		return stats;
	}

	/**
	 * Sends a message in a non-blocking way.<br>
	 * This means that the thread won't be blocked, but the message will be queued before being delivered.
	 */
	public void sendMessageDeffered(ERMessage message) throws ERMailSender.SizeOverflowException {
		try {
			String allRecipientsString = null;
			if (log.isDebugEnabled()) {
				try {
					allRecipientsString = message.allRecipientsAsString();
				}
				catch (MessagingException ex) {
					allRecipientsString = "(not available)";
				}
				// log.debug ("Adding a message in the queue: \n" + allRecipientsString);
			}

			messages.push(message);
			stats.updateMemoryUsage();

			if (log.isDebugEnabled())
				log.debug("(" + stats.formattedUsedMemory() + ") Added the message in the queue: " + allRecipientsString);
		}
		catch (ERQueue.SizeOverflowException e) {
			throw new ERMailSender.SizeOverflowException(e);
		}

		synchronized (messages) {
			// If we have not started to send mails, start the thread
			if (_senderThread == null) {
				_senderThread = new Thread(this, "ERMailSender");
				_senderThread.setPriority(Thread.MIN_PRIORITY);
				_senderThread.start();
			}
			else {
				messages.notifyAll();
			}
		}
	}

	/**
	 * Sends a message immediately.<br>
	 * This means that the thread could be blocked if the message takes time to be delivered.
	 */
	public void sendMessageNow(ERMessage message) {
		Transport transport = null;
		try {
			transport = this._connectedTransportForSession(ERJavaMail.sharedInstance().defaultSession(), ERJavaMail.sharedInstance().smtpProtocol(), false);
			this._sendMessageNow(message, transport);
		}
		catch (MessagingException e) {
			if (log.isDebugEnabled()) {
				log.debug("Caught exception when sending mail in a non-blocking manner.", e);
			}
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
		finally {
			// CHECKME (camille):
			// Should we really close this default transport instance?
			// I think there is no need to do so and that it should be closed
			// when the ERMailSender is finalized
			if (transport != null) {
				try {
					transport.close();
				}
				catch (MessagingException e) {
					// Fatal exception ... we must at least notify the use
					log.error("Caught exception when closing transport.", e);
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}
		}
	}

	/**
	 * Common method used by 'sendMessageNow' and 'sendMessageDeffered' (actully the 'run' method when the thread is
	 * running) to send a message.<br>
	 * This method sends the message and increments the processed mail count. If an exception occurs while sending the
	 * mail, and if a callback object has been given, the notifyInvalidEmails method is called.<br>
	 * If a MessagingException is thrown, then the exception is catched and rethrown immediately, thus letting us to
	 * process another callbacks or not. For example, This is used when sendMessageNow is used, the MessagingException
	 * is encapsulated in a ERMailSender.ForwardException, and thrown to the user.
	 */
	protected void _sendMessageNow(ERMessage message, Transport transport) throws MessagingException {
		boolean debug = log.isDebugEnabled();
		MimeMessage aMessage = message.mimeMessage();
		MessagingException exception = null;

		if (message.shouldSendMessage()) {
			// Send the message
			try {

				if (debug) {
					log.debug("Sending a message ... " + aMessage);
					Enumeration e = aMessage.getAllHeaderLines();
					while (e.hasMoreElements()) {
						String header = (String) e.nextElement();
						log.debug(header);
					}
				}
				transport.sendMessage(aMessage, aMessage.getAllRecipients());
				message._deliverySucceeded();
				if (debug)
					log.debug("Done.");
				stats.updateMemoryUsage();

				if (debug) {
					String allRecipientsString = null;
					try {
						allRecipientsString = message.allRecipientsAsString();
					}
					catch (MessagingException ex) {
						allRecipientsString = "(not available)";
					}
					log.debug("(" + stats.formattedUsedMemory() + ") Message sent: " + allRecipientsString);
				}
			}
			catch (SendFailedException e) {
				if (debug)
					log.debug("Failed to send message: \n" + message.allRecipientsAsString() + e.getMessage());
				stats.incrementErrorCount();

				NSArray invalidEmails = ERMailUtils.convertInternetAddressesToNSArray(e.getInvalidAddresses());
				this.notifyInvalidEmails(invalidEmails);
				message._invalidRecipients(invalidEmails);

				exception = e;
			}
			catch (MessagingException e) {
				exception = e;
			}
			catch (Throwable t) {
				log.error("An unexpected error occured while sending message: " + message + " mime message: " + aMessage + " sending to: " + aMessage.getAllRecipients() + " transport: " + transport, t);
				// Need to let someone know that something very, very bad happened
				message._deliveryFailed(t);
				throw NSForwardException._runtimeExceptionForThrowable(t);
			}
			finally {
				stats.incrementMailCount();
				if (exception != null) {
					message._deliveryFailed(exception);
					throw exception;
				}
			}
		}
		else if (log.isDebugEnabled()) {
			log.debug("Message has instructed me not to send it, not sending message: " + message);
		}
	}

	/**
	 * Utility method that gets the SMTP Transport method for a session and connects the Transport before returning it.
	 */
	protected Transport _connectedTransportForSession(javax.mail.Session session, String smtpProtocol, boolean _throwExceptionIfConnectionFails) throws MessagingException {
		Transport transport = null;
		try {
			transport = session.getTransport(smtpProtocol);
			if (!transport.isConnected()) {
				String userName = session.getProperty("mail." + smtpProtocol + ".user");
				String password = session.getProperty("mail." + smtpProtocol + ".password");
				if (userName != null && password != null) {
					transport.connect(session.getProperty("mail." + smtpProtocol + ".host"), userName, password);
				}
				else {
					transport.connect();
				}
			}
		}
		catch (MessagingException e) {
			log.error("Unable to connect to SMTP Transport. MessagingException: " + e.getMessage(), e);
			if (_throwExceptionIfConnectionFails) {
				throw e;
			} else {
				log.error("Unable to connect to SMTP Transport. MessagingException: " + e.getMessage(), e);
			}
		}

		return transport;
	}

	/**
	 * Don't call this method, this is the thread run loop and is automatically called.
	 */
	public void run() {
		try {
			while (true) {
				synchronized (messages) {
					while (messages.empty()) {
						messages.wait(milliSecondsWaitRunLoop);
					}
				}

				// If there are still messages pending ...
				if (!messages.empty()) {
					Session session = ERJavaMail.sharedInstance().newSession();
					String smtpProtocol = ERJavaMail.sharedInstance().smtpProtocol();
					try {
						Transport transport = this._connectedTransportForSession(session, smtpProtocol, true);
						if (!transport.isConnected()) {
							transport.connect();
						}

						while (!messages.empty()) {
							ERMessage message = (ERMessage) messages.pop();
							try {
								this._sendMessageNow(message, transport);
							} catch(SendFailedException ex) {
								log.error("Can't send message: " + message + ": " + ex, ex);
							}
							// if (useSenderDelay) {
							// this.wait (senderDelayMillis);
							// }
							// Here we get all the exceptions that are
							// not 'SendFailedException's.
							// All we can do is warn the admin.
						}

						if (transport != null) {
							transport.close();
						}
					} catch (MessagingException e) {
						log.error("General mail error: " + e, e);
						if (e.getNextException() instanceof ConnectException) {
							log.error("Can't connect to mail server, waiting");
							Thread.sleep(10000);
						}
					}

				}
			}
		}
		catch (InterruptedException e) {
			log.warn("ERMailSender thread has been interrupted.");
			//return;
		}
		// assures the thread will get restarted next time around.
		_senderThread = null;
	}

	/**
	 * Executes the callback method to notify the calling application of any invalid emails.
	 */
	protected void notifyInvalidEmails(NSArray invalidEmails) {
		NSNotification notification = new NSNotification(InvalidEmailNotification, invalidEmails);
		NSNotificationCenter.defaultCenter().postNotification(notification);
	}

	/**
	 * This class is about logging mail event for stats purposes. More stats to come in the future.
	 */
	public class Stats {
		private NSTimestamp lastResetTime = new NSTimestamp();
		private int errorCount = 0;
		private int mailCount = 0;
		private double _peakMemoryUsage = 0.0d;
		private Runtime _runtime;
		private ERXUnitAwareDecimalFormat _decimalFormatter;

		public Stats() {
			_decimalFormatter = new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
			_decimalFormatter.setMaximumFractionDigits(2);
			_runtime = Runtime.getRuntime();
			updateMemoryUsage();
		}

		/** Resets statistics information */
		public synchronized void reset() {
			String savedStatsString = this.toString();
			errorCount = 0;
			mailCount = 0;
			_peakMemoryUsage = 0.0d;
			updateMemoryUsage();
			lastResetTime = new NSTimestamp();
			if (log.isDebugEnabled())
				log.debug(savedStatsString + " has been reset to initial value.");
		}

		/** @return the count of error that were encountered during mail seonding process */
		public synchronized int errorCount() {
			return errorCount;
		}

		/**
		 * @return the total count of mails being sent. This number does not take in accordance the number of errors. To
		 *         get the actual count of mail sent without error use 'errorCount - mailCount'.
		 */
		public synchronized int mailCount() {
			return mailCount;
		}

		/**
		 * @return the current queue size. This method is useful for simplistic load balancing between apps that are
		 *         supposed to send mails
		 */
		public synchronized int currentQueueSize() {
			return messages.size();
		}

		private void incrementErrorCount() {
			errorCount++;
		}

		private void incrementMailCount() {
			mailCount++;
		}

		/** @return the timestamp that respresents when the stats object was reset. */
		public NSTimestamp lastResetTime() {
			return lastResetTime;
		}

		private void updateMemoryUsage() {
			long currentMemoryUsed = usedMemory();
			if (currentMemoryUsed > _peakMemoryUsage)
				_peakMemoryUsage = currentMemoryUsed;
		}

		public long usedMemory() {
			long totalMemory = _runtime.totalMemory();
			long freeMemory = _runtime.freeMemory();
			long usedMemory = totalMemory - freeMemory;
			return usedMemory;
		}

		public String formattedUsedMemory() {
			return _decimalFormatter.format(usedMemory());
		}

		public double peakMemoryUsage() {
			return _peakMemoryUsage;
		}

		public String formattedPeakMemoryUsage() {
			return _decimalFormatter.format(_peakMemoryUsage);
		}

		/** @return a string representation of the Stats object. */
		public String toString() {
			return "<" + this.getClass().getName() + " lastResetTime: " + lastResetTime() + ", mailCount: " + mailCount() + ", errorCount: " + errorCount() + ", currentQueueSize: " + currentQueueSize() + ", peakMemoryUsage: " + formattedPeakMemoryUsage() + ">";
		}
	}
}
