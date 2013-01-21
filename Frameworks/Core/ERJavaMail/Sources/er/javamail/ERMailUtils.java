/*
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

import er.extensions.appserver.ERXApplication;

/**
 * <span class="en">
 * <code>ERMailUtils</code> contains various utility method related to mail sending.
 * </span>
 * 
 * <span class="ja">
 * <code>ERMailUtils</code> はメール送信の為のユーティリティー・メソッド集
 * </span>
 * 
 * @author <a href="mailto:tuscland@mac.com">Camille Troillard</a>
 * @version $Id$
 */
public class ERMailUtils {

	/** The class logger. */
	private static Logger log = Logger.getLogger(ERMailUtils.class);

	/** 
	 * <span class="en">
	 * The shared mail deliverer
	 * </span>
	 * 
	 * <span class="ja">
	 * メール配信の共有インスタンス 
	 * </span>
	 */
	private static ERMailDeliveryHTML sharedDeliverer;

	/**
	 * <span class="en">
	 * Accessor to the shared instance of a ERMailDeliveryHTML.
	 * 
	 * @return the <code>ERMailDeliveryHTML</code> singleton
	 * </span>
	 * 
	 * <span class="ja">
	 * 共有インスタンス ERMailDeliveryHTML へのアクセス
	 * 
	 * @return <code>ERMailDeliveryHTML</code> シングルトン
	 * </span>
	 */
	public static ERMailDeliveryHTML sharedDeliverer() {
		if (sharedDeliverer == null) {
			sharedDeliverer = ERMailDeliveryHTML.newMailDelivery();
		}

		return sharedDeliverer;
	}

	/**
	 * <span class="en">
	 * Augmented version of the method found in {@link ERXApplication}. Used to instantiate a WOComponent, typically
	 * outside of a session.
	 * 
	 * @param pageName
	 *            The name of the WOComponent that must be instantiated.
	 * @param sessionDict
	 *            can be provided in order to set objects/keys in the newly created session of the component. This is
	 *            useful when one want to preserve state when sending a mail.
	 * @return a newly instantiated <code>WOComponent</code>.
	 * </span>
	 * 
	 * <span class="ja">
	 * {@link ERXApplication} 内にある同名メソッドの拡張版。
	 * セッションの外側のインスタンス化に使用します。
	 * 
	 * @param pageName - インスタンス化する WOComponent 名
	 * @param sessionDict - コンポーネントのために、新規作成されるセッションにセットする「オブジェクト/キー」
	 * 						メール送信にセッション情報が必要な場合に有効です。
	 *            
	 * @return 新規のインスタンス済み <code>WOComponent</code> 
	 * </span>
	 */
	public static WOComponent instantiatePage(String pageName, NSDictionary sessionDict) {
		WOComponent component = ERXApplication.instantiatePage(pageName);
		if (sessionDict != null) {
			setDictionaryValuesInSession(sessionDict, component.session());
		}

		return component;
	}

	/**
	 * <span class="en">
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
	 * </span>
	 * 
	 * <span class="ja">
	 * HTML メール送信に使用します。
	 * 
	 * @param delivery - メール配信に使用される  <code>ERMailDeliveryHTML</code>
	 * @param pageName - HTMLメッセージを持つインスタンス化する WOComponent 名
	 * @param alternatePageName - テキスト・メッセージを持つインスタンス化する WOComponent 名
	 * @param emailFrom - 送信元のメール・アドレス
	 * @param emailTo - 送信先のメール・アドレス
	 * @param emailReplyTo - 返信先のメール・アドレス
	 * @param subject - メールのサブジェクト
	 * </span>
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
	 * <span class="en">
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
	 * </span>
	 * 
	 * <span class="ja">
	 * デフォルト配信インスタンスを使用し、 HTML メール送信を行います。
	 * 
	 * @param pageName - HTMLメッセージを持つインスタンス化する WOComponent 名
	 * @param alternatePageName - テキスト・メッセージを持つインスタンス化する WOComponent 名
	 * @param emailFrom - 送信元のメール・アドレス
	 * @param emailTo - 送信先のメール・アドレス
	 * @param emailReplyTo - 返信先のメール・アドレス
	 * @param subject - メールのサブジェクト
	 * </span>
	 */
	public static void sendHTMLMail(String pageName, String alternatePageName, String emailFrom, String emailTo, String emailReplyTo, String subject) {
		sendHTMLMail(sharedDeliverer(), pageName, alternatePageName, emailFrom, emailTo, emailReplyTo, subject);
	}

	/**
	 * <span class="en">
	 * This method sets the values found in a dictionary into the session's state dictionary. This method is useful when
	 * one want to transfer current session's state into a newly created session (for example when sending a mail whose
	 * page has been instantiated with {@link #instantiatePage(String, NSDictionary)} or
	 * {@link er.extensions.appserver.ERXApplication#instantiatePage(String)}.)
	 * 
	 * @param dict
	 *            a <code>NSDictionary</code> value containing the values we want to set in the session parameter.
	 * @param session
	 *            a <code>WOSession</code> value that will receive the values contained in the dict parameter.
	 * </span>
	 * 
	 * <span class="ja">
	 * このメソッドはディクショナリー内で見つかる値をセッション状態ディクショナリーにセットします。
	 * このメソッドはカレント・セッションを新セッションに移行する時に有効です。
	 * （たとえば、{@link #instantiatePage(String, NSDictionary)} や {@link er.extensions.appserver.ERXApplication#instantiatePage(String)}
	 * でインスタンス化されているページを送信する場合に有効です。）
	 * 
	 * @param dict - セッションに設定する情報を持つ <code>NSDictionary</code> 
	 * @param session - ディクショナリー内に設定されている値をセットする <code>WOSession</code>
	 * </span>
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

	/** <span class="ja">エンコーディング処理</span> */
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
	 * <span class="en">
	 * Method that converts NSArray of String emails to InternetAddress [].
	 * 
	 * @param addrs
	 *            a <code>NSArray</code> value
	 * @return an <code>InternetAddress[]</code> value
	 * @exception AddressException
	 *                if an error occurs
	 * </span>
	 * 
	 * <span class="ja">
	 * String メールの NSArray を InternetAddress [] へ変換します。
	 * 
	 * @param addrs - <code>NSArray</code>
	 * 
	 * @return <code>InternetAddress[]</code>
	 * 
	 * @exception AddressException - エラー発生した場合
	 * </span>
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
	 * <span class="en">
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
	 * </span>
	 * 
	 * <span class="ja">
	 * Address [] 又は InternetAddress [] を String メールの NSArray へ変換します。
	 * <p>
	 * 注意： Address [] 又は InternetAddress [] が有効です
	 * 
	 * @param addressesArray - <code>Address[]</code>
	 * 
	 * @return <code>NSArray</code>
	 * 
	 * @exception AddressException - エラー発生した場合
	 * </span>
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

	/**
	 * <span class="en">
	 * Method that converts NSDictionary consisting of String emails as keys and String personal names
	 * to InternetAddress [].
	 * 
	 * @param addrs
	 *            a <code>NSDictionary</code> with email, personal name as key value pair
	 * @param charset
	 *            a <code>String</code> of the charset to use for personal string
	 * @return an <code>InternetAddress[]</code> value
	 * @exception AddressException
	 *                if an error occurs
	 * </span>
	 * 
	 * <span class="ja">
	 * String メールの NSDictionary を InternetAddress [] へ変換します。
	 * キーが String メール・アドレスでオブジェクトが String の個人名
	 * 
	 * @param addrs - <code>NSDictionary</code> メール 個人名の KV
	 * @param charset - 個人名文字列に使用する文字セット
	 *            
	 * @return <code>InternetAddress[]</code>
	 * 
	 * @exception AddressException - エラー発生した場合
	 * </span>
	 */
	public static InternetAddress[] convertNSDictionaryToInternetAddresses(NSDictionary<String, String> addrs, String charset) throws AddressException {
		if (addrs == null || addrs.isEmpty())
			return new InternetAddress[0];
		InternetAddress[] addrArray = new InternetAddress[addrs.count()];
		InternetAddress address;
		int i = 0;

		for (String email : addrs.allKeys()) {
			String personal = addrs.objectForKey(email);
			
			if (personal != null && personal.length() > 0) {
				address = new InternetAddress();
				address.setAddress(email);

				try {
					address.setPersonal(personal, charset);
				}
				catch (java.io.UnsupportedEncodingException ex) {
					// set the string anyway.
					try {
						address.setPersonal(personal);
					}
					catch (Exception e) {
						// give up ...
					}
				}
			}
			else {
				address = new InternetAddress(email);
			}
			addrArray[i++] = address;
		}

		return addrArray;
	}
	
	/**
	* This method will parse a large string of email address that could be separated by commas,
	* semicolon, tabs, spaces, carriage returns, (even mixed) and will return an NSArray of addresses(strings)
	* @param str
	* @return NSArray<String> of email address
	*/
	public static NSArray<String> emailsFromBulkList(String str) {
		if ( (str!=null) && (str.length() > 3) ) {
			//str = str.toLowerCase();
			str = str.replace("\"", "");
			str = str.replace(";", "");
			str = str.replace(":", "");
			str = str.replace("'", "");
			str = str.replace("\n", ",");
			str = str.replace("\r", ",");
			str = str.replace(" ", ",");
			str = str.replace("\t", ",");
			str = str.replaceAll(",+", ",");
			String[] tokens = str.split(",");
			return new NSArray<String>(tokens);
		}
		return null;
	}

}
