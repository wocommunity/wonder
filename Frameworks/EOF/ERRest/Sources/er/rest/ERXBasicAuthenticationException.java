package er.rest;


/**
 * Basic Authentication Exception.
 * 
 * <p>
 * This class responsible for exception when use Basic Authentication.
 * 
 * This exception can be used with checkAcces method of ERXRouteController class.
 * </p>
 * 
 * <b>Example</b>
 * 
 * <pre>
 * protected void checkAccess() throws SecurityException {
 *     throw new ERXBasicAuthenticationException("invalid credentials");
 * }
 * </pre>
 */
public class ERXBasicAuthenticationException extends SecurityException {

	private final String basicRealm;
	
    /**
     * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message and cause. 
     * 
     * For @param basicRealm use default 'application'. 
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     */	
	public ERXBasicAuthenticationException(String message) {
		this(message, "application");
	}

    /**
     * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     * @param basicRealm message about server authentication requested for user. 
     */	
	public ERXBasicAuthenticationException(String message, String basicRealm) {
		super(message);
		
		this.basicRealm = basicRealm;
	}

    /**
     * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A <tt>null</tt> value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     * @param basicRealm message about server authentication requested for user. 
     */
	public ERXBasicAuthenticationException(String message, Throwable cause, String basicRealm) {
		super(message, cause);
		
		this.basicRealm = basicRealm;
	}

	/**
	 * Message about server authentication.
	 * 
	 * @return basicRealm message
	 */
	public String basicRealm() {
		return basicRealm;
	}

}