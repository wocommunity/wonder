package er.distribution.client.exceptions;


public class RequestedApplicationNotFoundException extends LostServerConnectionException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
