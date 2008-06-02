/*
 $Id$

 ERMailUtils.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.util.Enumeration;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXApplication;

/**
 * <code>ERMailUtils</code> contains various utility method related to mail sending.
 * 
 * @author <a href="mailto:tuscland@mac.com">Camille Troillard</a>
 * @version $Id$
 */
public class ERMailUtils extends Object {

	/** The class logger. */
	private static Logger log = Logger.getLogger(ERMailUtils.class);

	/** The shared mail deliverer */
	private static ERMailDeliveryHTML sharedDeliverer;

	/**
	 * Accessor to the shared instance of a ERMailDeliveryHTML.
	 * 
	 * @return the <code>ERMailDeliveryHTML</code> singleton
	 */
	public static ERMailDeliveryHTML sharedDeliverer() {
		if (sharedDeliverer == null) {
			sharedDeliverer = ERMailDeliveryHTML.newMailDelivery();
		}

		return sharedDeliverer;
	}

	/**
	 * Augmented version of the method found in {@link ERXApplication}. Used to instantiate a WOComponent, typically
	 * outside of a session.
	 * 
	 * @param pageName
	 *            The name of the WOComponent that must be instantiated.
	 * @param sessionDict
	 *            can be provided in order to set objects/keys in the newly created session of the component. This is
	 *            useful when one want to preserve state when sending a mail.
	 * @return a newly instantiated <code>WOComponent</code>.
	 */
	public static WOComponent instantiatePage(String pageName, NSDictionary sessionDict) {
		WOComponent component = ERXApplication.instantiatePage(pageName);
		if (sessionDict != null) {
			setDictionaryValuesInSession(sessionDict, component.session());
		}

		return component;
	}

	/**
	 * Use this method to send an HTML mail.
	 * 
	 * @param delivery
	 *            the <code>ERMailDeliveryHTML</code> used to send the mail.
	 * @param pageName
	 *            The name of the WOComponent that must be instantiated.
	 * @param alternatePageName
	 *            The name of the WOComponent that represents for the text that must be displayed when an alternate
	 *            plain text version of the mail needs to be provided.
	 * @param emailFrom
	 *            the email address the mail is sent from
	 * @param emailTo
	 *            the email address the mail is sent to
	 * @param emailReplyTo
	 *            the email address where the mail must be replied-to.
	 * @param subject
	 *            the subject of the mail
	 */
	public static void sendHTMLMail(ERMailDeliveryHTML delivery, String pageName, String alternatePageName, String emailFrom, String emailTo, String emailReplyTo, String subject) {
		WOComponent mailPage = ERMailUtils.instantiatePage(pageName, delivery.sessionDictionary());

		delivery.newMail();
		delivery.setComponent(mailPage);

		if (alternatePageName != null) {
			String alternateString = null;
			WOComponent alternateMailTemplate = ERMailUtils.instantiatePage(alternatePageName, delivery.sessionDictionary());

			alternateString = alternateMailTemplate.generateResponse().contentString();

			if (alternateString != null) {
				delivery.setHiddenPlainTextContent(alternateString);
				alternateMailTemplate.session().terminate();
			}
		}

		try {
			delivery.setFromAddress(emailFrom);
			delivery.setToAddress(emailTo);
			delivery.setReplyToAddress(emailReplyTo);
			delivery.setSubject((subject == null) ? "" : subject);
			delivery.sendMail();
		}
		catch (javax.mail.MessagingException e) {
			// we must handle this exception correctly because the
			// mail cannot be sent
			log.warn("While trying to sendMail: ", e);
		}
		finally {
			// We need to force the termination of the sessions
			// because there is some sort of circular reference
			// between the context and the session when it is
			// instantiated from a newly created WOContext.
			mailPage.session().terminate();
		}
	}

	/**
	 * Use this method to send an HTML mail, but default mail delivery.
	 * 
	 * @param pageName
	 *            The name of the WOComponent that must be instantiated.
	 * @param alternatePageName
	 *            The name of the WOComponent that represents for the text that must be displayed when an alternate
	 *            plain text version of the mail needs to be provided.
	 * @param emailFrom
	 *            the email address the mail is sent from
	 * @param emailTo
	 *            the email address the mail is sent to
	 * @param emailReplyTo
	 *            the email address where the mail must be replied-to.
	 * @param subject
	 *            the subject of the mail
	 */
	public static void sendHTMLMail(String pageName, String alternatePageName, String emailFrom, String emailTo, String emailReplyTo, String subject) {
		sendHTMLMail(sharedDeliverer(), pageName, alternatePageName, emailFrom, emailTo, emailReplyTo, subject);
	}

	/**
	 * This method sets the values found in a dictionary into the session's state dictionary. This method is useful when
	 * one want to transfer current session's state into a newly created session (for example when sending a mail whose
	 * page has been instantiated with {@link ERMailUtils.instantiatePage} or {@link ERXApplication.instantiatePage}.)
	 * 
	 * @param dict
	 *            a <code>NSDictionary</code> value containing the values we want to set in the session parameter.
	 * @param session
	 *            a <code>WOSession</code> value that will receive the values contained in the dict parameter.
	 */
	public static void setDictionaryValuesInSession(NSDictionary dict, WOSession session) {
		if ((dict == null) || (session == null)) {
			return;
		}

		Enumeration en = dict.keyEnumerator();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			Object object = dict.objectForKey(key);
			if (object != null) {
				if (log.isDebugEnabled()) {
					log.debug("Setting in session dict value '" + object.toString() + "' for key '" + key + "'");
				}

				session.setObjectForKey(object, key);
			}
		}
	}

	public static String encodeString(String string, String charset) {
		String encodedString = null;

		try {
			encodedString = MimeUtility.encodeText(string, charset, !charset.equals(ERMailDelivery.DefaultCharset) ? "B" : null);
		}
		catch (Exception e) {
			encodedString = string;
		}

		return encodedString;
	}

	/**
	 * Method that converts NSArray of String emails to InternetAddress [].
	 * 
	 * @param addrs
	 *            a <code>NSArray</code> value
	 * @return an <code>InternetAddress[]</code> value
	 * @exception AddressException
	 *                if an error occurs
	 */
	public static InternetAddress[] convertNSArrayToInternetAddresses(NSArray addrs) throws AddressException {
		if (addrs == null)
			return new InternetAddress[0];
		InternetAddress[] addrArray = new InternetAddress[addrs.count()];

		Enumeration en = addrs.objectEnumerator();
		for (int i = 0; en.hasMoreElements(); i++) {
			String anAddress = (String) en.nextElement();
			addrArray[i] = new InternetAddress(anAddress);
		}

		return addrArray;
	}

	/**
	 * Method that converts Address [] loaded with either Address or InternetAddress objects to NSArray of String
	 * emails.
	 * <p>
	 * Note that this method will not only accept Address [] but also InternetAddress [].
	 * 
	 * @param addressesArray
	 *            an <code>Address[]</code> value
	 * @return a <code>NSArray</code> value
	 * @exception AddressException
	 *                if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public static NSArray<String> convertInternetAddressesToNSArray(Address[] addressesArray) {
		if (addressesArray == null)
			return NSArray.EmptyArray;
		NSMutableArray<String> addresses = new NSMutableArray<String>(addressesArray.length);

		for (int i = 0; i < addressesArray.length; i++) {
			Address anAddress = addressesArray[i];
			String emailAddress = null;

			if (anAddress instanceof InternetAddress)
				emailAddress = ((InternetAddress) anAddress).toUnicodeString();
			else
				// anAddress will be a instance of Address
				emailAddress = anAddress.toString();

			addresses.addObject(emailAddress);
		}

		return addresses;
	}

}
