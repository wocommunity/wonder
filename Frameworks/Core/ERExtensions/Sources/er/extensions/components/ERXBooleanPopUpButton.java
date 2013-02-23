package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * A three-state popup button (defaults to "Yes", "No", and "All") for use as, for instance, a boolean search filter.
 * Use ERXBooleanSelector with uiMode= "popup" instead.
 * @author mschrag
 * @author ak
 *
 * @binding yesString the string to show for the "Yes" option
 * @binding noString the string to show for the "No" option
 * @binding noSelectionString the string to show for the "All" option
 * @binding selection the selected value
 */
@Deprecated
public class ERXBooleanPopUpButton extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXBooleanPopUpButton(WOContext context) {
		super(context);
	}

	public String noSelectionString() {
		return stringValueForBinding("noSelectionString", "All");
	}

	public String yesString() {
		return stringValueForBinding("yesString", "Yes");
	}

	public String noString() {
		return stringValueForBinding("noString", "No");
	}

	@Override
	public boolean isStateless() {
		return true;
	}
}
