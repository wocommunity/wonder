/*
 $Id$

 ERMailAttachement.java - Camille Troillard - tuscland@mac.com
*/

package er.javamail;

import javax.mail.*;

public abstract class ERMailAttachment {
    protected Object _content;

    public ERMailAttachment (Object content) {
        super ();
        this.setContent (content);
    }

	protected Object content () {
		return _content;
	}

    protected void setContent (Object content) {
		_content = content;
	}

    protected abstract BodyPart getBodyPart () throws Exception;
}
