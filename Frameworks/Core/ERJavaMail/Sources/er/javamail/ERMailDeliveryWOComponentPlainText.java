/*
 $Id$
 
 ERMailDeliveryWOComponentPlainText.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.activation.DataHandler;

/**
 * This ERMailDelivery subclass is specifically crafted for plain text messages using a WOComponent as redering device.
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public class ERMailDeliveryWOComponentPlainText extends ERMailDeliveryComponentBased {

	/**
	 * Pre-processes the mail before it gets sent.
	 * 
	 * @see ERMailDelivery#prepareMail
	 */
	@Override
	protected DataHandler prepareMail() {
		String messageContent = this.componentContentString();
		return new DataHandler(messageContent, "text/plain; charset=\"" + charset() + "\"");
	}
}
