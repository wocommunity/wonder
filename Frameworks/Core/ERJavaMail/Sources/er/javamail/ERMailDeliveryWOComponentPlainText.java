/*
 ERMailDeliveryWOComponentPlainText.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.activation.DataHandler;

/**
 * <div class="en">
 * This ERMailDelivery subclass is specifically crafted for plain text messages using a WOComponent as rendering device.
 * </div>
 * 
 * <div class="ja">
 * レンダリングに WOComponent を使用する場合の標準テキスト・メッセージの ERMailDelivery サブクラス
 * </div>
 * 
 * @author Camille Troillard &lt;tuscland@mac.com&gt;
 */
public class ERMailDeliveryWOComponentPlainText extends ERMailDeliveryComponentBased {

	/**
	 * <div class="en">
	 * Pre-processes the mail before it gets sent.
	 * </div>
	 * 
	 * <div class="ja">
	 * 送信前に前処理を行います。
	 * </div>
	 * 
	 * @see ERMailDelivery#prepareMail()
	 */
	@Override
	protected DataHandler prepareMail() {
		String messageContent = componentContentString();
		return new DataHandler(messageContent, "text/plain; charset=\"" + charset() + "\"");
	}
}
