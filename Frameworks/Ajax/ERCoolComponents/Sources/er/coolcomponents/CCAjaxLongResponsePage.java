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
import er.extensions.appserver.IERXPerformWOAction;
import er.extensions.appserver.IERXPerformWOActionForResult;
import er.extensions.concurrency.ERXExecutorService;
import er.extensions.concurrency.ERXFutureTask;
import er.extensions.concurrency.ERXTaskPercentComplete;
import er.extensions.concurrency.IERXStoppable;
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
	
	// Constants to determine the CSS stylesheet used for the long response page for this app
	private static final String STYLESHEET_FRAMEWORK = ERXProperties.stringForKeyWithDefault("er.coolcomponents.CCAjaxLongResponsePage.stylesheet.framework", "ERCoolComponents");
	private static final String STYLESHEET_FILENAME = ERXProperties.stringForKeyWithDefault("er.coolcomponents.CCAjaxLongResponsePage.stylesheet.filename", "CCAjaxLongResponsePage.css");

	// flag to indicate that the user stopped the task (if it was stoppable and the stop control was visible)
	private boolean _wasStoppedByUser = false;
	
	// The page that instantiated this long response page
	private final WOComponent _referringPage;
	
	public CCAjaxLongResponsePage(WOContext context) {
        super(context);
        // Grab the referring page when this long response page is created
    	_referringPage = context.page();
    }

    private IERXPerformWOActionForResult _nextPageForResultController;

	/** 
	 * @return the page controller that will be given the result of the long task and 
	 * return the next page except for the case where the user stops the task.
	 * 
	 * */
	public IERXPerformWOActionForResult nextPageForResultController() {
		if (_nextPageForResultController == null) {
			_nextPageForResultController = new ERXNextPageForResultWOAction(_referringPage);
		} //~ if (_nextPageForResultController == null)
		return _nextPageForResultController;
	}

	/** 
	 * @param nextPageForResultController the page controller that will be given the result of the long task and 
	 * return the next page except for the case where the user stops the task.
	 * 
	 **/
	public void setNextPageForResultController(IERXPerformWOActionForResult nextPageForResultController){
		_nextPageForResultController = nextPageForResultController;
	}
	
	private IERXPerformWOAction _nextPageForCancelController;
	
	/** @return the controller that handles the scenario where the user stops a stoppable task */
	public IERXPerformWOAction nextPageForCancelController() {
		if ( _nextPageForCancelController == null ) {
			// By default, return the originating page
			_nextPageForCancelController = new ERXNextPageForResultWOAction(_referringPage);;
		}
		return _nextPageForCancelController;
	}
	
	/** @param nextPageForCancelController the controller that handles the scenario where the user stops a stoppable task */
	public void setNextPageForCancelController(IERXPerformWOAction nextPageForCancelController){
		_nextPageForCancelController = nextPageForCancelController;
	}
	
	private Object _task;

	/** @return the Runnable and/or Callable task */
	public Object task() {
		return _task;
	}

	/**
	 * @param task
	 *            the Runnable and/or Callable task
	 */
	public void setTask(Object task) {
		if ( task instanceof Runnable || task instanceof Callable ) {
			_task = task;
		} else {
			throw new IllegalArgumentException("The task must implement the Runnable or the Callable interface!");
		}
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
	
	/** @param refreshInterval the refresh interval in seconds. Defaults to value of er.coolcomponents.CCAjaxLongResponsePage.refreshInterval or 2 seconds. */
	public void setRefreshInterval(Integer refreshInterval){
		_refreshInterval = refreshInterval;
	}

	private ERXFutureTask<?> _future;

	/** 
	 * @return the {@link Future} that is bound to the long running task.
	 * 
	 * The first time this method is accessed, it is lazily initialized and
	 * it starts the long running task.
	 * 
	 * */
	@SuppressWarnings("unchecked")  // Unchecked cast
	public ERXFutureTask<?> future() {
		if ( _future == null ) {

			Object task = task();
			if (task instanceof Callable) {
				_future = new ERXFutureTask<Object>((Callable<Object>)task);
			} else {
				// Runnable interface only
				_future = new ERXFutureTask<Object>((Runnable)task, null);
			}

			// This is where we hand off the task to our executor service to run
			// it in a background thread
			ERXExecutorService.executorService().execute(_future);
		}
		return _future;
	}

	public WOActionResults nextPage() {
		if (log.isDebugEnabled()) log.debug("nextPage action fired");
		WOActionResults results = null;
		
		// If user canceled, we just call that controller
		if (_wasStoppedByUser) {
			if (log.isDebugEnabled())
				log.debug("The task was canceled by the user, so now calling " + nextPageForCancelController());
			results = nextPageForCancelController().performAction();
		} else {
			if (log.isDebugEnabled())
				log.debug("The task completed normally. Now setting the result, " + result()
						+ ", and calling " + nextPageForResultController());
			nextPageForResultController().setResult(result());
			results = nextPageForResultController().performAction();
		}
		

		if (log.isDebugEnabled())
			log.debug("results = " + (results == null ? "null" : results.toString()));

		return results;
	}

	private Object _result;

	/** 
	 * @return the result of the task 
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
	
	/**
	 * @return the javascript snippet that will call the nextPage action when the task is done.
	 */
	public String controlScriptContent() {
		String result = ";";
		if (future().isDone()) {
			// To avoid confusion and users saying it never reaches 100% (which can happen if we complete and return the result 
			// before the last refresh that _would_ display 100% if we waited), we will wait for a period slightly longer than the
			// refresh interval to get one more refresh and let the user visually see 100%.
			// Wait one refresh interval plus 900 milliseconds as long as the refresh interval is not customized to some huge value by the
			// developer
			int delay = Math.min(((refreshInterval().intValue() * 1000) + 900), 2900);
			result = "window.setTimeout(performNextPageAction, " + delay + ");";
		}
		if (log.isDebugEnabled())
			log.debug("controlScriptContent on refresh = " + result);
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
	 * @return boolean to hide the unfinished table cell to avoid a tiny slice of unfinished when we are at 100%
	 */
	public boolean hideUnfinishedProgressTableCell() {
		return future().isDone() && !wasStoppedByUser();
	}
	
	/**
	 * @return true if logging is Debug level. Used to display page config info in the long response page itself during development.
	 */
	public boolean isDebugMode() {
		return log.isDebugEnabled();
	}
	
	/**
	 * @return the framework containing the CSS stylesheet for this page
	 */
	public String styleSheetFramework() {
		return STYLESHEET_FRAMEWORK;
	}
	
	/**
	 * @return the filename of the CSS stylesheet webserver resource for this page
	 */
	public String styleSheetFilename() {
		return STYLESHEET_FILENAME;
	}

	/**
	 * User action to stop the task if it implements {@link IERXStoppable}. If the task is not
	 * stoppable, this action has no effect.
	 */
	public WOActionResults stopTask() {
		Object task = future().task();
		if (task instanceof IERXStoppable) {
			IERXStoppable stoppable = (IERXStoppable)task;
			stoppable.stop();
			_wasStoppedByUser = true;
		}
		return null;
	}
	
	/**
	 * @return true if the user stopped the task while it was in progress.
	 */
	public boolean wasStoppedByUser() {
		return _wasStoppedByUser;
	}

	
	
	
	
	
}