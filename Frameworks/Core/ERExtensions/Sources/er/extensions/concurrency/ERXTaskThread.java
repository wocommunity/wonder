package er.extensions.concurrency;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXStopWatch;

/**
 * This is the custom {@link Thread} subclass that is used for running background tasks.
 * This {@link Thread} subclass is automatically created by the {@link ERXTaskThreadFactory}
 * which in turn is used by the {@link ERXTaskThreadPoolExecutor}
 * 
 * This {@link Thread} subclass allows me to get status info when I enumerate threads in the application.
 * 
 * @author kieran
 *
 */
public class ERXTaskThread extends Thread {
	public ERXTaskThread(Runnable target) {
		super(target);
	}

	private Runnable _task;
	private ERXStopWatch _stopWatch;

	/** @return the current task being executed */
	public Runnable task() {
		return _task;
	}

	/** @param task the current task being executed */
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
		NSMutableArray<T> tasks = new NSMutableArray<T>();
		for (ERXTaskInfo taskInfo : taskInfos) {
			Object r = taskInfo.task();
			System.err.println("ERXTaskThread.taskForTaskClass(): r = " + r.toString());
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
