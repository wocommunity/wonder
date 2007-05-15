package er.rest;

public class ERXRestException extends Exception {
	public ERXRestException(String message) {
		super(message);
	}

	public ERXRestException(String message, Throwable cause) {
		super(message, cause);
	}
}
