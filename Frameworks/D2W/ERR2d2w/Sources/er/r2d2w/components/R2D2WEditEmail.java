package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

public class R2D2WEditEmail extends R2D2WEditString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final String _COMPONENT_CLASS = "email";

	public String componentClasses() {
		return _COMPONENT_CLASS;
	}

	public R2D2WEditEmail(WOContext context) {
		super(context);
	}
}