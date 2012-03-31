package er.extensions.concurrency;

public interface IERXPercentComplete {
	/**
	 * @return a Double between 0 and 1.0 indicating how far a task has progressed toward completion. A null return value
	 * indicates that percent complete is unknown
	 */
	public Double percentComplete();
}
