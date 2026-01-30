/*
 ERMailAttachement.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;

public abstract class ERMailAttachment {
	protected Object _content;

	public ERMailAttachment(Object content) {
		super();
		_content = content;
	}

	protected Object content() {
		return _content;
	}

	protected void setContent(Object content) {
		_content = content;
	}

	protected abstract BodyPart getBodyPart() throws MessagingException;
}
