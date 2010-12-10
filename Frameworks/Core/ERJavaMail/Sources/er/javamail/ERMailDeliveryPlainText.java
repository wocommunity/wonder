/*
 $Id$
 
 ERMailDeliveryPlainText.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import java.util.*;
import java.io.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

/** This ERMailDelivery subclass is specifically crafted for plain text messages.
    @author Camille Troillard <tuscland@mac.com> */
public class ERMailDeliveryPlainText extends ERMailDelivery {

    /** String Message content */
    private String textContent;

    /** Sets the text content of the current message. */
    public void setTextContent (String text) {
        textContent = text;
    }

    /** Pre-processes the mail before it gets sent.
        @see ERMailDelivery#prepareMail */
    protected DataHandler prepareMail () throws MessagingException {
    	String charset = charset();
    	DataHandler dataHandler;
    	if (charset != null) {
        	dataHandler = new DataHandler (textContent, "text/plain; charset=\""  + charset () + "\"");
    	}
    	else {
        	dataHandler = new DataHandler (textContent, "text/plain");
    	}
    	return dataHandler;
    }

}
