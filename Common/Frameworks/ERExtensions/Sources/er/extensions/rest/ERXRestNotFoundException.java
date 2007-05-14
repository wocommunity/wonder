package er.extensions.rest;

public class ERXRestNotFoundException extends Exception {
	public ERXRestNotFoundException(String message) {
		super(message);
	}

	public ERXRestNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
