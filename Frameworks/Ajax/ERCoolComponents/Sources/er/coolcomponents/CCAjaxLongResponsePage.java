package er.coolcomponents;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXNextPageForResultWOAction;
import er.extensions.appserver.IERXPerformWOActionForResult;
import er.extensions.concurrency.ERXExecutorService;
import er.extensions.concurrency.ERXFutureTask;
import er.extensions.concurrency.ERXTaskPercentComplete;
import er.extensions.foundation.ERXAssert;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXStatusInterface;
import er.extensions.foundation.ERXStopWatch;

/**
 * A generic long response page that controls the execution of and provides user feedback on a long
 * running task.
 * This is designed to be really easy, and flexible, for the developer to re-use.
 * <p>
 * The common case of running a task and automatically returning to the same originating page is 
 * really simple and requires just a few lines of code, for example:
 * <pre>
 * <code>
 * 	public WOActionResults runLongTask() {
		Runnable task = new LongRunningTask();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setLongRunningRunnable(task);
		
		return nextPage;
	}

 * </code>
 * </pre>
 * <p>
 * <strong>Usage:</strong>
 * <ol>
 * <li>
 * Create a {@link Runnable} task, or a {@link Callable} task that returns some result.
 * 	<ol>
 *    <li>Optionally implement the {@link ERXStatusInterface} interface (just one method to return status message) 
 *    to have the task's status displayed in the long response page.
 *    <li>Optionally implement the {@link ERXTaskPercentComplete} interface (just one method to return percentage complete)
 *    to have a progress bar and a percentage complete automatically displayed in the long response page.
 *    </ol>
 * </li>
 * <li>If you don't just want the originating page to be returned (default behavior) then
 *    <ol>
 *    <li>Create a simple class that implements @link {@link IERXPerformWOActionForResult} interface, or use {@link ERXNextPageForResultWOAction}, which
 *    provides a fairly generic implementation of that interface
 *    <li>This controller class will get the result pushed into it when the task is complete. If the
 *    task threw an uncaught error during execution, then the error is pushed in as the result.
 *    <li>The nextPage method of this controller class can do whatever it needs to do with the result
 *    and return a new page according to your logic in {@link IERXPerformWOActionForResult#performAction()}.
 * 	</ol>
 * </li><li>
 * In your component action, simply create an instance of this long response page just as you would
 *    create any other page.
 * </li><li>
 * Push in an instance of your Runnable (or Callable) task into the long response page using {@link CCAjaxLongResponsePage#setLongRunningCallable(Callable)}
 * </li><li>
 * Optionally push in your custom next page controller for execution when the task is finished using {@link #setNextPageForResultController(IERXPerformWOActionForResult)}
 * </li><li>
 * Just return the long response page in your action method
 * </li></ol>
 * 
 * 
 * @author kieran
 *
 */
public class CCAjaxLongResponsePage extends WOComponent {
	private static final Logger log = Logger.getLogger(CCAjaxLongResponsePage.class);
	
	private static final String STYLESHEET_FRAMEWORK = ERXProperties.stringForKeyWithDefault("er.coolcomponents.CCAjaxLongResponsePage.stylesheet.framework", "ERCoolComponents");
	private static final String STYLESHEET_FILENAME = ERXProperties.stringForKeyWithDefault("er.coolcomponents.CCAjaxLongResponsePage.stylesheet.filename", "CCAjaxLongResponsePage.css");

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
	
	private String _defaultStatus;
	
	/** @return a status message that is displayed if the task does not provide a status message */
	public String defaultStatus() {
		if ( _defaultStatus == null ) {
			_defaultStatus = ERXProperties.stringForKeyWithDefault("er.coolcomponents.CCAjaxLongResponsePage.defaultStatus", "Please wait...");
		}
		return _defaultStatus;
	}
	
	/** @param defaultStatus a status message that is displayed if the task does not provide a status message */
	public void setDefaultStatus(String defaultStatus){
		_defaultStatus = defaultStatus;
	}
	
	private Integer _refreshInterval;
	
	/** @return the refresh interval in seconds. Defaults to value of er.coolcomponents.CCAjaxLongResponsePage.refreshInterval */
	public Integer refreshInterval() {
		if ( _refreshInterval == null ) {
			_refreshInterval = ERXProperties.intForKeyWithDefault("er.coolcomponents.CCAjaxLongResponsePage.refreshInterval", 2);
		}
		return _refreshInterval;
	}
	
	/** @param refreshInterval the refresh interval in seconds. Defaults to value of er.coolcomponents.CCAjaxLongResponsePage.refreshInterval */
	public void setRefreshInterval(Integer refreshInterval){
		_refreshInterval = refreshInterval;
	}

	private ERXFutureTask<?> _future;

	/** 
	 * @return the {@link Future} that is bound to the long running task.
	 * 
	 * The first time this is accessed, it is lazily initialized and
	 * it kicks off the long running task. So it is important that the initial page response causes
	 * this to be lazily initialized, otherwise the task will never get started. 
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
		ERXAssert.POST.isTrue(future().isDone());
		if ( _result == null ) {
			try {
				_result = future().get();
			} catch (CancellationException cancellationException) {
				_result = cancellationException;
			} catch (InterruptedException interruptedException) {
				_result = interruptedException;
			} catch (ExecutionException executionException) {
				log.error("Long Response Error:\n" + ERXRuntimeUtilities.informationForException(executionException), executionException);
				_result = executionException;
			}
		}
		return _result;
	}

	private boolean isStopWatchRunning = false;

	/**
	 * @return the elapsedTime since the task started running
	 */
	public String elapsedTime() {
		if (future().isDone() && isStopWatchRunning) {
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
	

	
	public String controlScriptContent() {
		String result = ";";
		if (future().isDone()) {
			//result = "performNextPageAction();";
			// Delay 1 second so that user can see the 100% complete progress message
			result = "window.setTimeout(performNextPageAction, 1000);";
		}
		return result;
	}

	
	
	
	/**
	 * @return the table cell width value for the finished part of the progress bar, for example "56%". 
	 * The same string can be used to display user-friendly percentage complete value.
	 */
	public String finishedPercentage() {
		String result = "1%";
		Double percentComplete = future().percentComplete();
		if (percentComplete != null) {
			long userPercentComplete = Math.round(percentComplete.doubleValue() * 100.0d);
			if (userPercentComplete < 1) {
				userPercentComplete = 1;
			}
			if (userPercentComplete > 100) {
				userPercentComplete = 100;
			}
			result = userPercentComplete + "%";
		}
		return result;
	}
	
	/**
	 * @return true if logging is Debug level. Used to display page config info in the long response page itself during development.
	 */
	public boolean isDebugMode() {
		return log.isDebugEnabled();
	}
	
	public String styleSheetFramework() {
		return STYLESHEET_FRAMEWORK;
	}
	
	public String styleSheetFilename() {
		return STYLESHEET_FILENAME;
	}

	
	
	
	
	
}