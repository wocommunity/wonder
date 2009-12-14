package com.webobjects.jdbcadaptor;

import er.h2.jdbcadaptor.ERH2PlugIn;

/**
 * This is a default plugIn for the H2 database protocol.
 * 
 * @author ldeck
 */
public class H2PlugIn extends ERH2PlugIn {
	
	public H2PlugIn(final JDBCAdaptor adaptor) {
		super(adaptor);
	}
	
}
