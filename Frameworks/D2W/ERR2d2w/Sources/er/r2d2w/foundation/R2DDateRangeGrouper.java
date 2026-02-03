package er.r2d2w.foundation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.components.ERXDateGrouper;
import er.extensions.foundation.ERXTimestampUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class R2DDateRangeGrouper extends ERXDateGrouper {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public enum CalendarView { 
		DAY("dayButtonLabel"){
			public void nextCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.nextDay(); }
			public void previousCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.previousDay(); }
		}, 
		WEEK("weekButtonLabel") {
			public void nextCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.nextWeek(); }
			public void previousCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.previousWeek(); }
			
		}, 
		MONTH("monthButtonLabel") {
			public void nextCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.nextMonth(); }
			public void previousCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.previousMonth(); }			
		}, 
		YEAR("yearButtonLabel") {
			public void nextCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.nextYear(); }
			public void previousCalendar(R2DDateRangeGrouper rangeGrouper) { rangeGrouper.previousYear(); }
		};
		private final String locKey;
		CalendarView(String locKey) {
			this.locKey = locKey;
		}
		public String localizationKey() { return locKey; }
		public abstract void nextCalendar(R2DDateRangeGrouper rangeGrouper);
		public abstract void previousCalendar(R2DDateRangeGrouper rangeGrouper);
	}
	
	protected CalendarView _calendarView = CalendarView.MONTH;
	protected TimeZone _currentTimeZone = TimeZone.getDefault();
	protected String _durationKeyPath;
	
	/** logging support */
    private static Logger log = LoggerFactory.getLogger(R2DDateRangeGrouper.class);
    
    protected NSDictionary _groupedObjects() {
    	if(_groupedObjects == null) {
    		log.debug("_groupedObjects == null: Building new dictionary");
    		NSMutableDictionary<Object, NSMutableArray<Object>> groupedObjects = new NSMutableDictionary<Object, NSMutableArray<Object>>();
    		
    		for(Object o : allObjects()) {
    			Object startDate = NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, dateKeyPath());
    			
    			// If date is null, the object is not grouped.
    			if(!ERXValueUtilities.isNull(startDate)) {
    				NSTimestamp date = (NSTimestamp)startDate;
    				Object duration = NSKeyValueCodingAdditions.Utility.valueForKeyPath(o, durationKeyPath());
    				
    				// Create a range from the date and duration
    				R2DDateRange rangeValue = null;
    				if(ERXValueUtilities.isNull(duration)) {
    					rangeValue = new R2DDateRange(date, 0);
    				} else if(duration instanceof Number) {
    					rangeValue = new R2DDateRange(date, ((Number)duration).longValue());
    				} else if(duration instanceof NSTimestamp) {
    					NSTimestamp endDate = (NSTimestamp)duration;
    					rangeValue = new R2DDateRange(date, endDate);
    				} else {
    					throw new IllegalArgumentException("Value for durationKeyPath [" + durationKeyPath() + "] is not a Number or NSTimestamp. Value = " + duration);
    				}
    				
    				// Create groups with range value
    				date = _dateToCurrentZone(date);
    				R2DDateRange rangeKey = (R2DDateRange)_groupingKeyForDate(date);
    				while(rangeKey.intersectsRange(rangeValue) || rangeKey.containsMoment(rangeValue.date())) {
    					NSMutableArray<Object> existingGroup = groupedObjects.objectForKey(rangeKey);
    					if(existingGroup == null) {
    						existingGroup = new NSMutableArray<Object>();
    						groupedObjects.setObjectForKey(existingGroup, rangeKey);
    					}
    					existingGroup.addObject(o);
    					date = _nextDateForGroupingMode(date);
    					rangeKey = (R2DDateRange)_groupingKeyForDate(date);
    				}
    			}
    		}
    		_groupedObjects = groupedObjects;
    	}
    	return _groupedObjects;
    }
    
    protected Object _groupingKeyForDate(NSTimestamp date) {
		NSTimestamp keyDate = null;
    	switch(groupingMode()) {
    	case DAY:
    		keyDate = _firstDateInSameDay(date);
    		break;
    	case WEEK:
    		keyDate = _firstDateInSameWeek(date); 
    		break;
    		
    	case MONTH:
    		keyDate = _firstDateInSameMonth(date); 
    		break;
    		
    	case YEAR:
    		keyDate = _firstDateInSameYear(date); 
    		break;
    	default:
    		throw new IllegalStateException("Unknown grouping mode set on date grouper");
    	}
    	keyDate = _dateFromCurrentZone(keyDate);
    	return new R2DDateRange(keyDate, _nextDateForGroupingMode(keyDate));
    }
    
    protected NSTimestamp _nextDateForGroupingMode(NSTimestamp date) {
    	NSTimestamp result = null;
    	switch(groupingMode()) {
    	case DAY:
    		result = date.timestampByAddingGregorianUnits(0, 0, 1, 0, 0, 0);
    		break;
    	case WEEK:
    		result = date.timestampByAddingGregorianUnits(0, 0, 7, 0, 0, 0);
    		break;
    	case MONTH:
    		result = date.timestampByAddingGregorianUnits(0, 1, 0, 0, 0, 0);
    		break;
    	case YEAR:
    		result = date.timestampByAddingGregorianUnits(1, 0, 0, 0, 0, 0);
    		break;
    	default:
    		throw new IllegalStateException("Unknown grouping mode set on date grouper");
    	}
    	return result;
    }
    
    public NSTimestamp _firstDateInSameYear(NSTimestamp value) {
    	NSTimestamp result = _dateForDayInYear(ERXTimestampUtilities.yearOfCommonEra(value), 1);
    	return result;
    }
    
    public NSTimestamp _firstDateInSameDay(NSTimestamp value) {
    	NSTimestamp result = _dateForDayInYear(ERXTimestampUtilities.yearOfCommonEra(value), ERXTimestampUtilities.dayOfYear(value));
    	return result;
    }
    
	public CalendarView calendarView() {
		return _calendarView;
	}

	public void setCalendarView(CalendarView calendarView) {
		_calendarView = calendarView;
	}
	
	public TimeZone currentTimeZone() {
		return _currentTimeZone;
	}
	
	public void setCurrentTimeZone(TimeZone timeZone) {
		if(timeZone == null) { throw new NullPointerException("TimeZone must not be null!"); }
		_currentTimeZone = timeZone;
		_groupedObjects = null;
	}
	
	/**
	 * Overridden to adjust set date to current time zone
	 */
	public void setCurrentDate(NSTimestamp date) {
		if(date!=null) {
			date = _dateFromCurrentZone(date);
		}
		super.setCurrentDate(date);
	}

	/**
	 * Overridden to adjust for current time zone
	 */
    public boolean isToday() {
    	NSTimestamp today = _dateToCurrentZone(today());
        return ERXTimestampUtilities.differenceByDay(today, currentDate()) == 0;
    }
    
    /**
     * Overridden to adjust returned dates to current time zone.
     */
    public NSArray<NSArray<NSTimestamp>> datesForWeeksForCurrentMonth() {
    	NSArray<NSArray<NSTimestamp>> dates = super.datesForWeeksForCurrentMonth();
    	NSMutableArray<NSArray<NSTimestamp>> result = new NSMutableArray<NSArray<NSTimestamp>>();
    	for(NSArray<NSTimestamp> weeks : dates) {
        	NSMutableArray<NSTimestamp> week = new NSMutableArray<NSTimestamp>();
    		for(NSTimestamp day : weeks) {
    			day = _dateToCurrentZone(day);
    			week.addObject(day);
    		}
    		result.addObject(week.immutableClone());
    	}
    	return result.immutableClone();
    }

    public String durationKeyPath() {
		return _durationKeyPath;
	}

	public void setDurationKeyPath(String durationKeyPath) {
		_groupedObjects = null;
		_durationKeyPath = durationKeyPath;
	}
	
	/**
	 * Adjusts a date from default to the current time zone.
	 * @param date
	 * @return
	 */
	protected NSTimestamp _dateToCurrentZone(NSTimestamp date) {
		date = new NSTimestamp(date.getTime(), currentTimeZone());
		return date;
	}
	
	/**
	 * Adjusts a date from the current time zone to default.
	 * @param date
	 * @return
	 */
	protected NSTimestamp _dateFromCurrentZone(NSTimestamp date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss:SSS");
		String dateString = sdf.format(date);
		sdf.setTimeZone(currentTimeZone());
		try {
			date = new NSTimestamp(sdf.parse(dateString));
		} catch (ParseException pe) {
			throw NSForwardException._runtimeExceptionForThrowable(pe);
		}
		return date;
	}
}
