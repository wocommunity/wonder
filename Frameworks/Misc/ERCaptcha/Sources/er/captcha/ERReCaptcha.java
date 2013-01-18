package er.captcha;

import java.util.Properties;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSValidation;

import er.extensions.appserver.ERXRequest;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.components.ERXSimpleSpamCheck;
import er.extensions.foundation.ERXProperties;

/**
 * <p>
 * ERReCaptcha uses the ReCaptcha system for identifying humans vs spambots. For more information, see
 * http://recaptcha.net. To use this component, you must sign up for a recaptcha account and set the system properties
 * "er.captcha.recaptcha.publicKey" and "er.captcha.recaptcha.privateKey" accordingly.
 * </p>
 * <p>
 * This component will both set a "valid" binding to true/false based on the check as well as call a
 * validationFailedWithException when the check fails.
 * </p>
 * 
 * @binding secure sets whether or not the recaptcha URL should be secure (defaults to using the request's protocol)
 * @binding theme the recaptcha theme to use
 * @binding valid will be set to true or false depending on whether the check passed
 * @binding errorMessage the error message to display for an incorrect-captcha-sol error
 * @property er.captcha.recaptcha.publicKey your ReCaptcha public key
 * @property er.captcha.recaptcha.privateKey your ReCaptcha private key
 * 
 * @author mschrag
 */
public class ERReCaptcha extends ERXNonSynchronizingComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new ERReCaptcha component.
	 * 
	 * @param context
	 *            the context
	 */
	public ERReCaptcha(WOContext context) {
		super(context);
	}

	/**
	 * Returns a ReCaptcha object configured with the current settings.
	 * 
	 * @return a ReCaptcha object configured with the current settings
	 */
	protected ReCaptcha recaptcha() {
		String publicKey = ERXProperties.stringForKey("er.captcha.recaptcha.publicKey");
		String privateKey = ERXProperties.stringForKey("er.captcha.recaptcha.privateKey");
		if (publicKey == null || privateKey == null) {
			throw new IllegalStateException("You have not set 'er.captcha.recaptcha.publicKey' or 'er.captcha.recaptcha.publicKey'. Please go to http://recaptcha.net and sign up for a key.");
		}
		ReCaptcha recaptcha;
		boolean secure = booleanValueForBinding("secure", ERXRequest.isRequestSecure(context().request()));
		if (secure) {
			recaptcha = ReCaptchaFactory.newSecureReCaptcha(publicKey, privateKey, false);
		}
		else {
			recaptcha = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);
		}
		return recaptcha;
	}

	/**
	 * Returns the ReCaptcha HTML chunk to render into the page.
	 * 
	 * @return the ReCaptcha HTML chunk to render into the page
	 */
	public String recaptchaHTML() {
		Properties props = new Properties();
		// props.setProperty("tabindex", null);
		String theme = stringValueForBinding("theme");
		if (theme != null) {
			props.setProperty("theme", theme);
		}
		String errorMessage = stringValueForBinding("errorMessage");
		String html = recaptcha().createRecaptchaHtml(errorMessage, props);
		return html;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		if (context.wasFormSubmitted()) {
			String challenge = request.stringFormValueForKey("recaptcha_challenge_field");
			if (challenge == null) {
				challenge = "";
			}
			String response = request.stringFormValueForKey("recaptcha_response_field");
			if (response == null) {
				response = "";
			}
			String remoteAddress = request._remoteAddress();
			if (remoteAddress == null) {
				remoteAddress = WOApplication.application().hostAddress().getHostAddress();
			}
			ReCaptchaResponse recaptchaResponse = recaptcha().checkAnswer(remoteAddress, challenge, response);
			if (!recaptchaResponse.isValid()) {
				String errorMessage = recaptchaResponse.getErrorMessage();
				if (errorMessage != null && errorMessage.equals("incorrect-captcha-sol")) {
					errorMessage = stringValueForBinding("errorMessage");
				}
				validationFailedWithException(new NSValidation.ValidationException(errorMessage), this, ERXSimpleSpamCheck.SPAM_CHECK_KEY);
				setValueForBinding(Boolean.FALSE, "valid");
			}
			else {
				setValueForBinding(Boolean.TRUE, "valid");
			}
		}
	}
}