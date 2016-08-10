package wowodc.background.components;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import wowodc.eof.TaskInfo;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;

import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXSortOrdering.ERXSortOrderings;
import er.extensions.formatters.ERXTimeDurationFormatter;

public class TaskInfoList extends WOComponent {
    public TaskInfoList(WOContext context) {
        super(context);
    }
    
    public TaskInfo item;
    
    private ERXDisplayGroup<TaskInfo> _dg;
	
	/** @return The display group */
	public ERXDisplayGroup<TaskInfo> dg() {
		if ( _dg == null ) {
			// Show most recent first.
			EOSortOrdering pkOrder = new EOSortOrdering("id", EOSortOrdering.CompareDescending);
			ERXFetchSpecification<TaskInfo> fs = new ERXFetchSpecification<TaskInfo>(TaskInfo.ENTITY_NAME, null, new ERXSortOrderings(pkOrder));
			
			EOEditingContext ec = ERXEC.newEditingContext();
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, TaskInfo.ENTITY_NAME);
			ds.setFetchSpecification(fs);
			
			_dg = new ERXDisplayGroup<TaskInfo>();
			_dg.setDataSource(ds);
			
			_dg.setNumberOfObjectsPerBatch(10);
			_dg.setCurrentBatchIndex(1);
			_dg.setSelectsFirstObjectAfterFetch(false);
			_dg.fetch();
		}
		return _dg;
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
}
