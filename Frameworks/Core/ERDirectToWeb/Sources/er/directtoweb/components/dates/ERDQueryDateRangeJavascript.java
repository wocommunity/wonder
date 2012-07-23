/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.dates;

import java.text.Format;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.components._private.ERXWOForm;
import er.extensions.components.javascript.ERXEditDateJavascript;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationFactory;

/**
 * <span class="en">Used for building date queries with javascript.</span>
 * <span class="ja">Javascript を使用した日付ビルド・クエリ</span>
 */

public class ERDQueryDateRangeJavascript extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected static String _datePickerJavaScriptUrl;
	protected String key;
	protected Format _dateFormatter;
	protected String _minValue;
	protected String _maxValue;
	protected String _minName;
	protected String _formatter;
	protected String _maxName;
	protected String _formName;
	protected String _javascriptName;
		
	public ERDQueryDateRangeJavascript(WOContext context) { 
    	super(context); 
    }
    
    public String propertyKey() {
        if(key == null)
            key = (String)valueForBinding("propertyKey");
        return key;
    }

    public Format dateFormatter() {
    	if(_dateFormatter == null) {
    		_dateFormatter = ERXTimestampFormatter.dateFormatterForPattern(formatter());
    	}
    	return _dateFormatter;
    }
    
    protected String stringForDate(NSTimestamp d) {
        String result=null;
        if(d != null) {
            try {
            	result = dateFormatter().format(d);
            } catch(IllegalArgumentException nsfe) {
            }
        }
        return result;
    }

    public String javascriptName() {
    	if(_javascriptName == null) _javascriptName = "date_" + context().elementID().replace('.', '_');
    	return _javascriptName;
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
        		date = (NSTimestamp)dateFormatter().parseObject(dateString);
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

    public String formName() {
        if (_formName==null) _formName=ERXWOForm.formName(context(), "QueryForm");
        return _formName;
    }

    public String minName() {
        if (_minName==null) _minName=javascriptName()+ "min";
        return _minName;
    }
    
    public String minHREF() {
        return "javascript:show_calendar('"+formName()+"." + minName() + "',null,null,'"+formatterStringForScript()+"')"; 
    }

    public String maxName() {
    	if (_maxName==null) _maxName=javascriptName()+ "max";
    	return _maxName;
    }
    public String maxHREF() {
    	return "javascript:show_calendar('"+formName()+ "." + maxName() + "',null,null,'"+formatterStringForScript()+"')";
    }
    
    public int formatLength() {
        String formatter = formatterStringForScript();
        return formatter.length() < 12 ? 12 : formatter.length();
    }
    
    public String localizedFormatString() {
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(formatter());
    }
    
    public String formatter() {
		if(_formatter == null) {
			_formatter = (String)valueForBinding("formatter");
			if(_formatter == null || _formatter.length() == 0) {
				_formatter = ERXTimestampFormatter.DEFAULT_PATTERN;
			}
 		}
		return _formatter;
	}
    
    public String formatterStringForScript() {
    	return ERXEditDateJavascript.formatterStringForScript(localizedFormatString());
    }
}
