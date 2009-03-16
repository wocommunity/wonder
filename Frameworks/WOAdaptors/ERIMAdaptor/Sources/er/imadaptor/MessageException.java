package er.imadaptor;

public class MessageException extends InstantMessengerException {
	public MessageException(String message) {
		super(message);
	}

	public MessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
