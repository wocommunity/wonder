package com.webobjects.jdbcadaptor;

/**
 * 5.4 declares the same class name for the DerbyPlugIn. If your classpath isn't
 * exactly right, they'll win, so we pushed the real code into _DerbyPlugIn and
 * we set a custom principal class that registers the _ variant that is
 * "guaranteed" to not have collisions as the plugin for the "derby"
 * subprotocol.
 * 
 * @author hprange guided by mschrag
 */
public class DerbyPlugIn extends _DerbyPlugIn {
	public DerbyPlugIn(final JDBCAdaptor adaptor) {
		super(adaptor);
	}
}
