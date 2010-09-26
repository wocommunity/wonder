package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;

public class SPPage extends SPComponent {

	public SPPage(WOContext context) {
		super(context);
	}
	
	@Override
	protected boolean isPageAccessAllowed() {
		return true;
	}

	protected boolean isAuthenticationRequired() {
		return true;
	}
	
	@Override
	protected void checkAccess() throws SecurityException {
		if (isAuthenticationRequired() && (!context().hasSession() || session().currentPerson() == null)) {
			throw new SecurityException("You must be logged in to access this page.");
		}
		super.checkAccess();
	}
}
