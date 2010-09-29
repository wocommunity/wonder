package com.webobjects.monitor.rest;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MHost;

import er.extensions.eof.ERXQ;

public class MHostRestDelegate extends JavaMonitorRestDelegate {

	protected Object _createObjectOfEntityWithID(EOClassDescription arg0,
			Object arg1) {
		return new MHost(siteConfig(), (String)arg1, MHost.MAC_HOST_TYPE);
	}

	protected Object _fetchObjectOfEntityWithID(EOClassDescription arg0,
			Object arg1) {
		return (siteConfig().hostWithName((String)arg1));
	}

	protected boolean _isDelegateForEntity(EOClassDescription arg0) {
		return "MHost".equals(arg0.entityName());
	}

	protected Object _primaryKeyForObject(EOClassDescription arg0, Object arg1) {
		NSArray<MHost> objects = ERXQ.filtered(siteConfig().hostArray(), ERXQ.is("name", arg1));
		return objects.size() == 0 ? null : objects.objectAtIndex(0);
	}

}
