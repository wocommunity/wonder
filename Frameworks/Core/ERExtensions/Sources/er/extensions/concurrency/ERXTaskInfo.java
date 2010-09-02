package er.extensions.concurrency;

/**
 * A simple class to capture a Runnable and some snapshot states about it. Useful for monitoring.
 *
 * @author kieran
 *
 */
public final class ERXTaskInfo {
	private final Runnable r;
	private final String elapsedTime;

	public ERXTaskInfo(Runnable r, String elapsedTime) {
		this.r = r;
		this.elapsedTime = elapsedTime;
	}

	public Runnable task() {
		return r;
	}

	public String elapsedTime() {
		return elapsedTime;
	}

}
