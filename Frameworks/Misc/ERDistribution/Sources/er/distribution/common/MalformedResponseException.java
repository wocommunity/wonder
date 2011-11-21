package er.distribution.common;

public class MalformedResponseException extends RuntimeException {

	public MalformedResponseException() {
	}

	public MalformedResponseException(String message) {
		super(message);
	}

	public MalformedResponseException(Throwable cause) {
		super(cause);
	}

	public MalformedResponseException(String message, Throwable cause) {
		super(message, cause);
	}

}
