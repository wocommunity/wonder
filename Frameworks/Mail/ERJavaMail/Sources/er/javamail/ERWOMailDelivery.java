/*
 Copyright (c) 2002 Red Shed Software. All rights reserved.
 by Jonathan 'Wolf' Rentzsch (jon at redshed dot net)
 */

package er.javamail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;

/**
 * <span class="en">
 * Drop-in replacement for WOMailDelivery.
 * 
 * <P>
 * ERWOMailDelivery operates just the same as WOMailDelivery, and has all of the same limitations and weird API.
 * However, instead of using the Sun's broken* and unsupported sun.net.smtp.SmtpClient, it uses JavaMail.
 * </P>
 * 
 * <P>
 * <I>*sun.net.smtp.SmtpClient doesn't put addresses in angle brackets when sending the SMTP MAIL FROM command. Many
 * SMTP servers won't work with it.</I>
 * </P>
 * </span>
 * 
 * <span class="ja">
 * WOMailDelivery の替わりに使用します。
 * 
 * <P>
 * ERWOMailDelivery は WOMailDelivery と同じように動作します。全く同じ制限と不思議な API を持っています。
 * 正しい、Sun 社の壊れているサポートしない sun.net.smtp.SmtpClient と違って JavaMail を使用しています。
 * </P>
 * 
 * <P>
 * <I>*sun.net.smtp.SmtpClient は SMTP MAIL FROM で送信するアドレスを括弧で囲まないので、大抵の SMTP サーバは動作しないのです。</I>
 * </P>
 * </span>
 * 
 * @author Jonathan 'Wolf' Rentzsch (jon at redshed dot net)
 * @see <A
 *      HREF="file://localhost/Ten/Developer/Documentation/WebObjects/Reference/com/webobjects/appserver/WOMailDelivery.html">com.webobjects.appserver.WOMailDelivery</A>
 */

public class ERWOMailDelivery {
	/** 
	 * <span class="en">
	 * @return The shared instance. 
	 * </span>
	 * 
	 * <span class="ja">
	 * @return 共有インスタンス
	 * </span>
	 */
	public static ERWOMailDelivery sharedInstance() {
		if (_sharedInstance == null)
			_sharedInstance = new ERWOMailDelivery();
		return _sharedInstance;
	}

	/** 
	 * <span class="en">Default constructor (don't use). Use {@link #sharedInstance()} instead. </span>
	 * <span class="ja">未使用のコンストラクタ： {@link #sharedInstance()} を使用します。 </span>
	 */
	protected ERWOMailDelivery() {
		// Just here & protected so folks don't try to construct directly.
	}

	/**
	 * <span class="en">
	 * Creates and optionally sends a plain text email.
	 *
	 * @param fromEmailAddress
	 *            Originating email address. Required.
	 * @param toEmailAddresses
	 *            Destination email address. Required.
	 * @param bccEmailAddresses
	 *            Array of Strings containing additional addressed to BCC. Can be null.
	 * @param subject
	 *            Subject the message. Can be null.
	 * @param message
	 *            Body the the message. Required.
	 * @param sendNow
	 *            Whether to send the message right away. If you're going to send the message right away, it's faster to
	 *            set sendNow to true than set it to false and calling {@link #sendEmail(String)} later.
	 * </span>
	 * 
	 * <span class="ja">
	 * 標準テキスト・メールを作成と送信します。
	 *
	 * @param fromEmailAddress - 送信元メール・アドレス（必須）
	 * @param toEmailAddresses - 送信先メール・アドレス NSArray（必須）
	 * @param bccEmailAddresses - BCC メール・アドレス NSArray （Null可）
	 * @param subject - メール・サブジェクト（Null可）
	 * @param message - メッセージ（必須）
	 * @param sendNow - true ですぐに送信します。
	 * 					すぐに送信時には false を設定し後で {@link #sendEmail(String)} で送信するよりも true の方が早い
	 * </span>
	 */
	public String composePlainTextEmail(String fromEmailAddress, NSArray<String> toEmailAddresses, NSArray<String> bccEmailAddresses, String subject, String message, boolean sendNow) {
		// /JAssert.notEmpty( fromEmailAddress );
		// /JAssert.notNull( toEmailAddresses );
		// /JAssert.greaterThan( toEmailAddresses.count(), 0 );
		// /JAssert.notEmpty( message );

		// /JAssert.notNull( ERJavaMail.sharedInstance().defaultSession() );

		MimeMessage smtpMessage = newMimeMessage(fromEmailAddress, toEmailAddresses, bccEmailAddresses, subject, message, "text/plain", sendNow);

		return mimeMessageToString(smtpMessage);
	}

	/**
	 * <span class="en">
	 * Creates and optionally sends a WOComponent as email.
	 * 
	 * @param fromEmailAddress
	 *            Originating email address. Required.
	 * @param toEmailAddresses
	 *            Destination email address. Required.
	 * @param bccEmailAddresses
	 *            Array of Strings containing additional addressed to BCC. Null OK.
	 * @param subject
	 *            Subject the the message. Null OK.
	 * @param component
	 *            Body the the message. Required.
	 * @param sendNow
	 *            Whether to send the message right away. If you're going to send the message right away, it's faster to
	 *            set sendNow to true than set it to false and calling {@link #sendEmail(String)} later.
	 * </span>
	 * 
	 * <span class="ja">
	 * WOComponent メールを作成と送信します。
	 * 
	 * @param fromEmailAddress - 送信元メール・アドレス（必須）
	 * @param toEmailAddresses - 送信先メール・アドレス NSArray（必須）
	 * @param bccEmailAddresses - BCC メール・アドレス NSArray （Null可）
	 * @param subject - メール・サブジェクト（Null可）
	 * @param component - コンポーネント（必須）
	 * @param sendNow - true ですぐに送信します。
	 * 					すぐに送信時には false を設定し後で {@link #sendEmail(String)} で送信するよりも true の方が早い
	 * </span>
	 */
	public String composeComponentEmail(String fromEmailAddress, NSArray<String> toEmailAddresses, NSArray<String> bccEmailAddresses, String subject, WOComponent component, boolean sendNow) {
                // XXX the component parameter above was 'message'. the real parameter could be renamed.
		// /JAssert.notEmpty( fromEmailAddress );
		// /JAssert.notNull( toEmailAddresses );
		// /JAssert.greaterThan( toEmailAddresses.count(), 0 );
		// /JAssert.notNull( component );
		// /JAssert.notNull( component.context() );

		// /JAssert.notNull( ERJavaMail.sharedInstance().defaultSession() );

		WOSession session = component.context()._session();
		String response;

		component.context().generateCompleteURLs();
		if (session == null) {
			response = component.generateResponse().contentString();
		}
		else {
			boolean oldStoresIDsInURLs = session.storesIDsInURLs();
			session.setStoresIDsInURLs(true);
			response = component.generateResponse().contentString();
			session.setStoresIDsInURLs(oldStoresIDsInURLs);
		}
		component.context().generateRelativeURLs();

		// --

		MimeMessage smtpMessage = newMimeMessage(fromEmailAddress, toEmailAddresses, bccEmailAddresses, subject, response, "text/html", sendNow);

		return mimeMessageToString(smtpMessage);
	}

	/**
	 * <span class="en">
	 * Sends the RFC822 mail string created with either
	 * {@link #composePlainTextEmail(String,NSArray,NSArray,String,String,boolean)} or
	 * {@link #composeComponentEmail(String,NSArray,NSArray,String,WOComponent,boolean)}. It's faster to call either
	 * method with the sendNow parameter set to true than to use this method.
	 * </span>
	 * 
	 * <span class="ja>
	 * {@link #composePlainTextEmail(String,NSArray,NSArray,String,String,boolean)} や
	 * {@link #composeComponentEmail(String,NSArray,NSArray,String,WOComponent,boolean)} で
	 * 作成されている RFC822 メールを送信します。
	 * </span>
	 */
	public void sendEmail(String mailString) {
		// /JAssert.notEmpty( mailString );
		// /JAssert.notNull( ERJavaMail.sharedInstance().defaultSession() );

		ByteArrayInputStream bais = new ByteArrayInputStream(mailString.getBytes());
		try {
			MimeMessage smtpMessage = new MimeMessage(ERJavaMail.sharedInstance().defaultSession(), bais);
			(new MimeMessageMailDelivery(smtpMessage)).sendMail();
		}
		catch (Exception x) {
			log.error(x);
		}
	}

	@Override
	public String toString() {
		return "<ERWOMailDelivery smtpHost=" + WOApplication.application().SMTPHost() + ">";
	}

	// Private Implementation.
	private static Logger log = Logger.getLogger(ERWOMailDelivery.class);
	private static ERWOMailDelivery _sharedInstance = null;

	private MimeMessage newMimeMessage(String fromEmailAddress, NSArray<String> toEmailAddresses, NSArray<String> bccEmailAddresses, String subject, String message, String contentType, boolean sendNow) {
		// /JAssert.notEmpty( fromEmailAddress );
		// /JAssert.notNull( toEmailAddresses );
		// /JAssert.greaterThan( toEmailAddresses.count(), 0 );
		// /JAssert.notEmpty( message );
		// /JAssert.notEmpty( contentType );

		// /JAssert.notNull( ERJavaMail.sharedInstance().defaultSession() );

		MimeMessage smtpMessage = null;

		try {
			smtpMessage = new MimeMessage(ERJavaMail.sharedInstance().defaultSession());
			smtpMessage.setFrom(new InternetAddress(fromEmailAddress));

			Enumeration<String> addressEnumerator = toEmailAddresses.objectEnumerator();
			while (addressEnumerator.hasMoreElements()) {
				String address = addressEnumerator.nextElement();
				smtpMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
			}

			if (bccEmailAddresses != null && bccEmailAddresses.count() > 0) {
				addressEnumerator = bccEmailAddresses.objectEnumerator();
				while (addressEnumerator.hasMoreElements()) {
					String address = addressEnumerator.nextElement();
					smtpMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(address));
				}
			}

			smtpMessage.setSubject(subject);
			smtpMessage.setContent(message, contentType);

			if (sendNow)
				(new MimeMessageMailDelivery(smtpMessage)).sendMail();
		}
		catch (Exception x) {
			log.error(x);
		}

		return smtpMessage;
	}

	private static String mimeMessageToString(MimeMessage smtpMessage) {
		// /JAssert.notNull( smtpMessage );

		String result = null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			smtpMessage.writeTo(baos);
			result = baos.toString();
		}
		catch (Exception x) {
			log.error(x);
		}

		return result;
	}

	private static class MimeMessageMailDelivery extends ERMailDelivery {
		public MimeMessageMailDelivery(MimeMessage msg) {
			super();
			setMimeMessage(msg);
		}

		@Override
		protected DataHandler prepareMail() {
			MimeMessage msg = mimeMessage();
			String contentType = "text/plain";

			try {
				contentType = msg.getContentType();
			}
			catch (javax.mail.MessagingException x) {
				ERWOMailDelivery.log.error(x);
			}

			return new DataHandler(ERWOMailDelivery.mimeMessageToString(msg), contentType);
		}
	}
}
