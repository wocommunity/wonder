// Person.java
// Created on Mon Nov 05 10:53:49  2001 by Apple EOModeler Version 410

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.util.*;
import java.math.BigDecimal;
import com.webobjects.eoaccess.*;
import com.uw.shared.*;

public class Person extends _Person implements SelectionInterface {


    /** constructor */
    public Person() {
        super();
    }

    public Integer selectedYear;
    public Person editorUser;
    public static GregorianCalendar _archiveDate;

    public Person(EOEditingContext newContext, NSArray years) {
        super();
        newContext.insertObject(this);

        // if an array of years is provided, create a record for each year
        if (years!=null) {
            Enumeration enumerator = years.objectEnumerator();

            while (enumerator.hasMoreElements()) {
                YearlyData yearlyData = new YearlyData();
                yearlyData.setYear((Integer) enumerator.nextElement());
                newContext.insertObject(yearlyData);
                addToYearlyData(yearlyData);
            }
        }
        // else if no array is provided, just create a record for this year
        else {
            YearlyData yearlyData = new YearlyData();
            newContext.insertObject(yearlyData);
            addToYearlyData(yearlyData);
        }

    }

    public NSArray adminUsers(EOEditingContext ec, Group group) {
        NSMutableDictionary bindings = new NSMutableDictionary();
        bindings.setObjectForKey("Admin","TYPE");

        if (!type().equals("Admin")) bindings.setObjectForKey(group,"GROUP");
        else bindings.setObjectForKey(group.parentGroup(),"GROUP");  // admin requests should go to their parent

        NSArray adminusers = EOUtilities.objectsWithFetchSpecificationAndBindings(ec,
                                                                                  "Person","fetchUserByTypeAndGroup",bindings);

        // if the adminusers count is 0, then get then climb the tree until we get a set of admin users
        if (adminusers.count()==0 && group.parentGroup()!=null) return this.adminUsers(ec, group.parentGroup());
        else return adminusers;
        
    }

    /** Sorts the events displayed for the user being edited */
    public NSArray sortedDates() {
        EOSortOrdering uniqueIdOrder = new EOSortOrdering("fromDate", EOSortOrdering.CompareDescending);
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(datesForSelectedYear(), new NSArray(uniqueIdOrder));
    }

    public NSArray datesForSelectedYear() {
        if (selectedYear!=null) {

            GregorianCalendar startOfYear = new GregorianCalendar(selectedYear.intValue(), archiveDate().get(GregorianCalendar.MONTH), archiveDate().get(GregorianCalendar.DAY_OF_MONTH), 0, 0, 0);
            GregorianCalendar endOfYear = new GregorianCalendar(selectedYear.intValue()+1,archiveDate().get(GregorianCalendar.MONTH), archiveDate().get(GregorianCalendar.DAY_OF_MONTH), 0, 0, 0);

            // construct qualifiers
            EOKeyValueQualifier qual1 = new EOKeyValueQualifier("fromDate", EOQualifier.QualifierOperatorGreaterThanOrEqualTo, startOfYear.getTime());
            EOKeyValueQualifier qual2 = new EOKeyValueQualifier("fromDate", EOQualifier.QualifierOperatorLessThan,  endOfYear.getTime());

            // because of some kind of caching bug
            return EOQualifier.filteredArrayWithQualifier(dates(), new EOAndQualifier(new NSArray(new Object[] {qual1, qual2})));

        }
        else return null;
    }

    public GregorianCalendar archiveDate() {
        if (_archiveDate==null) {
            
            _archiveDate = new GregorianCalendar();
            Setting setting = Setting.currentSettingForContext(editingContext());
        
            if (setting != null) _archiveDate.setTime(setting.archiveDate());
            else _archiveDate = new GregorianCalendar(selectedYear.intValue(),Calendar.JANUARY,1,0,0,0);
        }

        return _archiveDate;
    }

    public YearlyData currentYearlyData() {

        java.util.Enumeration enumerator = yearlyData().objectEnumerator();

        while (enumerator.hasMoreElements()) {
            YearlyData yearlyDataIterator = (YearlyData) enumerator.nextElement();
            if (yearlyDataIterator.year().intValue() == selectedYear.intValue()) return yearlyDataIterator;
        }
        return null; // should never get here

    }
    
    /** returns the events for a person that match the dates requested */
    public NSArray eventsForType(String eventType) {
        EOKeyValueQualifier qualifier = new EOKeyValueQualifier("type", EOQualifier.QualifierOperatorEqual, eventType);
        return EOQualifier.filteredArrayWithQualifier(datesForSelectedYear(), qualifier);
    }

    public NSArray eventsForTypeLike(String eventTypeLike) {
        EOKeyValueQualifier qualifier = new EOKeyValueQualifier("type", EOQualifier.QualifierOperatorContains, eventTypeLike);
        return EOQualifier.filteredArrayWithQualifier(datesForSelectedYear(), qualifier);
    }

    public NSArray lieuDates() {
        return eventsForType("Lieu Time Taken");
    }

    public NSArray lieuDatesEarned() {
        return eventsForType("Lieu Time Earned");
    }
    
    public NSArray absentDates() {
        return eventsForType("Absent");
    }

    public NSArray sickDates() {
        return eventsForType("Sick");
    }

    public NSArray vacationDates() {
        return eventsForType("Leave");
    }


    /** used to track if the user has been selected from a group of users */
    protected int selected;

    public void setSelected(int newValue) {
        selected = newValue;
    }

    /** used to track if the user has been selected from a group of users */
    public int selected() {
        return selected;
    }

    /** checks if the user is of Admin type */
    public boolean isAdmin() {
        if (type().equals("Admin")) return true;
        else return false;
    }

    public boolean isNotAdmin() {
        return (!isAdmin());
    }

    /** Returns a boolean indicating whether there are requested dates for the user */
    public boolean hasRequestedDates() {
        if (eventsForTypeLike("Request").count()>0) return true;
        else return false;
    }

    public double vacationEntitled() {
        return currentYearlyData().vacationCarryOver().floatValue() + currentYearlyData().vacationEntitled().floatValue() + ((BigDecimal) valueForKeyPath("lieuDatesEarned.@sum.totalTime")).floatValue();
    }

    public double vacationUsed() {
        return ((BigDecimal) valueForKeyPath("vacationDates.@sum.totalTime")).floatValue() +
        ((BigDecimal) valueForKeyPath("lieuDates.@sum.totalTime")).floatValue();
    }

    /** Calculates the number of days left in the user's vacation */
    public double vacationUnused() {
        return vacationEntitled() - vacationUsed();
    }
}
