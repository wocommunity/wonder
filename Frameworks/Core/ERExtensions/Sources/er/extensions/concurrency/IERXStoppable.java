package er.extensions.concurrency;

/**
 * Interface that identifies an object as stoppable. Useful for long running tasks.
 * Note that this is <em>not</em> named IERXCancellable since canceling a task implies
 * that data is unaffected, whereas stopping something may indicate that the task has been partially
 * completed.
 * 
 * Either way, classes that implement this interface should stop gracefully and not leave the object graph in
 * a completely unstable state. For example if a task is processing 100 objects, then stopping it while it is working
 * on object #51 should not result in item #51 being left in a semi-processed state. Ideally, the end result of stopping
 * should be that either 50 or 51 items are completely processed and others are not processed at all.
 * 
 * Typically implementations use this method to set a boolean flag, which is then checked at the start of the loop before
 * processing the next item.
 * 
 * @author kieran
 *
 */
public interface IERXStoppable {

	/**
	 * Gracefully stops this task. Work that has already completed is left in completed state and work that is yet to be done is not started.
	 */
	public void stop();

}