/*
 $Id$

 ERMessage.java - Camille Troillard - tuscland@mac.com
*/


package er.javamail;

import javax.mail.*;
import javax.mail.internet.*;

public class ERMessage extends Object {

    private MimeMessage message;
    private Object anObject;
    
    public ERMessage() {};
    
    public ERMessage(MimeMessage mimeMessage, Object callbackObject ) {
    	super();
    	message = mimeMessage;
    	anObject = callbackObject;
    }

    public void setMimeMessage (MimeMessage m) {
        message = m;
    }
    
    public MimeMessage mimeMessage () {
        return message;
    }
    
    public void setCallbackObject (Object obj) {
        anObject = obj;
    }
    
    public Object callbackObject () {
        return anObject;
    }
    
}
