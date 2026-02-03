package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;

public class R2D2WDisplayAttachmentFile extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WDisplayAttachmentFile(WOContext context) {
        super(context);
    }

	public String altText() {
		String altText = (String)d2wContext().valueForKey("altText");
		if(!ERXStringUtilities.stringIsNullOrEmpty(altText)) {
			altText = WOMessage.stringByEscapingHTMLString(altText);
		}
		return altText;
	}
}