package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.woextensions.WOStatsPage;

import er.extensions.ERXStats.LogEntry;

public class ERXStatisticsPage extends WOStatsPage {
	private NSArray<ERXStats.LogEntry> _aggregateLogEntries;
	private ERXStats.LogEntry _aggregateLogEntry;

	public ERXStatisticsPage(WOContext context) {
		super(context);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		_aggregateLogEntries = null;
	}

	public NSArray<LogEntry> aggregateLogEntries() {
		if (_aggregateLogEntries == null) {
			_aggregateLogEntries = ERXStats.aggregateLogEntries();
			// AK: should be stored in session...
			String key = context().request().stringFormValueForKey("sort");
			if(key == null) {
				key = "avg";
			}
			_aggregateLogEntries = (NSArray<LogEntry>) _aggregateLogEntries.valueForKeyPath("@sortDesc."+key);
		}
		return _aggregateLogEntries;
	}

	public void setAggregateLogEntry(ERXStats.LogEntry aggregateLogEntry) {
		_aggregateLogEntry = aggregateLogEntry;
	}

	public ERXStats.LogEntry aggregateLogEntry() {
		return _aggregateLogEntry;
	}
	
	public WOActionResults resetStats() {
		ERXStats.reset();
		return this;
	}
}