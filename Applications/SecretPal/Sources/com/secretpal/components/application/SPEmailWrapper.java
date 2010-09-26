package com.secretpal.components.application;

import com.webobjects.appserver.WOContext;

public class SPEmailWrapper extends SPComponent {
	public SPEmailWrapper(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
}