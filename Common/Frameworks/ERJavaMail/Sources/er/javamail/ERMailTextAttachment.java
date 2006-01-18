/*
 $Id$
 
 ERMailTextAttachment.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import javax.mail.*;
import javax.mail.internet.*;

public class ERMailTextAttachment extends ERMailFileAttachment {

    public ERMailTextAttachment (String fileName, String content) {
        super (content);
	this.setFileName (fileName);
    }

    protected BodyPart getBodyPart () throws MessagingException {
        MimeBodyPart bp = new MimeBodyPart ();

        bp.setText ((String)this.content (), ERMailDelivery.DefaultCharset);
        bp.setFileName (this.fileName ());

        return bp;
    }
}
