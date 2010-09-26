package com.secretpal.components.application;

import com.secretpal.Session;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class SPComponent extends ERXComponent {

	public SPComponent(WOContext context) {
		super(context);
	}

	@Override
	protected boolean isPageAccessAllowed() {
		return false;
	}

	public Session session() {
		return (Session) super.session();
	}

}
