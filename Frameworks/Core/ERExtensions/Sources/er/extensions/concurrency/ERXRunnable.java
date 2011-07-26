package er.extensions.concurrency;

import er.extensions.appserver.ERXApplication;

/**
 * ERXRunnable provides support for cleaning up editing context
 * locks at the end of your thread's run() method just like the
 * behavior at the end of a normal R-R loop.
 * 
 * Alternatively you can use EXRExecutorService to ensure editing context locks are cleaned up
 * automatically for you after a plain Runnable has completed, for example:
 * 		<code>ERXExecutorService.executorService().execute(myPlainOldRunnable);</code>
 * 
 * @author mschrag
 */
public abstract class ERXRunnable implements Runnable {
  /**
   * Do not override run like implementing Runnable
   * directly.  Instead, override _run.  The run
   * method in ERXRunnable makes your _run method
   * appear to be in a request, and cleans up
   * resources at the end of the request.
   */
	public void run() {
		ERXApplication._startRequest();
		try {
			_run();
		}
		finally {
			ERXApplication._endRequest();
		}
	}
	
	/**
	 * Override _run to provide your Thread's implementation.
	 */
	public abstract void _run();
}
