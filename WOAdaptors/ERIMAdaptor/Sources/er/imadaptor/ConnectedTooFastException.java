package er.imadaptor;

public class ConnectedTooFastException extends IMConnectionException {
	public ConnectedTooFastException(String message) {
		super(message);
	}

	public ConnectedTooFastException(String message, Throwable cause) {
		super(message, cause);
	}
}
