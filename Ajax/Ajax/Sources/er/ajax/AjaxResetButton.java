package er.ajax;

import com.webobjects.appserver.*;

/**
 * Simple submit button wrapper around Prototypes Form.reset('formID');. Does a client side reset (set to blank / no
 * selection) of all of a form's inputs.
 * 
 * @binding formId String, the id of the form to be reset
 * @binding value String, optional label for the button, the default is Reset
 * @binding id, String, optional HTML ID for the button element
 * @binding class, String, optional CSS class name for the button element
 * @binding style, String, optional CSS style definition for the button element
 * 
 * @author Chuck Hill
 */
public class AjaxResetButton extends WOComponent {
	public static final String FORM_ID_BINDING = "formId";
	public static final String VALUE_BINDING = "value";

	public AjaxResetButton(WOContext context) {
		super(context);
	}

	/**
	 * @return <code>true</code>
	 */
	public boolean isStateless() {
		return true;
	}

	/**
	 * Adds prototype.js to the header.
	 */
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
	}

	/**
	 * @return JavaScript to reset form
	 */
	public String resetFormJavaScript() {
		return "Form.reset('" + formId() + "'); return false;";
	}

	/**
	 * @return value of formId binding
	 */
	public String formId() {
		if (!hasBinding(FORM_ID_BINDING)) {
			throw new IllegalArgumentException(FORM_ID_BINDING + " is a required binding");
		}

		return (String) valueForBinding(FORM_ID_BINDING);
	}

	/**
	 * @return value of value binding
	 */
	public String value() {
		return hasBinding(VALUE_BINDING) ? (String) valueForBinding(VALUE_BINDING) : "Reset";
	}

	public WOComponent dummy() {
		throw new RuntimeException("dummy action called");
	}

}