/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.QueryComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.localization.ERXLocalizer;

/**
 * Query component for null or non-null.
 * 
 * @d2wKey choicesNames
 */
public class ERD2WQueryNonNull extends QueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WQueryNonNull(WOContext context) { super(context); }
    
    public Object item;
    public int index;

    private final static Object YES_VALUE=new Object(); 
    private final static Object NO_VALUE=new Object(); 
    private final static Object DONT_CARE_VALUE=new Object(); 
    private final static Object[] _queryNumbers={ DONT_CARE_VALUE, YES_VALUE, NO_VALUE };
    protected final static NSArray _queryNumbersArray=new NSArray(_queryNumbers);

    public NSArray choicesNames() {
        return (NSArray) d2wContext().valueForKey("choicesNames");
    }
    public NSArray queryNumbers() { return _queryNumbersArray; }
    public String displayString() { 
        String label = (String)choicesNames().objectAtIndex(index);
        if(ERXLocalizer.isLocalizationEnabled()) {
            return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(label); 
        }
        return label;
    }
    @Override
    public Object value() { 
        Object value = displayGroup().queryMatch().valueForKey(propertyKey());
        Object operator = displayGroup().queryOperator().valueForKey(propertyKey());
        if(value == null || operator == null) {
            return DONT_CARE_VALUE; 
        } else {
            if("<>".equals(operator)) {
                return YES_VALUE;
            }
            return NO_VALUE;
        }
    }

    @Override
    public void setValue(Object newValue) {
        if (newValue==DONT_CARE_VALUE) {
            displayGroup().queryMatch().takeValueForKey(null, propertyKey());
            displayGroup().queryOperator().takeValueForKey(null, propertyKey());            
        } else {
            displayGroup().queryMatch().takeValueForKey(NSKeyValueCoding.NullValue, propertyKey());
            displayGroup().queryOperator().takeValueForKey(newValue==YES_VALUE ? "<>" : "=", propertyKey());            
        }
    }
}
