/*
 ERMailSender.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.formatters.ERXUnitAwareDecimalFormat;

/**
 * <span class="en">
 * This class is used to send mails in a threaded way.
 *
 * This is needed in WebObjects because if sending 20 mails takes 40 seconds, then the user must wait 40 seconds before
 * attempting to use the application.
 * </span>
 * 
 * <span class="ja">
 * このクラスはメール送信をスレッド系で送信します。
 * WebObjects には必要な方法です。なぜなら、メール 20通が約 40秒かかるとユーザが 40秒もアプリケーションが使えるようになるまでに
 * 待つ必要が発生します。
 * </span>
 * 
 * @author Camille Troillard <tuscland@mac.com>
 * @author Tatsuya Kawano <tatsuyak@mac.com>
 * @author Max Muller <maxmuller@mac.com>
 */
public class ERMailSender implements Runnable {

	public static final String InvalidEmailNotification = "InvalidEmailNotification";

	static Logger log = Logger.getLogger(ERMailSender.class);

	private static ERMailSender _sharedMailSender;

	private Stats _stats;

	// Holds sending messages. The queue size can be set by
	// er.javamail.senderQueue.size property
	private ERQueue<ERMessage> _messages;
	// For thread management
	private int _milliSecondsWaitRunLoop = 5000;
	
	private Thread _senderThread;

	/**
	 * <span class="en">
	 * Exception class for alerting about a stack overflow
	 * </span>
	 * 
	 * <span class="ja">
	 * オーバフローの例外発生クラス
	 * </span>
	 */
	public static class SizeOverflowException extends Exception {
		private static final long serialVersionUID = 1L;

		public SizeOverflowException(Exception e) {
			super(e);
		}
	}

	private ERMailSender() {
		_stats = new Stats();
		_messages = new ERQueue<ERMessage>(ERJavaMail.sharedInstance().senderQueueSize());

        if (WOApplication.application() == null || WOApplication.application ().isDebuggingEnabled()) {
            _milliSecondsWaitRunLoop = 2000;
        }
        
		if (log.isDebugEnabled()) {
			log.debug("ERMailSender initialized (JVM heap size: " + _stats.formattedUsedMemory() + ")");
		}
	}

	/** 
	 * <span class="en">
	 * @return the shared instance of the singleton ERMailSender object 
	 * </span>
	 * 
	 * <span class="ja">
	 * @return ERMailSender シングルトン・オブジェクトを戻します。
	 * </span>
	 */
	public static synchronized ERMailSender sharedMailSender() {
		if (_sharedMailSender == null) {
			_sharedMailSender = new ERMailSender();
		}
		return _sharedMailSender;
	}

	/** 
	 * <span class="en">
	 * @return the stats associated with this ERMailSender object
	 * </span>
	 * 
	 * <span class="ja">
	 * @return ERMailSender オブジェクトと関連されている統計を戻します 
	 * </span>
	 */
	public Stats stats() {
		return _stats;
	}

	/**
	 * <span class="en">
	 * Sends a message in a non-blocking way.
	 *
	 * This means that the thread won't be blocked, but the message will be queued before being delivered.
	 * 
	 * </span>
	 * <span class="ja">
	 * メッセージをブロックされない形で送信します。<br>
	 * スレッドはブロックされませんが、メッセージは送信する前にキューに入れられます。
	 * </span>
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

			_messages.push(message);
			_stats.updateMemoryUsage();

			if (log.isDebugEnabled())
				log.debug("(" + _stats.formattedUsedMemory() + ") Added the message in the queue: " + allRecipientsString);
		}
		catch (ERQueue.SizeOverflowException e) {
			throw new ERMailSender.SizeOverflowException(e);
		}

		synchronized (_messages) {
			// If we have not started to send mails, start the thread
			if (_senderThread == null) {
				_senderThread = new Thread(this, "ERMailSender");
				_senderThread.setPriority(Thread.MIN_PRIORITY);
				_senderThread.start();
			}
			else {
				_messages.notifyAll();
			}
		}
	}

	/**
	 * <span class="en">
	 * Sends a message immediately.
	 *
	 * This means that the thread could be blocked if the message takes time to be delivered.
	 * </span>
	 * 
	 * <span class="ja">
	 * メッセージを直ちに送信する<br>
	 * メッセージが送信に時間がかかりすぎるとスレッドがブロックされる可能性があります。
	 * </span>
	 */
	public void sendMessageNow(ERMessage message) {
		Transport transport = null;
		try {
			transport = _connectedTransportForSession(ERJavaMail.sharedInstance().sessionForContext(message.contextString()), ERJavaMail.sharedInstance().smtpProtocolForContext(message.contextString()), false);
			_sendMessageNow(message, transport);
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
	 * <span class="en">
	 * Common method used by 'sendMessageNow' and 'sendMessageDeffered' (actully the 'run' method when the thread is
	 * running) to send a message.
	 *
	 * This method sends the message and increments the processed mail count. If an exception occurs while sending the
	 * mail, and if a callback object has been given, the notifyInvalidEmails method is called.<br>
	 * If a MessagingException is thrown, then the exception is catched and rethrown immediately, thus letting us to
	 * process another callbacks or not. For example, This is used when sendMessageNow is used, the MessagingException
	 * is encapsulated in a ERMailSender.ForwardException, and thrown to the user.
	 * </span>
	 * 
	 * <span class="ja">
	 * 'sendMessageNow' と 'sendMessageDeffered' (実際は 'run' メソッド) のメール送信共通メソッドです。<br>
	 * このメソッドはメッセージを送信し、メール送信カウンターを進みます。メール送信中に例外が発生するとコールバック指定があれば、
	 * notifyInvalidEmails メソッドがコールされます。<br>
	 * MessagingException が発生すると例外がキャチュされ、再度発生させます。他のコールバックを対応できるようになります。
	 * 例えば、sendMessageNow が使用されている時 MessagingException は ERMailSender.ForwardException 内にカプセル化されます。
	 * </span>
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
					Enumeration<String> e = aMessage.getAllHeaderLines();
					while (e.hasMoreElements()) {
						String header = e.nextElement();
						log.debug(header);
					}
				}
				transport.sendMessage(aMessage, aMessage.getAllRecipients());
				message._deliverySucceeded();
				if (debug)
					log.debug("Done.");
				_stats.updateMemoryUsage();

				if (debug) {
					String allRecipientsString = null;
					try {
						allRecipientsString = message.allRecipientsAsString();
					}
					catch (MessagingException ex) {
						allRecipientsString = "(not available)";
					}
					log.debug("(" + _stats.formattedUsedMemory() + ") Message sent: " + allRecipientsString);
				}
			}
			catch (SendFailedException e) {
				if (debug)
					log.debug("Failed to send message: \n" + message.allRecipientsAsString() + e.getMessage());
				_stats.incrementErrorCount();

				NSArray<String> invalidEmails = ERMailUtils.convertInternetAddressesToNSArray(e.getInvalidAddresses());
				notifyInvalidEmails(invalidEmails);
				message._invalidRecipients(invalidEmails);

				exception = e;
			}
			catch (MessagingException e) {
				exception = e;
			}
			catch (Throwable t) {
				log.error("An unexpected error occured while sending message: " + message + " mime message: " + aMessage
						+ " sending to: " + Arrays.toString(aMessage.getAllRecipients()) + " transport: " + transport, t);
				// Need to let someone know that something very, very bad happened
				message._deliveryFailed(t);
				throw NSForwardException._runtimeExceptionForThrowable(t);
			}
			finally {
				_stats.incrementMailCount();
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
	 * <span class="en">
	 * Utility method that gets the SMTP Transport method for a session and connects the Transport before returning it.
	 * </span>
	 * 
	 * <span class="ja">
	 * セッションの SMTP トランスポート方法を取得するユーティリティー・メソッド。
	 * 戻す前にトランスポートへの接続を開始します。
	 * </span>
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
		} catch (MessagingException e) {
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
	 * <span class="en">
	 * Don't call this method, this is the thread run loop and is automatically called.
	 * </span>
	 * 
	 * <span class="ja">
	 * このメソッドをコールしないでください。
	 * これはスレッド実行ループで自動的に処理されます。
	 * </span>
	 */
	public void run() {
		try {
			while (true) {
				synchronized (_messages) {
					while (_messages.empty()) {
						_messages.wait(_milliSecondsWaitRunLoop);
					}
				}

				// If there are still messages pending ...
				if (!_messages.empty()) {
					Map<String, Transport> transports = new HashMap<String, Transport>();
					
					try {
						while (!_messages.empty()) {
							ERMessage message = _messages.pop();
		                    String contextString = message.contextString();
		                    String smtpProtocol = ERJavaMail.sharedInstance().smtpProtocolForContext(contextString);
		                    if (contextString == null) {
		                        contextString = "___DEFAULT___";
		                    }
		                    Transport transport = transports.get(contextString);
		                    if (transport == null) {
		                        Session session = ERJavaMail.sharedInstance().newSessionForMessage(message);
		                    	try {
		                    		transport = _connectedTransportForSession(session, smtpProtocol, true);
		                    	}
		                    	catch (MessagingException e) {
			        				message._deliveryFailed(e);
		                    		throw e;
		                    	}
		                        transports.put(contextString, transport);
		                    }
		                    try {
		                        if (!transport.isConnected()) {
		                            transport.connect();
		                        }
		                    } catch (MessagingException e) {
		                        // Notify error in logs
		                        log.error ("Unable to connect transport.", e);
		                        
		        				message._deliveryFailed(e);

		                        // Exit run loop
		                        throw new RuntimeException ("Unable to connect transport.", e);
		                    }
							try {
								_sendMessageNow(message, transport);
							} catch(SendFailedException ex) {
								log.error("Can't send message: " + message + ": " + ex, ex);
							}
							// if (useSenderDelay) {
							//     wait (senderDelayMillis);
							// }
							// Here we get all the exceptions that are
							// not 'SendFailedException's.
							// All we can do is warn the admin.
						}
					}
					catch (AuthenticationFailedException e) {
						log.error("Unable to connect to SMTP Transport. AuthenticationFailedException: " + e.getMessage() + " waiting 20 seconds", e);
						Thread.sleep(20000);
					}
					catch (MessagingException e) {
						if (e.getNextException() instanceof ConnectException) {
							log.error("Can't connect to mail server, waiting");
							Thread.sleep(10000);
						} else if (e.getNextException() instanceof UnknownHostException) {
							log.error("Can't find to mail server, exiting");
							return;
						} else {
							log.error("General mail error: " + e, e);
						}
					}
					finally {
						for (Transport transport : transports.values()) {
							try {
								if (transport != null) {
									transport.close();
								}
							} catch (MessagingException e) /* once again ... */ {
								log.warn ("Unable to close transport.  Perhaps it has already been closed?", e);
							}
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

	public ERQueue<ERMessage> messages() {
		return _messages;
	}
	
	/**
	 * <span class="en">
	 * Executes the callback method to notify the calling application of any invalid emails.
	 * </span>
	 * 
	 * <span class="ja">
	 * メール送信失敗のアプリケーションのコールバックを実行します。
	 * </span>
	 */
	protected void notifyInvalidEmails(NSArray<String> invalidEmails) {
		NSNotification notification = new NSNotification(InvalidEmailNotification, invalidEmails);
		NSNotificationCenter.defaultCenter().postNotification(notification);
	}

	/**
	 * <span class="en">
	 * This class is about logging mail event for stats purposes. More stats to come in the future.
	 * </span>
	 * 
	 * <span class="ja">
	 * このクラスはメール・イベントを統計のためにログします。
	 * </span>
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

		/** 
		 * <span class="en">
		 * Resets statistics information 
		 * </span>
		 * 
		 * <span class="ja">
		 * 統計情報のリセット
		 * </span>
		 */
		public synchronized void reset() {
			String savedStatsString = toString();
			errorCount = 0;
			mailCount = 0;
			_peakMemoryUsage = 0.0d;
			updateMemoryUsage();
			lastResetTime = new NSTimestamp();
			if (log.isDebugEnabled())
				log.debug(savedStatsString + " has been reset to initial value.");
		}

		/** 
		 * <span class="en">
		 * @return the number of errors that were encountered during mail sending process 
		 * </span>
		 * 
		 * <span class="ja">
		 * @return メール送信中に発生されているエラー・カウントを戻します。
		 * </span>
		 */
		public synchronized int errorCount() {
			return errorCount;
		}

		/**
		 * <span class="en">
		 * @return the total count of mails being sent. This number does not take in accordance the number of errors. To
		 *         get the actual count of mail sent without error use 'errorCount - mailCount'.
		 * </span>
		 * 
		 * <span class="ja">
		 * @return 送信メールの合計を戻します。エラー・メールを含む「'errorCount - mailCount'　=　送信成功メール」
		 * </span>
		 */
		public synchronized int mailCount() {
			return mailCount;
		}

		/**
		 * <span class="en">
		 * @return the current queue size. This method is useful for simplistic load balancing between apps that are
		 *         supposed to send mails
		 * </span>
		 * 
		 * <span class="ja">
		 *　@return カレント・キュー・サイズを戻します。複数のアプリケーションのロード・バランスに最適です。
		 * </span>
		 */
		public synchronized int currentQueueSize() {
			return _messages.size();
		}

		private synchronized void incrementErrorCount() {
			errorCount++;
		}

		private synchronized void incrementMailCount() {
			mailCount++;
		}

		/** 
		 * <span class="en">
		 * @return the timestamp that respresents when the stats object was reset. 
		 * </span>
		 * 
		 * <span class="ja">
		 * @return 統計オブジェクトがリセットされているタイムスタンプ
		 * </span>
		 */
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

		/** 
		 * <span class="en">
		 * @return a string representation of the Stats object.
		 * </span>
		 * 
		 * <span class="ja">
		 * @return 統計オブジェクトの文字列表記
		 * </span>
		 */
		@Override
		public String toString() {
			return "<" + getClass().getName() + " lastResetTime: " + lastResetTime() + ", mailCount: " + mailCount() + ", errorCount: " + errorCount() + ", currentQueueSize: " + currentQueueSize() + ", peakMemoryUsage: " + formattedPeakMemoryUsage() + ">";
		}
	}
}
