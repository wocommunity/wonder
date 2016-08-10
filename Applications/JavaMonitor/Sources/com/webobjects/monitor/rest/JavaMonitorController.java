package com.webobjects.monitor.rest;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.application.Session;
import com.webobjects.monitor.application.WOTaskdHandler;

import er.rest.routes.ERXDefaultRouteController;

public class JavaMonitorController extends ERXDefaultRouteController {

    private WOTaskdHandler _handler;

	public JavaMonitorController(WORequest request) {
		super(request);
		_handler = new WOTaskdHandler(mySession());
	}
		
	protected MSiteConfig siteConfig() {
		return WOTaskdHandler.siteConfig();
	}

	public WOTaskdHandler handler() {
		return _handler;
	}
    
    public Session mySession() {
        return (Session) super.session();
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
