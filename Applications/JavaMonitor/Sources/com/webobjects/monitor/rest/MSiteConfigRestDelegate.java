package com.webobjects.monitor.rest;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.monitor._private.MSiteConfig;



public class MSiteConfigRestDelegate extends JavaMonitorRestDelegate {

	protected Object _createObjectOfEntityWithID(EOClassDescription arg0,
			Object arg1) {
		return new MSiteConfig(null);
	}

	protected Object _fetchObjectOfEntityWithID(EOClassDescription arg0,
			Object arg1) {
		return siteConfig();
	}

	protected boolean _isDelegateForEntity(EOClassDescription arg0) {
		return "MSiteConfig".equals(arg0.entityName());
	}

	protected Object _primaryKeyForObject(EOClassDescription arg0, Object arg1) {
		return siteConfig();
	}

}
