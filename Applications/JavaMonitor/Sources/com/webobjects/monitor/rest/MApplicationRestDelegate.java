package com.webobjects.monitor.rest;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MApplication;

import er.extensions.eof.ERXQ;

public class MApplicationRestDelegate extends JavaMonitorRestDelegate {

	protected Object _createObjectOfEntityWithID(EOClassDescription arg0,
			Object arg1) {
		return new MApplication((String)arg1, siteConfig());
	}

	protected Object _fetchObjectOfEntityWithID(EOClassDescription arg0,
			Object arg1) {
		return (siteConfig().applicationWithName((String)arg1));
	}

	protected boolean _isDelegateForEntity(EOClassDescription arg0) {
		return "MApplication".equals(arg0.entityName());
	}

	protected Object _primaryKeyForObject(EOClassDescription arg0, Object arg1) {
		NSArray<MApplication> objects = ERXQ.filtered(siteConfig().applicationArray(), ERXQ.is("name", arg1));
		return objects.size() == 0 ? null : objects.objectAtIndex(0);
	}

}
