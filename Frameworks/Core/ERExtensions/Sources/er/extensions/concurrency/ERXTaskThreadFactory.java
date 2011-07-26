package er.extensions.concurrency;

import java.util.concurrent.ThreadFactory;

/**
 * This is the custom {@link ThreadFactory} subclass that creates instances of {@link ERXTaskThread}
 *
 * @see ERXTaskThread
 * @author kieran
 *
 */
public class ERXTaskThreadFactory implements ThreadFactory {

	public Thread newThread(Runnable r) {
		return new ERXTaskThread(r);
	}

}
