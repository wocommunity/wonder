package er.distribution.client.exceptions;

/**
 * Represents an exception that originated on the server and has now been relayed to the client
 */
public class ServerException extends RuntimeException {

	public static final String MESSAGE = "An error occurred on the server. Please try again.";
	
	public ServerException() {
		super(MESSAGE);
	}

	public ServerException(Throwable cause) {
		super(MESSAGE, cause);
	}

}
