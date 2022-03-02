/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.dates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

/**
 * Crazy cool little date picker that uses javascript to pick the date from a little calendar.
 * Uses ERXEditDateJavascript.
 */

public class ERDEditDateJavascript extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(ERDEditDateJavascript.class);

    public ERDEditDateJavascript(WOContext context) {super(context);}

    public String dateString;
    protected String _formatter;
    protected NSTimestampFormatter _dateFormatter;
    
    @Override
    public void appendToResponse(WOResponse r, WOContext c){
        NSTimestamp date = (NSTimestamp)objectPropertyValue();
        if(date != null) {
            try {
                dateString = dateFormatter().format(date);
            } catch(IllegalArgumentException nsfe){
                // nothing?
            }
        } else {
            dateString = null;
        }
        super.appendToResponse(r,c);
    }
    
    public Object value() {
    	return dateString;
    }
    
    @Override
    public void takeValuesFromRequest (WORequest request, WOContext context) {
        super.takeValuesFromRequest (request,context);
        NSTimestamp date = null;
        try {
            if(dateString != null) {
            	date = (NSTimestamp)dateFormatter().parseObject(dateString);
            }
            if(object() != null) {
            	object().validateTakeValueForKeyPath(date, key());
            }
        } catch (java.text.ParseException npse) {
            log.debug("java.text.ParseException:" + npse);
            ERXValidationException v = ERXValidationFactory.defaultFactory().createException(object(), key(), dateString, "InvalidDateFormatException");
            parent().validationFailedWithException( v, date, key());
        } catch (NSValidation.ValidationException v) {
            log.debug("NSValidation.ValidationException:" + v);
            parent().validationFailedWithException(v,date,key());
        } catch(Exception e) {
            log.debug("Exception:" + e);
            parent().validationFailedWithException(e,date,key());
        }
    }

    
    protected NSTimestampFormatter dateFormatter() {
    	if(_dateFormatter == null) {
    		_dateFormatter = ERXTimestampFormatter.dateFormatterForPattern(formatter());
    	}
    	return _dateFormatter;
    }

	public String formatter() {
		if(_formatter == null) {
			_formatter = (String)valueForBinding("formatter");
		}
		if(_formatter == null || _formatter.length() == 0) {
			_formatter = ERXTimestampFormatter.DEFAULT_PATTERN;
		}
		return _formatter;
	}

	public void setFormatter(String formatter) {
		_formatter = formatter;
	}
 }
