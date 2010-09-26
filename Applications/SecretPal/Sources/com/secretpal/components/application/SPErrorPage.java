package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXExceptionUtilities;

public class SPErrorPage extends SPPage {
	private Throwable _throwable;

	public SPErrorPage(WOContext context) {
		super(context);
	}

	@Override
	protected boolean isAuthenticationRequired() {
		return false;
	}

	public void setException(Throwable throwable) {
		_throwable = ERXExceptionUtilities.getMeaningfulThrowable(throwable);
	}

	public Throwable getException() {
		return _throwable;
	}
}