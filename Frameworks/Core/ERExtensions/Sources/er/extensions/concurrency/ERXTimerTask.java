package er.extensions.concurrency;

import java.util.TimerTask;

import er.extensions.appserver.ERXApplication;

/**
 * ERXTimerTask provides support for cleaning up editing context
 * locks at the end of your task's run() method just like the
 * behavior at the end of a normal R-R loop.
 * 
 * @author q
 */
public abstract class ERXTimerTask extends TimerTask {
  /**
   * Do not override run like implementing TimeTask
   * directly.  Instead, override _run.  The run
   * method in ERXTimeTask makes your _run method
   * appear to be in a request, and cleans up
   * resources at the end of the request.
   */
	public final void run() {
		ERXApplication._startRequest();
		try {
			_run();
		}
		finally {
			ERXApplication._endRequest();
		}
	}
	
	/**
	 * Override _run to provide your Task's implementation.
	 */
	public abstract void _run();
}
