/*
 * WOTableString.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 *
 * IMPORTANT:  This Apple software is supplied to you by Apple Computer,
 * Inc. (“Apple”) in consideration of your agreement to the following 
 * terms, and your use, installation, modification or redistribution of 
 * this Apple software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Apple software.
 *
 * In consideration of your agreement to abide by the following terms, 
 * and subject to these terms, Apple grants you a personal, non-
 * exclusive license, under Apple’s copyrights in this original Apple 
 * software (the “Apple Software”), to use, reproduce, modify and 
 * redistribute the Apple Software, with or without modifications, in 
 * source and/or binary forms; provided that if you redistribute the  
 * Apple Software in its entirety and without modifications, you must 
 * retain this notice and the following text and disclaimers in all such
 * redistributions of the Apple Software.  Neither the name, trademarks,
 * service marks or logos of Apple Computer, Inc. may be used to endorse
 * or promote products derived from the Apple Software without specific
 * prior written permission from Apple.  Except as expressly stated in
 * this notice, no other rights or licenses, express or implied, are
 * granted by Apple herein, including but not limited to any patent
 * rights that may be infringed by your derivative works or by other
 * works in which the Apple Software may be incorporated.
 *
 * The Apple Software is provided by Apple on an "AS IS" basis.  APPLE 
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS
 * USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 
 *
 * IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT,
 * INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE,
 * REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE,
 * HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
 * NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/*
 this component exists because browsers are displaying tables in a slightly odd fashion: in a cell that does not contain
 anything, no borders are drawn which make a page which contains eo with empty properties in a table look odd.
 this component just puts out an &nbsp when the string is nil
 */

/**
 * @deprecated
 * WOTableString is no longer supported
 */
public class WOTableString extends WOComponent {
    protected Object _value;
    protected String oldFormatString = null;
    protected NSTimestampFormatter _cachedTSFormatter = null;

    public WOTableString(WOContext aContext)  {
        super(aContext);
    }

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

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        _resetInternalCaches();
        super.appendToResponse(aResponse, aContext);
    }
}
