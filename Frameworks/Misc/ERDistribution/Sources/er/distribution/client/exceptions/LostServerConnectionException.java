package er.distribution.client.exceptions;

import java.io.IOException;

public class LostServerConnectionException extends IOException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public LostServerConnectionException() {
	}

	public LostServerConnectionException(String message) {
		super(message);
	}

	public LostServerConnectionException(Throwable error) {
		super(error);
	}

	public LostServerConnectionException(String message, Throwable error) {
		super(message, error);
	}

}
