/*
 * WOTableString.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSTimestampFormatter;

/*
 this component exists because browsers are displaying tables in a slightly odd fashion: in a cell that does not contain
 anything, no borders are drawn which make a page which contains eo with empty properties in a table look odd.
 this component just puts out an &nbsp when the string is nil
 */

/**
 * @deprecated
 * WOTableString is no longer supported
 */
@Deprecated
public class WOTableString extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected Object _value;
    protected String oldFormatString = null;
    protected NSTimestampFormatter _cachedTSFormatter = null;

    public WOTableString(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public Object value()  {
        if (_value==null) {
            _value=valueForBinding("value");
        }
        return _value;
    }

    public boolean valueIsNonNull()  {
        Object v=value();
        return (((v instanceof String) && ((String)v).length()!=0) || (v!=null));
    }

    public java.text.Format formatter() {
        String formatString;

        if (hasBinding("formatter"))
            return (java.text.Format)_WOJExtensionsUtil.valueForBindingOrNull("formatter",this);
        formatString = (String)_WOJExtensionsUtil.valueForBindingOrNull("numberformat",this);
        
        if (formatString!=null)
            return new NSNumberFormatter(formatString);
        
        formatString = (String)_WOJExtensionsUtil.valueForBindingOrNull("dateformat",this);

        if (formatString!=null) {
            if (!formatString.equals(oldFormatString)) {
                oldFormatString = formatString;
                _cachedTSFormatter = new NSTimestampFormatter(formatString);
            }
            return _cachedTSFormatter;
        }
        return null;
   }

    protected void _resetInternalCaches() {
        // ** By setting these to nil, we allow for cycling of the page)
        _value = null;
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        _resetInternalCaches();
        super.appendToResponse(aResponse, aContext);
    }
}
