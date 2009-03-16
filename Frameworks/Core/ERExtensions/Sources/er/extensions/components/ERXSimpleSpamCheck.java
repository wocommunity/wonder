package er.extensions.components;

import java.util.UUID;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXExtensions;
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
		if (context._wasFormSubmitted()) {
			super.takeValuesFromRequest(request, context);
			if (!ERXExtensions.safeEquals(_expectedSpamCheck, _spamCheck)) {
				validationFailedWithException(new NSValidation.ValidationException("Spam check failed."), this, ERXSimpleSpamCheck.SPAM_CHECK_KEY);
			}
			_spamCheck = null;
		}
	}
}
