/*
 ERMailDeliveryHTML.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.Date;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * <span class="en">
 * This ERMailDelivery subclass is specifically crafted for HTML messages using a WOComponent as rendering device.
 * </span>
 * 
 * <span class="ja">
 * HTML メッセージに使用する ERMailDelivery サブクラス
 * </span>
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public class ERMailDeliveryHTML extends ERMailDeliveryComponentBased {
	protected static Factory factory;

	/** Holds the HTML content */
	protected String _htmlContent;

	/**
	 * Plain text preamble set in top of HTML source so that non-HTML compliant mail readers can at least display this
	 * message.
	 */
	private String _hiddenPlainTextContent;

	/**
	 * <span class="en">
	 * Gets the current factory. If the factory is unset, sets it to the default factory.
	 * 
	 * @return the current factory
	 * </span>
	 * 
	 * <span class="ja">
	 * カレント・ファクトリーを戻します。
	 * ファクトリーがセットされていない場合、デフォルト・ファクトリーが使用される
	 * 
	 * @return カレント・ファクトリー
	 * </span>
	 */
	public static Factory factory() {
		if (factory == null)
			factory = new DefaultFactory();

		return factory;
	}

	/**
	 * <span class="en">
	 * Sets the factory.
	 * 
	 * @param value
	 *            the factory to use
	 * </span>
	 * 
	 * <span class="ja">
	 * ファクトリーをセットします。
	 * 
	 * @param value - 使用されるファクトリー
	 * </span>
	 */
	public static void setFactory(Factory value) {
		factory = value;
	}

	public static ERMailDeliveryHTML newMailDelivery() {
		return factory().newHTMLMailDelivery();
	}

	/**
	 * <span class="en">
	 * Sets the Plain text preamble that will be displayed set in top of HTML source. Non-HTML compliant mail readers
	 * can at least display this message.
	 * </span>
	 * 
	 * <span class="ja">
	 * HTML ソースの前に標準テキストをセットします。
	 * HTML 表示が不可能なメール・ソフトウェアでも標準テキストが表示できるようになります。
	 * </span>
	 */
	public void setHiddenPlainTextContent(String content) {
		_hiddenPlainTextContent = content;
	}

	/**
	 * <span class="en">
	 * Sets the HTML content. Note that if you set the WOComponent to be used when rendering the message this content
	 * will be ignored.
	 * 
	 * @param content
	 *            HTML content to be used
	 * </span>
	 * 
	 * <span class="ja">
	 * HTML コンテントをセットします。
	 * 注意：レンダリングに WOComponent を使用される場合にはこのコンテントが無視されます。
	 * 
	 * @param content - 使用される HTML コンテント
	 * </span>
	 */
	public void setHTMLContent(String content) {
		_htmlContent = content;
	}

	/** 
	 * <span class="en">
	 * Creates a new mail instance within ERMailDelivery. Sets hasHiddenPlainTextContent to false. 
	 * </span>
	 * 
	 * <span class="ja">
	 * ERMailDelivery の新規メール・インスタンスを作成します。hasHiddenPlainTextContent を null にします。
	 * </span>
	 */
	@Override
	public void newMail() {
		super.newMail();
		_hiddenPlainTextContent = null;
		setHTMLContent(null);
	}

	protected String htmlContent() {
		String htmlContent = null;
		if (component() != null) {
			htmlContent = componentContentString();
		}
		else {
			htmlContent = _htmlContent;
		}
		return htmlContent;
	}

	/**
	 * <span class="en">
	 * Pre-processes the mail before it gets sent.
	 * </span>
	 * 
	 * <span class="ja">
	 * メールが送信される前の前処理
	 * 
	 * @see ERMailDelivery#prepareMail()
	 * </span>
	 */
	@Override
	protected DataHandler prepareMail() throws MessagingException {
		MimeMultipart multipart = null;
		MimeBodyPart textPart = null;
		MimeBodyPart htmlPart = null;

		mimeMessage().setSentDate(new Date());
		multipart = new MimeMultipart("alternative");

		// set the plain text part
		String textContent;
		if (_hiddenPlainTextContent != null) {
			textContent = _hiddenPlainTextContent;
		}
		else {
			textContent = alternativeComponentContentString();
		}
		
		if (textContent != null) {
			textPart = new MimeBodyPart();
			textPart.setText(textContent + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n", charset());
			multipart.addBodyPart(textPart);
		}

		// create and fill the html message part
		htmlPart = new MimeBodyPart();

		// Set the content of the html part
		htmlPart.setContent(htmlContent(), "text/html; charset=\"" + charset() + "\"");

		// Inline attachements
		if (inlineAttachments().count() == 0) {
			multipart.addBodyPart(htmlPart);
		}
		else {
			// Create a "related" MimeMultipart
			MimeMultipart relatedMultiparts = new MimeMultipart("related");
			relatedMultiparts.addBodyPart(htmlPart);

			// add each inline attachments to the message
			for (ERMailAttachment attachment : inlineAttachments()) {
				BodyPart bp = attachment.getBodyPart();
				relatedMultiparts.addBodyPart(bp);
			}

			// Add this multipart to the main multipart as a compound BodyPart
			BodyPart relatedAttachmentsBodyPart = new MimeBodyPart();
			relatedAttachmentsBodyPart.setDataHandler(new DataHandler(relatedMultiparts, relatedMultiparts.getContentType()));
			multipart.addBodyPart(relatedAttachmentsBodyPart);
		}

		return new DataHandler(multipart, multipart.getContentType());
	}

	public static interface Factory {
		/**
		 * <span class="en">
		 * Vends a new instance of an HTML mail delivery.
		 * 
		 * @return a new instance
		 * </span>
		 * 
		 * <span class="ja">
		 * HTML メールの新規インスタンスを作成します。
		 * 
		 * @return 新規インスタンス
		 * </span>
		 */
		public ERMailDeliveryHTML newHTMLMailDelivery();
	}

	/**
	 * <span class="en">
	 * The default factory. Vends the ERMailDeliveryHTML object back.
	 * </span>
	 * 
	 * <span class="ja">
	 * デフォルト・ファクトリー
	 * ERMailDeliveryHTML オブジェクトが戻ります。
	 * </span>
	 */
	public static class DefaultFactory implements Factory {
		public ERMailDeliveryHTML newHTMLMailDelivery() {
			return new ERMailDeliveryHTML();
		}
	}

}
