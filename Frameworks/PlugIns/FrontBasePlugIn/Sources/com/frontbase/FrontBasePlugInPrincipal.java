package com.frontbase;

import com.webobjects.jdbcadaptor._FrontbasePlugIn;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

/**
 * 5.4 declares the same class name for the FrontBasePlugIn.  If your classpath isn't exactly right, they'll win,
 * so we pushed the real code into _FrontbasePlugIn and we set a custom principal class that registers the _variant
 * that is "guaranteed" to not have collisions as the plugin for the "frontbase" subprotocol.
 *  
 * @author mschrag
 */
public class FrontBasePlugInPrincipal {
	static {
		JDBCPlugIn.setPlugInNameForSubprotocol(_FrontbasePlugIn.class.getName(), "frontbase");
	}
}
