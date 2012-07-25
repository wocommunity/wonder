package er.extensions.statistics;

import java.text.Format;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.woextensions.WOStatsPage;

import er.extensions.formatters.ERXUnitAwareDecimalFormat;
import er.extensions.statistics.ERXStats.LogEntry;

/** Provides more and better functionality than the default WOStatsPage.
 * <p>As with WOStatsPage, you must set the WOStatisticsPassword property on launch.</p>
 */
public class ERXStatisticsPage extends WOStatsPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
	
	public Format byteFormat() {
		return new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.BYTE);
	}
	
	public Format timeFormat() {
		return new ERXUnitAwareDecimalFormat(ERXUnitAwareDecimalFormat.SECOND);
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
