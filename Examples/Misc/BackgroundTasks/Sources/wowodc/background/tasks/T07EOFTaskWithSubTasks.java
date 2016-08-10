package wowodc.background.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import wowodc.eof.TaskInfo;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.concurrency.ERXExecutorService;
import er.extensions.concurrency.ERXTask;
import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.foundation.IERXStatus;

/**
 * A demo task that runs two other tasks in sequence and uses the result of the first as the argument for the second.
 * 
 * @author kieran
 */
public class T07EOFTaskWithSubTasks extends ERXTask<EOGlobalID> implements Callable<EOGlobalID>, IERXStatus,
		IERXPercentComplete, IERXStoppable {
	private T04SimpleEOFTask _task1 = null;
	private T06EOFFactorialUpdateTask _task2 = null;

	private volatile boolean _isStopped = false;

	private EOGlobalID _taskInfoGID = null;

	@Override
	public EOGlobalID _call() throws Exception {
		long startTime = System.currentTimeMillis();

		if (!_isStopped) {
			_task1 = new T04SimpleEOFTask();
			// Two ways to skin the cat (http://www.worldwidewords.org/qa/qa-mor1.htm) are shown here
			// For the first task, we simple execute it in the current thread by calling it directly
			_taskInfoGID = _task1.call();
		}

		if (!_isStopped) {
			// We create an ec just for the sake of the constructor API on
			// T06EOFFactorialUpdateTask
			EOEditingContext ec = newEditingContext();
			ec.lock();
			try {
				TaskInfo taskInfo = (TaskInfo) ec.faultForGlobalID(_taskInfoGID, ec);
				_task2 = new T06EOFFactorialUpdateTask(taskInfo);
			} finally {
				ec.unlock();
			}
			
			// Sometimes it ise useful to share the parent OSC with a subtask to avoid data out of sync issues
			_task2.setParentObjectStore(parentObjectStore());
			
			// Here we show how the second task can be executed in yet another thread while this
			// thread waits for the result
			
			
			Future<EOGlobalID> future = ERXExecutorService.executorService().submit(_task2);
			// This next statement blocks until the task, running in another thread, is complete.
			_taskInfoGID = future.get();
			
			// Finally, overwrite the startTime and Duration to reflect this combo task
			// rather than the last task.
			ec = newEditingContext();
			ec.lock();
			try {
				TaskInfo taskInfo = (TaskInfo) ec.faultForGlobalID(_taskInfoGID, ec);
				taskInfo.setStartTime(new NSTimestamp(startTime));
				taskInfo.setDuration(Long.valueOf(taskInfo.endTime().getTime() - startTime));
				ec.saveChanges();
			} finally {
				ec.unlock();
			}
		}

		return _taskInfoGID;
	}

	public void stop() {
		_isStopped = true;

		if (_task1 != null) {
			_task1.stop();
		}
		if (_task2 != null) {
			_task2.stop();
		}
	}

	public Double percentComplete() {
		double _percent = 0.0d;
		if (_task1 != null) {
			_percent += _task1.percentComplete().doubleValue();
		}
		if (_task2 != null) {
			_percent += _task2.percentComplete().doubleValue();
		}
		// Two tasks, so get the average completion.
		// This also works for parallel tasks
		return Double.valueOf(_percent / 2);
	}

	public String status() {
		String _status = "Processing";
		
		// Check last task first (acceptable approach when each consecutive task is created as needed)
		if (_task2 != null) {
			return "Factorials: " + _task2.status();
		}
		
		if (_task1 != null) {
			return "Primes: " + _task1.status();
		}
		
		// Default
		return _status;
	}
}
