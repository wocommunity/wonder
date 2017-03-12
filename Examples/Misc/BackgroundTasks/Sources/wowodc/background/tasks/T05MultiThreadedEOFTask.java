package wowodc.background.tasks;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wowodc.background.utilities.Utilities;
import wowodc.eof.ResultItem;
import wowodc.eof.TaskInfo;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.concurrency.ERXExecutorService;
import er.extensions.concurrency.ERXTask;
import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.IERXStatus;

/**
 * A task that <em>returns</em> an EOGlobalID result.
 * 
 * What does this demonstration task do?
 * 
 * A "TaskInfo" entity is created for every run of this task.
 * Its attributes include
 * <ul>
 * <li>startNumber
 * <li>endNumber
 * <li>startTime
 * <li>endTime
 * </ul>
 * 
 * For a random amount of time between 5 and 15 seconds, and
 * starting at a random number, this task begins looking for prime numbers 
 * for every
 * 
 * @author kieran
 */
public class T05MultiThreadedEOFTask extends ERXTask<EOGlobalID> implements Callable<EOGlobalID>, IERXStatus , IERXPercentComplete, IERXStoppable {
	private static final Logger log = LoggerFactory.getLogger(T05MultiThreadedEOFTask.class);
	
	// Duration of the example task in milliseconds
	// Random between 5 and 15 seconds
	private final long _taskDuration;
	
	// Task elapsed time in milliseconds
	private long _elapsedTime = 0l;
	
	// Value between 0.0 and 1.0 indicating the task's percentage complete
	private double _percentComplete = 0.0d;
	
	// A message indicating current status
	private String _status = "Starting...";
	
	private final long _startNumber = 0;
	private long _endNumber;
	
	// Base quantity - we use this plus a random amount up to the same quantity
	// just to ensure slightly varied processing times per task thread.
	// Otherwise all threads are starting and stopping at the same time
	// and it looks too fake :-)
	private final int _childBatchBaseQuantity = 2000;
	
	// Volatile since it is being updated from child threads
	private volatile long _count = 0;
	
	// We assign an ID to each child just for demo purposes so that users can
	// see that different child tasks are being started and finished in the App Monitor page.
	// This value is passed to child task in constructor and used in toString.
	private int _nextChildIDValue = 1;
	
	private volatile boolean _isStopped = false;
	
	// Just so we can have this for toString for the AppMonitor display
	private int _parentTaskPrimaryKey = -1;
	
	// Lazy initialization of static variable.
	// This prevents more than N threads in this pool even if multiple instances of
	// this task is run, which may or may not be what you want.
	// If you want a pool per task, then use a lazy initialized instance variable
	private static class ChildTaskPool {
		final static ExecutorService EXECUTOR_SERVICE = ERXExecutorService.newFiniteThreadPool(4);
	}
	
	public T05MultiThreadedEOFTask() {
		_taskDuration = 15000;
	}
	
	/**
	 * Use a demo duration parameter rather than default random demo duration.
	 * 
	 * @param demoTaskDuration duration in milliseconds
	 */
	public T05MultiThreadedEOFTask(long demoTaskDuration) {
		_taskDuration = demoTaskDuration;
	}
	
	private EOGlobalID _resultGid;

	@Override
	public EOGlobalID _call() throws Exception {
		// Start at zero to gauge performance rate with different numbers of threads and OSCs
		//_startNumber = Utilities.newStartNumber();
		_elapsedTime = 0;
		Format wholeNumberFormatter = new DecimalFormat("#,##0");
		
		long startTime = System.currentTimeMillis();
		
		EOEditingContext ec = newEditingContext();
		ec.lock();
		try {
			// Array for monitoring completed tasks to ensure normal completion
			NSMutableArray<Future<?>> childFutures = new NSMutableArray<Future<?>>();
			// Create the new TaskInfo
			TaskInfo taskInfo = ERXEOControlUtilities.createAndInsertObject(ec, TaskInfo.class);
			
			// Task start time
			taskInfo.setStartTime(new NSTimestamp(startTime));
			
			taskInfo.setStartNumber(_startNumber);
			taskInfo.setDuration(_taskDuration);
			
			ec.saveChanges();
			_resultGid = ec.globalIDForObject(taskInfo);
			_parentTaskPrimaryKey = (Integer) taskInfo.rawPrimaryKey();
			
			// Initialize loop variables
			long childTaskStartNumber = _startNumber;
			int incrementQuantity = _childBatchBaseQuantity + Utilities.sharedRandom().nextInt(_childBatchBaseQuantity);
			long childTaskEndNumber =  childTaskStartNumber + incrementQuantity;
			
			
			// Loop for a period of time
			while (_elapsedTime < _taskDuration && !_isStopped) {
				ChildPrimeTask childTask = new ChildPrimeTask(_nextChildIDValue, _resultGid, childTaskStartNumber, childTaskEndNumber);
				_nextChildIDValue++;
				
				boolean isRejected = true;
				while (isRejected && !ChildTaskPool.EXECUTOR_SERVICE.isShutdown() && !_isStopped) {
					try {
						Future<?> future = ChildTaskPool.EXECUTOR_SERVICE.submit(childTask);
						
						log.info("Submitted task corresponding to {}", future);
						isRejected = false;
						childFutures.add(future);
						
						// For the sake of demo, we assume all child tasks complete their work.
						_endNumber = childTaskEndNumber;
					} catch (RejectedExecutionException e) {
						try {
							Thread.sleep(2000);
							removeCompletedFutures(childFutures);
						} catch (InterruptedException e1) {
							stop();
						}
					}
				}
				
				childTaskStartNumber = childTaskEndNumber + 1;
				incrementQuantity = _childBatchBaseQuantity + Utilities.sharedRandom().nextInt(_childBatchBaseQuantity * 2);
				childTaskEndNumber =  childTaskStartNumber + incrementQuantity;

				_elapsedTime = System.currentTimeMillis() - startTime;
				
				// Update progress variables
				_percentComplete = (double)(_elapsedTime) / (double)_taskDuration;
				_status = wholeNumberFormatter.format(_count) + " numbers checked for prime qualification";

			}
			
			if (_isStopped) {
				_status = "Stopped";
			}
			
			// Wait for all child tasks to finish
			while (childFutures.count() > 0) {
				removeCompletedFutures(childFutures);
				Thread.sleep(1000);
			}
			
			// Complete the stats
			// Refresh it since the object has been already updated (its relationship) and saved on ChildThreads
			ERXEOControlUtilities.refreshObject(taskInfo);
			taskInfo.setEndNumber(_endNumber);
			taskInfo.setEndTime(new NSTimestamp());
			taskInfo.setWorkflowState(TaskInfo.WORKFLOW_PRIME_CHECKED);
			ec.saveChanges();
			
			_resultGid = ec.globalIDForObject(taskInfo);
			
		} finally {
			ec.unlock();
		}
		
		return _resultGid;
	}
	
	/**
	 * Removes completed futures from the futures array.
	 * 
	 * @param futures array of futures
	 */
	public void removeCompletedFutures(NSMutableArray<Future<?>> futures) {
		Iterator<Future<?>> iterator = futures.iterator();
		while (iterator.hasNext()) {
			Future<?> future = iterator.next();
			
			if (future.isDone()) {
				// Before removal, we can take this opportunity to check for errors in the child tasks
				try {
					Object result = future.get();
				} catch (Exception e) {
					// An exception here means the task did not complete normally
					throw new RuntimeException("Unexpected exception occurred in ChildTask", e);
				}

				iterator.remove();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see er.extensions.concurrency.IERXPercentComplete#percentComplete()
	 */
	public Double percentComplete() {
		return _percentComplete;
	}

	/* (non-Javadoc)
	 * @see er.extensions.foundation.IERXStatus#status()
	 */
	public String status() {
		return _status;
	}

	/* (non-Javadoc)
	 * @see er.extensions.concurrency.IERXStoppable#stop()
	 */
	public void stop() {
		log.info("The task was stopped by the user.");
		_isStopped = true;
		_status = "Stopping";
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getSimpleName());
		if (_parentTaskPrimaryKey > 0) {
			b.append(": #").append(_parentTaskPrimaryKey);
		}
		return b.toString();
	}
	
	/**
	 * A child task that will be used to process a batch of numbers in its own thread.
	 * 
	 * Note we declare this as a non-static inner class so that the child thread can update the parent thread _count (for demo of volatile)
	 * 
	 * @author kieran
	 */
	private class ChildPrimeTask extends ERXTask implements Runnable, IERXStatus, IERXPercentComplete {
		
		private final int _childID;
		private EOGlobalID _childTaskInfoGID = null;
		private final long _childFromNumber;
		private final long _childToNumber;
		
		private long _childCurrentNumber;
		
		
		public ChildPrimeTask(int childID, EOGlobalID taskInfoGID, long fromNumber, long toNumber) {
			_childID = childID;
			_childTaskInfoGID = taskInfoGID;
			_childFromNumber = fromNumber;
			_childToNumber = toNumber;
			
			// Starting value
			_childCurrentNumber = fromNumber;
		}

		public Double percentComplete() {
			return Double.valueOf((_childCurrentNumber - _childFromNumber + 1) / (_childToNumber - _childFromNumber + 1));
		}

		public String status() {
			return "Checking " + _childCurrentNumber + " in range " + _childFromNumber + " - " + _childToNumber;
		}

		@Override
		public void _run() {
			EOEditingContext ec = newEditingContext();
			ec.lock();
			try {
				log.info("Started child in {} with OSC {}", Thread.currentThread().getName(), ec.parentObjectStore());
				
				TaskInfo taskInfo = (TaskInfo) ec.faultForGlobalID(_childTaskInfoGID, ec);
				
				while (_childCurrentNumber <= _childToNumber) {
					ResultItem resultItem = ERXEOControlUtilities.createAndInsertObject(ec, ResultItem.class);
					resultItem.setTaskInfo(taskInfo);
					
					resultItem.setNumberToCheck(_childCurrentNumber);

					if (Utilities.isPrime(_childCurrentNumber)) {
						log.info("==>> {} is a PRIME number.", _childCurrentNumber);
						resultItem.setIsPrime(Boolean.TRUE);
					} else {
						log.debug("{} is not a prime number but is a COMPOSITE number.", _childCurrentNumber);
						resultItem.setIsPrime(Boolean.FALSE);
					}
					
					// We could save changes once per child task, but let's do this to keep EOF busy for the demo.
					ec.saveChanges();
					
					// Update our number to check
					_childCurrentNumber++;
					
					// Update parent task count statistic
					_count++;
				}
			} finally {
				ec.unlock();
			}
		}
		
		// 
		private String _toString = null;
		
		@Override
		public String toString() {
			if (_toString == null) {
				// We cache it since it will not change.
				StringBuilder b = new StringBuilder();
				b.append("ChildTask: #");
				b.append(_childID);
				b.append(", Parent ID=" + _parentTaskPrimaryKey);
				b.append(", From=" + _childFromNumber);
				b.append(", To=" + _childToNumber);
				
				_toString = b.toString();
			}
			return _toString;
		}
	}
}
