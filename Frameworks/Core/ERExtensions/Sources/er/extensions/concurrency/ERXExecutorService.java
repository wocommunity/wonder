//
//  WKExecutorService.java
//  cheetah
//
//  Created by Kieran Kelleher on 4/13/06.
//  Copyright 2006 Kieran Kelleher. All rights reserved.
//

package er.extensions.concurrency;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * A simple class that provides a single general purpose executor service for a
 * single application. Resource efficient.
 *
 * Implements custom Thread and ThreadPoolExecutor subclasses that cooperate to
 * maintain reference to currently executing task while executing and
 * to ensure locked editing contexts are unlocked at the end of a task.
 * 
 *
 */
public class ERXExecutorService {
	private static final ExecutorService _executorService = new ERXTaskThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
					TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ERXTaskThreadFactory());

	/** @return a global ExecutorService with no limit for executing runnables. */
	public static ExecutorService executorService() {
		return _executorService;
	}
}
