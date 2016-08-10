package com.webobjects.monitor.rest;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;

import er.extensions.eof.ERXKeyFilter;

public class MApplicationController extends JavaMonitorController {

	public MApplicationController(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults createAction() throws Throwable {
		checkPassword();
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		MApplication application = create(filter);
		siteConfig().addApplication_M(application);
		if (siteConfig().hostArray().count() != 0) {
			handler().sendAddApplicationToWotaskds(application, siteConfig().hostArray());
		}		
		pushValues(application);
		return response(application, filter);
	}

	@Override
	public WOActionResults destroyAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("mApplication");		
		deleteApplication(application);
		return response(application, ERXKeyFilter.filterWithNone());
	}

	@Override
	public WOActionResults indexAction() throws Throwable {
		checkPassword();
		return response(siteConfig().applicationArray(), ERXKeyFilter.filterWithAttributes());
	}

	@Override
	public WOActionResults showAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("mApplication");	
		return response(application, ERXKeyFilter.filterWithAttributes());
	}

	@Override
	public WOActionResults updateAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("mApplication");		
		update(application, ERXKeyFilter.filterWithAttributes());
		pushValues(application);
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

	public WOActionResults addInstanceOnAllHostsAction() throws Throwable {
		checkPassword();
		MApplication application = (MApplication) routeObjectForKey("name");
		addInstance(application, null, true);
		return response(application, ERXKeyFilter.filterWithNone());
	}

	private void pushValues(MApplication application) {
		handler().startReading();
		try {
			application.pushValuesToInstances();
			if (siteConfig().hostArray().count() != 0) {
				handler().sendUpdateApplicationAndInstancesToWotaskds(application, siteConfig().hostArray());
			}
		} finally {
			handler().endReading();
		}
	}

	private void addInstance(MApplication application, MHost host, boolean addToAllHosts) {
		NSMutableArray newInstanceArray = new NSMutableArray();
		handler().startWriting();
		try {
			if (addToAllHosts) {
				for (MHost aHost : siteConfig().hostArray()) {
					newInstanceArray = siteConfig().addInstances_M(aHost, application, 1);
					handler().sendAddInstancesToWotaskds(newInstanceArray, siteConfig().hostArray());
				}
			} else {
				newInstanceArray = siteConfig().addInstances_M(host, application, 1);
				handler().sendAddInstancesToWotaskds(newInstanceArray, siteConfig().hostArray());
			}
		} finally {
			handler().endWriting();
		}
	}

	private void deleteInstance(MApplication application, Integer instanceId) {
		final MInstance instance = application.instanceWithID(instanceId);
		handler().startWriting();
		try {
			siteConfig().removeInstance_M(instance);
			if (siteConfig().hostArray().count() != 0) {
				handler().sendRemoveInstancesToWotaskds(new NSArray(instance), siteConfig().hostArray());
			}
		} finally {
			handler().endWriting();
		}
	}

	private void deleteApplication(MApplication application) {
		handler().startWriting();
		try {
			siteConfig().removeApplication_M(application);

			if (siteConfig().hostArray().count() != 0) {
				handler().sendRemoveApplicationToWotaskds(application, siteConfig().hostArray());
			}
		} finally {
			handler().endWriting();
		}
	}

}
