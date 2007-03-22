/*
 $Id$

 ERMessage.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * ERMessage represents an email message.
 */
public class ERMessage extends Object {
	/**
	 * Defines a delegate interface for receiving notifications
	 * about email messages.
	 * 
	 * @author mschrag
	 */
	public static interface Delegate {
		/**
		 * Called when a message is successfully delivered.
		 * 
		 * @param message the message that was delivered
		 */
		public void deliverySucceeded(ERMessage message);
		
		/**
		 * Called when a message fails with invalid recipients.  You will get
		 * a call to invalidRecipients AND a call to deliveryFailed.
		 * 
		 * @param message the message that was not delivered
		 * @param invalidRecipientAddresses the array of invalid email addresses
		 */
		public void invalidRecipients(ERMessage message, NSArray invalidRecipientAddresses);
		
		/**
		 * Called when a message fails to deliver.
		 * 
		 * @param message the message that failed
		 * @param failure the exception of the failure
		 */
		public void deliveryFailed(ERMessage message, Throwable failure);
	}

	private ERMessage.Delegate _delegate;
	private MimeMessage _message;
	private NSDictionary _userInfo;

	public void setDelegate(ERMessage.Delegate delegate) {
		_delegate = delegate;
	}
	
	public void setUserInfo(NSDictionary userInfo) {
		_userInfo = userInfo;
	}
	
	public NSDictionary userInfo() {
		return _userInfo;
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
	 * @param recipientType
	 *            which can be: <code>Message.RecipientType.To</code>, <code>Message.RecipientType.CC</code>, or
	 *            <code>Message.RecipientType.BCC</code>
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

		StringBuffer result = new StringBuffer();
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
		StringBuffer recipients = new StringBuffer();
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

	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<").append(getClass().getName()).append(" ");
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
		sbuf.append(">");
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
	public void _invalidRecipients(NSArray invalidRecipientAddresses) {
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
}
