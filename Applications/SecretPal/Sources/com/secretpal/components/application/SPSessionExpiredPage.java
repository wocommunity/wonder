package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;

public class SPSessionExpiredPage extends SPPage {
	public SPSessionExpiredPage(WOContext context) {
		super(context);
	}

	@Override
	protected boolean isAuthenticationRequired() {
		return false;
	}
}