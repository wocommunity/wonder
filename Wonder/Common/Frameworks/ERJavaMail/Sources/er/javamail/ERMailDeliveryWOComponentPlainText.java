/*
 $Id$
 
 ERMailDeliveryWOComponentPlainText.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

import java.util.*;
import java.io.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

/** This ERMailDelivery subclass is specifically crafted for plain
    text messages using a WOComponent as redering device.
    @author Camille Troillard <tuscland@mac.com> */
public class ERMailDeliveryWOComponentPlainText extends ERMailDeliveryComponentBased {
    
    /** Pre-processes the mail before it gets sent.
        @see ERMailDelivery#prepareMail */
    protected DataHandler prepareMail () throws MessagingException {
	// Generate text
	NSData data = this.component ().generateResponse ().content ();
	String messageContent = new String (data.bytes (0, data.length ()));		
	return new DataHandler (messageContent, "text/plain");
    }
}
