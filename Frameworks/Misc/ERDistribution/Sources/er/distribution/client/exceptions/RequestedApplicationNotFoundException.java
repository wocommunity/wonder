package er.distribution.client.exceptions;


public class RequestedApplicationNotFoundException extends LostServerConnectionException {

	public RequestedApplicationNotFoundException() {
	}

	public RequestedApplicationNotFoundException(String message) {
		super(message);
	}

	public RequestedApplicationNotFoundException(Throwable error) {
		super(error);
	}

	public RequestedApplicationNotFoundException(String message, Throwable error) {
		super(message, error);
	}

}
