package er.rest;

/**
 * If a general REST exception occurs.
 * 
 * @author mschrag
 */
public class ERXRestException extends Exception {
	public ERXRestException(String message) {
		super(message);
	}

	public ERXRestException(String message, Throwable cause) {
		super(message, cause);
	}
}
