package er.rest;

/**
 * IERXRestAuthenticationDelegate provides a hook for you to
 * authenticate users for the purposes of a restful request.  
 * ERXRestContext provides access to the WOContext if you want
 * to create a session and access users through the session.
 * Alternatively, you can work without a session by using the
 * ERXRestContext as a dictionary and simply attaching the 
 * current user to the context. 
 * 
 * @author mschrag
 */
public interface IERXRestAuthenticationDelegate {
	/**
	 * Attempt to authenticate the user with the given context.  The
	 * context provides access to things like the WOContext, which you
	 * can traverse to get the WORequest and access form variables if
	 * need be.
	 * 
	 * @param context the rest context
	 * @return whether or not authentication was successful
	 */
	public boolean authenticate(ERXRestContext context);
}
