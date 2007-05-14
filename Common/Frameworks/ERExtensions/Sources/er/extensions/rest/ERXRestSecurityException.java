package er.extensions.rest;

public class ERXRestSecurityException extends Exception {
	public ERXRestSecurityException(String message) {
		super(message);
	}

	public ERXRestSecurityException(String message, Throwable cause) {
		super(message, cause);
	}
}
