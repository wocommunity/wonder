package er.extensions.foundation;

public interface IERXStatus {

	/**
	 * @return the status of a task. Useful for a long running task to implement an optional status message.
	 */
	public String status();

}
