/*
 ERMailDeliveryWOComponentPlainText.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.activation.DataHandler;

/**
 * <span class="en">
 * This ERMailDelivery subclass is specifically crafted for plain text messages using a WOComponent as rendering device.
 * </span>
 * 
 * <span class="ja">
 * レンダリングに WOComponent を使用する場合の標準テキスト・メッセージの ERMailDelivery サブクラス
 * </span>
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public class ERMailDeliveryWOComponentPlainText extends ERMailDeliveryComponentBased {

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
		String messageContent = componentContentString();
		return new DataHandler(messageContent, "text/plain; charset=\"" + charset() + "\"");
	}
}
