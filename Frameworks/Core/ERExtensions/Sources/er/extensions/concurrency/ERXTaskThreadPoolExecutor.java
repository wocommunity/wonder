package er.extensions.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.eof.ERXEC;

/**
 * This is a custom {@link ThreadPoolExecutor} subclass whose purpose in life is
 * <ul>
 * <li>to ensure that we initialize {@link ERXTaskThread} status before task execution and reset status after execution,
 * <li>use ERXFutureTask subclass of {@link FutureTask} so we have a reference to the wrapped task.
 * <li>tell ERXEC to unlock all editing contexts in the background thread at the end of task execution.
 * </ul>
 * 
 * <p>
 * This is accomplished by overriding the protected hook methods {@link ThreadPoolExecutor#beforeExecute(Thread t, Runnable r)}
 * and {@link ThreadPoolExecutor#afterExecute(Runnable r, Throwable t)}, and also the submit methods.
 * </p>
 * 
 * <p>
 * A user does not generally need to instantiate this class. This class is generally used by {@link ExecutorService} instances
 * that are created by {@link ERXExecutorService} static utility methods.
 * </p>
 * 
 * @see ERXExecutorService
 * @see ERXTaskThreadFactory
 * @see ERXTaskThread
 * 
 * @author kieran
 */
public class ERXTaskThreadPoolExecutor extends ThreadPoolExecutor {
	private static final Logger log = LoggerFactory.getLogger(ERXTaskThreadPoolExecutor.class);

	public ERXTaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
					BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public ERXTaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
					BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public ERXTaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
					BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public ERXTaskThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
					BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}
	
    @Override
	public Future<?> submit(Runnable task) {
        if (task == null) throw new NullPointerException();
        ERXFutureTask<Object> ftask = new ERXFutureTask<>(task, null);
        execute(ftask);
        return ftask;
    }

    @Override
	public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException();
        ERXFutureTask<T> ftask = new ERXFutureTask<>(task, result);
        execute(ftask);
        return ftask;
    }

    @Override
	public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException();
        ERXFutureTask<T> ftask = new ERXFutureTask<>(task);
        execute(ftask);
        return ftask;
    }

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		// Store reference to the task
		if (t instanceof ERXTaskThread) {
			((ERXTaskThread)t).setTask(r);
			((ERXTaskThread)t).startStopWatch();
			log.debug("About to execute {} in thread {}", r, t);
		}

		if (r instanceof IERXExecutionStateTransition) {
			((IERXExecutionStateTransition)r).beforeExecute();
		} //~ if (r instanceof IERXExecutionStateTransition)

		super.beforeExecute(t, r);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		// Clear reference to the task
		if (Thread.currentThread() instanceof ERXTaskThread) {
			ERXTaskThread thread = (ERXTaskThread)Thread.currentThread();
			thread.setTask(null);
			thread.stopStopWatch();
			if (log.isDebugEnabled()) {
				String elapsedTime = thread.elapsedTime();
				log.debug("Finished executing {} after {}", r, elapsedTime);
			}
		}

		if (r instanceof IERXExecutionStateTransition) {
			((IERXExecutionStateTransition)r).afterExecute();
		} //~ if (r instanceof IERXExecutionStateTransition)
		
		if (shouldUnlockContexts(r)) {
			// Safety net to unlock any locked EC's at the end of this task's operation in this thread
			ERXEC.unlockAllContextsForCurrentThread();
		}
	}
	
	private boolean shouldUnlockContexts(Runnable r) {
		if (r instanceof ERXFutureTask) {
			Object task = ((ERXFutureTask)r).task();
			if (task instanceof ERXTask || task instanceof ERXTimerTask) {
				// these two classes already call ERXApplication._endRequest() at the end
				return false;
			}
		}
		return true;
	}
}
