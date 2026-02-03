package er.r2d2w.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WTabInspectPage;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WInspectPage extends ERD2WTabInspectPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2D2WInspectPage(WOContext context) {
		super(context);
	}

	public boolean disableForm() {
		boolean hasForm = ERXValueUtilities.booleanValue(d2wContext().valueForKey("hasForm"));
		return context().isInForm() || !hasForm;
	}
}