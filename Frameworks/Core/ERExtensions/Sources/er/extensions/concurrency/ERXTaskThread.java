package er.extensions.concurrency;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStopWatch;

/**
 * This is the custom {@link Thread} subclass that is used for running background tasks.
 * This {@link Thread} subclass is automatically created by the {@link ERXTaskThreadFactory}
 * which in turn is used by the {@link ERXTaskThreadPoolExecutor}
 * 
 * The purpose of this subclass is 
 * <ul>
 * <li> to identify threads that were created by {@link ERXTaskThreadFactory}
 * using instanceof when enumerating all threads
 * <li> to store a reference to the task itself in this Thread subclass while the task is executing
 * making it easy to get a reference to tasks for application monitoring
 * <li> provide related static utility methods to find all tasks or tasks of a certain class that are currently running and that were
 * created by {@link ERXTaskThreadFactory}
 * </ul>
 * 
 * <p>
 * A user does not generally need to instantiate this class. This class is generally used by {@link ExecutorService} instances
 * that are created by {@link ERXExecutorService} static utility methods.
 * </p>
 * 
 * @author kieran
 *
 * @see ERXTaskThreadPoolExecutor
 * @see ERXExecutorService
 * @see ERXTaskThreadFactory
 */
public class ERXTaskThread extends Thread {
	private static final Logger log = LoggerFactory.getLogger(ERXTaskThread.class);
	
	public ERXTaskThread(Runnable target) {
		super(target);
	}

	private Runnable _task;
	private ERXStopWatch _stopWatch;

	/** @return the current task being executed */
	public Runnable task() {
		return _task;
	}

	/** 
	 * @param task the current task being executed 
	 * 
	 * TODO: Check if the Runnable is a Future wrapping the real task and unwrap it. */
	public void setTask(Runnable task){
		_task = task;
	}

	/**
	 * @return NSArray of background tasks
	 */
	public static NSArray tasks() {
		NSMutableArray  tasks = new NSMutableArray();

		Thread threads[] = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);

		for (int i = 0; i < threads.length; i++) {
			Thread thread = threads[i];
			if (thread instanceof ERXTaskThread) {
				Runnable task = ((ERXTaskThread)thread).task();
				if (task != null) {
					tasks.add(task);
				} //~ if (task != null)
			} //~ if (thread instanceof ERXTaskThread)
		}
		return tasks.immutableClone();
	}

	/**
	 * @return NSArray of {@link ERXTaskInfo}
	 */
	public static NSArray taskInfos() {
		NSMutableArray  taskInfos = new NSMutableArray();

		Thread threads[] = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);

		for (int i = 0; i < threads.length; i++) {
			Thread thread = threads[i];
			if (thread instanceof ERXTaskThread) {
				Runnable task = ((ERXTaskThread)thread).task();
				if (task != null) {
					String elapsedTime = ((ERXTaskThread)thread).elapsedTime();
					ERXTaskInfo info = new ERXTaskInfo(task, elapsedTime);
					taskInfos.add(info);
				} //~ if (task != null)
			} //~ if (thread instanceof ERXTaskThread)
		}
		return taskInfos.immutableClone();
	}

	@SuppressWarnings("unchecked")
	public static <T> NSArray<T> taskForTaskClass(Class<T> clazz) {
		NSArray<ERXTaskInfo> taskInfos = taskInfos();
		NSMutableArray<T> tasks = new NSMutableArray<>();
		for (ERXTaskInfo taskInfo : taskInfos) {
			Object r = taskInfo.task();
			log.debug("ERXTaskThread.taskForTaskClass(): r = {}", r);
			if (clazz.isInstance(r)) {
				tasks.add((T)r);
			}
		}
		return tasks.immutableClone();
	}

	public void startStopWatch() {
		_stopWatch = new ERXStopWatch();
		_stopWatch.start();
	}

	public String elapsedTime() {
		return (_stopWatch == null ? null : _stopWatch.toString());
	}

	public void stopStopWatch() {
		if (_stopWatch != null) {
			_stopWatch.stop();
		} //~ if (_stopWatch != null)
	}
}
