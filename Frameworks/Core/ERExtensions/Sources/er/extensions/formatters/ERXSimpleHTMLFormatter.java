/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.formatters;

import java.text.FieldPosition;
import java.text.ParsePosition;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import er.extensions.foundation.ERXStringUtilities;

/**
 * This is a simple class for converting ASCII strings to HTML and vice versa.
 * In the current implementation, all this class does is convert newlines to HTML breaks and
 * tab characters to HTML &lt;spacer&gt; tags.
 */
public class ERXSimpleHTMLFormatter extends java.text.Format {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public final static Logger log = Logger.getLogger(ERXSimpleHTMLFormatter.class);

    /** holds the HTML return string */
    private final static String HTMLReturn = "<br />";
    /** holds the ASCII return string*/
    // FIXME: Should support all the kinds of returns, see ERXStringWithLineBreaks
    private final static String ASCIIReturn = "\n";
    /** holds the ASCII tab string */
    private final static String ASCIITab = "\t";

    /** holds the reference to the shared formatter */
    private static ERXSimpleHTMLFormatter _formatter;

    /** holds a reference to the url of the spacer image */
    private static String _HTMLTab;    

    /**
     * Simple method used to get the url to the spacer gif
     * s.gif from the WOResourceManager.
     * @return url to the space gif image set in an image ref
     */
    protected static String HTMLTab() {
        if (_HTMLTab == null)
            // A pixel size of 50 is arbitrary
            // FIXME: Should be able to customize
            // FIXME: Should have all framework name references broken out into
            //		a single entry off the principal class.
            _HTMLTab = "<spacer width=\"50\" />";
        return _HTMLTab;
    }

    /**
     * Method used to retrieve the shared instance of the
     * html formatter.
     * @return shared instance of the html formatter
     */
    public static ERXSimpleHTMLFormatter formatter() {
        if (_formatter == null)
            _formatter = new ERXSimpleHTMLFormatter();
        return _formatter;
    }

    /**
     * Converts an ASCII string into an HTML
     * string.
     * @param aString to be converted
     * @return html-ified string
     */
    // CHECKME: Should this method be static?
    public String htmlStringFromString(String aString) {
        String returnString = "";
        try {
            returnString = formatter().format(aString);
        } catch (IllegalArgumentException e) {
            returnString = aString;
        }
        return returnString;
    }

    /**
     * The FieldPosition is not important, so this method
     * just calls <code>applyFormat</code> and appends that
     * string to the buffer.
     * @param object to be formatted
     * @param buffer to have the formatted object appended to
     * @param fp ignored parameter
     * @return buffer after having the format appended to it.
     */
    @Override
    public StringBuffer format(Object object, StringBuffer buffer, FieldPosition fp) {
        // The value of fp does not matter in this case.
        return buffer.append(applyFormat(object));
    }    

    /**
     * Applies the HTML formatting to a given string
     * object replacing ASCII formatting with HTML
     * formatting.
     * @param anObject to have the formatting applied to
     * @return formatted object
     */
    public String applyFormat(Object anObject) throws IllegalArgumentException {
        String newString;

        if (anObject == null || !(anObject instanceof String))
            return null;

        // Convert tabs in the argument (which must be a String) to HTML spacers.
        newString = StringUtils.replace((String)anObject, ASCIITab, HTMLTab());
        // Convert new-lines in the argument (which must be a String) to HTML breaks.
        return StringUtils.replace(newString, ASCIIReturn, HTMLReturn);
    }

    /**
     * Converts an HTML string into an ASCII string.
     * @param inString HTML string
     * @return ASCII-fied string
     */
    @Override
    public Object parseObject(String inString) throws java.text.ParseException {
        String newString;

        if(inString == null)
            return null;

        // Convert new-lines in the argument (which must be a String) to HTML breaks.
        newString = StringUtils.replace(inString, HTMLReturn, ASCIIReturn);
        // Convert tabs in the argument (which must be a String) to HTML spacers.
        return StringUtils.replace(newString, HTMLTab(), ASCIITab);
    }

    /**
     * Converts an HTML string into an ASCII string
     * starting from a given parse position.
     * @param string HTML string
     * @param p current parsing position
     * @return ASCII representation of the string
     */
    @Override
    public Object parseObject(String string, ParsePosition p) {
        int index = p.getIndex();
        String substring = string.substring(index);
        String result;
        try {
            result = (String)parseObject(substring);
            p.setIndex(string.length() + 1);
        } catch ( java.text.ParseException e) {
            result = null;
        }
        return result;
    }

    /**
     * Accessor method used to convert an ASCII
     * string into an HTML string.
     * @param anObject string to convert
     *
     */
    public String stringForObjectValue(Object anObject)
        throws IllegalArgumentException
    {  return applyFormat(anObject); }
}
