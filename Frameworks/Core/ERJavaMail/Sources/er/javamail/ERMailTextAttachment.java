/*
 $Id$
 
 ERMailTextAttachment.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

public class ERMailTextAttachment extends ERMailFileAttachment {

	public ERMailTextAttachment(String fileName, String content) {
		super(content);
		this.setFileName(fileName);
	}

	@Override
	protected BodyPart getBodyPart() throws MessagingException {
		MimeBodyPart bp = new MimeBodyPart();

		bp.setText((String) this.content(), ERMailDelivery.DefaultCharset);
		bp.setFileName(this.fileName());

		return bp;
	}
}
