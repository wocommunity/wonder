/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.ERXConstant;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// Important D2W Keys:
//	editTime - 0 || 1 denotes whether to show the time aspect of the component.
//	yearRangeTop - specifies the upper limit for year to be displayed.
//	yearRangeBottom - specifies the lower limit for year to be displayed
///////////////////////////////////////////////////////////////////////////////////////////////////////
public class ERDEditDatePopupCommon extends ERDCustomEditComponent {

    public ERDEditDatePopupCommon(WOContext context) { super(context); }

    protected String day;
    protected String month;
    protected String year;
    protected NSMutableArray yearList;
    protected static NSArray monthList;
    protected static NSArray dayList;

    protected static final NSTimestampFormatter DAY_FORMAT =
        new NSTimestampFormatter("%d");
    protected static final NSTimestampFormatter MONTH_FORMAT =
        new NSTimestampFormatter("%b");
    protected static final NSTimestampFormatter YEAR_FORMAT =
        new NSTimestampFormatter("%Y");
    protected static final NSTimestampFormatter TIME_FORMAT =
        new NSTimestampFormatter("%H:%M");
    protected static final NSTimestampFormatter ALL_FORMAT =
        new NSTimestampFormatter("%d %b %Y");

    protected static final NSTimestampFormatter ALL_FORMAT_AND_TIME =
        new NSTimestampFormatter("%d %b %Y %H:%M");

    protected String time;

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }
    public void reset() {
        super.reset();
        yearList = null;
        time=null;
        _editTime=null;
        day=null;
        month=null;
        year=null;
    }

    public NSArray dayList() {
        if (dayList == null) {
            dayList = new NSMutableArray(new Object[] {
                "01","02","03","04","05","06","07","08","09","10",
                "11","12","13","14","15","16","17","18","19","20",
                "21","22","23","24","25","26","27","28","29","30","31"
            });
        }
        return dayList;
    }

    public NSArray monthList() {
        if (monthList == null) {
            monthList = new NSArray(new Object[] {
                "Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
            });
        }
        return monthList;
    }

    public NSArray yearList() {
        if (yearList == null) {
            yearList = new NSMutableArray();
            int startYear = 1950, endYear = 2050;
            String yearRangeTop = null, yearRangeBottom = null;
            if ((valueForBinding("yearRangeTop") != null) && (valueForBinding("yearRangeBottom") != null)) {
                yearRangeTop = (String)valueForBinding("yearRangeTop");
                yearRangeBottom = (String)valueForBinding("yearRangeBottom");
            }
            if (yearRangeBottom != null && yearRangeTop != null) {
                try {
                    Integer start = ERXConstant.integerForString(yearRangeBottom);
                    Integer end = ERXConstant.integerForString(yearRangeTop);
                    if (end.intValue() > start.intValue()) {
                        startYear = start.intValue();
                        endYear = end.intValue();
                    }
                } catch (NumberFormatException e) {
                    NSLog.err.appendln("Binding exception in D2WEditDatePopup: " + e.toString());
                }
            }
            for (int year = startYear; year <= endYear; year++)
                yearList.addObject(""+year);
        }
        return yearList;
    }

    public String time() throws Exception {
        NSTimestamp date = (NSTimestamp)objectPropertyValue();
        if (date != null)
            time = TIME_FORMAT.format(date);
        else
            time = TIME_FORMAT.format(new NSTimestamp());
        return time;
    }

    public String day() throws Exception {
        NSTimestamp date = (NSTimestamp)objectPropertyValue();
        if (date != null)
            day = DAY_FORMAT.format(date);
        else
            day = DAY_FORMAT.format(new NSTimestamp());
        return day;
    }
    public void setDay(String newDay) throws Exception { day = newDay; }

    public String month() throws Exception {
        NSTimestamp date = (NSTimestamp)objectPropertyValue();
        if (date != null)
            month = MONTH_FORMAT.format(date);
        else
            month = MONTH_FORMAT.format(new NSTimestamp());
        return month;
    }
    public void setMonth(String newMonth) throws Exception { month = newMonth; }

    public String year() throws Exception {
        NSTimestamp date = (NSTimestamp) objectPropertyValue();
        if (date != null)
            year = YEAR_FORMAT.format(date);
        else
            year = YEAR_FORMAT.format(new NSTimestamp());
        return year;
    }
    public void setYear(String newYear) throws Exception { year = newYear; }
    public boolean checkFutureDate() { return false; }

    private Integer _editTime;
    public boolean editTime(){
        if (_editTime==null) {
            Object et=valueForBinding("editTime");
            if (et!=null) {
                _editTime=et instanceof Integer ? (Integer)valueForBinding("editTime") :
                ((String)et).equals("YES") || ((String)et).equals("1") ? ERXConstant.OneInteger : ERXConstant.ZeroInteger;
            }
        }
        return _editTime!=null ? _editTime.intValue()!=0 : false;
    }


    //JavaScript which returns today's date in the format 07/12/2000
    public String today() {
        return "var mydate=new Date() /n var year=mydate.getYear() /n if (year < 1000) /n year+=1900 /n var day=mydate.getDay() /n var month=mydate.getMonth()+1 /n if (month<10) /n month=\"0\"+month /n var daym=mydate.getDate() /n if (daym<10) /n daym=\"0\"+daym /n document.write(\"<small><font color='000000' face='Arial'><b>\"+month+\"/\"+daym+\"/\"+year+\"</b></font></small>)\"";
    }
}
