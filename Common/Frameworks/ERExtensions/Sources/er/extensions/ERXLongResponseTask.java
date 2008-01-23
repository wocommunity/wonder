/*
 * Created on 04.02.2004
 */
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

/**
 * Long response task interface and default implementation should take away the need to tie your
 * long running task directly to a component like with WOLongReponsePage. To use it, you should
 * subclass the default implementation, implement <code>performAction()</code>, drop an instance 
 * of ERXLongResponse on your page and bind it's <code>task</code> binding to it.
 * 
 * @author ak
 */
public interface ERXLongResponseTask extends Runnable {
	
	/** Sets the ERXLongResponse that pulls info from this task */
	public void setLongResponse(ERXLongResponse sender);
	
	/** @return true if the task is still running */
	public boolean isDone();

	/** Start the task. */
	public void start();
	
	/** Stop the task. */
	public void stop();
	
	/** @return next page according to inner status. */
	public WOComponent nextPage();
	
	/**
	 * Special worker thread that holds the reference to the task so we can 
	 * get a list of them.
	 *
	 * @author ak
	 */
	public static class WorkerThread extends Thread {
		
		protected ERXLongResponseTask _task;
		
		public WorkerThread(ERXLongResponseTask task) {
			super(task);
			_task = task;
		}
		
		public ERXLongResponseTask task() {
			return _task;
		}

		public void run() {
			try {
				super.run();
			} finally {
				_task = null;
			}
		}
		
		public static NSArray tasks() {
			NSMutableArray tasks = new NSMutableArray();
			Thread threads[] = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			
			for (int i = 0; i < threads.length; i++) {
				Thread thread = threads[i];
				if(thread instanceof WorkerThread) {
					ERXLongResponseTask task = ((WorkerThread)thread).task();
					if(task != null) {
						tasks.addObject(task);
					}
				}
			}
			return tasks.immutableClone();
		}
	}

	public abstract class DefaultImplementation implements Runnable, ERXLongResponseTask {
		
		
		/** logging support */
		public Logger log = Logger.getLogger(ERXUtilities.class);
		
		/** Refresh page that controls this task */
		protected ERXLongResponse _longResponse;
		
		/** Status code */
		protected Object _status;
		
		/** Result code */
		protected Object _result;
		
		/** Exception code */
		protected Exception _exception;
		
		/** Holds the cancel flag */
		protected boolean _cancelled;
		
		/** Holds the done flag */
		protected boolean _done;
		
		/** Hold the thread that performs the task */
		protected Thread _thread;

		/**
		 * Constructor
		 */
		public DefaultImplementation() {
			_finishInitialization();
			log = Logger.getLogger(getClass().getName());
			_thread = null;
		}
		
		/** 
		 * Sets up the object.
		 */
		protected void _finishInitialization() {
			if (!WOApplication.application().adaptorsDispatchRequestsConcurrently()) {
				throw new RuntimeException("<"+getClass().getName()+"> Cannot initialize because:\nThe application must be set to run with multiple threads to use this component. You must first increase the application's worker thread count to at least 1. You then have several options:\n1. If you set the count to 1, your code does not need to be thread safe.\n2. If you set the count above 1, and your code is not thread safe, disable concurrent request handling.\n3. you set the count above 1, and your code is thread safe, you can enable concurrent request handling.");
			}
			_status = null;
			_result = null;
			_done = false;
			_exception = null;
			_cancelled = false;
		}
		
		/**
		 * Sets the long response that controls this task.
		 */
		public void setLongResponse(ERXLongResponse sender) {
			_longResponse = sender;
		}
		
		/**
		 * Returns the long response for this task.
		 */
		public ERXLongResponse longResponse() {
			return _longResponse;
		}
		
		/**
		 * Implementation of the {@link Runnable} interface.
		 */
		public void run() {
		    WOApplication app = WOApplication.application();
		    
		    setResult(null);
		    
		    _done = false;
		    
		    log.debug("creating computation thread");
		    
		    // called to start new thread
		    try {
		        setResult(performAction());
		    } catch (Exception localException) {
		        setException(localException);
		        log.error("long response thread raised : "+localException.getMessage(), localException);
		    } finally {
			    ERXEC.unlockAllContextsForCurrentThread();
                _thread = null;
		    }
		    log.debug("exiting computation thread");
		    _done = true;
		}

		/**
		 * Returns a current status. This can be any object and will given 
		 * again to you to divine the next step.
		 */
		public Object status() {
			return _status;
		}

		protected void setStatus(Object anObject) {
			if (anObject != _status) {
				synchronized(this) {
					_status = anObject;
				}
			}
		}
		
		/**
		 * Returns the exception that may have occurred in the {@link run()} method.
		 */
		protected Exception exception() {
			return _exception;
		}

		/**
		 * Use this method to flag if an exception page should get displayed after 
		 * finishing the current step.
		 * @param anObject
		 */
		protected void setException(Exception anObject) {
			if (anObject != _exception) {
				synchronized(this) {
					_exception = anObject;
				}
			}
		}

		/**
		 * The abstract result object that has been returned by {@link performAction()}.
		 */
		protected Object result() {
			return _result;
		}

		/**
		 * Abstract result object that will get set when the task is finished.
		 * @param anObject
		 */
		protected void setResult(Object anObject) {
			if (anObject != _result) {
				synchronized(this) {
					_result = anObject;
				}
			}
		}

		/**
		 * Checks if the task was stopped externally.
		 * @return true if {@link stop()} was called.
		 */
		protected boolean isCancelled() {
			return _cancelled;
		}

		/**
		 *  (non-Javadoc)
		 * @see er.extensions.ERXLongResponseTask#isDone()
		 */
		public boolean isDone() {
			return _done;
		}

		/**
		 * Stops the task. This just sets the cancel flag. Its up to you
		 * to check in your task if you are interruptable. So you should
		 * check {@link isCancelled()} in your {@link performAction()}.
		 */
		public void stop() {
			synchronized(this) {
				_cancelled = true;
                _thread = null;
			}
		}

		/**
		 * Default implementation of the {@link ERXLongResponseTask.start()} method.
		 * Creates a new thread unless there already exists one.
		 */
		public void start() {
			try {
				if(_thread == null) {
					_thread = new WorkerThread(this);
                    _thread.setName(this.toString());
				}
				if(!_thread.isAlive()) {
					_thread.start();
				}
			} catch (Exception localException) {
				throw new NSForwardException(localException, "<ERXLongResponse> Exception occurred while creating long response thread: "+localException.toString());
			}
		}
		
		/**
		 * Override this to return an exception page suitable for the 
		 * given exception. This implementation just re-throws the exception.
		 * @param exception
		 * @return page for the exceptino
		 */
		protected WOComponent pageForException(Exception exception) {
			throw new NSForwardException(exception, "<WOLongResponsePage> Exception occurred in long response thread: "+exception.toString());
		}

		/**
		 * Override this to return and modify the refresh page.
		 * This is called while the task is still running. 
		 * Note that is the place where you can call {@link ERXLongResponse.setRefreshInterval(int)} to
		 * set the next refresh time.
		 * @param aStatus
		 */
		protected WOComponent refreshPageForStatus(Object aStatus)  {
			return longResponse().context().page();
		}

		/**
		 * Override this to return the page after the task was completed without 
		 * beeing stopped. Whether or not this counts as a success should
		 * be divined from the result object. This is the same object you 
		 * return after being asked for {@link result()}.
		 * @param aResult some result object
		 * @return result page for successfu completion
		 */
		protected WOComponent pageForResult(Object aResult)  {
			return longResponse().context().page();
		}

		/**
		 * Override this to return a sensible page to show after the
		 * task was stopped. The default implementation returns the refresh 
		 * component's top-level page
		 * @param aStatus some status object
		 * @return result page for the cancelled task.
		 */
		protected WOComponent cancelPageForStatus(Object aStatus)  {
			return refreshPageForStatus(aStatus);
		}
		
		/**
		 * Default implementation that controls the pages returned on each iteration.
		 */
		
		public WOComponent nextPage() {
			Exception e = exception();
			if (e != null) {
				return pageForException(e);
			}
			
			if (isDone()) {
				if (isCancelled()) {
					return cancelPageForStatus(status());
				} else {
					return pageForResult(result());
				}
			}
			return refreshPageForStatus(status());
		}

		/**
		 * You need to override this and perform your long running task.
		 * @return result of performing the action 
		 */
		public abstract Object performAction();

	}
}
