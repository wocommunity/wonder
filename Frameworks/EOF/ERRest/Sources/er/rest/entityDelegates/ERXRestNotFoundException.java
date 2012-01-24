package er.rest.entityDelegates;

/**
 * Thrown in the event of a missing requested object during rest processing.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXRestNotFoundException extends Exception {
	public ERXRestNotFoundException(String message) {
		super(message);
	}

	public ERXRestNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
