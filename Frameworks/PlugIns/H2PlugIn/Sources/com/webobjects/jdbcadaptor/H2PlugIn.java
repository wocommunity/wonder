package com.webobjects.jdbcadaptor;

/**
 * This is a default plugIn for the H2 database protocol.
 * 
 * 5.4 declares the same class name for the H2PlugIn. If your classpath isn't
 * exactly right, they'll win, so we pushed the real code into _H2PlugIn and
 * we set a custom principal class that registers the _ variant that is
 * "guaranteed" to not have collisions as the plugin for the "h2"
 * subprotocol.
 * 
 * @author ldeck
 */
public class H2PlugIn extends _H2PlugIn {
	public H2PlugIn(final JDBCAdaptor adaptor) {
		super(adaptor);
	}
}
