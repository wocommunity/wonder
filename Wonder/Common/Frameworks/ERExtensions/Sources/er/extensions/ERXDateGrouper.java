package er.extensions;
import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Works much the same as a {@link WODisplayGroup}.
 * See {@link ERXMonthView} and {@link ERXWeekDisplay} for examples on how to use it.
 *
 * @created ak on Mon Nov 04 2002
 */

public class ERXDateGrouper extends WODisplayGroup {
    /** logging support */
    private static ERXLogger log = ERXLogger.getERXLogger(ERXDateGrouper.class);

    public static final int DAY = Calendar.DAY_OF_YEAR;
    public static final int MONTH = Calendar.MONTH;
    public static final int WEEK = Calendar.WEEK_OF_YEAR;
    public static final int YEAR = Calendar.YEAR;

    static NSMutableDictionary cachedDays = new NSMutableDictionary();
    static int currentYear = ERXTimestampUtility.yearOfCommonEra(new NSTimestamp());

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
        return ERXTimestampUtility.differenceByDay(today(), currentDate()) == 0;
    }
    public boolean isSelectedDate() {
        return ERXTimestampUtility.differenceByDay(selectedDate(), currentDate()) == 0;
    }
    public boolean isInMonth() {
        return ERXTimestampUtility.differenceByDay(firstDateInSameMonth(), currentDate()) >= 0 && ERXTimestampUtility.differenceByDay(firstDateInNextMonth(), currentDate()) < 0;
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
    public NSArray allObjects() {
        return super.allObjects();
    }
    public void setObjectArray(NSArray value) {
        _groupedObjects = null;
        _reset();
        super.setObjectArray(value);
    }
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
    public void setSelectedDate(NSTimestamp value) {
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
                value = ERXTimestampUtility.dayOfCommonEra(date) - currentYear * 365;
                break;
            case MONTH:
                value = ERXTimestampUtility.yearOfCommonEra(date) * 12 + ERXTimestampUtility.monthOfYear(date) - currentYear * 12;
                break;
            case WEEK:
                value = ERXTimestampUtility.yearOfCommonEra(date) * 53 + ERXTimestampUtility.monthOfYear(date) - currentYear * 53;
                break;
            case YEAR:
                value = ERXTimestampUtility.yearOfCommonEra(date) - currentYear;
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
        int dayOfWeek = ERXTimestampUtility.dayOfWeek(value);
        int dayOfYear = ERXTimestampUtility.dayOfYear(value);
        if(log.isDebugEnabled()) {
            log.debug("dayOfYear: " + dayOfYear);
            log.debug("dayOfWeek: " + dayOfWeek);
            log.debug("SUNDAY: " + Calendar.SUNDAY);
        }
        if(dayOfWeek == (!weekStartsMonday() ? Calendar.SUNDAY : Calendar.MONDAY)) {
            return _dateForDayInYear(ERXTimestampUtility.yearOfCommonEra(value), ERXTimestampUtility.dayOfYear(value));
        }
        return _dateForDayInYear(ERXTimestampUtility.yearOfCommonEra(value), ERXTimestampUtility.dayOfYear(value) - dayOfWeek + 1);
    }
    protected NSTimestamp _firstDateInSameMonth(NSTimestamp value) {
        int dayOfMonth = ERXTimestampUtility.dayOfMonth(value);
        int dayOfYear = ERXTimestampUtility.dayOfYear(value);
        if(log.isDebugEnabled()) {
            log.debug("dayOfYear: " + dayOfYear);
            log.debug("dayOfMonth: " + dayOfMonth);
        }
        return _dateForDayInYear(ERXTimestampUtility.yearOfCommonEra(value), ERXTimestampUtility.dayOfYear(value) - dayOfMonth + 1);
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
        int startOffset = ERXTimestampUtility.dayOfYear(startDate);
        int year = ERXTimestampUtility.yearOfCommonEra(startDate);
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
            int year =  ERXTimestampUtility.yearOfCommonEra(startDate);
            int startOffset = ERXTimestampUtility.dayOfYear(startDate);
            for(int i = 0; i < 6; i ++) {
                NSMutableArray weekDates = new NSMutableArray();
                for(int j = 0; j < 7; j ++) {
                    NSTimestamp day = _dateForDayInYear(year, startOffset + i * 7 + j);
                    if(j == 0 && ERXTimestampUtility.differenceByDay(endDate, day) >= 0) {
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
            int weekOfMonth = ERXTimestampUtility.weekOfMonth(selectedDate());
            result = (NSArray)datesForWeeksForCurrentMonth().objectAtIndex(weekOfMonth -1);
        }
        return result;
    }
    public NSArray datesForCurrentMonth() {
        NSTimestamp startDate = currentDate().timestampByAddingGregorianUnits(0, 0, -ERXTimestampUtility.dayOfMonth(currentDate()) + 1, 0, 0, 0);
        int year = ERXTimestampUtility.yearOfCommonEra(startDate);
        int startOffset = ERXTimestampUtility.dayOfYear(startDate);
        int daysInMonth = 31;
        if(ERXTimestampUtility.monthOfYear(startDate) != 12) {
            daysInMonth = ERXTimestampUtility.dayOfYear(startDate.timestampByAddingGregorianUnits(0, 1, -1, 0, 0, 0)) - startOffset;
        }
        return _datesForYearStartDays(year, startOffset, daysInMonth);
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
