package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

/**
 * A three-state popup button (defaults to "Yes", "No", and "All") for use as, for instance, a boolean search filter.
 * 
 * @author mschrag
 *
 * @binding yesString the string to show for the "Yes" option
 * @binding noString the string to show for the "No" option
 * @binding noSelectionString the string to show for the "All" option
 * @binding selection the selected value
 */
public class ERXBooleanPopUpButton extends ERXComponent {
	private NSArray<Boolean> _options;
	public Boolean _option;

	public ERXBooleanPopUpButton(WOContext context) {
		super(context);
		_options = new NSArray<Boolean>(new Boolean[] { Boolean.TRUE, Boolean.FALSE });
	}

	public String noSelectionString() {
		return stringValueForBinding("noSelectionString", "All");
	}

	public String displayString() {
		String displayString;
		if (_option == Boolean.TRUE) {
			displayString = stringValueForBinding("yesString", "Yes");
		}
		else if (_option == Boolean.FALSE) {
			displayString = stringValueForBinding("noString", "No");
		}
		else {
			displayString = "";
		}
		return displayString;
	}

	public NSArray<Boolean> options() {
		return _options;
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
}