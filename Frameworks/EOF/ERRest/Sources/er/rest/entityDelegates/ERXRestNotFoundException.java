package er.rest.entityDelegates;

/**
 * Thrown in the event of a missing requested object during rest processing.
 * 
 * @author mschrag
 */
public class ERXRestNotFoundException extends Exception {
	public ERXRestNotFoundException(String message) {
		super(message);
	}

	public ERXRestNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
