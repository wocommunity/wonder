package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;

public class SPBacktrackErrorPage extends SPPage {
	public SPBacktrackErrorPage(WOContext context) {
		super(context);
	}

	@Override
	protected boolean isAuthenticationRequired() {
		return false;
	}
}