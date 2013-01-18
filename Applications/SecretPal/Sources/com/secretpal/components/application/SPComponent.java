package com.secretpal.components.application;

import com.secretpal.Session;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class SPComponent extends ERXComponent {

	public SPComponent(WOContext context) {
		super(context);
	}
	
	@Override
	public void validationFailedWithException(Throwable t, Object value, String keyPath) {
		super.validationFailedWithException(t, value, keyPath);
		session().errors().addNotice(t.getMessage());
	}

	@Override
	protected boolean isPageAccessAllowed() {
		return false;
	}

	@Override
	public Session session() {
		return (Session) super.session();
	}

}
