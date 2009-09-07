/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* DictionaryPropertyAccessor.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import java.util.Map;

import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.PropertyAccessor;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class NSDictionaryPropertyAccessor implements PropertyAccessor {

    public Object getProperty(Map map, Object target, Object name) throws OgnlException {
        return getProperty(target, name);
    }    
    
    public Object getProperty(Object target, Object name) throws OgnlException {
        Object property = null;
        try {
            NSDictionary dictionary = (NSDictionary)target;
            property = dictionary.objectForKey(name);
        } catch (Exception ex) {
            throw new OgnlException(name.toString(), ex);
        }
        return property;
    }

    public void setProperty(Object target, Object name, Object value) throws OgnlException {
        try {
            ((NSMutableDictionary)target).setObjectForKey(value, name);
        } catch (Exception ex) {
            throw new OgnlException(name.toString(), ex);
        }
    }

    public void setProperty(Map map, Object target, Object name, Object value) throws OgnlException {
        setProperty(target, name, value);
    }

    public String getSourceAccessor(OgnlContext context, Object target, Object name) {
        return ".valueForKey(" + name +")";
    }

    public String getSourceSetter(OgnlContext context, Object target, Object name) {
        return ".takeValueForKey($3," + name + ")";
    }
}
