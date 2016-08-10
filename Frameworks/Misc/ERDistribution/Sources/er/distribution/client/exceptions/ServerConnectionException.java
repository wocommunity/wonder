package er.distribution.client.exceptions;

import java.io.IOException;

public class ServerConnectionException extends IOException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ServerConnectionException() {
	}

	public ServerConnectionException(String message) {
		super(message);
	}

	public ServerConnectionException(Throwable error) {
		super(error);
	}

	public ServerConnectionException(String message, Throwable error) {
		super(message, error);
	}

}
