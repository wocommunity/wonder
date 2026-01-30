/*
 ERMailTextAttachment.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;

public class ERMailTextAttachment extends ERMailFileAttachment {

	public ERMailTextAttachment(String fileName, String content) {
		super(content);
		_fileName = fileName;
	}

	@Override
	protected BodyPart getBodyPart() throws MessagingException {
		MimeBodyPart bp = new MimeBodyPart();

		bp.setText((String) content(), ERMailDelivery.DefaultCharset);
		bp.setFileName(fileName());

		return bp;
	}
}
