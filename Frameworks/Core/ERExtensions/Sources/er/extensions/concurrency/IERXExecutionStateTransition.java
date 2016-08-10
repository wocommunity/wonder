package er.extensions.concurrency;

/**
 * Intended as an interface to be implemented by Runnables or Callables so that they have the opportunity
 * to setup and clear thread state when the task is executed by a {@link ERXTaskThreadPoolExecutor}
 *
 * @author kieran
 */
public interface IERXExecutionStateTransition {

	/**
	 * Method invoked prior to executing the given Runnable/Callable.
	 */
	public void beforeExecute();

	/**
	 * Method invoked upon completion of execution of the given Runnable/Callable.
	 */
	public void afterExecute();
}
