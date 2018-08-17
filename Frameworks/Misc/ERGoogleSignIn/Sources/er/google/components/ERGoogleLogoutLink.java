package er.google.components;

import com.webobjects.appserver.WOContext;

/**
 * Sends a logout link
 * 
 * Required Bindings
 * @binding action The action that is called by the link.
 * 
 * Optional Bindings
 * @binding clientID The client ID used for authentication.
 * @binding clientIDName The name of the client ID that will be pulled from application properties as per GoogleAuthenticationUtilities.clientID(name);.
 * @binding includeInitializationScript Whether or not to include the google authentication initialization script.
 * @binding includePlatformScript Whether or not to include the google platform script.
 * 
 * @author Taylor Hadden
 */
public class ERGoogleLogoutLink extends ERGoogleSignInComponent {
    public ERGoogleLogoutLink(WOContext context) {
        super(context);
    }
}