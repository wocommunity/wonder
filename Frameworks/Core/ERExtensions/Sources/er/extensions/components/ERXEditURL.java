/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import java.net.URL;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * Used to edit urls with a default value.
 * 
 * @binding value The URL to edit (can be a string or a java.net.URL object). If not set, 
 * the default value is http://www.
 * @binding size A integer to set the size of the text field
 */
public class ERXEditURL extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXEditURL(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean isStateless() { return true; }
    public final static String DEFAULT="http://www.";

    public Object value() {
        Object result = valueForBinding("value");
        if(result instanceof URL)
            result = result.toString();
        if (result==null || ((String)result).length()==0)
            result=DEFAULT;
        return result;
    }

    public void setValue(Object newValue) {
        if (newValue!=null && newValue.equals(DEFAULT))
            newValue=null;
        setValueForBinding(newValue,"value");
    }
}
