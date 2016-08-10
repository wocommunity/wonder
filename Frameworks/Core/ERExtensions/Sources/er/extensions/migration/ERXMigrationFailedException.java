package er.extensions.migration;

/**
 * Thrown when something during a migration process fails.
 * 
 * @author mschrag
 */
public class ERXMigrationFailedException extends RuntimeException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXMigrationFailedException(String message) {
		super(message);
	}

	public ERXMigrationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
