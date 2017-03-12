package wowodc.background.components;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import wowodc.background.tasks.T06EOFFactorialUpdateTask;
import wowodc.eof.TaskInfo;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.coolcomponents.CCAjaxLongResponsePage;
import er.extensions.appserver.IERXRefreshPage;
import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.formatters.ERXTimeDurationFormatter;

public class TaskInfoPage extends ERXComponent  implements IERXRefreshPage {
    public TaskInfoPage(WOContext context) {
        super(context);
    }
    
    private TaskInfo _taskInfo;
	
	/** @return taskInfo whose detail is displayed on this page */
	public TaskInfo taskInfo() {
		return _taskInfo;
	}
	
	/** @param taskInfo taskInfo whose detail is displayed on this page */
	public void setTaskInfo(TaskInfo taskInfo){
		_taskInfo = taskInfo;
	}
	
	private ERXTimeDurationFormatter _durationFormatter;
	
	/** @return a human-readable formatter for time durations */
	public ERXTimeDurationFormatter durationFormatter() {
		if ( _durationFormatter == null ) {
			_durationFormatter = new ERXTimeDurationFormatter(TimeUnit.MILLISECONDS);
		}
		return _durationFormatter;
	}
	
	private DateFormat _timeFormatter;
	
	/** @return time formatter */
	public DateFormat timeFormatter() {
		if ( _timeFormatter == null ) {
			_timeFormatter = DateFormat.getDateTimeInstance();
		}
		return _timeFormatter;
	}
	
	private NumberFormat _wholeNumberFormatter;
	
	/** @return whole number formatter */
	public NumberFormat wholeNumberFormatter() {
		if ( _wholeNumberFormatter == null ) {
			_wholeNumberFormatter = new DecimalFormat("#,##0");
		}
		return _wholeNumberFormatter;
	}
	
	/* (non-Javadoc)
	 * @see er.extensions.appserver.IERXRefreshPage#refresh()
	 */
	public void refresh() {
		ERXEOControlUtilities.refreshObject(taskInfo());
	}
	
	public WOActionResults processFactorials() {
		T06EOFFactorialUpdateTask task = new T06EOFFactorialUpdateTask(taskInfo());
		
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		nextPage.setTask(task);
		return nextPage;
	}
}
