package er.coolcomponents;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.ajax.AjaxProgress;
import er.extensions.appserver.ERXNextPageForResultWOAction;
import er.extensions.appserver.IERXPerformWOActionForResult;
import er.extensions.concurrency.ERXExecutorService;
import er.extensions.concurrency.ERXFutureTask;
import er.extensions.concurrency.ERXTaskPercentComplete;
import er.extensions.foundation.ERXAssert;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXStatusInterface;
import er.extensions.foundation.ERXStopWatch;

/**
 * A generic long response page that controls the execution of and provides user feedback on a long
 * running task.
 * This is designed to be really easy, and flexible, for the developer to re-use.
 *
 * Usage:
 * 1) Create a {@link Callable} class that returns some result or a {@link Runnable} that just does some background operation.
 *    A) Optionally implement the {@link ERXStatusInterface} interface (one method to return status message)
 *    B) Optionally implement the {@link ERXTaskPercentComplete} interface (one method to return percentage complete)
 * 2) If you don't just want the originating page to be returned (default behaviour) then
 *    A) Create a simple class that implements @link {@link IERXPerformWOActionForResult} interface
 *    B) This controller class will get the result pushed into it when the task is complete. If the
 *    task threw an uncaught error during execution, then the error is pushed in as the result.
 *    C) The nextPage method of this controller class can do whatever it needs to do with the result
 *    and return a new page according to your logic.
 * 3) In your component action, simply create an instance of this long response page just as you would
 *    create any other page.
 * 4) Push in an instance of your Callable task into the long response page using {@link CCAjaxLongResponsePage#setLongRunningCallable(Callable)}
 * 5) Optionally push in your custom next page controller for execution when the task is finished using {@link #setNextPageForResultController(IERXPerformWOActionForResult)}
 * 6) Just return the long response page in your action method
 *
 * @author kieran
 *
 */
public class CCAjaxLongResponsePage extends WOComponent {
	private static final Logger log = Logger
			.getLogger(CCAjaxLongResponsePage.class);

	private final WOComponent _referringPage;
	
	public CCAjaxLongResponsePage(WOContext context) {
        super(context);
        // Grab the referring page when this long response page is created
    	_referringPage = context.page();
    }

    private IERXPerformWOActionForResult _nextPageForResultController;

	/** 
	 * @return the page controllers that will be given the result of the long task 
	 * 
	 * */
	public IERXPerformWOActionForResult nextPageForResultController() {
		if (_nextPageForResultController == null) {
			_nextPageForResultController = new ERXNextPageForResultWOAction(_referringPage);
		} //~ if (_nextPageForResultController == null)
		return _nextPageForResultController;
	}

	/** 
	 * @param nextPageForResultController the page controllers that will be given the result of the long task 
	 * 
	 **/
	public void setNextPageForResultController(IERXPerformWOActionForResult nextPageForResultController){
		_nextPageForResultController = nextPageForResultController;
	}

	private Callable<?> _longRunningCallable;

	/** @return the long running {@link Callable} */
	public Callable<?> longRunningCallable() {
		return _longRunningCallable;
	}

	/** param longRunningCallable the long running {@link Callable} */
	public void setLongRunningCallable(Callable<?> longRunningCallable){
		_longRunningCallable = longRunningCallable;
	}
	
	private Runnable _longRunningRunnable;
	
	/** 
	 * @return the runnable 
	 * 
	 * */
	public Runnable longRunningRunnable() {
		return _longRunningRunnable;
	}
	
	/** 
	 * @param longRunningRunnable the runnable 
	 * 
	 * */
	public void setLongRunningRunnable(Runnable longRunningRunnable){
		_longRunningRunnable = longRunningRunnable;
	}
	
	private String _staticMessage;
	
	/** 
	 * @return a static message to display while the task is running 
	 * 
	 * */
	public String staticMessage() {
		return _staticMessage;
	}
	
	/** 
	 * @param staticMessage a static message to display while the task is running 
	 * 
	 * */
	public void setStaticMessage(String staticMessage){
		_staticMessage = staticMessage;
	}

	private ERXFutureTask<?> _future;

	/** 
	 * @return the {@link Runnable} controller for the {@link Callable}
	 * The first time this is accessed, it is lazily initialized and
	 * it kicks off the long running task. So it is important that the initial page causes
	 * this to be accessed - currently the isDone method ensures that happens. 
	 * 
	 * */
	public ERXFutureTask<?> future() {
		if ( _future == null ) {
			if (longRunningCallable() == null && longRunningRunnable() == null) {
				throw new IllegalArgumentException("Either the Callable or Runnable must be set before this page is returned");
			}
			
			if (longRunningCallable() != null) {
				_future = new ERXFutureTask(longRunningCallable());
			} else {
				_future = new ERXFutureTask(longRunningRunnable(), null);
			}

			// This is where we hand off the task to our executor service to run
			// it in a background thread
			ERXExecutorService.executorService().execute(_future);
		}
		return _future;
	}

	/**
	 * @return whether task is done. This needs to be executed on every refresh for the end of the task to fire events.
	 */
	public boolean isDone() {
		boolean isDone = future().isDone();
		taskProgress().setDone(isDone);
		return isDone;
	}

	private AjaxProgress _taskProgress;

	/** 
	 * @return the AjaxProgress model. This is a layer between the ProgressBar component and the Future/Callable objects
	 * 
	 **/
	public AjaxProgress taskProgress() {
		if ( _taskProgress == null ) {
			if (longRunningCallable() != null) {
				_taskProgress = new WKLongResponseProgress(future(), longRunningCallable());
			} else {
				_taskProgress = new WKLongResponseProgress(future(), longRunningRunnable());
			}
			
		}
		return _taskProgress;
	}


	public WOActionResults nextPage() {
		if (log.isDebugEnabled()) log.debug("nextPage action fired");
		nextPageForResultController().setResult(result());
		WOActionResults results = nextPageForResultController().performAction();
		if (log.isDebugEnabled())
			log.debug("results = " + (results == null ? "null" : results.toString()));

		return results;
	}

	private Object _result;

	/** 
	 * @return the result of the callable task 
	 * 
	 **/
	public Object result() {
		ERXAssert.POST.isTrue(isDone());
		if ( _result == null ) {
			try {
				_result = future().get();
			} catch (CancellationException cancellationException) {
				taskProgress().cancel();
				_result = cancellationException;
			} catch (InterruptedException interruptedException) {
				taskProgress().cancel();
				_result = interruptedException;
			} catch (ExecutionException executionException) {
				log.error("Long Response Error:\n" + ERXRuntimeUtilities.informationForException(executionException), executionException);
				taskProgress().setFailure(executionException);
				_result = executionException;
			}
		}
		return _result;
	}

	/**
	 * No point in showing it if we don't have percent complete feature.
	 * When no percent complete we show spinner instead
	 *
	 * @return whether to show the progress bar or not
	 */
	public boolean showProgressBar() {
		return future().hasPercentComplete();
	}

	private boolean isStopWatchRunning = false;

	/**
	 * Yes, kind of lame that we depend on the refresh calling elapsedTime to check if we are done and whether to stop the clock
	 * 
	 * @return the elapsedTime since the task started running
	 */
	public String elapsedTime() {
		if (isDone() && isStopWatchRunning) {
			stopWatch().stop();
			isStopWatchRunning = false;
		} //~ if (isDone())
		return stopWatch().toString();
	}

	private ERXStopWatch _stopWatch;

	/** 
	 * @return a stopwatch timer, lazy initialized and started on first call of this method
	 **/
	public ERXStopWatch stopWatch() {
		if ( _stopWatch == null ) {
			_stopWatch = new ERXStopWatch();
			_stopWatch.start();
			isStopWatchRunning = true;

		}
		return _stopWatch;
	}
	
	public static class WKLongResponseProgress extends AjaxProgress  {
		private final Future<?> future;
		private final Object task;

		public WKLongResponseProgress(Future<?> future, Callable<?> task) {
			super(100);
			this.future = future;
			this.task = task;
		}
		
		public WKLongResponseProgress(Future<?> future, Runnable task) {
			super(100);
			this.future = future;
			this.task = task;
		}


		@Override
		public boolean isDone() {
			boolean isFutureDone = future.isDone();
			if (isFutureDone != super.isDone()) {
				super.setDone(isFutureDone);
			} //~ if (isFutureDone != super.isDone())
			return super.isDone();
		}

		@Override
		public void cancel() {
			future.cancel(true);
			super.cancel();
		}

		@Override
		public String status() {
			if (task instanceof ERXStatusInterface) {
				return ((ERXStatusInterface)task).status();
			}
			
			// OK, so the task does not implement the status interface, let's do defaults
			String status = super.status();
			if (status == null) {
				if (future.isDone()) {
					status = "Task Complete";
					super.setDone(true);
				} else {
					status = "Processing...";
				}
			}
			return status;
		}

		@Override
		public double percentage() {
			if (task instanceof ERXTaskPercentComplete) {
				double percent = ((ERXTaskPercentComplete)task).percentComplete().doubleValue();
				super.setValue((long)(100.0 * percent));
				return percent;
			}
			return super.percentage();
		}

		public double percentNominalDisplay() {
			return percentage() * 100.0d;
		}




	}

	
}