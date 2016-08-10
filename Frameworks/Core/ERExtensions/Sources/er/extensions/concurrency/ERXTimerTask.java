package er.extensions.concurrency;

import java.util.TimerTask;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;

/**
 * A convenience class that provides some common logic that is used in
 * {@link TimerTask} tasks. It provides support for cleaning up editing
 * context locks at the end of your task's run() method just like the
 * behavior at the end of a normal R-R loop.
 * 
 * @author q
 */
public abstract class ERXTimerTask extends TimerTask {
	private volatile EOObjectStore _parentObjectStore;
	private Long _taskEditingContextTimestampLag;
	
	/**
	 * Do not override run like implementing TimeTask
	 * directly. Instead, override _run. The run
	 * method in ERXTimerTask makes your _run method
	 * appear to be in a request, and cleans up
	 * resources at the end of the request.
	 */
	@Override
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
	 * Override _run to provide your task's implementation.
	 */
	public abstract void _run();
	
	/**
	 * See Effective Java item #71 for explanation of this threadsafe lazy
	 * initialization technique
	 * 
	 * @return the parent, usually an {@link EOObjectStoreCoordinator} to
	 *         partition the task's EOF intensive work form the rest of the app.
	 */
	protected final EOObjectStore parentObjectStore() {
		EOObjectStore osc = _parentObjectStore;
		if (osc == null) {
			synchronized (this) {
				osc = _parentObjectStore;
				if (osc == null) {
					_parentObjectStore = osc = ERXTaskObjectStoreCoordinatorPool.objectStoreCoordinator();
				}
			}
		}
		return osc;
	}

	/**
	 * @param parentObjectStore
	 *            the parent, usually an {@link EOObjectStoreCoordinator} to
	 *            partition the task's EOF intensive work from the rest of the
	 *            app. If you are going to manually set this, you should do it
	 *            before starting the task.
	 */
	public final synchronized void setParentObjectStore(EOObjectStore parentObjectStore) {
		_parentObjectStore = parentObjectStore;
	}

	/**
	 * <strong>You must manually lock and unlock the editing context returned by
	 * this method.</strong> It is not recommended that you depend on auto
	 * locking in background threads.
	 * 
	 * Even though this method currently returns an auto-locking EC if
	 * auto-locking is turned on for the app, a future update is planned that
	 * will return a manually locking EC from this method even if auto-locking
	 * is turned on for regular ECs used in normal request-response execution.
	 * 
	 * @return a new EOEditingContext.
	 */
	protected EOEditingContext newEditingContext() {
		EOEditingContext ec = ERXEC.newEditingContext(parentObjectStore());
		// if this is not a nested EC, we can set the fetch time stamp
		if (!(parentObjectStore() instanceof EOEditingContext)) {
			ec.setFetchTimestamp(taskEditingContextTimestampLag());
		}
		return ec;
	}

	/**
	 * By design EOEditingContext's have a fetch timestamp (default is 1 hour)
	 * that effectively creates an in-memory caching system for EOs. This works
	 * great for users browsing through pages in the app. However, experience
	 * has shown that background EOF tasks are performing updates based on the
	 * state of other EOs, and thus we want to This is a long-running task. The
	 * last thing I want to do is perform a long running task with stale EOs, so
	 * we lazily create a fetch timestamp of the current time when we create the
	 * first EC and thus ensure fresh data. Secondly, we continue, by default to
	 * use this timestamp for the duration of the task since experience has
	 * shown that by doing so we can prevent unnecessary database fetching
	 * especially when our task is adding lots of items to a single relationship
	 * in batches.
	 * 
	 * However if you want fresh data each time you create an EC in your task,
	 * feel free to set the fetch time stamp to the current time in your task
	 * each time you create a new EC.
	 * 
	 * For R-R ec's we prefer fresh data on new pages. However for long running
	 * tasks, it is often best pick a single point in time, usually when the
	 * first ec is created as the timestamp lag. This works well when we are
	 * iterating and making new ec's especially if we are adding 100's of items
	 * to a relationship and cycling ec's
	 * 
	 * @return the timestamp lag to use for new ec's created in the task thread.
	 */
	private long taskEditingContextTimestampLag() {
		if (_taskEditingContextTimestampLag == null) {
			_taskEditingContextTimestampLag = Long.valueOf(System.currentTimeMillis());
		}
		return _taskEditingContextTimestampLag.longValue();
	}
}
