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
import er.extensions.*;

/**
 * Used for building date queries with javascript.<br />
 * 
 */

public class ERDQueryDateRangeJavascript extends ERDCustomQueryComponent {

    public ERDQueryDateRangeJavascript(WOContext context) { super(context); }

    protected static final NSTimestampFormatter DATE_FORMAT =
    new NSTimestampFormatter("%m/%d/%Y");
    protected static final NSTimestampFormatter DATE_FORMAT_YEAR_TWO_DIGITS =
        new NSTimestampFormatter("%m/%d/%y") ;

    protected String key;

    public String propertyKey() {
        if(key == null)
            key = (String)valueForBinding("propertyKey");
        return key;
    }

    private String stringForDate(NSTimestamp d) {
        String result=null;
        if(d != null) {
            try {
                result = DATE_FORMAT.format(d);
            } catch(IllegalArgumentException nsfe) {}
        }
        return result;
    }

    private String _minValue;
    public Object minValue() {
        if(_minValue == null){
            _minValue=stringForDate((NSTimestamp)displayGroup().queryMin().valueForKey(propertyKey()));
        }
        return _minValue;
    }

    private String _maxValue;
    public Object maxValue() {
        if (_maxValue == null) {
            _maxValue=stringForDate((NSTimestamp)displayGroup().queryMax().valueForKey(propertyKey()));
        }
        return _maxValue;
    }

    public NSTimestamp dateForString(String dateString) {
        NSTimestamp date = null;
        try {
            if(dateString!=null) {
                boolean dateIsValid = false;
                NSMutableArray components = new NSMutableArray(NSArray.componentsSeparatedByString(dateString, "/"));
                if (components.count() == 3) {
                    String monthString = (String)components.objectAtIndex(0);
                    if (monthString.length() == 1)
                        components.replaceObjectAtIndex("0" + monthString, 0);
                    String dayString = (String)components.objectAtIndex(1);
                    if (dayString.length() == 1)
                        components.replaceObjectAtIndex("0" +dayString, 1);
                    String yearString = (String)components.objectAtIndex(2);
                    //String yearString = dateString.substring(dateString.lastIndexOf("/")+1, dateString.length());
                    String modifiedDateString = components.componentsJoinedByString("/");
                    java.text.Format formatter=yearString.length()==2 ? DATE_FORMAT_YEAR_TWO_DIGITS : DATE_FORMAT;
                    date = (NSTimestamp) formatter.parseObject(modifiedDateString);
                    String reformattedDate=formatter.format(date);
                    dateIsValid = reformattedDate.equals(modifiedDateString);
                }
                if (!dateIsValid)
                    throw ERXValidationFactory.defaultFactory().createException(null, propertyKey(), dateString, "InvalidDateFormatException");

            }
        } catch (java.text.ParseException nspe) {
            NSValidation.ValidationException v =
            ERXValidationFactory.defaultFactory().createException(null, propertyKey(), dateString, "InvalidDateFormatException");
            parent().validationFailedWithException( v, date, propertyKey());
        } catch (NSValidation.ValidationException v) {
            parent().validationFailedWithException(v,date,propertyKey());
        } catch(Exception e) {
            parent().validationFailedWithException(e,date,propertyKey());
        }

        return date;
    }

    public void setMinValue(String min) {
        _minValue=min;
        NSTimestamp minDate = dateForString(min);
        if(minDate != null)
            displayGroup().queryMin().takeValueForKey(minDate, propertyKey());
        else
           displayGroup().queryMin().removeObjectForKey(propertyKey());
    }

    public void setMaxValue(String max) {
        _maxValue=max;
        NSTimestamp maxDate = dateForString(max);
        if(maxDate != null)
            displayGroup().queryMax().takeValueForKey(maxDate, propertyKey());
        else
        	displayGroup().queryMax().removeObjectForKey(propertyKey());
    }

    private static String _datePickerJavaScriptUrl;
    public String datePickerJavaScriptUrl() {
        if (_datePickerJavaScriptUrl==null) {
            _datePickerJavaScriptUrl= application().resourceManager().urlForResourceNamed("date-picker.js", "ERExtensions", null, context().request());
        }
        return _datePickerJavaScriptUrl;
    }

    private String _minName;
    public String minName() {
        if (_minName==null) _minName="min"+hashCode();
        return _minName;
    }
    public String minHREF() {
        return "javascript:show_calendar('QueryForm." + minName() + "')"; 
    }
    private String _maxName;
    public String maxName() {
        if (_maxName==null) _maxName="max"+hashCode();
        return _maxName;
    }
    public String maxHREF() {
        return "javascript:show_calendar('QueryForm." + maxName() + "')";
    }
}
