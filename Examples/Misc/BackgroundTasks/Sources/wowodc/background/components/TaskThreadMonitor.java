package wowodc.background.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.concurrency.ERXFutureTask;
import er.extensions.concurrency.ERXTaskInfo;
import er.extensions.concurrency.ERXTaskThread;
import er.extensions.concurrency.IERXPercentComplete;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.foundation.IERXStatus;

/**
 * This stateless component is regenerated on each refresh with fresh statistics.
 *
 * @author kieran
 */
public class TaskThreadMonitor extends ERXStatelessComponent {
    public TaskThreadMonitor(WOContext context) {
        super(context);
    }
  

    private NSArray<ERXTaskInfo> _tasks;

	/** @return the current tasks */
	public NSArray<ERXTaskInfo> tasks() {
		if ( _tasks == null ) {
			// Grab all tasks that are instances of ERXTaskThread
			_tasks = ERXTaskThread.taskInfos();
		}
		return _tasks;
	}

	private ERXTaskInfo _loopTaskItem;

	/** @return the loop task item */
	public ERXTaskInfo loopTaskItem() {
		return _loopTaskItem;
	}

	/** @param loopTaskItem the loop task item */
	public void setLoopTaskItem(ERXTaskInfo loopTaskItem){
		_loopTaskItem = loopTaskItem;
	}

	public Double taskPercentageComplete() {
		Double result = null;
		if (loopTaskItem().task() != null && loopTaskItem().task() instanceof IERXPercentComplete) {
			result = ((IERXPercentComplete) loopTaskItem().task()).percentComplete();
			if (result != null) {
				result = result * 100.0;
			} //~ if (result != null)
		} //~ if (loopTaskItem() != null && loopTaskItem() instanceof IERXPercentComplete)
		return result;
	}

	public String taskStatus() {
		String result = null;
		if (loopTaskItem().task() instanceof IERXStatus) {
			result = ((IERXStatus) loopTaskItem().task()).status();
		} //~ if (loopTaskItem() instanceof IERXStatus)
		return result;
	}

	public String taskDescription() {
		return loopTaskItem().task().toString();
	}

	@Override
	public void reset() {
		super.reset();
		_loopTaskItem = null;
		_tasks = null;
	}

	public boolean showCancel() {
		boolean show = false;
		ERXTaskInfo taskInfo = loopTaskItem();
		Object task = taskInfo.task();
		if (task instanceof ERXFutureTask) {
			show = ((ERXFutureTask)task).isStoppable();
		} else {
			show = task instanceof IERXStoppable;
		}
		return show;
	}

	public WOActionResults stopTask() {
		IERXStoppable task = loopStopTask();
		if (task != null) {
			task.stop();
		} //~ if (task != null)
		return null;
	}
	
	/**
	 * @return the current task if it implements {@link IERXStoppable}, otherwise returns null.
	 */
	private IERXStoppable loopStopTask() {
		IERXStoppable iERXStop = null;
		ERXTaskInfo info = loopTaskItem();
		Object task = info.task();

		if (task instanceof ERXFutureTask) {
			task = ((ERXFutureTask)task).task();
		}
		
		if (task instanceof IERXStoppable) {
			iERXStop = (IERXStoppable) task;
		}

		return iERXStop;
	}
}
