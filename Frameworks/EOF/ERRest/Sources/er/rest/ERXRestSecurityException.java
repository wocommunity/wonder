package er.rest;

/**
 * Thrown in the event of a security violation during rest processing.
 * 
 * @author mschrag
 */
public class ERXRestSecurityException extends Exception {
	public ERXRestSecurityException(String message) {
		super(message);
	}

	public ERXRestSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
}
