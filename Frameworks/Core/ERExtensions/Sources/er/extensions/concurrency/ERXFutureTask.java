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
 * Usage: <code>
			// If null, then submit the callable task
			ERXFutureTask _future = new ERXFutureTask(callable);

			ERXExecutorService.executorService().execute(_future);
			</code>
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

	public String status() {
		return (hasStatus() && _task != null) ? ((ERXStatusInterface) _task).status() : null;
	}

	public Double percentComplete() {
		return (hasPercentComplete() && _task != null) ? ((ERXTaskPercentComplete) _task).percentComplete() : null;
	}

	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	public Boolean _hasStatus;

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

//	public String userPresentableDescription() {
//		return (hasUserPresentableDescription() && _task != null) ? ((UserPresentableDescription) _task)
//						.userPresentableDescription() : _task.toString();
//	}
//
//	private Boolean _userPresentableDescription;
//
//	/** @return Callable has task description feature */
//	public boolean hasUserPresentableDescription() {
//		if (_userPresentableDescription == null) {
//			_userPresentableDescription = Boolean.valueOf(_task instanceof UserPresentableDescription);
//		}
//		return _userPresentableDescription.booleanValue();
//	}

	public void afterExecute() {
		if (_task instanceof ERXExecutionStateTransition) {
			((ERXExecutionStateTransition) _task).afterExecute();
		} // ~ if (_callable instanceof ERXExecutionStateTransition)

	}

	public void beforeExecute() {
		if (_task instanceof ERXExecutionStateTransition) {
			((ERXExecutionStateTransition) _task).beforeExecute();
		} // ~ if (_callable instanceof ERXExecutionStateTransition)
	}
	
	@Override
	public String toString() {
		return _task == null ? super.toString() : _task.toString();
	}

}
