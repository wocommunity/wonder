/*
 $Id$

 ERMailUtils.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class ERMailUtils extends Object {

	/** The shared mail deliverer */
	private static ERMailDeliveryHTML delivery = new ERMailDeliveryHTML ();

	/** Used to instanciate a WOComponent when no context is available,
	  * typically ouside of a session
	  *
	  * @param pageName - The name of the WOComponent that must be instanciated.
	  */
	public static WOComponent instanciatePage (String pageName) {
        // Create a context from a fake request
        WOContext context = new WOContext
		(new WORequest ("GET", "", "HTTP/1.1", null, null, null));
        return WOApplication.application ().pageWithName (pageName, context);
    }

	/** Use this method to send an HTML mail.
	  *
      * @param pageName - The name of the WOComponent that must be instanciated.
	  * @param alternatePageName - The name of the WOComponent that represents
	  *	for the text that must be displayed when an alternate plain text version
	  * of the mail needs to be provided.
	  */	
	public static void sendHTMLMail (String pageName,  String alternatePageName,
								  String emailFrom, String emailTo,
								  String emailReplyTo,
								  String subject) {
		WOComponent mailPage = (WOComponent)ERMailUtils.instanciatePage (pageName);

        delivery.newMail ();
        delivery.setWOComponentContent (mailPage);

		if (alternatePageName != null) {
			String alternateString = null;
			WOComponent alternateMailTemplate = (WOComponent)ERMailUtils.instanciatePage (alternatePageName);
			alternateString = alternateMailTemplate.generateResponse ().contentString ();
			if (alternateString != null) {
				delivery.setHiddenPlainTextContent (alternateString);
				alternateMailTemplate.session ().terminate ();
			}
		}

		try {
			delivery.setFromAddress	(emailFrom);
			delivery.setToAddress	(emailTo);
			delivery.setReplyToAddress (emailReplyTo);
			delivery.setSubject ((subject == null) ? "" : subject);
			delivery.sendMail ();
		} catch (javax.mail.MessagingException e) {
			// we must handle this exception correctly because the mail cannot be sent
			e.printStackTrace ();
		} finally {
			// We need to force the termination of the sessions because there is some
            // sort of circular reference between the context and the session when it is
            // instanciated from a newly created WOContext.
			mailPage.session ().terminate ();
		}
	}	
	
}
