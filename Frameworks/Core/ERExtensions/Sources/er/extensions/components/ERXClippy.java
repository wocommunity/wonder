package er.extensions.components;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResourceManager;

/**
 * Thin wrapper for Clippy (http://github.com/mojombo/clippy) as used by, for example, github.
 * 
 * Clippy is a very simple Flash widget that makes it possible to place arbitrary text onto the client's clipboard.
 * 
 * @binding text The text to be copied
 * @binding bgcolor color to use as background, defaults to #FFFFFF
 * 
 * @author th
 */
public class ERXClippy extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXClippy(WOContext context) {
		super(context);
	}

	public String src() {
		WOResourceManager rm = WOApplication.application().resourceManager();
		return rm.urlForResourceNamed("clippy.swf", "ERExtensions", null, context().request());
	}

	public String text() {
		return stringValueForBinding("text", "");
	}

	public String flashVars() {
		return "text=" + WOMessage.stringByEscapingHTMLAttributeValue(text());
	}

	public String bgcolor() {
		return stringValueForBinding("bgcolor", "#FFFFFF");
	}
}
