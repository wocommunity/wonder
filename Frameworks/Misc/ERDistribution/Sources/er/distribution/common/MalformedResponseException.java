package er.distribution.common;

public class MalformedResponseException extends RuntimeException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
