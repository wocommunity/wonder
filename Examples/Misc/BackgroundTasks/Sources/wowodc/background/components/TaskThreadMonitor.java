package wowodc.background.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.concurrency.ERXFutureTask;
import er.extensions.concurrency.ERXTaskInfo;
import er.extensions.concurrency.ERXTaskPercentComplete;
import er.extensions.concurrency.ERXTaskThread;
import er.extensions.concurrency.IERXStoppable;
import er.extensions.foundation.ERXStatusInterface;

/**
 * This stateless component is regenerated on each refresh with fresh statistics.
 *
 * @author kieran
 *
 */
public class TaskThreadMonitor extends WOComponent {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(TaskThreadMonitor.class);
	
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
		if (loopTaskItem().task() != null && loopTaskItem().task() instanceof ERXTaskPercentComplete) {
			result = ((ERXTaskPercentComplete)loopTaskItem().task()).percentComplete();
			if (result != null) {
				result = result * 100.0;
			} //~ if (result != null)
		} //~ if (loopTaskItem() != null && loopTaskItem() instanceof ERXTaskPercentComplete)
		return result;
	}

	public String taskStatus() {
		String result = null;
		if (loopTaskItem().task() instanceof ERXStatusInterface) {
			result = ((ERXStatusInterface)loopTaskItem().task()).status();
		} //~ if (loopTaskItem() instanceof ERXStatusInterface)
		return result;
	}

	public String taskDescription() {
		return loopTaskItem().task().toString();
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		// makes this component non-synchronizing
		return false;
	}

	@Override
	public boolean isStateless() {
		// makes this component stateless
		return true;
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