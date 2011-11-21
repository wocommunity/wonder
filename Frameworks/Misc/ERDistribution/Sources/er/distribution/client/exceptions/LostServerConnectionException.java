package er.distribution.client.exceptions;

import java.io.IOException;

public class LostServerConnectionException extends IOException {

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
