/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;

/**
 * This component will display is content (as in WOComponentContent) if
 * it detects cookies are disabled. This can be useful for displaying
 * messages to the user that they should have cookies enabled in order
 * to use different features of the application.<br/>
 * <br/>
 * No Bindings
 */

// ENHANCEME: Should support the negate binding
public class ERXJSCookiesConditional extends WOComponent {

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXJSCookiesConditional(WOContext aContext) {
        super(aContext);
    }

    /**
     * Component is stateless
     * @return true
     */
    public boolean isStateless() { return true; }

    /**
     * First part of the javascript to check if cookies are
     * enabled.
     * @return first part of the js string
     */
    public String string1() {
        return "<script LANGUAGE=\"JavaScript\">\n"+
        "<!--\n"+
        "if (top.frames.length != 0) {\n"+
        "   top.location = self.document.location\n"+
        "}\n"+
        "var sessionCookies = true\n"+
        "var persistentCookies = true\n"+
        "document.cookie = \"session=on\"\n"+
        "if (document.cookie.indexOf(\"session=on\") == -1){\n"+
        "sessionCookies = false\n"+
        " \n}"+
        " var exp = new Date()\n"+
        "var oneYearFromNow = exp.getTime() + (365*24*60*60*1000)\n"+
        "exp.setTime(oneYearFromNow)\n"+
        "document.cookie= \"persistent=on; expires=\" + exp.toGMTString();\n"+
        "if (document.cookie.indexOf(\"persistent=on\") == -1 ){\n"+
        "persistentCookies = false\n"+
        "}\n"+
        "if ( !persistentCookies || !persistentCookies ) {\n"+
        "document.write(\"";
    }

    /**
     * Second part of the js string
     * @return second part of the js string.
     */
    public String string2() {
        return "\")\n"+
            "}\n"+
            "//-->\n"+
            "</script>\n";
    }
}