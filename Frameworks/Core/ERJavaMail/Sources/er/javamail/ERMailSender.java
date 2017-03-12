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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.formatters.ERXUnitAwareDecimalFormat;

/**
 * <div class="en">
 * This class is used to send mails in a threaded way.
 *
 * This is needed in WebObjects because if sending 20 mails takes 40 seconds, then the user must wait 40 seconds before
 * attempting to use the application.
 * </div>
 * 
 * <div class="ja">
 * このクラスはメール送信をスレッド系で送信します。
 * WebObjects には必要な方法です。なぜなら、メール 20通が約 40秒かかるとユーザが 40秒もアプリケーションが使えるようになるまでに
 * 待つ必要が発生します。
 * </div>
 * 
 * @author Camille Troillard &lt;tuscland@mac.com&gt;
 * @author Tatsuya Kawano &lt;tatsuyak@mac.com&gt;
 * @author Max Muller &lt;maxmuller@mac.com&gt;
 */
public class ERMailSender implements Runnable {

	public static final String InvalidEmailNotification = "InvalidEmailNotification";

	private static final Logger log = LoggerFactory.getLogger(ERMailSender.class);

	private static ERMailSender _sharedMailSender;

	private Stats _stats;

	// Holds sending messages. The queue size can be set by
	// er.javamail.senderQueue.size property
	private ERQueue<ERMessage> _messages;
	// For thread management
	private int _milliSecondsWaitRunLoop = 5000;
	
	private Thread _senderThread;

	/**
	 * <div class="en">
	 * Exception class for alerting about a stack overflow
	 * </div>
	 * 
	 * <div class="ja">
	 * オーバフローの例外発生クラス
	 * </div>
	 */
	public static class SizeOverflowException extends Exception {
		private static final long serialVersionUID = 1L;

		public SizeOverflowException(Exception e) {
			super(e);
		}
	}

	private ERMailSender() {
		_stats = new Stats();
		_messages = new ERQueue<>(ERJavaMail.sharedInstance().senderQueueSize());

        if (WOApplication.application() == null || WOApplication.application ().isDebuggingEnabled()) {
            _milliSecondsWaitRunLoop = 2000;
        }
        
		if (log.isDebugEnabled()) {
			log.debug("ERMailSender initialized (JVM heap size: {})", _stats.formattedUsedMemory());
		}
	}

	/** 
	 * @return <div class="en">the shared instance of the singleton ERMailSender object</div>
	 *         <div class="ja">ERMailSender シングルトン・オブジェクトを戻します。</div>
	 */
	public static synchronized ERMailSender sharedMailSender() {
		if (_sharedMailSender == null) {
			_sharedMailSender = new ERMailSender();
		}
		return _sharedMailSender;
	}

	/** 
	 * @return <div class="en">the stats associated with this ERMailSender object</div>
	 *         <div class="ja">ERMailSender オブジェクトと関連されている統計を戻します</div>
	 */
	public Stats stats() {
		return _stats;
	}

	/**
	 * <div class="en">
	 * Sends a message in a non-blocking way.
	 *
	 * This means that the thread won't be blocked, but the message will be queued before being delivered.
	 * 
	 * </div>
	 * <div class="ja">
	 * メッセージをブロックされない形で送信します。<br>
	 * スレッドはブロックされませんが、メッセージは送信する前にキューに入れられます。
	 * </div>
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
			}

			_messages.push(message);
			_stats.updateMemoryUsage();

			if (log.isDebugEnabled()) {
				log.debug("({}) Added the message in the queue: {}", _stats.formattedUsedMemory(), allRecipientsString);
			}
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
	 * <div class="en">
	 * Sends a message immediately.
	 *
	 * This means that the thread could be blocked if the message takes time to be delivered.
	 * </div>
	 * 
	 * <div class="ja">
	 * メッセージを直ちに送信する<br>
	 * メッセージが送信に時間がかかりすぎるとスレッドがブロックされる可能性があります。
	 * </div>
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
	 * <div class="en">
	 * Common method used by 'sendMessageNow' and 'sendMessageDeffered' (actually the 'run' method when the thread is
	 * running) to send a message.
	 *
	 * This method sends the message and increments the processed mail count. If an exception occurs while sending the
	 * mail, and if a callback object has been given, the notifyInvalidEmails method is called.<br>
	 * If a MessagingException is thrown, then the exception is catched and rethrown immediately, thus letting us to
	 * process another callbacks or not. For example, This is used when sendMessageNow is used, the MessagingException
	 * is encapsulated in a ERMailSender.ForwardException, and thrown to the user.
	 * </div>
	 * 
	 * <div class="ja">
	 * 'sendMessageNow' と 'sendMessageDeffered' (実際は 'run' メソッド) のメール送信共通メソッドです。<br>
	 * このメソッドはメッセージを送信し、メール送信カウンターを進みます。メール送信中に例外が発生するとコールバック指定があれば、
	 * notifyInvalidEmails メソッドがコールされます。<br>
	 * MessagingException が発生すると例外がキャチュされ、再度発生させます。他のコールバックを対応できるようになります。
	 * 例えば、sendMessageNow が使用されている時 MessagingException は ERMailSender.ForwardException 内にカプセル化されます。
	 * </div>
	 */
	protected void _sendMessageNow(ERMessage message, Transport transport) throws MessagingException {
		boolean debug = log.isDebugEnabled();
		MimeMessage aMessage = message.mimeMessage();
		MessagingException exception = null;

		if (message.shouldSendMessage()) {
			// Send the message
			try {

				if (debug) {
					log.debug("Sending a message ... {}", aMessage);
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
					log.debug("({}) Message sent: {}", _stats.formattedUsedMemory(), allRecipientsString);
				}
			}
			catch (SendFailedException e) {
				if (debug)
					log.debug("Failed to send message:\n{}", message.allRecipientsAsString(), e);
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
				log.error("An unexpected error occured while sending message: {} mime message: {}"
						+ " sending to: {} transport: {}", message, aMessage, Arrays.toString(aMessage.getAllRecipients()), transport, t);
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
			log.debug("Message has instructed me not to send it, not sending message: {}", message);
		}
	}

	/**
	 * <div class="en">
	 * Utility method that gets the SMTP Transport method for a session and connects the Transport before returning it.
	 * </div>
	 * 
	 * <div class="ja">
	 * セッションの SMTP トランスポート方法を取得するユーティリティー・メソッド。
	 * 戻す前にトランスポートへの接続を開始します。
	 * </div>
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
			log.error("Unable to connect to SMTP Transport. MessagingException: {}", e.getMessage(), e);
			if (_throwExceptionIfConnectionFails) {
				throw e;
			} else {
				log.error("Unable to connect to SMTP Transport. MessagingException: {}", e.getMessage(), e);
			}
		}

		return transport;
	}

	/**
	 * <div class="en">
	 * Don't call this method, this is the thread run loop and is automatically called.
	 * </div>
	 * 
	 * <div class="ja">
	 * このメソッドをコールしないでください。
	 * これはスレッド実行ループで自動的に処理されます。
	 * </div>
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
					Map<String, Transport> transports = new HashMap<>();
					
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
								log.error("Can't send message: {}", message, ex);
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
						log.error("Unable to connect to SMTP Transport. AuthenticationFailedException: {} waiting 20 seconds", e.getMessage(), e);
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
							log.error("General mail error.", e);
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
	 * <div class="en">
	 * Executes the callback method to notify the calling application of any invalid emails.
	 * </div>
	 * 
	 * <div class="ja">
	 * メール送信失敗のアプリケーションのコールバックを実行します。
	 * </div>
	 */
	protected void notifyInvalidEmails(NSArray<String> invalidEmails) {
		NSNotification notification = new NSNotification(InvalidEmailNotification, invalidEmails);
		NSNotificationCenter.defaultCenter().postNotification(notification);
	}

	/**
	 * <div class="en">
	 * This class is about logging mail event for stats purposes. More stats to come in the future.
	 * </div>
	 * 
	 * <div class="ja">
	 * このクラスはメール・イベントを統計のためにログします。
	 * </div>
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
		 * <div class="en">
		 * Resets statistics information 
		 * </div>
		 * 
		 * <div class="ja">
		 * 統計情報のリセット
		 * </div>
		 */
		public synchronized void reset() {
			String savedStatsString = toString();
			errorCount = 0;
			mailCount = 0;
			_peakMemoryUsage = 0.0d;
			updateMemoryUsage();
			lastResetTime = new NSTimestamp();
			log.debug("{} has been reset to initial value.", savedStatsString);
		}

		/** 
		 * <div class="en">
		 * @return the number of errors that were encountered during mail sending process 
		 * </div>
		 * 
		 * <div class="ja">
		 * @return メール送信中に発生されているエラー・カウントを戻します。
		 * </div>
		 */
		public synchronized int errorCount() {
			return errorCount;
		}

		/**
		 * <div class="en">
		 * @return the total count of mails being sent. This number does not take in accordance the number of errors. To
		 *         get the actual count of mail sent without error use 'errorCount - mailCount'.
		 * </div>
		 * 
		 * <div class="ja">
		 * @return 送信メールの合計を戻します。エラー・メールを含む「'errorCount - mailCount'　=　送信成功メール」
		 * </div>
		 */
		public synchronized int mailCount() {
			return mailCount;
		}

		/**
		 * <div class="en">
		 * @return the current queue size. This method is useful for simplistic load balancing between apps that are
		 *         supposed to send mails
		 * </div>
		 * 
		 * <div class="ja">
		 *　@return カレント・キュー・サイズを戻します。複数のアプリケーションのロード・バランスに最適です。
		 * </div>
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
		 * @return <div class="en">the timestamp that represents when the stats object was reset.</div>
		 *         <div class="ja">統計オブジェクトがリセットされているタイムスタンプ</div>
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
		 * @return <div class="en">a string representation of the Stats object.</div>
		 *         <div class="ja">統計オブジェクトの文字列表記</div>
		 */
		@Override
		public String toString() {
			return "<" + getClass().getName() + " lastResetTime: " + lastResetTime() + ", mailCount: " + mailCount() + ", errorCount: " + errorCount() + ", currentQueueSize: " + currentQueueSize() + ", peakMemoryUsage: " + formattedPeakMemoryUsage() + ">";
		}
	}
}
