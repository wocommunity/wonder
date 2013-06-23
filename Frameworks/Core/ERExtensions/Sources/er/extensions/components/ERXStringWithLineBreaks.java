/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import org.apache.commons.lang.StringUtils;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;

import er.extensions.foundation.ERXStringUtilities;

/**
 * <p>
 * Converts a string that has line breaks and tabs in it into a corresponding
 * HTML string with <code>&lt;br /&gt;</code> and (five of)
 * <code>&amp;nbsp;</code> instead. Useful, for example, for preserving line
 * breaks that are typed into a {@code WOTextBox}. Note that this component
 * renders its output via a {@code WOString} element with
 * {@code escapeHTML=false}, which is a security risk if the value being
 * rendered comes from an untrusted source.
 * </p>
 * 
 * <h3>Synopsis</h3>
 * <p>
 * value=<i>aString</i>;[valueWhenEmpty=<i>aString</i>;]
 * </p>
 * 
 * @binding value string to be converted
 * @binding valueWhenEmpty what to display when <code>value</code> is null or
 *          empty
 */
public class ERXStringWithLineBreaks extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Holds the HTML-ified string
	 */
    public String _value;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            current context
	 */
    public ERXStringWithLineBreaks(WOContext context) {
        super(context);
    }
    
	/**
	 * Nulls out cached instance variable: _value
	 */
    @Override
    public void reset() {
        super.reset();
        _value = null;
    }

	/**
	 * Converts '<code>\r\n</code>', '<code>\n</code>', '<code>\r</code>' into '
	 * <code>&lt;br /&gt;</code>' and converts '<code>\t</code>' into five
	 * non-breaking spaces.
	 * 
	 * @return converted string
	 */
    // FIXME: Should use ERXSimpleHTMLFormatter
    public String value() {
        if (_value == null) {
            Object value = objectValueForBinding("value");
            _value = valueToString(value);
        }
        return _value;
    }

    protected String valueToString(Object value) {
        String result = null;
        if (value != null) {
            result = (value instanceof String) ? (String)value : value.toString();
            result = WOMessage.stringByEscapingHTMLString(result);
            // FIXME: This could be optimized
            result = StringUtils.replace(result, "\r\n", "\r");
            result = StringUtils.replace(result, "\n", "\r");
            result = StringUtils.replace(result, "\r", br());
            result = StringUtils.replace(result, "\t", tabs());
        }
        return result;
    }
    
	/**
	 * Sets the value to be displayed. This is useful when you want to return a
	 * string from a DirectAction, for example, for debugging purposes.
	 * 
	 * @param newValue
	 *            Object to display
	 */
    public void setValue(Object newValue) {
        if(newValue != null) {
            _value = valueToString(newValue);
        }
    }
    
    public String br() {
        return "<br />";
    }

    public String tabs() {
        return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    }
    
	/**
	 * Returns binding {@code valueWhenEmpty}.
	 * 
	 * @return value to display when the string is empty
	 */
	public Object valueWhenEmpty() {
		return valueToString(objectValueForBinding("valueWhenEmpty"));
	}
}
