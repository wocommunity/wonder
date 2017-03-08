package er.extensions.components;

import java.io.Serializable;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.localization.ERXLocalizer;

/**
 * A custom boolean selector (defaults to "Yes", "No", and "All") for use as, for instance, a boolean search filter.
 * 
 * @author mschrag
 * @author ak
 *
 * @binding yesString the string to show for the "Yes" option
 * @binding noString the string to show for the "No" option
 * @binding noSelectionString the string to show for the "All" option
 * @binding selection the selected value
 */
public class ERXBooleanSelector extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private final NSArray<Boolean> _options = new NSArray<>(new Boolean[] { Boolean.TRUE, Boolean.FALSE });

	public static class BooleanProxy implements Serializable {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		private final Boolean _value;

		BooleanProxy(Boolean value) {
			_value = value;
		}

		public Boolean value() {
			return _value;
		}

		@Override
		public String toString() {
			return _value != null ? _value.toString() : null;
		}

		@Override
		public int hashCode() {
			return _value == null ? 0 : _value.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			return other == _value || (other != null && ((BooleanProxy) other).value() == _value);
		}
    }
    
    private static final BooleanProxy TRUE = new BooleanProxy(Boolean.TRUE);
    private static final BooleanProxy FALSE = new BooleanProxy(Boolean.FALSE);
    private static final BooleanProxy NULL = new BooleanProxy(null);
    
	private final NSArray<BooleanProxy> _proxyOptions = new NSArray<>(new BooleanProxy[] { TRUE, FALSE });
	private final NSArray<BooleanProxy> _proxyOptionsWithNull = new NSArray<>(new BooleanProxy[] { TRUE, FALSE, NULL});
	
	public Boolean _option;
	public BooleanProxy _proxy;

	public ERXBooleanSelector(WOContext context) {
		super(context);
	}

	public String noSelectionString() {
		String noSelectionString = stringValueForBinding("noSelectionString");
		if(noSelectionString != null) {
			noSelectionString = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(noSelectionString);
		}
		return noSelectionString;
	}

	public String displayString() {
		String displayString;
		if (_option == Boolean.TRUE || _proxy == TRUE) {
			displayString = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(stringValueForBinding("yesString", "Yes"));
		}
		else if (_option == Boolean.FALSE || _proxy == FALSE) {
			displayString = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(stringValueForBinding("noString", "No"));
		}
		else {
			displayString = noSelectionString();
		}
		return displayString;
	}

	public String uiMode() {
		return stringValueForBinding("uiMode", "popup");
	}

	public boolean isPopup() {
		return "popup".equals(uiMode());
	}

	public boolean isCheckbox() {
		return "checkbox".equals(uiMode());
	}

	public boolean isRadio() {
		return "radio".equals(uiMode());
	}

	public Object proxySelection() {
		Boolean value = (Boolean) valueForBinding("selection");
		if(value == null) return NULL;
		if(value) return TRUE;
		return FALSE;
	}

	public void setProxySelection(Object object) {
		setValueForBinding(((BooleanProxy)object).value(), "selection");
	}

	public NSArray<BooleanProxy> proxyOptions() {
		if(noSelectionString() == null) {
			return _proxyOptions;
		}
		return _proxyOptionsWithNull;
	}

	public NSArray<Boolean> options() {
		return _options;
	}

	@Override
	public void reset() {
		_option = null;
		_proxy = null;
		super.reset();
	}
}