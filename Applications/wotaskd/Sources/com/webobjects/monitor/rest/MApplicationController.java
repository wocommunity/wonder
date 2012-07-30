package com.webobjects.monitor.rest;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;

import er.extensions.eof.ERXKeyFilter;

public class MApplicationController extends JavaMonitorController {

	public MApplicationController(WORequest request) {
		super(request);
	}

	public WOActionResults createAction() throws Throwable {
		checkPassword();
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		MApplication application = create(filter);
		siteConfig().addApplication_M(application);
		if (siteConfig().hostArray().count() != 0) {
		  siteConfig().addApplication_W(application);
		}		
		return response(application, filter);
	}

	public WOActionResults destroyAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("mApplication");		
		deleteApplication(application);
		return response(application, ERXKeyFilter.filterWithNone());
	}

	public WOActionResults indexAction() throws Throwable {
		checkPassword();
		return response(siteConfig().applicationArray(), ERXKeyFilter.filterWithAttributes());
	}

	public WOActionResults showAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("mApplication");	
		return response(application, ERXKeyFilter.filterWithAttributes());
	}

	public WOActionResults updateAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("mApplication");		
		update(application, ERXKeyFilter.filterWithAttributes());
		return response(application, ERXKeyFilter.filterWithAttributes());
	}

	public WOActionResults addInstanceAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("name");
		// Old code. The if statement replaces this code along with the addInstanceOnAllHostsAction() method. kib 20110622
		//		addInstance(application, (MHost)routeObjectForKey("host"), false);
		if (request().stringFormValueForKey("host") != null) {
			MHost mHost = siteConfig().hostWithName(request().stringFormValueForKey("host"));
			addInstance(application, mHost, false);
		} else
			addInstance(application, null, true);
		return response(application, ERXKeyFilter.filterWithNone());
	}

	public WOActionResults deleteInstanceAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("name");
		deleteInstance(application, Integer.valueOf(request().stringFormValueForKey("id")));
		return response(application, ERXKeyFilter.filterWithNone());
	}

	private void addInstance(MApplication application, MHost host, boolean addToAllHosts) {
		try {
			if (addToAllHosts) {
				for (MHost aHost : siteConfig().hostArray()) {
					siteConfig().addInstances_M(aHost, application, 1);
				}
			} else {
        siteConfig().addInstances_M(host, application, 1);
			}
		} finally {
		}
	}

	private void deleteInstance(MApplication application, Integer instanceId) {
		final MInstance instance = application.instanceWithID(instanceId);
		try {
			siteConfig().removeInstance_M(instance);
		} finally {
		}
	}

	private void deleteApplication(MApplication application) {
		try {
			siteConfig().removeApplication_M(application);
		} finally {
		}
	}

}
