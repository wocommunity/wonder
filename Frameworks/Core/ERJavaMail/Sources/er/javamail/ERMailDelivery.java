/*
 ERMailDelivery.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.Date;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * <div class="en">
 * This is the main class for sending mail with the JavaMail API. You should create
 * instances of its subclasses that match with specific use cases.
 *
 * <p>Here is an example of its usage:
 * </div>
 * 
 * <div class="ja">
 * JavaMail API でメールを送信するメインクラスです。このクラスは抽選クラスなので、のインスタンスを作成しません。
 * 替わりに特集なサブクラスを作る場合にインスタンス化する必要はあるでしょう！
 * 
 * <p>使用方法：
 * </div>

<pre><code>
    ERMailDeliveryHTML mail = new ERMailDeliveryHTML();
    mail.setWOComponentContent(mailPage);
 
    try {
        mail.newMail();
        mail.setFromAddress(emailFrom);
        mail.setReplyToAddress(emailReplyTo);
        mail.setSubject(emailSubject);
        mail.setToAddresses(new NSArray(toEmailAddresses));

        // Send the mail
        mail.sendMail();
    } catch (Exception e) {
        // do something ...
    }</code></pre>
 * 
 * @property er.javamail.defaultEncoding
 *
 * @author Camille Troillard &lt;tuscland@mac.com&gt;
 * @author ak fixes
 */
public abstract class ERMailDelivery {
	private static final Logger log = LoggerFactory.getLogger(ERMailDelivery.class);

	/** JavaMail session */
	private javax.mail.Session _session;

	/**
	 * <div class="en">
	 * Content of sent mail. In one instance of ERMailDelivery, when creating multiple mails, you must be sure to call
	 * newMail () method before send a new mail in order to have a cleared fresh mail
	 * </div>
	 * 
	 * <div class="ja">
	 * メールのコンテントです。
	 * ERMailDelivery のインスタンスで複数メールを送信する時には newMail() コマンドで初期化を行ってください。
	 * </div>
	 */
	protected MimeMessage _mimeMessage;

	/** 
	 * <div class="en">
	 * NSArray of ERMailAttachment that must be binded to the message as ATTACHEMENT. 
	 * </div>
	 * 
	 * <div class="ja">
	 * ERMailAttachment の NSArray でメッセージの ATTACHEMENT としてバインディングされる
	 * </div>
	 */
	protected NSMutableArray<ERMailAttachment> _attachments;

	/** 
	 * <div class="en">
	 * NSArray of ERMailAttachment that must be binded to the message as INLINE. 
	 * </div>
	 * 
	 * <div class="ja">
	 * ERMailAttachment の NSArray でメッセージの INLINE としてバインディングされる
	 * </div>
	 */
	protected NSMutableArray<ERMailAttachment> _inlineAttachments;

	private ERMessage.Delegate _delegate;
	private NSDictionary<String, Object> _userInfo;
	private String _contextString;

	public static final String DefaultCharset = System.getProperty("er.javamail.defaultEncoding", CharEncoding.UTF_8);
	public String _charset = DefaultCharset;

	/** Designated constructor */
	public ERMailDelivery(javax.mail.Session session) {
		super();
		_session = session;
		_mimeMessage = new MimeMessage(session);
	}

	/** Default constructor */
	public ERMailDelivery() {
		this(ERJavaMail.sharedInstance().defaultSession());
	}

	/**
	 * <div class="en">
	 * Sets the given delegate to listen to any messages that are created from this ERMailDelivery. This will
	 * automatically call ERMessage.setDelegate(delegate) for any ERMessage that is generated.
	 * 
	 * @param delegate
	 *            the delegate to use for notifications
	 * </div>
	 * 
	 * <div class="ja">
	 * この ERMailDelivery で作成されるすべてのメッセージをリスンするデリゲートを指定します。
	 * 作成される全メッセージの ERMessage.setDelegate(delegate) が確実に呼ばれます。
	 * 
	 * @param delegate - 通知のためのデリゲート
	 * </div>
	 */
	public void setDelegate(ERMessage.Delegate delegate) {
		_delegate = delegate;
	}

	/**
	 * <div class="en">
	 * Sets the userInfo dictionary for this ERMailDelivery. This userInfo is passed through to any ERMessage that is
	 * created by this ERMailDelivery, which can be used by delegates to get additional information about the message.
	 * 
	 * @param userInfo
	 *            the userInfo dictionary
	 * </div>
	 * 
	 * <div class="ja">
	 * ERMailDelivery のユーザ情報ディクショナリーをセットします。
	 * このユーザ情報は ERMailDelivery で作成される全メッセージに行き渡ります。
	 * デリゲートと合わせてメッセージの追加情報で使用できます。
	 * 
	 * @param userInfo - ユーザ情報ディクショナリー
	 * </div>
	 */
	public void setUserInfo(NSDictionary<String, Object> userInfo) {
		_userInfo = userInfo;
	}

	/**
	 * <div class="en">
	 * Returns the userInfo dictionary for this ERMailDelivery.
	 * 
	 * @return the userInfo dictionary
	 * </div>
	 * 
	 * <div class="ja">
	 * ERMailDelivery のユーザ情報ディクショナリーを戻します。
	 * 
	 * @return ユーザ情報ディクショナリー
	 * </div>
	 */
	public NSDictionary<String, Object> userInfo() {
		return _userInfo;
	}
	
	public void setContextString(String contextString) {
		_contextString = contextString;
	}
	
	public String contextString() {
		return _contextString;
	}

	public String charset() {
		return _charset;
	}

	public void setCharset(String charset) {
		_charset = charset;
	}

	protected javax.mail.Session session() {
		return _session;
	}

	protected void setSession(javax.mail.Session aSession) {
		_session = aSession;
	}

	/** 
	 * <div class="en">
	 * Creates a new mail instance within ERMailDelivery 
	 * </div>
	 * 
	 * <div class="ja">
	 * ERMailDelivery インスタンス内で新しいメールを作成します。
	 * </div>
	 */
	public void newMail() {
		_attachments().removeAllObjects();
		_inlineAttachments().removeAllObjects();
		setMimeMessage(new MimeMessage(session()));
	}

	protected MimeMessage mimeMessage() {
		return _mimeMessage;
	}

	protected void setMimeMessage(MimeMessage message) {
		_mimeMessage = message;
	}

	public void addAttachment(ERMailAttachment attachment) {
		_attachments().addObject(attachment);
	}

	public void addInlineAttachment(ERMailAttachment attachment) {
		_inlineAttachments().addObject(attachment);
	}

	protected NSMutableArray<ERMailAttachment> _inlineAttachments() {
		if (_inlineAttachments == null)
			_inlineAttachments = new NSMutableArray<>();
		return _inlineAttachments;
	}

	public NSArray<ERMailAttachment> inlineAttachments() {
		return _inlineAttachments();
	}

	protected NSMutableArray<ERMailAttachment> _attachments() {
		if (_attachments == null)
			_attachments = new NSMutableArray<>();
		return _attachments;
	}

	public NSArray<ERMailAttachment> attachments() {
		return _attachments();
	}

	public void removeAttachment(ERMailAttachment attachment) {
		_attachments().removeObject(attachment);
		_inlineAttachments().removeObject(attachment);
	}
	
	/** 
	 * <div class="ja">
	 * メール・アドレスと名前を InternetAddress としてインスタンス化と戻します
	 * </div>
	 * 
	 * @return address object
	 * @throws AddressException if parsing of email failed
	 */
	protected InternetAddress internetAddressWithEmailAndPersonal(String email, String personal) throws AddressException {
		InternetAddress address = null;

		if (personal != null) {
			address = new InternetAddress();
			address.setAddress(email);

			try {
				address.setPersonal(personal, charset());
			}
			catch (java.io.UnsupportedEncodingException ex) {
				// set the string anyway.
				try {
					address.setPersonal(personal);
				}
				catch (Exception e) {
					// give up ...
				}
			}
		}
		else {
			address = new InternetAddress(email);
		}

		return address;
	}

	/** 
	 * <div class="en">
	 * Sets the from address for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの送信元アドレスをセットします
	 * </div>
	 */
	public void setFromAddress(String fromAddress) throws MessagingException, AddressException {
		setFromAddress(fromAddress, null);
	}

	/** 
	 * <div class="en">
	 * Sets the from address for the current message instance using an email and the personal name. 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの送信元アドレスと名前をセットします
	 * </div>
	 */
	public void setFromAddress(String fromAddress, String personalName) throws MessagingException, AddressException {
		InternetAddress address = internetAddressWithEmailAndPersonal(fromAddress, personalName);
		mimeMessage().setFrom(address);
	}

	/** 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの送信先アドレスをセットします 
	 * </div>
	 */
	public void setToAddress(String toAddress) throws MessagingException, AddressException {
		setToAddress(toAddress, null);
	}

	/** 
	 * <div class="en">
	 * Sets the to address for the current message instance using an email and the personal name. 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの送信先アドレスと名前をセットします
	 * </div>
	 */
	public void setToAddress(String toAddress, String personalName) throws MessagingException, AddressException {
		InternetAddress address = internetAddressWithEmailAndPersonal(toAddress, personalName);
		setInternetAddresses(new NSArray<>(address), Message.RecipientType.TO);
	}

	/** 
	 * <div class="en">
	 * Sets the to-addresses array for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの送信先 NSArray アドレスをセットします
	 * </div>
	 */
	public void setToAddresses(NSArray<String> toAddresses) throws MessagingException, AddressException {
		setAddresses(toAddresses, Message.RecipientType.TO, true);
	}

	/** 
	 * <div class="en">
	 * Sets the to-addresses array for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの送信先 NSDictionary アドレスをセットします
	 * </div>
	 */
	public void setToAddresses(NSDictionary<String, String> toAddresses) throws MessagingException, AddressException {
		setAddresses(toAddresses, Message.RecipientType.TO, true);
	}

	/** 
	 * <div class="en">
	 * Sets the reply-to address for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの Reply To アドレスをセットします
	 * </div>
	 */
	public void setReplyToAddress(String toAddress) throws MessagingException, AddressException {
		setReplyToAddress(toAddress, null);
	}

	/** 
	 * <div class="en">
	 * Sets the reply-to address for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの Reply To アドレスと名前をセットします
	 * </div>
	 */
	public void setReplyToAddress(String toAddress, String personalName) throws MessagingException, AddressException {
		InternetAddress addresses[] = new InternetAddress[] { internetAddressWithEmailAndPersonal(toAddress, personalName) };
		mimeMessage().setReplyTo(addresses);
	}

	/** 
	 * <div class="en">
	 * Sets the cc-addresses array for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの CCアドレス NSArray をセットします
	 * </div>
	 */
	public void setCCAddresses(NSArray<String> ccAddresses) throws MessagingException, AddressException {
		setAddresses(ccAddresses, Message.RecipientType.CC, true);
	}

	/** 
	 * <div class="en">
	 * Sets the cc-addresses array for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの CCアドレス NSDictionary をセットします
	 * </div>
	 */
	public void setCCAddresses(NSDictionary<String, String> ccAddresses) throws MessagingException, AddressException {
		setAddresses(ccAddresses, Message.RecipientType.CC, true);
	}

	/** 
	 * <div class="en">
	 * Sets the bcc-addresses array for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの BCCアドレス NSArray をセットします
	 * </div>
	 */
	public void setBCCAddresses(NSArray<String> bccAddresses) throws MessagingException, AddressException {
		setAddresses(bccAddresses, Message.RecipientType.BCC, true);
	}
	
	/** 
	 * <div class="en">
	 * Sets the bcc-addresses array for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの BCCアドレス NSDictionary をセットします
	 * </div>
	 */
	public void setBCCAddresses(NSDictionary<String, String> bccAddresses) throws MessagingException, AddressException {
		setAddresses(bccAddresses, Message.RecipientType.BCC, true);
	}

	/** 
	 * <div class="en">
	 * Sets the subject for the current message instance 
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージ・インスタンスの題名をセットします
	 * </div>
	 * @param subject subject string
	 * @throws MessagingException if the charset conversion of the subject fails
	 */
	public void setSubject(String subject) throws MessagingException {
		mimeMessage().setSubject(ERMailUtils.encodeString(subject, charset()));
	}

	/**
	 * <div class="en">
	 * Sets the X-Mailer header for the message. Useful for tracking which mailers are sending messages.
	 * 
	 * @param xMailer
	 *            value to set
	 * </div>
	 * 
	 * <div class="ja">
	 * メッセージの X-Mailer ヘッダーをセットします。
	 * どのメールソフトが送信しているかどうかの調査使用します。
	 * 
	 * @param xMailer - セットする値
	 * </div>
	 */
	public void setXMailerHeader(String xMailer) throws MessagingException {
		mimeMessage().setHeader("X-Mailer", xMailer);
	}

	/**
	 * <div class="en">
	 * Gets the X-Mailer header set on the MimeMessage.
	 * 
	 * @return X-Mailer header if it is set
	 * </div>
	 * 
	 * <div class="ja">
	 * メッセージの X-Mailer ヘッダーを取得します。
	 * 
	 * @return セットされていれば、X-Mailer ヘッダー
	 * </div>
	 */
	public String xMailerHeader() throws MessagingException {
		String[] headers = mimeMessage().getHeader("X-Mailer");
		return headers != null && headers.length > 0 ? headers[0] : null;
	}
	
	/**
	 * Sets an additional custom header element for the message.
	 * 
	 * @param headerKey
	 * @param value
	 *            value to set
	 */
	public void setAdditionalHeader(String headerKey, String value) throws MessagingException {
		mimeMessage().setHeader(headerKey, value);
	}

	/**
	 * <div class="en">
	 * Builds an ERMessage for the current MimeMessage.
	 * 
	 * @return ERMessage for the current MimeMessage.
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント MimeMessage のために ERMessage を生成します。
	 * 
	 * @return カレント MimeMessage のための ERMessage
	 * </div>
	 */
	protected ERMessage buildMessage() {
		ERMessage message = new ERMessage();
		message.setDelegate(_delegate);
		message.setUserInfo(_userInfo);
		message.setContextString(_contextString);
		MimeMessage mimeMessage = mimeMessage();
		try {
			Address[] bccRecipients = mimeMessage.getRecipients(RecipientType.BCC);
			if (bccRecipients != null && bccRecipients.length > 0) {
				Address[] toRecipients = mimeMessage.getRecipients(RecipientType.TO);
				if (toRecipients == null || toRecipients.length == 0) {
					mimeMessage.addRecipient(RecipientType.TO, internetAddressWithEmailAndPersonal("Undisclosed recipients:;", null));
				}
			}
		}
		catch (MessagingException e) {
			throw new RuntimeException("Failed to set 'undisclosed recipients' recipient for your bulk mail.", e);
		}
		message.setMimeMessage(mimeMessage);
		return message;
	}

	/**
	 * <div class="en">
	 * Sends the mail immediately. The message is put in a FIFO queue managed by a static threaded inner class
	 * </div>
	 * 
	 * <div class="ja">
	 * メールを直ちに送信します。メッセージは内部スレッド・クラスによる FIFO キューに保存されます。
	 * </div>
	 */
	public void sendMail() {
		try {
			sendMail(false);
		}
		catch (NSForwardException e) {
			log.warn("Sending mail in a non-blocking manner and a forward exception was thrown.", e);
		}
	}

	/**
	 * <div class="en">
	 * Method used to construct a MimeMessage and then send it. This method can be specified to block until the message
	 * is sent or to add the message to a queue and have a callback object handle any exceptions that happen. If sending
	 * is blocking then any exception thrown will be wrapped in a general {@link NSForwardException}.
	 * 
	 * @param shouldBlock
	 *            boolean to indicate if the message should be added to a queue or sent directly.
	 * </div>
	 * 
	 * <div class="ja">
	 * MimeMessage を生成し、送信を試します。
	 * このメソッドは送信完了までにブロックするかキューに登録し例外が発生する場合はコールバック・オブジェクトで処理されるかを指定できます。
	 * 送信でブロックを使用するとすべての例外は {@link NSForwardException} にラップされます。
	 * 
	 * @param shouldBlock - ブロックするかキューを使うかの boolean
	 * </div>
	 */
	public void sendMail(boolean shouldBlock) {
		try {
			if (ERJavaMail.sharedInstance().centralize()) {
				if (ERJavaMail.sharedInstance().adminEmail() == null) {
					throw new IllegalArgumentException("When setting 'er.javamail.centralize=true' (which means you just test sending mails), you must also give a valid 'er.javamail.adminEmail=foo@bar.com' to which the mails are sent.");
				}
				InternetAddress[] addresses = new InternetAddress[] { new InternetAddress(ERJavaMail.sharedInstance().adminEmail()) };
				mimeMessage().setRecipients(Message.RecipientType.TO, addresses);
				mimeMessage().setRecipients(Message.RecipientType.CC, new InternetAddress[0]);
				mimeMessage().setRecipients(Message.RecipientType.BCC, new InternetAddress[0]);
			}
			if (mimeMessage().getAllRecipients().length == 0) {
				return;
			}

			finishMessagePreparation();
			ERMailSender sender = ERMailSender.sharedMailSender();
			ERMessage message = buildMessage();

			if (shouldBlock)
				sender.sendMessageNow(message);
			else {
				// add the current message to the message stack
				boolean mailAccepted = false;
				while (!mailAccepted) {
					try {
						sender.sendMessageDeffered(message);
						mailAccepted = true;
					}
					catch (ERMailSender.SizeOverflowException e) {
						// The mail sender is overflowed, we need to wait
						try {
							// Ask the current thread to stop
							// computing for a little while.
							// Here, we make the assumption that
							// the current thread is the one that
							// feeds the ERMailSender.
							Thread.sleep(ERJavaMail.sharedInstance().milliSecondsWaitIfSenderOverflowed());
						}
						catch (InterruptedException ie) {
							log.warn("Caught InterruptedException.", ie);
						}
					}
				}
			}
		}
		catch (MessagingException e) {
			log.warn("MessagingException exception caught, re-throwing exception.", e);
			throw new NSForwardException(e);
		}
		finally {
			setMimeMessage(null);
		}
	}

	protected void finishMessagePreparation() throws MessagingException {
		DataHandler messageDataHandler = prepareMail();

		// Add all the attachements to the javamail message
		if (attachments().count() > 0) {
			// Create a Multipart that will hold the prepared multipart and the attachments
			MimeMultipart multipart = new MimeMultipart();

			// Create the main body part
			BodyPart mainBodyPart = new MimeBodyPart();
			mainBodyPart.setDataHandler(messageDataHandler);

			// add the main body part to the content of the message
			multipart.addBodyPart(mainBodyPart);

			// add each attachments to the former multipart
			for (ERMailAttachment attachment : attachments()) {
				BodyPart bp = attachment.getBodyPart();
				bp.setDisposition(Part.ATTACHMENT);
				multipart.addBodyPart(bp);
			}

			mimeMessage().setContent(multipart);
		}
		else {
			mimeMessage().setDataHandler(messageDataHandler);
		}

		// If the xMailer property has not been set, check if one has been provided
		// in the System properties
		if ((xMailerHeader() == null) && (ERJavaMail.sharedInstance().defaultXMailerHeader() != null)) {
			setXMailerHeader(ERJavaMail.sharedInstance().defaultXMailerHeader());
		}

		mimeMessage().setSentDate(new Date());
		mimeMessage().saveChanges();
	}

	/**
	 * <div class="en">
	 * Sets addresses using an NSArray of InternetAddress objects.
	 * </div>
	 * 
	 * <div class="ja">
	 * InternetAddress オブジェクトの NSArray をセットします
	 * </div>
	 */
	public void setInternetAddresses(NSArray<InternetAddress> addresses, Message.RecipientType type) throws MessagingException {
		if ((type == null) || (addresses == null) || (addresses.count() == 0)) {
			// don't do anything.
			return;
		}

		InternetAddress[] internetAddresses = new InternetAddress[addresses.count()];
		for (int i = 0; i < addresses.count(); i++) {
			internetAddresses[i] = addresses.objectAtIndex(i);
		}

		mimeMessage().setRecipients(type, internetAddresses);
	}

	/**
	 * <div class="en">
	 * Sets addresses regarding their recipient type in the current message. Has the option to filter the address list
	 * based on the white and black lists.
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージの送信宛先タイプのアドレスをセットします。
	 * オプションでホワイト＆ブラック・リスト・フィルターされる
	 * </div>
	 */
	private void setAddresses(NSArray<String> addressesArray, Message.RecipientType type, boolean filterAddresses) throws MessagingException, AddressException {
		if (filterAddresses) {
			addressesArray = ERJavaMail.sharedInstance().filterEmailAddresses(addressesArray);
		}
		if (addressesArray.count() == 0) {
			// don't do anything.
			return;
		}
		InternetAddress[] addresses = ERMailUtils.convertNSArrayToInternetAddresses(addressesArray);
		mimeMessage().setRecipients(type, addresses);
	}

	/**
	 * <div class="en">
	 * Sets addresses regarding their recipient type in the current message. Has the option to filter the address list
	 * based on the white and black lists.
	 * </div>
	 * 
	 * <div class="ja">
	 * カレント・メッセージの送信宛先タイプのアドレスをセットします。
	 * オプションでホワイト＆ブラック・リスト・フィルターされる
	 * </div>
	 */
	private void setAddresses(NSDictionary<String, String> addressesDictionary, Message.RecipientType type, boolean filterAddresses) throws MessagingException, AddressException {
		NSArray<String> mailAdresses = addressesDictionary.allKeys();
		if (filterAddresses) {
			mailAdresses = ERJavaMail.sharedInstance().filterEmailAddresses(mailAdresses);
		}
		if (mailAdresses.count() == 0) {
			// don't do anything.
			return;
		}
		NSMutableDictionary<String, String> newDictionary = new NSMutableDictionary<>();
		for (String key: mailAdresses) {
			newDictionary.takeValueForKey(addressesDictionary.objectForKey(key), key);
		}
		InternetAddress[] addresses = ERMailUtils.convertNSDictionaryToInternetAddresses(newDictionary.immutableClone(), charset());
		mimeMessage().setRecipients(type, addresses);
	}

	/**
	 * <div class="en">
	 * Abstract method called by subclasses for doing pre-processing before sending the mail.
	 * 
	 * @return the multipart used to put in the mail.
	 * </div>
	 * 
	 * <div class="ja">
	 * メールを送信前の処理のサブクラスの抽選メソッド
	 * 
	 * @return メールのマルチパート
	 * </div>
	 */
	protected abstract DataHandler prepareMail() throws MessagingException;
}
