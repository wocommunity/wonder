/*
 ERMailDeliveryPlainText.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.activation.DataHandler;

/**
 * <span class="en">
 * This ERMailDelivery subclass is specifically crafted for plain text messages.
 * </span>
 * 
 * <span class="ja">
 * 標準テキスト・メッセージに使用する ERMailDelivery サブクラス
 * </span>
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public class ERMailDeliveryPlainText extends ERMailDelivery {
	
	/** Designated constructor */
	public ERMailDeliveryPlainText(javax.mail.Session session) {
		super(session);
	}

	/** Default constructor */
	public ERMailDeliveryPlainText() {
		super();
	}

	/** String Message content */
	private String textContent;

	/** Sets the text content of the current message. */
	public void setTextContent(String text) {
		textContent = text;
	}

	/**
	 * <span class="en">
	 * Pre-processes the mail before it gets sent.
	 * </span>
	 * 
	 * <span class="ja">
	 * 送信前に前処理を行います。
	 * </span>
	 * 
	 * @see ERMailDelivery#prepareMail()
	 */
	@Override
	protected DataHandler prepareMail() {
		String charset = charset();
		DataHandler dataHandler;
		if (charset != null) {
			dataHandler = new DataHandler(textContent, "text/plain; charset=\""  + charset() + "\"");
		}
		else {
			dataHandler = new DataHandler(textContent, "text/plain");
		}
		return dataHandler;
	}

}
