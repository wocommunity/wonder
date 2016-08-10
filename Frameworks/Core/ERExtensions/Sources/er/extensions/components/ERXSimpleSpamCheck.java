package er.extensions.components;

import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXSimpleSpamCheck generates a display: none text field in your
 * page that has its value filled in with Javascript.  When the form
 * submits, if the value in the field does not match the expected
 * value, a validation failure is generated.  Your end-users should
 * not notice anything at all, but because the expected value is 
 * filled in with Javascript, this will trip up many bots.
 *  
 * @author mschrag
 */
public class ERXSimpleSpamCheck extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String SPAM_CHECK_KEY = "spamCheck";

	private String _id;
	private String _expectedSpamCheck;
	public String _spamCheck;

	public ERXSimpleSpamCheck(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String id() {
		if (_id == null) {
			_id = ERXStringUtilities.safeIdentifierName(UUID.randomUUID().toString());
		}
		return _id;
	}

	public String expectedSpamCheck() {
		if (_expectedSpamCheck == null) {
			_expectedSpamCheck = UUID.randomUUID().toString();
		}
		return _expectedSpamCheck;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (context.wasFormSubmitted()) {
			super.takeValuesFromRequest(request, context);
			if (ObjectUtils.notEqual(_expectedSpamCheck, _spamCheck)) {
				validationFailedWithException(new NSValidation.ValidationException("Spam check failed."), this, ERXSimpleSpamCheck.SPAM_CHECK_KEY);
				setValueForBinding(Boolean.FALSE, "valid");
			}
			else {
				setValueForBinding(Boolean.TRUE, "valid");
			}
			_spamCheck = null;
		}
	}
}
