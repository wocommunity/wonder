/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;
import java.util.*;

public class ERD2WCalendarPage extends ERD2WListPage {
    
    private final static String dateSortKeyKey="dateSortKey";
    private final static int maximumNumbersOfEntriesPerDay=99;
    private final static int maxLengthForMonthView=11; // FIXME could make this rule based?
    protected NSTimestamp today=new NSTimestamp();    
    protected NSTimestamp date;
    private NSTimestamp _beginningDate;
    private NSTimestamp _beginningOfTheMonth;
    private NSTimestamp _endDate;

    public ERD2WCalendarPage(WOContext c) {
        super(c);
        setSelectedDate(today);
    }

    public int numberOfObjectsPerBatch() {
        return 0;	// we want all the objects in one batch
    }

    //private NSArray _allObjects = null;
    private NSArray _allObjects() {
        return displayGroup().allObjects();
        /* Useful for debugging-- remove me later
        if (_allObjects == null) {
            WODisplayGroup dg = displayGroup();
            System.err.println("Display group: " + dg);
            EODataSource ds = dg.dataSource();
            if (ds != null) {
                System.err.println("Data Source: " + ds + "\n class: " + ds.getClass());
            _allObjects = ds.fetchObjects();
            } else {
                _allObjects = NSArray.EmptyArray; // Empty Array
                System.err.println("Data Source: " + ds);
            }
            System.err.println("All Objects: " + _allObjects);

        }
        return _allObjects;*/
    }
    
    private String _defaultSortKey;
    public String defaultSortKey() {
        if (_defaultSortKey==null) {
            NSArray sortOrderingDefinition=(NSArray)d2wContext().valueForKey("defaultSortOrdering");
            if (sortOrderingDefinition!=null) {
                _defaultSortKey=(String)sortOrderingDefinition.objectAtIndex(0);
            }
        }
        if (_defaultSortKey==null)
            throw new RuntimeException("Could not find defaultSortOrdering for Calendar page");
        return _defaultSortKey;
    }
    private String _monthViewDisplayKey;
    public String monthViewDisplayKey () {
        if (_monthViewDisplayKey ==null)
            _monthViewDisplayKey =(String)d2wContext().valueForKey("monthViewDisplayKey");        
        if (_monthViewDisplayKey ==null)
            throw new RuntimeException("Could not find monthViewDisplayKey for Calendar page");
        return _monthViewDisplayKey;
    }
    private String _dayViewDisplayKey;
    public String dayViewDisplayKey () {
        if (_dayViewDisplayKey ==null)
            _dayViewDisplayKey =(String)d2wContext().valueForKey("dayViewDisplayKey");        
        if (_dayViewDisplayKey ==null)
            throw new RuntimeException("Could not find dayViewDisplayKey for Calendar page");
        return _dayViewDisplayKey;
    }

    private NSTimestamp _selectedDate = null;
    public NSTimestamp selectedDate() {
        return _selectedDate;
    }
    protected void setSelectedDate(NSTimestamp selectedDate) {
        _selectedDate = selectedDate;
        session().setObjectForKey(_selectedDate, "selectedDate");
    }

    public NSTimestamp dateForEOAtIndex(int i) {
        EOEnterpriseObject eo=(EOEnterpriseObject)_allObjects().objectAtIndex(i);
        return (NSTimestamp)eo.valueForKey(defaultSortKey());
    }

    public boolean isEditable() {
        boolean result = false;
        Integer isEditable = (Integer)d2wContext().valueForKey("isEntityEditable");
        if (isEditable != null) {
            result = isEditable.intValue() != 0;
        }
        Object o = object();
        if (o instanceof ERXGuardedObjectInterface) {
            result = result && ((ERXGuardedObjectInterface)o).canUpdate();
        }
        return result;
    }
    
    public String monthDescriptionForEO() {
        Object o=object().valueForKey(monthViewDisplayKey());
        String description=o!=null ? o.toString() : null;
        description=description!=null && description.length()> maxLengthForMonthView ? description.substring(0, maxLengthForMonthView) : description;
        return description;
    }
    public String dayDescriptionForEO() {
        Object o=object().valueForKey(dayViewDisplayKey());
        return o!=null ? o.toString() : null;
    }
    
    public NSTimestamp beginningOfTheMonth() {
        if (_beginningOfTheMonth==null) {
            // we take the last date by default
            /* 
            NSGregorianDate d=_allObjects().count()>0 ?
                new NSGregorianDate(0,dateForEOAtIndex(_allObjects().count()-1)) :
                new NSGregorianDate();
            // get the beginning of that month
             */
            // we take now by default
            _beginningOfTheMonth=new NSTimestamp(ERXTimestampUtility.yearOfCommonEra(today),
                                                 ERXTimestampUtility.monthOfYear(today),
                                                 1,
                                                 0,
                                                 0,
                                                 0,
                                                 NSTimeZone.getDefault());
        }
        return _beginningOfTheMonth;
    }

    private NSTimestamp _endOfTheMonth;
    public NSTimestamp endOfTheMonth() {
        if (_endOfTheMonth == null)
            _endOfTheMonth = beginningOfTheMonth().timestampByAddingGregorianUnits(0,1,-1,0,0,0);
        return _endOfTheMonth;
    }

    public NSTimestamp endDate() {
        if (_endDate == null) {
            _endDate = endOfTheMonth().timestampByAddingGregorianUnits(0, 0,
                                                                       6 - ERXTimestampUtility.dayOfWeek(endOfTheMonth()), 0, 0, 0);
        }
        return _endDate;
    }
    
    public NSTimestamp beginningDate() {
        if (_beginningDate==null) {
            _beginningDate=beginningOfTheMonth().timestampByAddingGregorianUnits(0, 0, - ERXTimestampUtility.dayOfWeek(beginningOfTheMonth()), 0, 0, 0);
        }
        return _beginningDate;
    }

/*
    public boolean hasItemPreviousMonth() {
        return dateForEOAtIndex(0).compare(beginningOfTheMonth())<0;
    }

    public boolean hasItemNextMonth() {
        return dateForEOAtIndex(_allObjects().count()-1).compare(endOfTheMonth())>=0;
    }    
*/

    private void _reset() {
        _endOfTheMonth=null;
        _beginningDate=null;
        _dates=null;
        _endDate=null;
    }
    
    public WOComponent switchToToday() {
        _beginningOfTheMonth=null;
        _reset();
        setSelectedDate(today);
        return null;
    }
    
    public WOComponent nextMonth() {
        _reset();
        _beginningOfTheMonth=beginningOfTheMonth().timestampByAddingGregorianUnits(0,1,0,0,0,0);
        return null;
    }

    public WOComponent previousMonth() {
        _reset();
        _beginningOfTheMonth=beginningOfTheMonth().timestampByAddingGregorianUnits(0,-1,0,0,0,0);
        return null;
    }

    private NSMutableArray _dates;
    // FIXME we can make those static and cached
    public NSArray dates() {
        if (_dates==null) {
            _dates=new NSMutableArray();
            int imax=0;
            //WO5FIXME BOOGIE int imax = endDate().get(Calendar.DAY_OF_YEAR) - beginningDate().get(Calendar.DAY_OF_YEAR) + 1;
            for (int i=0; i < imax; i++)
                _dates.addObject(beginningDate().timestampByAddingGregorianUnits(0,0,i,0,0,0));
        }
        return _dates;
    }

    public void setDate(NSTimestamp newDate) {
        date=newDate;
        _eosOnCurrentDateComputed=false;
    }

    public boolean dateIsToday() {
        return (today!=null && date!=null &&
                 ERXTimestampUtility.dayOfCommonEra(today)==ERXTimestampUtility.dayOfCommonEra(date));
    }
    public boolean dateIsSelectedDate() {
        return (selectedDate()!=null && date!=null &&
                 ERXTimestampUtility.dayOfCommonEra(selectedDate())==ERXTimestampUtility.dayOfCommonEra(date));
    }

    private void _eosOnDate(NSTimestamp aDate, NSMutableArray eos) {
        long dayOfCommonEra = ERXTimestampUtility.dayOfCommonEra(aDate);
        NSArray displayedObjects = _allObjects();
        boolean stop = false;
        int imax = displayedObjects.count();
        for (int i=0; i < imax; i++) {
            NSTimestamp d=dateForEOAtIndex(i);
            if (dayOfCommonEra == ERXTimestampUtility.dayOfCommonEra(d)) {
                stop = true;  // will stop when days no longer match
                eos.addObject(displayedObjects.objectAtIndex(i));
            }
            else if (stop)
                break; // This assumes that the objects are sorted by date
        }
    }

    private NSMutableArray _eosOnCurrentDate=new NSMutableArray();
    private boolean _eosOnCurrentDateComputed=false;
    public NSArray eosOnCurrentDate() {
        if (! _eosOnCurrentDateComputed) {
            _eosOnCurrentDate.removeAllObjects();
            _eosOnDate(date, _eosOnCurrentDate);
        }
        return _eosOnCurrentDate;
    }
    
    private NSMutableArray _eosOnSelectedDate = null;
    public NSArray eosOnSelectedDate() {
        if (_eosOnSelectedDate==null && selectedDate()!=null) {
            _eosOnSelectedDate = new NSMutableArray();
            _eosOnDate(selectedDate(), _eosOnSelectedDate);
        }
        return _eosOnSelectedDate;
    }    

    public WOComponent selectDate() {
        setSelectedDate(date);
        _eosOnSelectedDate=null;
        return null;
    }


        // ---------------------------------------------------------------------
    // EO manipulation

    // Add is performed in the WOComponentContent in PostItemView -- FIXME this is cheating

    public WOComponent editItem() {
        EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed((String)d2wContext().valueForKey("editConfigurationName"),
                                                                                         session());
        epi.setObject(object());
        epi.setNextPage(context().page());
        _eosOnSelectedDate=null; // make sure we recompute those
        return (WOComponent)epi;
    }

    public void appendToResponse(WOResponse r, WOContext c) {
        _eosOnSelectedDate=null;
        super.appendToResponse(r,c);
    }
}
