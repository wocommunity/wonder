/*
 $Id$
 
 ERMailDeliveryPlainText.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.activation.DataHandler;

/**
 * This ERMailDelivery subclass is specifically crafted for plain text messages.
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public class ERMailDeliveryPlainText extends ERMailDelivery {

	/** String Message content */
	private String textContent;

	/** Sets the text content of the current message. */
	public void setTextContent(String text) {
		textContent = text;
	}

	/**
	 * Pre-processes the mail before it gets sent.
	 * 
	 * @see ERMailDelivery#prepareMail
	 */
	@Override
	protected DataHandler prepareMail() {
		return new DataHandler(textContent, "text/plain; charset=\"" + charset() + "\"");
	}

}
