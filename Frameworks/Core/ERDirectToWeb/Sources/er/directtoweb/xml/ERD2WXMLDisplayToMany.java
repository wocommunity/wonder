/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.xml;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.foundation.NSArray;

/**
 * xml display component for to many relationships
 * 
 * @d2wKey propertyKey
 */
public class ERD2WXMLDisplayToMany extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WXMLDisplayToMany(WOContext context) { super(context); }
    
    public String value(){
        String result = "";
        Object value = object().valueForKeyPath((String)d2wContext().valueForKey("propertyKey"));
        if(value instanceof NSArray){
            if(((NSArray)value).count()>1)
                result = ((NSArray)value).componentsJoinedByString("&#10;");
            else if (((NSArray)value).count()==1)
                result = ((NSArray)value).objectAtIndex(0).toString();
            else
                result = null;
        }
        else if (value != null)
            result = value.toString();
        return result;
    }
}
