/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;
import java.text.*;

/*
 This is a simple class for converting ASCII strings to HTML and vice versa.
 In the current implementation, all this class does is convert newlines to HTML breaks and
 tab characters to HTML <spacer> tags.
 */

public class ERXSimpleHTMLFormatter extends java.text.Format {

    ///////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXSimpleHTMLFormatter.class);

    public StringBuffer format(Object object, StringBuffer buffer, FieldPosition fp) {
        // The value of fp does not matter in this case.
        return buffer.append(applyFormat(object));
    }

    static private String HTMLReturn = "<br>";
    static private String ASCIIReturn = "\n";
    static private String ASCIITab = "\t";
    static private String _HTMLTab;
    protected static String HTMLTab() {
        if (_HTMLTab == null)
            // A pixel size of 50 is arbitrary
            _HTMLTab = "<img src=" + WOApplication.application().resourceManager().urlForResourceNamed("s.gif", "ERExtensions",null,null)
                + " width=50>";
        return _HTMLTab;
    }

    private static ERXSimpleHTMLFormatter _formatter;
    public static ERXSimpleHTMLFormatter formatter(){
        if (_formatter==null)
            _formatter=new ERXSimpleHTMLFormatter();
        return _formatter;
    }

    public String htmlStringFromString(String aString) {
        String returnString = "";
        try {
            returnString = formatter().format(aString);
        } catch (IllegalArgumentException e) {
            returnString = aString;
        }
        return returnString;
    }

    public static String replaceStringByStringInString(String old, String newString, String buffer) {
        int begin, end;
        int oldLength = old.length();
        int length = buffer.length();
        StringBuffer convertedString = new StringBuffer(length + 100);

        begin = 0;
        while(begin < length)
        {
            end = buffer.indexOf(old, begin);
            if(end == -1)
            {
                convertedString.append(buffer.substring(begin));
                break;
            }
            if(end == 0)
                convertedString.append(newString);
            else {
                convertedString.append(buffer.substring(begin, end));
                convertedString.append(newString);
            }
            begin = end+oldLength;
        }
        return convertedString.toString();
    }

    public String applyFormat(Object anObject) throws IllegalArgumentException {
        String newString;

        if(anObject == null || !(anObject instanceof String))
            return null;

        // Convert tabs in the argument (which must be a String) to HTML spacers.
        newString = replaceStringByStringInString(ASCIITab, HTMLTab(), (String)anObject);
        // Convert new-lines in the argument (which must be a String) to HTML breaks.
        return replaceStringByStringInString(ASCIIReturn, HTMLReturn, newString);
    }

    public Object parseObject(String inString) throws java.text.ParseException {
        String newString;

        if(inString == null)
            return null;

        // Convert new-lines in the argument (which must be a String) to HTML breaks.
        newString = replaceStringByStringInString(HTMLReturn, ASCIIReturn, inString);
        // Convert tabs in the argument (which must be a String) to HTML spacers.
        return replaceStringByStringInString(HTMLTab(), ASCIITab, newString);
    }

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

    public String stringForObjectValue(Object anObject)
        throws IllegalArgumentException
    {  return applyFormat(anObject); }
}
