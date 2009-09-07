/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* NSObjectPropertyAccessor.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import java.util.Map;

import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.PropertyAccessor;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSValidation;

public class NSObjectPropertyAccessor implements PropertyAccessor {

    public Object getProperty(Map map, Object target, Object name) throws OgnlException {
        return getProperty(target, name);
    }
    
    public Object getProperty(Object target, Object name) throws OgnlException {
        return NSKeyValueCoding.Utility.valueForKey(target, (String)name);
    }

    public void setProperty(Object target, Object name, Object value ) throws OgnlException {
        try {
            if (target instanceof NSValidation)
                ((NSValidation)target).validateTakeValueForKeyPath(value, (String)name);
            else
                NSKeyValueCoding.Utility.takeValueForKey(target, value, (String)name);
        } catch (Exception e) {
            throw new OgnlException(name.toString(), e);
        }
    }

    public void setProperty(Map map, Object target, Object name, Object value) throws OgnlException {
        setProperty(target, name, value);
    }

	public String getSourceAccessor(OgnlContext context, Object target, Object name) {
		context.put("_noRoot", "true");
		return "com.webobjects.foundation.NSKeyValueCoding.Utility#valueForKey($2, " + name + ")";
	}

	public String getSourceSetter(OgnlContext context, Object target, Object name) {
		if (target instanceof NSValidation)
			return ".validateTakeValueForKeyPath($3," + name + ")";
		context.put("_noRoot", "true");
		return "com.webobjects.foundation.NSKeyValueCoding.Utility#takeValueForKey($2,$3," + name + ")";
	}
}
