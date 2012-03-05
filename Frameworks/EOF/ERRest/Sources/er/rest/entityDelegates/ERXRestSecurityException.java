package er.rest.entityDelegates;

/**
 * Thrown in the event of a security violation during rest processing.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXRestSecurityException extends Exception {
	public ERXRestSecurityException(String message) {
		super(message);
	}

	public ERXRestSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
}
