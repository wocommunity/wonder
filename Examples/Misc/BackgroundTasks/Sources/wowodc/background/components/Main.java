package wowodc.background.components;

import java.util.concurrent.Callable;

import wowodc.background.tasks.T01T02SimpleBackgroundTask;
import wowodc.background.tasks.T03BackgroundTaskWithProgressFeedback;
import wowodc.background.tasks.T04SimpleEOFTask;
import wowodc.background.tasks.T05MultiThreadedEOFTask;
import wowodc.background.tasks.T07EOFTaskWithSubTasks;
import wowodc.background.tasks.T08CallableWithSimulatedError;
import wowodc.background.tasks.T09RunnableWithSimulatedError;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOGlobalID;

import er.coolcomponents.CCAjaxLongResponsePage;
import er.extensions.appserver.ERXNextPageForResultWOAction;
import er.extensions.appserver.IERXPerformWOActionForResult;
import er.extensions.components.ERXComponent;
import er.extensions.concurrency.ERXExecutorService;
import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.foundation.IERXStatus;

public class Main extends ERXComponent {
	public Main(WOContext context) {
		super(context);
	}

	/**
	 * Demo 1
	 * T01
	 * @return the current page (null) after creating a task and starting it in another thread.
	 */
	public WOActionResults dispatchBackgroundTask() {
		T01T02SimpleBackgroundTask task = new T01T02SimpleBackgroundTask();
		ERXExecutorService.executorService().execute(task);
		return null;
	}
	
	
	
	/**
	 * Demo 2
	 * 
	 * T01
	 * @return long response page running the same task in a long response page allowing user to wait and know when it is complete.
	 */
	public WOActionResults dispatchBackgroundTaskInLongRsponsePage() {
		Runnable task = new T01T02SimpleBackgroundTask();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setTask(task);
		
		return nextPage;
	}
	
	
	
	
	/**
	 * Demo 3
	 * 
	 * @return long response page running a task that implements {@link IERXStatus}, {@link IERXPercentComplete} and {@link IERXStoppable} interfaces.
	 */
	public WOActionResults dispatchBackgroundTaskWithLongResponsePageFeedback() {
		Runnable task = new T03BackgroundTaskWithProgressFeedback();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setTask(task);
		
		return nextPage;
	}
	
	
	
	
	
	/**
	 * Demo 4
	 * 
	 * @return result {@link TaskInfoPage} displaying result of task {@link T04SimpleEOFTask}
	 */
	public WOActionResults dispatchSimpleEOFTask() {
		// Set up the controller for the end of task action
		IERXPerformWOActionForResult controller = new ERXNextPageForResultWOAction(pageWithName(TaskInfoPage.class), "taskInfo");
		
		Callable<EOGlobalID> task = new T04SimpleEOFTask();
		
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setNextPageForResultController(controller);
		nextPage.setTask(task);
		
		return nextPage;
	}

	
	
	
	
	
	/**
	 * Demo 5
	 * 
	 * @return result {@link TaskInfoPage} displaying result of task {@link T05MultiThreadedEOFTask}
	 */
	public WOActionResults dispatchMultiThreadedTask() {
		// Set up the controller for the end of task action
		ERXNextPageForResultWOAction controller = new ERXNextPageForResultWOAction(pageWithName(TaskInfoPage.class), "taskInfo");
		
		Callable<EOGlobalID> task = new T05MultiThreadedEOFTask();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setNextPageForResultController(controller);
		nextPage.setTask(task);
		
		return nextPage;
	}
	
	
	
	
	
	
	/**
	 * Demo 6
	 * 
	 * Just to better visualize the multi-threaded task, we start it as a background task and
	 * go to the App Monitor page to view app current task activity.
	 * 
	 * @return result {@link TaskInfoPage} displaying result of task {@link T05MultiThreadedEOFTask}
	 */
	public WOActionResults dispatchMultiThreadedTaskWithoutLongResponse() {

		Callable<EOGlobalID> task = new T05MultiThreadedEOFTask(60000);
		// We just ignore the return value in the case of sending a Callable to background and not
		// caring about handling the result object
		ERXExecutorService.executorService().submit(task);
		
		return pageWithName(AppMonitor.class);
	}

	
	
	
	
	
	
	/**
	 * Demo 7
	 * 
	 * @return result {@link TaskInfoPage} displaying result of task {@link T07EOFTaskWithSubTasks}
	 */
	public WOActionResults dispatchComboTask() {
		// Set up the controller for the end of task action
		ERXNextPageForResultWOAction controller = new ERXNextPageForResultWOAction(pageWithName(TaskInfoPage.class), "taskInfo");
		
		Callable<EOGlobalID> task = new T07EOFTaskWithSubTasks();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setNextPageForResultController(controller);
		nextPage.setTask(task);
		
		return nextPage;
	}
	
	/**
	 * Comment out the property <code>er.coolcomponents.CCAjaxLongResponsePage.nextPageForErrorResultControllerClassName</code>
	 * in Properties to see the default error handling behavior.
	 *
	 * @return result of an error in a Callable task.
	 */
	public WOActionResults dispatchCallableWithSimulatedError() {
		T08CallableWithSimulatedError task = new T08CallableWithSimulatedError();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setTask(task);
		
		return nextPage;
	}
	
	/**
	 * Comment out the property <code>er.coolcomponents.CCAjaxLongResponsePage.nextPageForErrorResultControllerClassName</code>
	 * in Properties to see the default error handling behavior.
	 * 
	 * @return result of an error in a Runnable task.
	 */
	public WOActionResults dispatchRunnableWithSimulatedError() {
		T09RunnableWithSimulatedError task = new T09RunnableWithSimulatedError();
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setTask(task);
		
		return nextPage;
	}
}
