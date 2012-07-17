package er.imadaptor;

public class ConnectedTooFastException extends IMConnectionException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ConnectedTooFastException(String message) {
		super(message);
	}

	public ConnectedTooFastException(String message, Throwable cause) {
		super(message, cause);
	}
}
