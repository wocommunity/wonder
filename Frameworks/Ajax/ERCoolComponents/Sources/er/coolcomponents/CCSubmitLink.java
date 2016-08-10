package er.coolcomponents;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;

/**
 * This component is a hyperlink that can submit a form
 * and perform an action other than the form's default
 * action. Similar to WOSubmitButton and WOImageButton.
 * If this component is not located within a form, then
 * it acts like a normal WOHyperlink.
 *
 * Bindings: 
 * 	action    (required): Action for link to perform
 * 	string    (optional): Text for link
 * 	onClick   (optional): Script for onClick of link
 * 	class (optional): Name of CSS class for link
 * 	disabled  (optional): If true, no link is created
 * 	fieldName (optional): Name of hidden field
 *  additionalFunction (optional): js function to call before submitting form
 *  name		  (optional): name of link
 *
 * Component originally named: HWOSubmitLink
 * 
 * @author Greg Bartnick &lt;gbartnick@hosts.com&gt;
 * @version 1.0; April 04, 2005
 * 
 * Modified:
 * @author davidleber
 */
public class CCSubmitLink extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String _fieldName;
	private boolean _isInForm;
	
	protected String _additionalFunction;
	
    public CCSubmitLink(WOContext context) {
        super(context);
    }
    
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		String formValue = (String) request.formValueForKey(fieldName());
		if (fieldName().equals(formValue)) {
			// Tell context that an action was performed. If this is
			// not done, the form's default action will be called also.
			// *note* Uses undocumented method of WOContext.
			context.setActionInvoked(true);
		}
		return super.invokeAction(request, context);
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		// Check if the link is in a form so we know if we need to
		// use the link and a hidden field to submit the form.
		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERCoolComponents", "ercoolcomponents.js");
		_isInForm = context.isInForm();
		super.appendToResponse(response, context);
	}

	/**
	 * @return String containing a name for the hidden field.
	 */
	public String fieldName() {
		if (_fieldName == null) {
			if (hasBinding("fieldName")) {
				_fieldName = (String) valueForBinding("fieldName");
			} else {
				_fieldName = ERXStringUtilities.safeIdentifierName(context().elementID()) + "_hf";
			}
		}
		return (_fieldName);
	}
	
	/**
	 * @return String containing any additional js function to be executed before
	 * the form is submitted.
	 */
	public String additionalFunction() {
		if (_additionalFunction == null) {
			if (hasBinding("additionalFunction")) {
				_additionalFunction = (String)valueForBinding("additionalFunction");
			} else {
				_additionalFunction = "";
			}
		}
		return _additionalFunction;
	}

	/**
	 * @return String containing a script for hyperlink that gives
	 *         a value to the hidden field and submits the form.
	 */
	public String linkScript()
	{
		String func = additionalFunction();
		String addInFunction = (func.length() > 0) ? "', '" + func : ""; 
		return ("javascript:CCSL.submit('" + fieldName() + addInFunction + "');");
	}
	
	public boolean dontSubmitForm() {
		boolean result = !_isInForm;
		if (_isInForm && hasBinding("dontSubmitForm")) {
			result = booleanValueForBinding("dontSubmitForm");
		}
		return result;
	}
}