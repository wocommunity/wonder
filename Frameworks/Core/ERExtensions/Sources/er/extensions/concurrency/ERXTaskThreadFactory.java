package er.extensions.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * This is the custom {@link ThreadFactory} subclass that creates instances of {@link ERXTaskThread}
 * and which is used by {@link ERXTaskThreadPoolExecutor} instances that are returned by {@link ERXExecutorService}
 * 
 * <p>
 * A user does not generally need to instantiate this class. This class is generally used by {@link ExecutorService} instances
 * that are created by {@link ERXExecutorService} static utility methods.
 * </p>
 *
 * @see ERXTaskThreadPoolExecutor
 * @see ERXExecutorService
 * @see ERXTaskThread
 * 
 * @author kieran
 *
 */
public class ERXTaskThreadFactory implements ThreadFactory {

	public Thread newThread(Runnable r) {
		return new ERXTaskThread(r);
	}

}
