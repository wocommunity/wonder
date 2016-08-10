package er.distribution.client.exceptions;

/**
 * Represents an exception that originated on the server and has now been relayed to the client
 */
public class ServerException extends RuntimeException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String MESSAGE = "An error occurred on the server. Please try again.";
	
	public ServerException() {
		super(MESSAGE);
	}

	public ServerException(Throwable cause) {
		super(MESSAGE, cause);
	}

}
