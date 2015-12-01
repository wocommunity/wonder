/*
 ERMessage.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * <span class="en">
 * ERMessage represents an email message.
 * </span>
 * 
 * <span class="ja">
 * ERMessage はメール・メッセージを表現します
 * </span>
 */
public class ERMessage {
	/**
	 * <span class="en">
	 * Defines a delegate interface for receiving notifications
	 * about email messages.
	 * </span>
	 * 
	 * <span class="ja">
	 * メール・メッセージについての通知を受信するデリゲート・インタフェースを定義します。
	 * </span>
	 * 
	 * @author mschrag
	 */
	public static interface Delegate {
		/**
		 * <span class="en">
		 * Called when a message is successfully delivered.
		 * 
		 * @param message the message that was delivered
		 * </span>
		 * 
		 * <span class="ja">
		 * メッセージ送信が成功した場合
		 * 
		 * @param message - 成功したメッセージ
		 * </span>
		 */
		public void deliverySucceeded(ERMessage message);
		
		/**
		 * <span class="en">
		 * Called when a message fails with invalid recipients.  You will get
		 * a call to invalidRecipients AND a call to deliveryFailed.
		 * 
		 * @param message the message that was not delivered
		 * @param invalidRecipientAddresses the array of invalid email addresses
		 * </span>
		 * 
		 * <span class="ja">
		 * メッセージが送信先などで失敗したい場合
		 * invalidRecipients と deliveryFailed にコールが行きます。
		 * 
		 * @param message - 送信できなかったしたメッセージ
		 * @param invalidRecipientAddresses - 失敗したメール・アドレス配列
		 * </span>
		 */
		public void invalidRecipients(ERMessage message, NSArray<String> invalidRecipientAddresses);
		
		/**
		 * <span class="en">
		 * Called when a message fails to deliver.
		 * 
		 * @param message the message that failed
		 * @param failure the exception of the failure
		 * </span>
		 * 
		 * <span class="ja">
		 * メッセージ送信が失敗した場合
		 * 
		 * @param message - 失敗したメッセージ
		 * @param failure - 失敗した原因
		 * </span>
		 */
		public void deliveryFailed(ERMessage message, Throwable failure);
	}

	private ERMessage.Delegate _delegate;
	private MimeMessage _message;
	private NSDictionary<String, Object> _userInfo;
	private String _contextString;

	public void setDelegate(ERMessage.Delegate delegate) {
		_delegate = delegate;
	}
	
	public void setUserInfo(NSDictionary<String, Object> userInfo) {
		_userInfo = userInfo;
	}
	
	public NSDictionary<String, Object> userInfo() {
		return _userInfo;
	}
	
	public void setContextString(String contextString) {
		_contextString = contextString;
	}
	
	public String contextString() {
		return _contextString;
	}

	public void setMimeMessage(MimeMessage m) {
		_message = m;
	}

	public MimeMessage mimeMessage() {
		return _message;
	}

	public boolean shouldSendMessage() {
		Address to[] = null;
		try {
			to = _message.getRecipients(Message.RecipientType.TO);
		}
		catch (MessagingException m) {
		}
		return to != null && to.length > 0;
	}

	/**
	 * <span class="en">
	 * @param recipientType
	 *            which can be: <code>Message.RecipientType.To</code>, <code>Message.RecipientType.CC</code>, or
	 *            <code>Message.RecipientType.BCC</code>
	 * </span>
	 * 
	 * <span class="ja">
	 * @param recipientType
	 * 			<code>Message.RecipientType.To</code>、 <code>Message.RecipientType.CC</code> 又は 
	 * 			<code>Message.RecipientType.BCC</code> の中の一つ
	 * </span>
	 */
	public Address[] recipients(Message.RecipientType recipientType) throws MessagingException {
		return _message == null ? null : _message.getRecipients(recipientType);
	}

	public String recipientsAsString(Message.RecipientType recipientType) throws MessagingException, AddressException {
		return recipientsAsString(recipientType, -1);
	}

	public String recipientsAsString(Message.RecipientType recipientType, int maxAddresses) throws MessagingException, AddressException {
		Address[] allAddresses = recipients(recipientType);
		Address[] limitteredAddresses = null;

		if (allAddresses == null || allAddresses.length == 0)
			return null;

		if (maxAddresses > 0) {
			limitteredAddresses = new Address[maxAddresses];
			System.arraycopy(allAddresses, 0, limitteredAddresses, 0, Math.min(allAddresses.length, maxAddresses));
		}
		else {
			limitteredAddresses = allAddresses;
		}

		StringBuilder result = new StringBuilder();
		result.append(ERMailUtils.convertInternetAddressesToNSArray(limitteredAddresses).componentsJoinedByString(", "));
		if (0 < maxAddresses && maxAddresses < allAddresses.length) {
			result.append(", and ");
			result.append(allAddresses.length - maxAddresses);
			result.append(" other recipients");
		}
		return result.toString();
	}

	public String allRecipientsAsString() throws MessagingException {
		return allRecipientsAsString(true, -1);
	}

	public String allRecipientsAsString(boolean includeBcc) throws MessagingException {
		return allRecipientsAsString(includeBcc, -1);
	}

	public String allRecipientsAsString(boolean includeBcc, int maxAddresses) throws MessagingException {
		StringBuilder recipients = new StringBuilder();
		String addresses = recipientsAsString(Message.RecipientType.TO, maxAddresses);
		if (addresses != null && addresses.length() > 0)
			recipients.append("To: ").append(addresses);

		addresses = recipientsAsString(Message.RecipientType.CC, maxAddresses);
		if (addresses != null && addresses.length() > 0)
			recipients.append("CC: ").append(addresses);

		if (includeBcc) {
			addresses = recipientsAsString(Message.RecipientType.BCC, maxAddresses);
			if (addresses != null && addresses.length() > 0)
				recipients.append("BCC: ").append(addresses);
		}
		return recipients.toString();
	}

	@Override
	public String toString() {
		StringBuilder sbuf = new StringBuilder();
		sbuf.append('<').append(getClass().getName()).append(' ');
		if (_message == null) {
			sbuf.append("No mime message is set.");
		}
		else {
			try {
				sbuf.append(allRecipientsAsString());
			}
			catch (MessagingException ex) {
				// do nothing
			}
		}
		sbuf.append('>');
		return sbuf.toString();
	}
	
	/**
	 * Called by ERMailSender
	 */
	public void _deliverySucceeded() {
		if (_delegate != null) {
			_delegate.deliverySucceeded(this);
		}
	}
	
	/**
	 * Called by ERMailSender
	 */
	public void _invalidRecipients(NSArray<String> invalidRecipientAddresses) {
		if (_delegate != null) {
			_delegate.invalidRecipients(this, invalidRecipientAddresses);
		}
	}
	
	/**
	 * Called by ERMailSender
	 */
	public void _deliveryFailed(Throwable failure) {
		if (_delegate != null) {
			_delegate.deliveryFailed(this, failure);
		}
	}
	
	/*
	 * Set the DELETED flag on the message, so that you can expunge it when you close a IMAP folder.
	 */
	public void setDeleteFlag() throws MessagingException {
		mimeMessage().setFlag(Flags.Flag.DELETED,true);
	}
}
