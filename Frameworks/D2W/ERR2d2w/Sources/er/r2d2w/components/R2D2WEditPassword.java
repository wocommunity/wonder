package er.r2d2w.components;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.appserver.ERXWOContext;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

public class R2D2WEditPassword extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 2L;

	private static final Logger log = LoggerFactory.getLogger(R2D2WEditPassword.class);

	private static final String _COMPONENT_CLASS = "password";
	private String labelID;
	private String confirmLabelID;
	private String password;
	private String confirmPassword;

	public R2D2WEditPassword(WOContext context) {
		super(context);
	}

	public void reset() {
		super.reset();
		labelID = null;
		confirmLabelID = null;
		confirmPassword = null;
		password = null;
	}

	public String r2ConfirmLabelID() {
		if (confirmLabelID == null) {
			confirmLabelID = ERXWOContext.safeIdentifierName(context(), false);
		}
		return confirmLabelID;
	}

	public String passwordLabelID() {
		if (labelID == null) {
			labelID = ERXWOContext.safeIdentifierName(context(), false);
		}
		return labelID;
	}

	public String componentClasses() {
		return _COMPONENT_CLASS;
	}

	/**
	 * @return the confirmPassword
	 */
	public String confirmPassword() {
		return confirmPassword;
	}
	
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	/**
	 * @return the password
	 */
	public String password() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public void fail(String errorCode) {
		if (log.isDebugEnabled()) {
			log.debug("fail:<object:" + object() + "; key:" + propertyKey() + "; code:" + errorCode + ";>");
		}
		ERXValidationFactory factory = ERXValidationFactory.defaultFactory();
		ERXValidationException ex = factory.createException(object(), propertyKey(), null, errorCode);
		validationFailedWithException(ex, null, propertyKey());
	}

	protected void checkPasswords() {
		Predicate<String> isBlank = s -> s == null || s.isBlank();
		if (isBlank.test(password()) || isBlank.test(confirmPassword())) {
			fail("R2PasswordsFillBothFieldsException");
		} else if (!password().equals(confirmPassword())) {
			fail("R2PasswordsDontMatchException");
		} else {
			try {
				object().validateTakeValueForKeyPath(password(), propertyKey());
			} catch (NSValidation.ValidationException ex) {
				validationFailedWithException(ex, null, propertyKey());
			}
		}
	}

	public void takeValuesFromRequest(WORequest r, WOContext c) {
		super.takeValuesFromRequest(r, c);
		checkPasswords();
	}

	public String confirmToolTip() {
		return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject(
				"R2D2WEditPassword.confirmToolTip", d2wContext());
	}

	/**
	 * Validation Support. Passes errors to the parent.
	 */
	public void validationFailedWithException(Throwable e, Object value, String keyPath) {
		parent().validationFailedWithException(e, value, keyPath);
	}
}