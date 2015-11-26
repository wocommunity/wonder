package wowodc.background.tasks;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import wowodc.background.utilities.Utilities;
import wowodc.eof.ResultItem;
import wowodc.eof.TaskInfo;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.concurrency.ERXTask;
import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.foundation.IERXStatus;

/**
 * This task iterates thru a the ResultItems related to a TaskInfo argument.
 * 
 * For large data sets, the use of a {@link ERXFetchSpecificationBatchIterator} and replacing
 * the {@link EOEditingContext} for each batch can help
 * Factorial and factorial primes.
 * http://en.wikipedia.org/wiki/Factorial_prime
 * 
 * @author kieran
 */
public class T06EOFFactorialUpdateTask extends ERXTask<EOGlobalID> implements Callable<EOGlobalID>, IERXStatus , IERXPercentComplete, IERXStoppable {
	
	private static final Logger log = Logger.getLogger(T06EOFFactorialUpdateTask.class);
	
	// Duration of the example task in milliseconds
	// Random between 5 and 15 seconds
	private final long DURATION = Utilities.sharedRandom().nextInt(10001) + 5000;
	
	// Task elapsed time in milliseconds
	private long _elapsedTime = 0l;
	
	// Value between 0.0 and 1.0 indicating the task's percentage complete
	private double _percentComplete = 0.0d;
	
	// A message indicating current status
	private String _status = "Starting...";
	
	private long _countCompleted = 0;
	
	private long _totalCount = 0;
	
	private volatile boolean _isStopped = false;
	
	public T06EOFFactorialUpdateTask(TaskInfo taskInfo) {
		if (taskInfo.isNewObject()) {
			throw new IllegalArgumentException("taskInfo must be already saved to the database before this task can be run");
		}
		
		// Check if workflow state is appropriate
		if (!taskInfo.workflowState().equals(TaskInfo.WORKFLOW_PRIME_CHECKED)) {
			throw new IllegalStateException("The taskInfo must be in the " + TaskInfo.WORKFLOW_PRIME_CHECKED + " before it can begin Factorial processing.");
		}
		
		// Grab GID reference before the task is started.
		_taskInfoGID = taskInfo.editingContext().globalIDForObject(taskInfo);
	}
	
	private final EOGlobalID _taskInfoGID;

	@Override
	public EOGlobalID _call() {
		_elapsedTime = 0;
		Format wholeNumberFormatter = new DecimalFormat("#,##0");
		
		long startTime = System.currentTimeMillis();
		
		// Note we use the superclass convenience method here.
		EOEditingContext ec = newEditingContext();
		ec.lock();
		try {
			// Fetch the TaskInfo
			TaskInfo taskInfo = (TaskInfo) ec.faultForGlobalID(_taskInfoGID, ec);
			_totalCount = taskInfo.countResultItems().longValue();
			
			// Task start time
			// This is a demo, so we are going to replace the prime processing times with the factorial processing times
			taskInfo.setStartTime(new NSTimestamp(startTime));
			
			// For demo purposes we will use batches and EC recycling, which would be common for processing huge data sets
			ERXFetchSpecification<ResultItem> fs = taskInfo.fetchSpecificationForResultItems();
			
			// Batch iterator
			ERXFetchSpecificationBatchIterator<ResultItem> fsIterator = new ERXFetchSpecificationBatchIterator<ResultItem>(fs, ec);

			// Loop for a period of time
			while (fsIterator.hasNext() && !_isStopped) {
				@SuppressWarnings("unchecked")
				NSArray<ResultItem> batch = fsIterator.nextBatch();
				
				for (ResultItem resultItem : batch) {
					resultItem.setWorkflowState(ResultItem.WORKFLOW_CHECKING_FACTORIAL);
					performFactorialProcessing(resultItem);
					resultItem.setWorkflowState(ResultItem.WORKFLOW_PROCESSING_COMPLETE);

					ec.saveChanges();
					
					
					_elapsedTime = System.currentTimeMillis() - startTime;
					
					// Update progress variables
					_countCompleted++;
					_percentComplete = (double)(_countCompleted) / (double)_totalCount;
					_status = wholeNumberFormatter.format(_countCompleted) + " numbers checked for factorial proximity";
					
					if (_isStopped) {
						break;
					}

				}
				
				// Swap in a fresh EC for the next batch to help with memory management
				EOEditingContext freshEC = newEditingContext();
				ec.unlock();
				ec = freshEC;
				freshEC.lock();
				
				fsIterator.setEditingContext(ec);
				
				// We need to refault taskInfo into the new EC after swapping
				taskInfo = (TaskInfo) ec.faultForGlobalID(_taskInfoGID, ec);
				
			}
			
			// Complete the stats
			taskInfo.setEndTime(new NSTimestamp());
			taskInfo.setWorkflowState(TaskInfo.WORKFLOW_PROCESSING_COMPLETE);
			
			long duration = taskInfo.endTime().getTime() - taskInfo.startTime().getTime();
			taskInfo.setDuration(duration);
			
			ec.saveChanges();
			
		} finally {
			ec.unlock();
		}
		
		return _taskInfoGID;
	}
	
	/**
	 * Finds the closest factorial number and corresponding factor and updates resultItem.
	 * If the resultItem was a prime number, we check if it is a Factorial Prime.
	 * http://en.wikipedia.org/wiki/Factorial_prime
	 * 
	 * @param resultItem
	 */
	private void performFactorialProcessing(ResultItem resultItem) {
		long numberToCheck = resultItem.numberToCheck().longValue();
		long factor = 1;
		long factorial = 1;
		long distance = Math.abs(numberToCheck - factorial);
		long newDistance = 0;
		do {
			factor++;
			factorial = factorial * factor;
			newDistance = Math.abs(numberToCheck - factorial);
			
			log.debug("factor: " + factor + "; factorial: " + factorial + "; distance: " + distance + "; new Distance: " + newDistance);
		} while (newDistance < distance);
		
		// Upon exiting the loop, we will have gone one factor too far
		factorial = factorial / factor;
		factor--;
		
		resultItem.setClosestFactorial(factorial);
		resultItem.setFactorNumber((int)factor);
		
		// Check if Factorial prime
		if (resultItem.isPrime().booleanValue()) {
			if (Math.abs(numberToCheck - factorial) == 1) {
				// We have a Factorial Prime number
				resultItem.setIsFactorialPrime(Boolean.TRUE);
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
	}
}
