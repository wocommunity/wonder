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

public class ERDEditDateJavascript extends ERDCustomEditComponent {

    public ERDEditDateJavascript(WOContext context) {super(context);}

    protected static final NSTimestampFormatter DATE_FORMAT = new NSTimestampFormatter("%m/%d/%Y");
    protected static final NSTimestampFormatter DATE_FORMAT_YEAR_TWO_DIGITS = new NSTimestampFormatter("%m/%d/%y");
    public String dateString;

    public void appendToResponse(WOResponse r, WOContext c){
        if(dateString == null){
            NSTimestamp date = (NSTimestamp)objectPropertyValue();
            if(date != null)
                try {
                    dateString = DATE_FORMAT.format(date);
                } catch(IllegalArgumentException nsfe){ }
        }
        super.appendToResponse(r,c);
    }
    //Made the component stateful so that we can keep eroneous values
    /*public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }*/
    public String name() { return key()+"_datebox"; }

    public void takeValuesFromRequest (WORequest request, WOContext context) {
        super.takeValuesFromRequest (request,context);
        NSTimestamp date = null;
        try {
            if(dateString!=null) {
                boolean dateIsValid = false;
                NSMutableArray components = new NSMutableArray(NSArray.componentsSeparatedByString(dateString, "/"));
                if (components.count() == 3) {
                    String monthString = (String)components.objectAtIndex(0);
                    if (monthString.length() == 1)
                        components.replaceObjectAtIndex(0, "0" + monthString);
                    String dayString = (String)components.objectAtIndex(1);
                    if (dayString.length() == 1)
                        components.replaceObjectAtIndex(1, "0" +dayString);
                    String yearString = (String)components.objectAtIndex(2);
                    //String yearString = dateString.substring(dateString.lastIndexOf("/")+1, dateString.length());
                    String modifiedDateString = components.componentsJoinedByString("/");
                    java.text.Format formatter=yearString.length()==2 ? DATE_FORMAT_YEAR_TWO_DIGITS : DATE_FORMAT;
                    date = (NSTimestamp) formatter.parseObject(modifiedDateString);
                    String reformattedDate=formatter.format(date);
                    dateIsValid = reformattedDate.equals(modifiedDateString);
                }
                if (!dateIsValid)
                    throw new NSValidation.ValidationException("Please check <B>"+
                                                               valueForBinding("displayNameForProperty")+"</B>: "+dateString+" is not a valid date");

            }
            if (object()!=null) object().validateTakeValueForKeyPath(date, key());
        } catch (java.text.ParseException nspe) {
            NSValidation.ValidationException v =
            new NSValidation.ValidationException("Please check the format of <B>"+
                                                 valueForBinding("displayNameForProperty")+"</B> "+dateString+" is not a valid date");
            parent().validationFailedWithException( v, date, key());
        } catch (NSValidation.ValidationException v) {
            parent().validationFailedWithException(v,date,key());
        } catch(Exception e) {
            parent().validationFailedWithException(e,date,key());
        }
    }

    private static String _datePickerJavaScriptUrl;
    public String datePickerJavaScriptUrl() {
        if (_datePickerJavaScriptUrl==null) {
            _datePickerJavaScriptUrl= application().resourceManager().urlForResourceNamed("date-picker.js",
                                                                                          "ERExtensions",
                                                                                          null,
                                                                                          context().request());
        }
        return _datePickerJavaScriptUrl;
    }
}
