package er.distribution.client.exceptions;


public class MissingSessionException extends LostServerConnectionException {

	public MissingSessionException() {
	}

	public MissingSessionException(String message) {
		super(message);
	}

	public MissingSessionException(Throwable error) {
		super(error);
	}

	public MissingSessionException(String message, Throwable error) {
		super(message, error);
	}

}
