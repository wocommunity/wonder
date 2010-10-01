package com.webobjects.monitor.rest;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.application.Session;
import com.webobjects.monitor.application.WOTaskdHandler;

import er.rest.routes.ERXDefaultRouteController;

public class JavaMonitorController extends ERXDefaultRouteController {

	public JavaMonitorController(WORequest request) {
		super(request);
	}
		
    protected MSiteConfig siteConfig() {
        return WOTaskdHandler.siteConfig();
    }

    public WOTaskdHandler handler() {
    	return new WOTaskdHandler(mySession());
    }
    
    public Session mySession() {
        return (Session) super.session();
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
