package er.extensions.migration;

/**
 * Thrown when something during a migration process fails.
 * 
 * @author mschrag
 */
public class ERXMigrationFailedException extends RuntimeException {
	public ERXMigrationFailedException(String message) {
		super(message);
	}

	public ERXMigrationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
