/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Crazy cool little date picker that uses javascript to pick the date from a little calendar. <br />
 * Uses ERXEditDateJavascript.
 */

public class ERDEditDateJavascript extends ERDCustomEditComponent {
    static final ERXLogger log = ERXLogger.getERXLogger(ERDEditDateJavascript.class);

    public ERDEditDateJavascript(WOContext context) {super(context);}

    public String dateString;
    protected String _formatter;
    protected NSTimestampFormatter _dateFormatter;
    
    public void appendToResponse(WOResponse r, WOContext c){
        if(dateString == null){
            NSTimestamp date = (NSTimestamp)objectPropertyValue();
            if(date != null)
                try {
                    dateString = dateFormatter().format(date);
                } catch(IllegalArgumentException nsfe){ 
                }
        }
        super.appendToResponse(r,c);
    }
    
    public Object value() {
    	return dateString;
    }
    
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
    		_dateFormatter = new NSTimestampFormatter(formatter());
    	}
    	return _dateFormatter;
    }

	public String formatter() {
		if(_formatter == null) {
			_formatter = (String)valueForBinding("formatter");
		}
		if(_formatter == null || _formatter.length() == 0) {
			_formatter = "%m/%d/%Y";
		}
		return _formatter;
	}

	public void setFormatter(String formatter) {
		_formatter = formatter;
	}
 }
