/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.foundation.NSMutableArray;

/**
 * Generic edit or default value component, uses other components like editNumber or editString.
 * @d2wKey displayValueForNull
 * @d2wKey innerComponentName
 * @d2wKey defaultValue
 */
public class ERD2WEditOrDefault extends D2WComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WEditOrDefault(WOContext context) {super(context);}
    
    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WEditOrDefault.class);
    
    public String radioButtonGroupName() { return name() +"_"+d2wContext().propertyKey(); }
    
    public String editSelectionValue() { return "1"; }
    public String defaultSelectionValue() { return "0"; }

    protected String _selectionValue;
    public String selectionValue() { return _selectionValue; }
    public void setSelectionValue(String value) { _selectionValue = value; }

    public Object defaultValue() { return d2wContext().valueForKey("defaultValue"); }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        if (_selectionValue == null) {
            if ((defaultValue() == null && !objectPropertyValueIsNonNull()) || (defaultValue() != null &&
                                                                                objectPropertyValueIsNonNull() &&
                                                                                                defaultValue().equals(objectPropertyValue())))
                _selectionValue = defaultSelectionValue();
            else
                _selectionValue = editSelectionValue();
        }
        super.appendToResponse(r, c);
    }
    
    protected NSMutableArray validationExceptions = new NSMutableArray();
    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        validationExceptions.removeAllObjects();
        super.takeValuesFromRequest(r, c);
        if (selectionValue().equals(defaultSelectionValue())) {
            object().takeValueForKeyPath(defaultValue(), propertyKey());
        } else if (validationExceptions.count() > 0) {
            for (Enumeration e = validationExceptions.objectEnumerator(); e.hasMoreElements();) {
                ValidationExceptionHolder h = (ValidationExceptionHolder)e.nextElement();
                parent().validationFailedWithException(h.throwable, h.value, h.keyPath);
            }
        }
    }

    @Override
    public void validationFailedWithException(Throwable e, Object value, String keyPath) {
        validationExceptions.addObject(new ValidationExceptionHolder(e, value, keyPath));
    }

    public static class ValidationExceptionHolder {
        public Throwable throwable;
        public Object value;
        public String keyPath;

        public ValidationExceptionHolder(Throwable e, Object value, String keyPath) {
            throwable = e;
            this.value = value;
            this.keyPath = keyPath;
        }
    }
}
