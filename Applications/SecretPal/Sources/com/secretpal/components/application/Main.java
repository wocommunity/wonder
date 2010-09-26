package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXRedirect;

public class Main extends SPPage {
	public Main(WOContext context) {
		super(context);
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (session().currentPerson() != null) {
			ERXRedirect redirect = pageWithName(ERXRedirect.class);
			redirect.setDirectActionName("default");
			redirect.appendToResponse(response, context);
		}
		else {
			super.appendToResponse(response, context);
		}
	}
	
	@Override
	protected boolean isAuthenticationRequired() {
		return false;
	}
}
