package com.webobjects.monitor.rest;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.wotaskd.Application;
import com.webobjects.monitor.wotaskd.LocalMonitor;

import er.rest.routes.ERXDefaultRouteController;

public class JavaMonitorController extends ERXDefaultRouteController {

    private LocalMonitor _handler;

	public JavaMonitorController(WORequest request) {
		super(request);
	}
		
	protected MSiteConfig siteConfig() {
		return myApplication().siteConfig();
	}
	
	public Application myApplication() {
	  return ( (Application) WOApplication.application());
	}

	public LocalMonitor handler() {
		return _handler;
	}

	public WOActionResults createAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public WOActionResults destroyAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public WOActionResults indexAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public WOActionResults newAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public WOActionResults showAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public WOActionResults updateAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}
    
	protected void checkPassword() throws SecurityException {
        String pw = context().request().stringFormValueForKey("pw");
        if(!siteConfig().compareStringWithPassword(pw)) {
			throw new SecurityException("Invalid password");
        }
	}
	  
}
