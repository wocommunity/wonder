package com.webobjects.monitor.wotaskd.rest.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.wotaskd.Application;

import er.rest.routes.ERXDefaultRouteController;

public class JavaMonitorController extends ERXDefaultRouteController {

	public JavaMonitorController(WORequest request) {
		super(request);
	}
		
	protected MSiteConfig siteConfig() {
		return application().siteConfig();
	}
	
	public Application application() {
	  return (Application )WOApplication.application();
	}

	@Override
	public WOActionResults createAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WOActionResults destroyAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WOActionResults indexAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WOActionResults newAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WOActionResults showAction() throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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
