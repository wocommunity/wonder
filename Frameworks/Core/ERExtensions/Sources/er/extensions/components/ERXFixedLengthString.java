/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

/**
 * This stateless component is useful for displaying a
 * string of a fixed length. For example imagine you have
 * the string: 'The brown dog jumped' and for a given table
 * cell you only want to display at most 10 characters of the
 * string, then using this component you could bind the given
 * string to the 'value' binding, 10 to the 'length' binding
 * and the string '...' to the 'suffixWhenTrimmed' binding.
 * When rendering this would display:<br/>
 * The brown ...
 * <br/>
 * This component can also be used to pad whitespace onto the
 * end of strings that are shorter than the given length.
 * <br/>
 * Synopsis:<br/>
 * value=<i>aString</i>;length=<i>aNumber</i>;[shouldPadToLength=<i>aBoolean</i>;][suffixWhenTrimmed=<i>aString</i>;][escapeHTML=<i>aBoolean</i>;]
 *
 * @binding value string that is passed in to display in a fixed
 *		length setting.
 * @binding length fixed length that is compared to the length of
 *		the passed in string.
 * @binding shouldPadToLength boolean binding to indicate if the
 *		string to be displayed is shorter than the fixed
 *		length if it should then be padded with white space.
 * @binding suffixWhenTrimmed only appended to the end of the string
 *		if characters are trimmed from the end of the string
 *		to be displayed
 * @binding escapeHTML replace the entities &gt; and &amp; with their
 * 		escape codes (like WOString does). When this is set to
 *		true, all HTML text is cleared from the string first to
 *		prevent half-open tags
 */
public class ERXFixedLengthString extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * flag to indicate if characters were trimmed from the end of
     * the passed in string.
     */
    protected boolean valueWasTrimmed = false;
    /** Holds the local cache for the calculated fixed length string */
    protected String _fixedLengthString;
    
    /**
     * Public constructor
     * @param context to be used
     */
    public ERXFixedLengthString(WOContext context) {
        super(context);
    }

    /**
     * Fixed length of the string to be displayed.
     * @return int value of the binding: <b>length</b>
     */
    public int length() {
        int i=intValueForBinding("length", 0);
        return i;
    }

    /**
     * Resets cached instance variables.
     */
    @Override
    public void reset() {
        super.reset();
        valueWasTrimmed = false;
        _fixedLengthString = null;
    }

    /**
     * Calculates the fixed length string from the string
     * passed in via the binding: <b>value</b>.
     * If the length of the value string is greater than
     * the int value of the <b>length</b> binding then
     * the string is trimmed to the fixed length. If the
     * string is shorter than the fixed length size and
     * the binding: <b>shouldPadToLength</b> is set to
     * true then whitespace is added to the end of the string
     * buffer.
     * @return fixed length version of the string passed in
     * 		via the <b>value</b> binding.
     */
    // ENHANCEME: Should support adding either &nbsp or ' '
    public String value() {
        if (_fixedLengthString == null) {
            String result;
            if(escapeHTML())
                result =(String)valueForBinding("value");
            else
                result = strippedValue();
            int l=length();
            if (l!=0 && result!=null) {
                int sl=result.length();
                if (sl!=l) {
                    if (sl<l) {
                        StringBuilder sb = new StringBuilder(result);
                        if (booleanValueForBinding("shouldPadToLength", true)) {
                            for (int i=sl; i<l; i++) sb.append(' ');
                        }
                        result=sb.toString();
                    } else {
                        valueWasTrimmed = true;
                        result=result.substring(0,l-1);
                    }
                }
            }
            _fixedLengthString = result;
        }
        return _fixedLengthString;
    }

    /**
     * Returns the value stripped from HTML tags if <b>escapeHTML</b> is false.
     * This makes sense because it is not terribly useful to have half-finished tags in your code.
     * Note that the "length" of the resulting string is not very exact.
     * 
     * @see er.extensions.foundation.ERXStringUtilities#strippedValue(String, int)
     * 
     * @return value stripped from tags.
     */
    public String strippedValue() {
        String value=(String)valueForBinding("value");
        return ERXStringUtilities.strippedValue(value, length());
    }
    
    /**
     * Returns the value for the binding: <b>suffixWhenTrimmed</b>
     * only if the string was trimmed.
     * @return optionally returns the suffix to be added to the end
     * 		of the string to be displayed.
     */
    public String suffixWhenTrimmed() {
        value();
        String result = null;
        if ((value() != null && valueWasTrimmed))
            result = (String)objectValueForBinding("suffixWhenTrimmed");
        return result;
    }
    
    /**
     * Returns the value for the binding: <b>escapeHTML</b>.
     * @return optionally returns the boolean value of the binding or TRUE of not given.
     */
    public boolean escapeHTML() {
        return booleanValueForBinding("escapeHTML", true);
    }
}
