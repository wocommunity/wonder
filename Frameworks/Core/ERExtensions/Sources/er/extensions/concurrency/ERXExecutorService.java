//
//  WKExecutorService.java
//  cheetah
//
//  Created by Kieran Kelleher on 4/13/06.
//  Copyright 2006 Kieran Kelleher. All rights reserved.
//

package er.extensions.concurrency;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A simple class that provides a resource-efficient WebObjects-friendly {@link ExecutorService} for a
 * single application. ExecutorService instances are used for running tasks in asynchronous threads. 
 * Access the shared instance with:
 * <p>
 * <pre>
 * ExecutorService es = ERXExecutorService.executorService();
 * </pre>
 * 
 * <p>
 * This class also provides a factory method to create a fixed size thread pool ExecutorService 
 * that rejects tasks when all threads are busy. This can be useful for parallel processing
 * tasks.
 * </p>
 * 
 * <p>
 * Implements custom Thread and ThreadPoolExecutor subclasses that cooperate to
 * maintain reference to currently executing task while executing and
 * to ensure locked editing contexts are unlocked at the end of a task.
 * </p>
 * 
 * @see ERXTaskThreadPoolExecutor
 * @see ERXTaskThreadFactory
 * @see ERXTaskThread
 * @see ERXExecutorService
 * 
 */
public class ERXExecutorService {
	private static final ExecutorService _executorService = new ERXTaskThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
					TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ERXTaskThreadFactory());

	/** 
	 * @return a global ExecutorService with no limit for executing runnables. 
	 **/
	public static ExecutorService executorService() {
		return _executorService;
	}

	/**
	 * This ExecutorService is useful when you want to execute tasks in parallel, but you want to (create and)
	 * submit new tasks for execution only when the pool has at least one idle thread.
	 * 
	 * A task will be rejected with a {@link RejectedExecutionException} when all the threads are busy
	 * running other tasks.
	 * 
	 * Thus, you can fill the pool with tasks and use a try/catch/wait loop for submitting additional tasks.
	 * 
	 * Idle threads will be terminated if idle for more than 30 seconds.
	 * 
	 * @param nThreads
	 * @return a fixed-size thread pool that only accepts tasks when a thread is idle.
	 */
	public static ExecutorService newFiniteThreadPool(int nThreads) {
		return new ERXTaskThreadPoolExecutor(
				0,
				nThreads,
				30L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new ERXTaskThreadFactory());
	}
}
