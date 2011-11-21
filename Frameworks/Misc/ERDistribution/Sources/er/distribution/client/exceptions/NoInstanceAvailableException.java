package er.distribution.client.exceptions;


public class NoInstanceAvailableException extends LostServerConnectionException {

	public NoInstanceAvailableException() {
	}

	public NoInstanceAvailableException(String message) {
		super(message);
	}

	public NoInstanceAvailableException(Throwable error) {
		super(error);
	}

	public NoInstanceAvailableException(String message, Throwable error) {
		super(message, error);
	}

}
