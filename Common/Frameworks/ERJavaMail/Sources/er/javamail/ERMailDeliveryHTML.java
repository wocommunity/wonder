/*
 $Id$
 
 ERMailDeliveryHTML.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.Date;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * This ERMailDelivery subclass is specifically crafted for HTML messages using a WOComponent as redering device.
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
	 * Gets the current factory. If the factory is unset, sets it to the default factory.
	 * 
	 * @return the current factory
	 */
	public static Factory factory() {
		if (factory == null)
			factory = new DefaultFactory();

		return factory;
	}

	/**
	 * Sets the factory.
	 * 
	 * @param value
	 *            the factory to use
	 */
	public static void setFactory(Factory value) {
		factory = value;
	}

	public static ERMailDeliveryHTML newMailDelivery() {
		return factory().newHTMLMailDelivery();
	}

	/**
	 * Sets the Plain text preamble that will be displayed set in top of HTML source. Non-HTML compliant mail readers
	 * can at least display this message.
	 */
	public void setHiddenPlainTextContent(String content) {
		_hiddenPlainTextContent = content;
	}

	/**
	 * Sets the HTML content. Note that if you set the WOComponent to be used when rendering the message this content
	 * will be ignored.
	 * 
	 * @param content
	 *            HTML content to be used
	 */
	public void setHTMLContent(String content) {
		_htmlContent = content;
	}

	/** Creates a new mail instance within ERMailDelivery. Sets hasHiddenPlainTextContent to false. */
	public void newMail() {
		super.newMail();
		_hiddenPlainTextContent = null;
		setHTMLContent(null);
	}

	protected String htmlContent() {
		String htmlContent = null;
		if (this.component() != null) {
			htmlContent = this.componentContentString();
		}
		else {
			htmlContent = _htmlContent;
		}
		return htmlContent;
	}

	/**
	 * Pre-processes the mail before it gets sent.
	 * 
	 * @see ERMailDelivery#prepareMail
	 */
	protected DataHandler prepareMail() throws MessagingException {
		MimeMultipart multipart = null;
		MimeBodyPart textPart = null;
		MimeBodyPart htmlPart = null;

		this.mimeMessage().setSentDate(new Date());
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
		htmlPart.setContent(this.htmlContent(), "text/html; charset=\"" + charset() + "\"");

		// Inline attachements
		if (inlineAttachments().count() == 0) {
			multipart.addBodyPart(htmlPart);
		}
		else {
			// Create a "related" MimeMultipart
			MimeMultipart relatedMultiparts = new MimeMultipart("related");
			relatedMultiparts.addBodyPart(htmlPart);

			// add each inline attachments to the message
			Enumeration en = this.inlineAttachments().objectEnumerator();
			while (en.hasMoreElements()) {
				ERMailAttachment attachment = (ERMailAttachment) en.nextElement();
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
		 * Vends a new instance of an HTML mail delivery.
		 * 
		 * @return a new instance
		 */
		public ERMailDeliveryHTML newHTMLMailDelivery();
	}

	/**
	 * The default factory. Vends the ERMailDeliveryHTML object back.
	 */
	public static class DefaultFactory implements Factory {
		public ERMailDeliveryHTML newHTMLMailDelivery() {
			return new ERMailDeliveryHTML();
		}
	}

}
