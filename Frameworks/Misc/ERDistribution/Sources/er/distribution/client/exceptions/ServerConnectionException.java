package er.distribution.client.exceptions;

import java.io.IOException;

public class ServerConnectionException extends IOException {

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
