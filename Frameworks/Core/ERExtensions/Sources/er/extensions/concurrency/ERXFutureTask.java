package er.extensions.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.foundation.ERXStatusInterface;

/**
 * A FutureTask that implements @link {@link ERXStatusInterface} and @link
 * {@link ERXTaskPercentComplete} and @link {@link NSKeyValueCoding}. Additional
 * methods are provided in this FutureTask for checking if those interfaces are
 * implemented in the wrapped task and if so the values are
 * passed thru from the task.
 * 
 * Usage: 
 * <pre>
			// If null, then submit the callable task
			ERXFutureTask _future = new ERXFutureTask(callable);

			ERXExecutorService.executorService().execute(_future);
	</pre>
 * 
 * @author kieran
 * 
 */
public class ERXFutureTask<V> extends FutureTask<V> implements ERXExecutionStateTransition, ERXStatusInterface,
				ERXTaskPercentComplete, NSKeyValueCoding {
	private final Object _task;

	public ERXFutureTask(Callable<V> callable) {
		super(callable);
		_task = callable;
	}

	public ERXFutureTask(Runnable runnable, V result) {
		super(runnable, result);
		_task = runnable;
	}

	public Object task() {
		return _task;
	}

	/* (non-Javadoc)
	 * @see er.extensions.foundation.ERXStatusInterface#status()
	 */
	public String status() {
		return (hasStatus() && _task != null) ? ((ERXStatusInterface) _task).status() : null;
	}

	/* (non-Javadoc)
	 * @see er.extensions.concurrency.ERXTaskPercentComplete#percentComplete()
	 */
	public Double percentComplete() {
		return (hasPercentComplete() && _task != null) ? ((ERXTaskPercentComplete) _task).percentComplete() : null;
	}

	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	private Boolean _hasStatus;

	/**
	 * @return whether the wrapped task has @link
	 *         {@link ERXStatusInterface} interface
	 */
	public boolean hasStatus() {
		if (_hasStatus == null) {
			_hasStatus = Boolean.valueOf(_task instanceof ERXStatusInterface);
		} // ~ if (_hasStatus == null)
		return _hasStatus.booleanValue();
	}
	
	private Boolean _isStoppable;

	/** @return true if the task bound to this Future implements the {@link IERXStoppable} interface */
	public boolean isStoppable() {
		if (_isStoppable == null) {
			_isStoppable = Boolean.valueOf(_task instanceof IERXStoppable);
		}
		return _isStoppable.booleanValue();
	}

	private Boolean _hasPercentComplete;

	/**
	 * @return whether the wrapped task has @link
	 *         {@link ERXTaskPercentComplete} interface
	 */
	public boolean hasPercentComplete() {
		if (_hasPercentComplete == null) {
			_hasPercentComplete = Boolean.valueOf(_task instanceof ERXTaskPercentComplete);
		}
		return _hasPercentComplete;
	}

	public void afterExecute() {
		if (_task instanceof ERXExecutionStateTransition) {
			((ERXExecutionStateTransition) _task).afterExecute();
		}

	}

	public void beforeExecute() {
		if (_task instanceof ERXExecutionStateTransition) {
			((ERXExecutionStateTransition) _task).beforeExecute();
		}
	}
	
	@Override
	public String toString() {
		return _task == null ? super.toString() : _task.toString();
	}

}
