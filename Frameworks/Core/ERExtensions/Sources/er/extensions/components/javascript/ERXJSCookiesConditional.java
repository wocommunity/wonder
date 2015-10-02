/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

/**
 * This component will display is content (as in WOComponentContent) if
 * it detects cookies are disabled. This can be useful for displaying
 * messages to the user that they should have cookies enabled in order
 * to use different features of the application.
 * <p>
 * The content is used as a JavaScript string literal, so it must all be on single line.
 * Any line breaks will result in invalid JavaScript.
 * 
 * @binding negate 
 */
public class ERXJSCookiesConditional extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXJSCookiesConditional(WOContext aContext) {
        super(aContext);
    }

    /**
     * First part of the javascript to check if cookies are
     * enabled.
     * @return first part of the js string
     */
    public String string1() {
        return "<script LANGUAGE=\"JavaScript\">\n"+
        "<!--\n"+
        "var sessionCookies = true\n"+
        "var persistentCookies = true\n"+
        "document.cookie = \"session=on\"\n"+
        "if (document.cookie.indexOf(\"session=on\") == -1) {\n"+
        "   sessionCookies = false\n"+
        "}"+
        "var exp = new Date()\n"+
        "var oneYearFromNow = exp.getTime() + (365*24*60*60*1000)\n"+
        "exp.setTime(oneYearFromNow)\n"+
        "document.cookie= \"persistent=on; expires=\" + exp.toGMTString();\n"+
        "if (document.cookie.indexOf(\"persistent=on\") == -1) {\n"+
        "   persistentCookies = false\n"+
        "}\n"+
        "if (" + ( negate() ? "sessionCookies && persistentCookies" : "!sessionCookies || !persistentCookies") + " ) {\n" +
        "  document.write(\'";
    }

    private boolean negate() {
		return booleanValueForBinding("negate");
	}

	/**
     * Second part of the js string
     * @return second part of the js string.
     */
    public String string2() {
        return "')\n"+
            "}\n"+
            "//-->\n"+
            "</script>\n";
    }
}