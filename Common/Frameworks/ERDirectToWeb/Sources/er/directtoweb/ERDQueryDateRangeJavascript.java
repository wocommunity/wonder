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
 * WARNING: the format string used by this app is in plain java format (yyyy/MM/dd) instead of (%Y/%M/%D)
 */

public class ERDQueryDateRangeJavascript extends ERDCustomQueryComponent {

    public ERDQueryDateRangeJavascript(WOContext context) { super(context); }

    protected static String _datePickerJavaScriptUrl;
    protected String key;
    protected NSTimestampFormatter _formatter;
    protected String _minValue;
    protected String _maxValue;
    protected String _minName;
    protected String formatterString;
    protected String _maxName;
    
    public String propertyKey() {
        if(key == null)
            key = (String)valueForBinding("propertyKey");
        return key;
    }

    public NSTimestampFormatter formatter() {
    	if(_formatter == null) {
    		_formatter = new NSTimestampFormatter(formatterString());
    	}
    	return _formatter;
    }
    
    protected String stringForDate(NSTimestamp d) {
        String result=null;
        if(d != null) {
            try {
            	result =  formatter().format(d);
            } catch(IllegalArgumentException nsfe) {
            }
        }
        return result;
    }

    public Object minValue() {
        if(_minValue == null){
            _minValue=stringForDate((NSTimestamp)displayGroup().queryMin().valueForKey(propertyKey()));
        }
        return _minValue;
    }

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
        		date = (NSTimestamp)formatter().parseObject(dateString);
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

    public String datePickerJavaScriptUrl() {
        if (_datePickerJavaScriptUrl==null) {
            _datePickerJavaScriptUrl= application().resourceManager().urlForResourceNamed("date-picker.js", "ERExtensions", null, context().request());
        }
        return _datePickerJavaScriptUrl;
    }

    public String minName() {
        if (_minName==null) _minName="min"+hashCode();
        return _minName;
    }
    public String minHREF() {
        return "javascript:show_calendar('QueryForm." + minName() + "',null,null,'"+formatterString()+"')"; 
    }

    public String formatterString() {
		if(formatterString == null) {
			formatterString = (String)valueForBinding("formatter");
			if(formatterString == null) {
				formatterString = "MM/dd/yyyy";
			}
		}
		return formatterString;
	}

    public String maxName() {
        if (_maxName==null) _maxName="max"+hashCode();
        return _maxName;
    }
    public String maxHREF() {
        return "javascript:show_calendar('QueryForm." + maxName() + "',null,null,'"+formatterString()+"')";
    }
}
