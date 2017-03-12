package er.rest;


/**
 * The <code>ERXBasicAuthenticationException</code> can be thrown when performing an action
 * containing invalid credentials.
 * <p>
 * Usage:
 * <pre><code>
 * protected void checkAccess() throws SecurityException {
 *     if (invalidCredentials) {
 *         throw new ERXBasicAuthenticationException("Invalid credentials");
 *     }
 * }
 * </code></pre>
 */
public class ERXBasicAuthenticationException extends SecurityException {
	private static final String DEFAULT_REALM = "application";

	/**
	 * A string to be displayed to users so they know which username and password to use.
	 */
	private final String realm;

	/**
     * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message and the default realm.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     */
	public ERXBasicAuthenticationException(String message) {
		this(message, DEFAULT_REALM);
	}

	/**
     * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message and realm.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     * @param realm the realm for which the credentials are used
     */
	public ERXBasicAuthenticationException(String message, String realm) {
		super(message);

		this.realm = realm;
	}

	/**
	 * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message and cause using the default realm.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A <tt>null</tt> value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
	 */
	public ERXBasicAuthenticationException(String message, Throwable cause) {
		this(message, cause, DEFAULT_REALM);
	}

    /**
     * Creates a <code>ERXBasicAuthenticationException</code> with the specified
     * detail message, cause and realm.
     *
     * @param message the detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A <tt>null</tt> value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     * @param realm the realm for which the credentials are used
     */
	public ERXBasicAuthenticationException(String message, Throwable cause, String realm) {
		super(message, cause);

		this.realm = realm;
	}

	/**
	 * The realm for which the credentials are used.
	 *
	 * @return Returns the realm representation.
	 */
	public String realm() {
		return realm;
	}

}