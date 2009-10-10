package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXLocalizer;
import er.extensions.ERXStatelessComponent;

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

	private NSArray _options = new NSArray(new Boolean[] { Boolean.TRUE, Boolean.FALSE });

    public static class BooleanProxy {
    	
        private Boolean _value;
       
        BooleanProxy(Boolean value) {
            _value = value;
        }
    	
       	public Boolean value() {
            return _value;
    	}
       	
       	public String toString() {
            return _value != null ? _value.toString() : null;
    	}
    	
    	public boolean equals(Object other) {
            return other == _value || (other != null && ((BooleanProxy)other).value() == _value);
    	}
    }
    
    private static BooleanProxy TRUE = new BooleanProxy(true);
    private static BooleanProxy FALSE = new BooleanProxy(false);
    private static BooleanProxy NULL = new BooleanProxy(null);
    
	private NSArray _proxyOptions = new NSArray(new BooleanProxy[] { TRUE, FALSE });
	private NSArray _proxyOptionsWithNull = new NSArray(new BooleanProxy[] { TRUE, FALSE, NULL});
	
	public Boolean _option;
	public BooleanProxy _proxy;

	public ERXBooleanSelector(WOContext context) {
		super(context);
	}

	public String noSelectionString() {
		return stringValueForBinding("noSelectionString", null);
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
			displayString = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(noSelectionString());
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

	public NSArray proxyOptions() {
		if(noSelectionString() == null) {
			return _proxyOptions;
		}
		return _proxyOptionsWithNull;
	}

	public NSArray options() {
		return _options;
	}
}