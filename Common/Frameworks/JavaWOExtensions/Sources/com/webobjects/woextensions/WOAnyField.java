/*
 * WOAnyField.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

import java.text.ParseException;

public class WOAnyField extends WOComponent {
    protected static String DEFAULT_DATE_FORMAT = "YYYY/MM/DD";
    protected static String DEFAULT_NUMBER_FORMAT = "0";

    protected String _relationshipKey;
    protected String _selectedKey;
    protected String _selectedOperator;
    // ivars for PopUp
    public String selectedKeyItem;
    public String selectedOperatorItem;
    protected Object _value;
    protected WODisplayGroup _displayGroup;

    public WOAnyField(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public String relationshipKey() {
        if (null==_relationshipKey) {
            _relationshipKey = (String) _WOJExtensionsUtil.valueForBindingOrNull("relationshipKey",this);            
        }
        return _relationshipKey;
    }

    public String selectedKey() {
        if (null==_selectedKey)
            _selectedKey = (String)_WOJExtensionsUtil.valueForBindingOrNull("selectedKey",this);
        return _selectedKey;
    }

    public void setSelectedKey(String key) {
        _selectedKey = key;
    }

    public String valueClassNameForKey(String key) {
        String entityName = (String)_WOJExtensionsUtil.valueForBindingOrNull("sourceEntityName",this);
        EOModelGroup modelGroup = EOModelGroup.defaultGroup();
        EOEntity entity = modelGroup.entityNamed(entityName);
        EOAttribute selectedAttribute = null;
        if (relationshipKey()!=null) {
            EORelationship relationship = entity.relationshipNamed(relationshipKey());
            EOEntity destinationEntity = relationship.destinationEntity();
            selectedAttribute = destinationEntity.attributeNamed(key);
        } else
            selectedAttribute = entity.attributeNamed(key);
        return selectedAttribute.className();
    }

    public String formatterForKey(String key) {
        String formatter = null;
        if (hasBinding("formatter")) {
            setValueForBinding(key, "key");
            formatter = (String)_WOJExtensionsUtil.valueForBindingOrNull("formatter",this);
        }
        if (null==formatter) {
            String className=valueClassNameForKey(key);
            if (className.equals("com.webobjects.foundation.NSTimestamp"))
                formatter=DEFAULT_DATE_FORMAT;
            else if (className.equals("java.lang.Number") || className.equals("java.math.BigDecimal"))
                formatter=DEFAULT_NUMBER_FORMAT;
        }
        return formatter;
    }

    public WODisplayGroup displayGroup() {
        if (null==_displayGroup)
            _displayGroup = (WODisplayGroup) _WOJExtensionsUtil.valueForBindingOrNull("displayGroup",this);
        return _displayGroup;
    }

    public String selectedOperator() {
        return _selectedOperator;
    }

    public void setSelectedOperator(String anOperator) {
        if (anOperator.equals("="))
            _selectedOperator="";
        else _selectedOperator=anOperator;
    }

    public Object value() {
        if (null==_value)
            _value=valueForBinding("value");
        return _value;
    }

    public void setValue(Object newValue) {
        WODisplayGroup displayGroup=displayGroup();
        if (displayGroup!=null) {
            if (relationshipKey()!=null) {
                displayGroup.queryMatch().takeValueForKey(newValue, relationshipKey()+"."+selectedKey());
                if (newValue!=null) {
                   displayGroup.queryOperator().takeValueForKey( selectedOperator(), relationshipKey()+"."+selectedKey());
                }
            } else {
                displayGroup.queryMatch().takeValueForKey(newValue, selectedKey());
                if (newValue!=null) {
                    displayGroup.queryOperator().takeValueForKey( selectedOperator(), selectedKey());
                }
            }
        }
    }

    public String textFieldValue() {
        if (value()!=null) {
            java.text.Format formatter=null;
            String className = valueClassNameForKey(selectedKey());

            if (className.equals("com.webobjects.foundation.NSTimestamp")) {
               String dateFormatterString = formatterForKey(selectedKey());
                String errorMessage = "";
                Object objectValue;

                formatter = new NSTimestampFormatter(dateFormatterString);
            } else if (className.equals("java.lang.Number") || className.equals("java.math.BigDecimal")) {
                String numberFormatterString= formatterForKey(selectedKey());
                formatter= new NSNumberFormatter(numberFormatterString);
            }
            
            return (formatter!=null) ? formatter.format(value()) : value().toString();
        } else
            return null;
    }

    public void setTextFieldValue(String value) {
        String className= valueClassNameForKey
        (selectedKey());
        if (className.equals("com.webobjects.foundation.NSTimestamp")) {
            String dateFormatterString = formatterForKey(selectedKey());
            NSTimestampFormatter dateFormatter;
            String errorMessage = "";
            Object objectValue;

            dateFormatter = new NSTimestampFormatter(dateFormatterString);
            try {
                objectValue = dateFormatter.parseObject((value!=null)?value.toString():"");
            } catch (ParseException e) {
                objectValue = null;
                errorMessage = e.getMessage();
            }
            setValue(objectValue);
        } else if (className.equals("java.lang.Number") || className.equals("java.math.BigDecimal")) {
            String numberFormatterString= formatterForKey(selectedKey());
            NSNumberFormatter numberFormatter;
            String errorMessage = "";
            Object objectValue;

            numberFormatter= new NSNumberFormatter(numberFormatterString);
            try {
                objectValue = numberFormatter.parseObject((value!=null)?value.toString():"");
            } catch (ParseException e) {
                objectValue = null;
                errorMessage = e.getMessage();
            }
            setValue(objectValue);
        } else {
            // Assume String
            setValue(value);
        }
    }

    public void invalidateCaches() {
        // In order for this to behave like an element, all instance
        // variables need to be flushed before this components is used again
        // so that it will pull via association.
        _relationshipKey = null;
        _selectedKey = null;
        _selectedOperator = null;
        _value = null;
        _displayGroup = null;
    }

    public void finalize() throws Throwable {
        super.finalize();
        invalidateCaches();	
    }


    public void reset() {
        invalidateCaches();	
    }
}
