/*
  $Id$

  ERMailUtils.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;


import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.ERXLogger;
import java.util.Enumeration;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


public class ERMailUtils extends Object {

    private static ERXLogger log = ERXLogger.getERXLogger (ERMailUtils.class);

    /** The shared mail deliverer */
    private static ERMailDeliveryHTML _sharedDeliverer;

    public static ERMailDeliveryHTML sharedDeliverer () {
	if (_sharedDeliverer == null)
	    _sharedDeliverer = new ERMailDeliveryHTML ();
	return _sharedDeliverer;
    }

    /** Used to instanciate a WOComponent when no context is available,
     * typically ouside of a session.  An optional argument
     * sessionDictionary can be provided in order to set objects/keys
     * in the newly create session of the component.
     *
     * @param pageName - The name of the WOComponent that must be instanciated.
     */
    public static WOComponent instanciatePage (String pageName, NSDictionary sessionDictionary) {
        // Create a context from a fake request
        WOContext context = new WOContext
	    (new WORequest ("GET", "", "HTTP/1.1", null, null, null));
        WOComponent component = WOApplication.application ().pageWithName (pageName, context);
        if (sessionDictionary != null)
            setDictionaryValuesInSession (sessionDictionary, component.session ());
        return component;
    }

    /** Use this method to send an HTML mail.
     *
     * @param pageName - The name of the WOComponent that must be instanciated.
     * @param alternatePageName - The name of the WOComponent that represents
     *	for the text that must be displayed when an alternate plain text version
     * of the mail needs to be provided.
     */
    public static void sendHTMLMail (ERMailDeliveryHTML delivery,
				     String pageName,  String alternatePageName,
				     String emailFrom, String emailTo,
				     String emailReplyTo, String subject) {
	WOComponent mailPage = (WOComponent)ERMailUtils.instanciatePage (pageName, 
                                                                         delivery.sessionDictionary ());

        delivery.newMail ();
        delivery.setComponent (mailPage);

	if (alternatePageName != null) {
	    String alternateString = null;
	    WOComponent alternateMailTemplate =
                (WOComponent)ERMailUtils.instanciatePage (alternatePageName,
                                                          delivery.sessionDictionary ());
            alternateString = alternateMailTemplate.generateResponse ().contentString ();

            if (alternateString != null) {
		delivery.setHiddenPlainTextContent (alternateString);
		alternateMailTemplate.session ().terminate ();
	    }
	}

	try {
	    delivery.setFromAddress    (emailFrom);
	    delivery.setToAddress      (emailTo);
	    delivery.setReplyToAddress (emailReplyTo);
	    delivery.setSubject ((subject == null) ? "" : subject);
	    delivery.sendMail ();
	} catch (javax.mail.MessagingException e) {
	    // we must handle this exception correctly because the mail cannot be sent
	    log.warn ("While trying to sendMail: ", e);
	} finally {
	    // We need to force the termination of the sessions because there is some
            // sort of circular reference between the context and the session when it is
            // instanciated from a newly created WOContext.
	    mailPage.session ().terminate ();
	}
    }

    public static void setDictionaryValuesInSession (NSDictionary dict, WOSession session) {
        if ((dict == null) || (session == null))
            return;

        Enumeration en = dict.keyEnumerator ();
        while (en.hasMoreElements ()) {
            String key = (String)en.nextElement ();

            if (log.isDebugEnabled ())
                log.debug ("Setting dictionary value in session");

            Object object = dict.objectForKey (key);
            if (object != null)
                session.setObjectForKey (object, key);
        }
    }

    public static void sendHTMLMail (String pageName,  String alternatePageName,
				     String emailFrom, String emailTo,
				     String emailReplyTo, String subject) {
	sendHTMLMail (sharedDeliverer (), pageName, alternatePageName,
		      emailFrom, emailTo, emailReplyTo, subject);
    }

    /** Private method that converts NSArray of String emails to InternetAddress []. */
    public static InternetAddress [] convertNSArrayToInternetAddresses (NSArray addressesArray)
	throws AddressException {
        InternetAddress [] addresses = new InternetAddress [addressesArray.count ()];

        Enumeration en = addressesArray.objectEnumerator ();
        for (int i=0 ; en.hasMoreElements () ; i++) {
            String anAddress = (String)en.nextElement ();
            addresses [i] = new InternetAddress (anAddress);
        }

        return addresses;
    }

    /** Private method that converts NSArray of String emails to InternetAddress []. */
    public static NSArray convertInternetAddressesToNSArray (InternetAddress [] addressesArray)
	throws AddressException {
	NSMutableArray addresses = new NSMutableArray (addressesArray.length);

        for (int i=0 ; i<addressesArray.length ; i++) {
            InternetAddress anAddress = (InternetAddress)addressesArray[i];
            addresses.addObject (anAddress.toUnicodeString ());
        }

        return addresses;
    }
}
