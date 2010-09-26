package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;

public class PageWrapper extends SPComponent {
	public PageWrapper(WOContext context) {
		super(context);
	}

	public D2WContext d2wContext() {
		if (context().page() instanceof D2WPage) {
			D2WPage d2wPage = (D2WPage) context().page();
			return d2wPage.d2wContext();
		}
		return null;
	}
	
	@Override
	protected boolean shouldCheckAccess() {
		return true;
	}
	
	@Override
	protected void checkAccess() throws SecurityException {
		if (context().page() instanceof D2WPage && (session().currentPerson() == null || !session().currentPerson().admin().booleanValue())) {
			throw new SecurityException("You must be an administrator to access this page.");
		}
	}
	

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
}