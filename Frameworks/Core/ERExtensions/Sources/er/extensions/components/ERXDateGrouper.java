package er.extensions.components;

import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXTimestampUtilities;

/**
 * Works much the same as a {@link WODisplayGroup}.
 * See {@link ERXMonthView} for an example on how to use it.
 *
 * @author ak on Mon Nov 04 2002
 */

public class ERXDateGrouper extends WODisplayGroup {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static Logger log = Logger.getLogger(ERXDateGrouper.class);

    public static final int DAY = Calendar.DAY_OF_YEAR;
    public static final int MONTH = Calendar.MONTH;
    public static final int WEEK = Calendar.WEEK_OF_YEAR;
    public static final int YEAR = Calendar.YEAR;

    static NSMutableDictionary cachedDays = new NSMutableDictionary();
    static int currentYear = ERXTimestampUtilities.yearOfCommonEra(new NSTimestamp());

    protected NSArray _objects;
    protected String _dateKeyPath;
    protected NSTimestamp _selectedDate = new NSTimestamp();
    protected NSTimestamp _currentDate;
    protected NSTimestamp _firstDateInSameMonth;
    protected NSTimestamp _firstDateInNextMonth;
    protected NSTimestamp _today = new NSTimestamp();

    protected int _currentMonth;
    protected int _groupingMode = DAY;
    protected int _currentWeek;
    protected int _currentDayOfMonth;
    protected int _currentDayOfWeek;
    protected NSArray _datesForCurrentWeek;
    protected NSMutableDictionary _groupedObjects;
    protected NSMutableArray _datesForWeeksForCurrentMonth;
    protected boolean _weekStartsMonday = false;

    public boolean weekStartsMonday() {
        return _weekStartsMonday;
    }
    public void setWeekStartsMonday(boolean value) {
        _reset();
        _weekStartsMonday = value;
    }

    public boolean hasNoObjectsForCurrentDate() {
        return displayedObjects().count() == 0;
    }
    public boolean isToday() {
        return ERXTimestampUtilities.differenceByDay(today(), currentDate()) == 0;
    }
    public boolean isSelectedDate() {
        return ERXTimestampUtilities.differenceByDay(selectedDate(), currentDate()) == 0;
    }
    public boolean isInMonth() {
        return ERXTimestampUtilities.differenceByDay(firstDateInSameMonth(), currentDate()) >= 0 && ERXTimestampUtilities.differenceByDay(firstDateInNextMonth(), currentDate()) < 0;
    }
    public String dateKeyPath() { return _dateKeyPath; }
    public void setDateKeyPath(String value) {
        _groupedObjects = null;
        _dateKeyPath = value;
    }
    public int groupingMode() { return _groupingMode; }
    public void setGroupingMode(int value) {
        _groupingMode = value;
        _reset();
    }
    @Override
    public NSArray allObjects() {
        return super.allObjects();
    }
    @Override
    public void setObjectArray(NSArray value) {
        _groupedObjects = null;
        _reset();
        super.setObjectArray(value);
    }
    @Override
    public void setDataSource(EODataSource value) {
        _groupedObjects = null;
        _reset();
        super.setDataSource(value);
    }
    public NSTimestamp today() {
        return _today;
    }
    protected void _reset() {
        _firstDateInSameMonth = null;
        _firstDateInNextMonth = null;
        _datesForWeeksForCurrentMonth = null;
    }
    public NSTimestamp selectedDate() { return _selectedDate == null ? today() : _selectedDate; }
    public void setSelectedDate(Date date) {
    	NSTimestamp value = (date instanceof NSTimestamp)?(NSTimestamp)date:new NSTimestamp(date);
        _reset();
        _selectedDate = value;
    }
    public NSTimestamp currentDate() { return _currentDate == null ? selectedDate() : _currentDate; }
    public void setCurrentDate(NSTimestamp value) {
        _currentDate = value;
    }

    protected Object _groupingKeyForDate(NSTimestamp date) {
        int value = 0;
        switch(groupingMode()) {
            case DAY:
                value = ERXTimestampUtilities.dayOfCommonEra(date) - currentYear * 365;
                break;
            case MONTH:
                value = ERXTimestampUtilities.yearOfCommonEra(date) * 12 + ERXTimestampUtilities.monthOfYear(date) - currentYear * 12;
                break;
            case WEEK:
                value = ERXTimestampUtilities.yearOfCommonEra(date) * 53 + ERXTimestampUtilities.monthOfYear(date) - currentYear * 53;
                break;
            case YEAR:
                value = ERXTimestampUtilities.yearOfCommonEra(date) - currentYear;
                break;
        }
        return ERXConstant.integerForInt(value);
    }

    protected NSDictionary _groupedObjects() {
        if(_groupedObjects == null) {
            _groupedObjects = new NSMutableDictionary();
            for (Enumeration e = allObjects().objectEnumerator(); e.hasMoreElements();) {
                Object eo = e.nextElement();
                Object date = NSKeyValueCodingAdditions.Utility.valueForKeyPath(eo, dateKeyPath());
                boolean isNullKey = date == null || date instanceof NSKeyValueCoding.Null;
                if (!isNullKey) {
                    Object key = _groupingKeyForDate((NSTimestamp)date);
                    NSMutableArray existingGroup = (NSMutableArray)_groupedObjects.objectForKey(key);
                    if (existingGroup == null) {
                        existingGroup = new NSMutableArray();
                        _groupedObjects.setObjectForKey(existingGroup,key);
                    }
                    existingGroup.addObject(eo);
                }
            }
        }
        return _groupedObjects;
    }

    @Override
    public NSArray displayedObjects() {
        NSArray _displayedObjects = (NSArray)_groupedObjects().objectForKey(_groupingKeyForDate(currentDate()));
        return _displayedObjects == null ? NSArray.EmptyArray : _displayedObjects;
    }
    protected NSTimestamp _dateForDayInYear(int year, int day) {
        synchronized(cachedDays) {
            String key = year + "-" + day;
            NSTimestamp date = (NSTimestamp)cachedDays.valueForKey(key);
            if(date == null) {
                date = new NSTimestamp(year, 1, day, 0, 0, 0, NSTimeZone.defaultTimeZone());
                cachedDays.setObjectForKey(date, key);
            }
            return date;
        }
    }
    protected NSArray _datesForYearStartDays(int year, int startOffset, int count) {
        NSMutableArray dates = new NSMutableArray();
        for(int i = 0; i < count; i++) {
            dates.addObject(_dateForDayInYear(year, startOffset+i));
        }
        return dates;
    }
    protected NSTimestamp _firstDateInSameWeek(NSTimestamp value) {
        int dayOfWeek = ERXTimestampUtilities.dayOfWeek(value);
        int dayOfYear = ERXTimestampUtilities.dayOfYear(value);
        if(log.isDebugEnabled()) {
            log.debug("dayOfYear: " + dayOfYear);
            log.debug("dayOfWeek: " + dayOfWeek);
            log.debug("SUNDAY: " + Calendar.SUNDAY);
        }
        int startOfWeek = weekStartsMonday() ? Calendar.MONDAY : Calendar.SUNDAY;
        if(dayOfWeek == startOfWeek) {
        	return _dateForDayInYear(ERXTimestampUtilities.yearOfCommonEra(value), ERXTimestampUtilities.dayOfYear(value));
        }
		int offset = !weekStartsMonday() ? 1 : (dayOfWeek == Calendar.SUNDAY ? -5 : 2);
		return _dateForDayInYear(ERXTimestampUtilities.yearOfCommonEra(value), ERXTimestampUtilities.dayOfYear(value) - dayOfWeek + offset);
    }
    protected NSTimestamp _firstDateInSameMonth(NSTimestamp value) {
        int dayOfMonth = ERXTimestampUtilities.dayOfMonth(value);
        int dayOfYear = ERXTimestampUtilities.dayOfYear(value);
        if(log.isDebugEnabled()) {
            log.debug("dayOfYear: " + dayOfYear);
            log.debug("dayOfMonth: " + dayOfMonth);
        }
        return _dateForDayInYear(ERXTimestampUtilities.yearOfCommonEra(value), ERXTimestampUtilities.dayOfYear(value) - dayOfMonth + 1);
    }
    public NSTimestamp firstDateInSameMonth() {
        if(_firstDateInSameMonth == null) {
            _firstDateInSameMonth = _firstDateInSameMonth(selectedDate());
        }
        return _firstDateInSameMonth;
    }
    public NSTimestamp firstDateInNextMonth() {
        if(_firstDateInNextMonth == null) {
            _firstDateInNextMonth = firstDateInSameMonth().timestampByAddingGregorianUnits(0, 1, 0, 0, 0, 0);
        }
        return _firstDateInNextMonth;
    }
    protected NSArray _weekDatesForDate(NSTimestamp week) {
        NSTimestamp startDate = _firstDateInSameWeek(week);
        int startOffset = ERXTimestampUtilities.dayOfYear(startDate);
        int year = ERXTimestampUtilities.yearOfCommonEra(startDate);
        return _datesForYearStartDays(year, startOffset, 7);
    }

    public NSTimestamp lastDateForMonth() {
        return (NSTimestamp)((NSArray)datesForWeeksForCurrentMonth().lastObject()).lastObject();
    }
    public NSTimestamp firstDateForMonth() {
        return (NSTimestamp)((NSArray)datesForWeeksForCurrentMonth().objectAtIndex(0)).objectAtIndex(0);
    }
    
    public NSArray datesForWeeksForCurrentMonth() {
        if(_datesForWeeksForCurrentMonth == null) {
            _datesForWeeksForCurrentMonth = new NSMutableArray();
            NSTimestamp startDate = firstDateInSameMonth();
            NSTimestamp endDate = firstDateInNextMonth();
            startDate = _firstDateInSameWeek(startDate);
            int year =  ERXTimestampUtilities.yearOfCommonEra(startDate);
            int startOffset = ERXTimestampUtilities.dayOfYear(startDate);
            for(int i = 0; i < 6; i ++) {
                NSMutableArray weekDates = new NSMutableArray();
                for(int j = 0; j < 7; j ++) {
                    NSTimestamp day = _dateForDayInYear(year, startOffset + i * 7 + j);
                    if(j == 0 && ERXTimestampUtilities.differenceByDay(endDate, day) >= 0) {
                        return _datesForWeeksForCurrentMonth;
                    }
                    weekDates.addObject(day);
                }
                _datesForWeeksForCurrentMonth.addObject(weekDates);
            }
        }
        return _datesForWeeksForCurrentMonth;
    }
    public void setDatesForCurrentWeek(NSArray value) {
        _datesForCurrentWeek = value;
    }
    public NSArray datesForCurrentWeek() {
        NSArray result = _datesForCurrentWeek;
        if(result == null) {
        	// The weekOfMonth result is one based, not zero based
            int weekOfMonth = ERXTimestampUtilities.weekOfMonth(selectedDate()) - 1;
            // if the first week of the month has less than Calendar.getMinimalDaysInFirstWeek() (usually 4 days), 
            // the week belongs to the previous month. The weekOfMonth variable will then contain -1.
            if (weekOfMonth == -1) {
            	log.debug("weekOfMonth is -1, setting to 0");
            	weekOfMonth = 0;
            }
            result = (NSArray)datesForWeeksForCurrentMonth().objectAtIndex(weekOfMonth);
        }
        return result;
    }
    public NSArray datesForCurrentMonth() {
        NSTimestamp startDate = currentDate().timestampByAddingGregorianUnits(0, 0, -ERXTimestampUtilities.dayOfMonth(currentDate()) + 1, 0, 0, 0);
        int year = ERXTimestampUtilities.yearOfCommonEra(startDate);
        int startOffset = ERXTimestampUtilities.dayOfYear(startDate);
        int daysInMonth = 31;
        if(ERXTimestampUtilities.monthOfYear(startDate) != 12) {
            daysInMonth = ERXTimestampUtilities.dayOfYear(startDate.timestampByAddingGregorianUnits(0, 1, -1, 0, 0, 0)) - startOffset + 1;
        }
        return _datesForYearStartDays(year, startOffset, daysInMonth);
    }
    public void goToToday() {
        setSelectedDate(today());
    }
    public void nextMonth() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(0, 1, 0, 0, 0, 0));
    }
    public void previousMonth() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(0, -1, 0, 0, 0, 0));
    }
    public void nextDay() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(0, 0, 1, 0, 0, 0));
    }
    public void previousDay() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(0, 0, -1, 0, 0, 0));
    }
    public void nextWeek() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(0, 0, 7, 0, 0, 0));
    }
    public void previousWeek() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(0, 0, -7, 0, 0, 0));
    }
    public void nextYear() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(1, 0, 0, 0, 0, 0));
    }
    public void previousYear() {
        setSelectedDate(selectedDate().timestampByAddingGregorianUnits(-1, 0, 0, 0, 0, 0));
    }
}
