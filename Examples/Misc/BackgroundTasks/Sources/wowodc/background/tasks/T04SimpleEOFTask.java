package wowodc.background.tasks;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wowodc.background.utilities.Utilities;
import wowodc.eof.ResultItem;
import wowodc.eof.TaskInfo;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.IERXStatus;

/**
 * A task that <em>returns</em> an EOGlobalID result.
 * 
 * What does this demonstration task do?
 * 
 * A {@link TaskInfo} entity is created for every run of this task.
 * Its attributes include
 * <ul>
 * <li>startNumber
 * <li>endNumber
 * <li>startTime
 * <li>endTime
 * </ul>
 * 
 * For a random amount of time between 5 and 15 seconds, and
 * starting at a random number, this task begins looking for prime numbers.
 * 
 * Every number checked is stored as a {@link ResultItem} that is related to the {@link TaskInfo}
 * 
 * @author kieran
 */
public class T04SimpleEOFTask implements Callable<EOGlobalID>, IERXStatus , IERXPercentComplete, IERXStoppable {
	private static final Logger log = LoggerFactory.getLogger(T04SimpleEOFTask.class);
	
	// Duration of the example task in milliseconds
	private final long DURATION = 15000;
	
	// Task elapsed time in milliseconds
	private long _elapsedTime = 0l;
	
	// Value between 0.0 and 1.0 indicating the task's percentage complete
	private double _percentComplete = 0.0d;
	
	// A message indicating current status
	private String _status = "Starting...";
	
	private long _numberToCheck = 0;
	
	private long _count = 0;
	
	private volatile boolean _isStopped = false;
	
	private EOGlobalID _resultGid;

	public EOGlobalID call() throws Exception {
		_numberToCheck = Utilities.newStartNumber();
		_elapsedTime = 0;
		Format wholeNumberFormatter = new DecimalFormat("#,##0");
		
		long startTime = System.currentTimeMillis();
		
		// Create an EC and lock/try/finally/unlock.
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			// Create the new TaskInfo
			TaskInfo taskInfo = ERXEOControlUtilities.createAndInsertObject(ec, TaskInfo.class);
			
			// Task start time
			taskInfo.setStartTime(new NSTimestamp(startTime));
			
			taskInfo.setStartNumber(_numberToCheck);
			taskInfo.setDuration(DURATION);
			
			// Loop for a period of time
			while (_elapsedTime < DURATION && !_isStopped) {
				ResultItem resultItem = ERXEOControlUtilities.createAndInsertObject(ec, ResultItem.class);
				resultItem.setTaskInfo(taskInfo);
				
				resultItem.setNumberToCheck(_numberToCheck);

				if (Utilities.isPrime(_numberToCheck)) {
					log.info("==>> {} is a PRIME number.", _numberToCheck);
					resultItem.setIsPrime(Boolean.TRUE);
				} else {
					log.debug("{} is not a prime number but is a COMPOSITE number.", _numberToCheck);
					resultItem.setIsPrime(Boolean.FALSE);
				}
				
				ec.saveChanges();
				
				
				_elapsedTime = System.currentTimeMillis() - startTime;
				
				// Update progress variables
				_count++;
				_percentComplete = (double)(_elapsedTime) / (double)DURATION;
				_status = wholeNumberFormatter.format(_count) + " numbers checked for prime qualification";

				_numberToCheck++;
			}
			
			// Complete the stats
			taskInfo.setEndNumber(_numberToCheck - 1);
			taskInfo.setEndTime(new NSTimestamp());
			taskInfo.setWorkflowState(TaskInfo.WORKFLOW_PRIME_CHECKED);
			
			ec.saveChanges();
			
			_resultGid = ec.globalIDForObject(taskInfo);
			
		} finally {
			ec.unlock();
		}
		
		return _resultGid;
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
