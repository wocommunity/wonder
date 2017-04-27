package er.google.components;

import com.webobjects.appserver.WOContext;

/**
 * Sends a google authentication key to a DirectAction.
 * 
 * To create a client ID, follow the instructions found at: https://developers.google.com/identity/sign-in/web/devconsole-project
 * 
 * Example action implementation
 * <pre>
 * public WOActionResults googleLoginAction() {
 *		String token = request().stringFormValueForKey("authToken");
 *		
 *		Session session = (Session) session();
 *		User user = User.validateForGoogleToken(session.defaultEditingContext(), token);
 *		if (user != null) {
 *			session.setUser(user);
 *			return D2W.factory().defaultPage(session);
 *		}
 *		
 *		Main result = pageWithName(Main.class);
 *		result.setErrorMessage("Invalid Google User");
 *		return result;
 * }
 * </pre>
 * 
 * Example user authentication implementation
 * <pre>
 * public static User userForGoogleID(EOEditingContext ec, String googleToken) {
 *		if (googleToken == null) {
 *			throw new NullArgumentException("googleToken cannot be null");
 *		}
 *		try {
 *			Payload payload = ERGoogleSignInsUtilities.payloadFromToken(googleToken);
 *			return User.fetchRequiredUser(ec, User.EMAIL.is(payload.getEmail()));
 *		}
 *		catch (Exception e) {
 *			return null;
 *		}
 * }
 * </pre>
 * 
 * Required Bindings:
 * @binding error If the google user is not authenticated for your system, return a non-null value here.
 * 
 * Optional Bindings:
 * @binding clientID The client ID that will be used to authenticate the user.
 * @binding clientIDName The name of the client ID that will be pulled from application properties as per GoogleAuthenticationUtilities.clientID(name);.
 * @binding id The ID of the Google Login Button.
 * @binding formName The name of the form used to submit the authentication key.
 * @binding directActionName The name of the direct action function used to submit the authentication key.
 * @binding directActionClass The class containing the direct action.
 * @binding tokenFieldName The name of form field that delivers the authentication key to the direct action.
 * @binding includePlatformScript Whether or not to include the platform script.
 * 
 * @author Taylor Hadden
 */
public class ERGoogleLoginForm extends ERGoogleSignInComponent {

	public String token;
	
    public ERGoogleLoginForm(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	token = null;
    }
    
    public String id() {
    	return stringValueForBinding("id", "GoogleLoginContainer");
    }
    
    public String formName() {
    	return stringValueForBinding("formName", "GoogleForm");
    }
    
    public String directActionName() {
    	return stringValueForBinding("directActionName", "googleLogin");
    }
    
    public String directActionClass() {
    	return stringValueForBinding("directActionClass", "DirectAction");
    }
    
    public String tokenFieldName() {
    	return stringValueForBinding("tokenFieldName", "authToken");
    }
}