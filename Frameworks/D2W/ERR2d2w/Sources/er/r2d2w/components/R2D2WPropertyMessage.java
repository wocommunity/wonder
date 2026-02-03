package er.r2d2w.components;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;

public class R2D2WPropertyMessage extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2D2WPropertyMessage(WOContext context) {
		super(context);
	}

	public Object originalValueForBinding(String binding) {
		return super.valueForBinding(binding);
	}

	public Object valueForBinding(String binding) {
		return hasBinding(binding) ? originalValueForBinding(binding) : d2wContext().valueForKey(binding);
	}
}