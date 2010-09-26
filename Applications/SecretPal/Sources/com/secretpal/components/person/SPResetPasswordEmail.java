package com.secretpal.components.person;

import com.secretpal.SPUtilities;
import com.secretpal.components.application.SPComponent;
import com.secretpal.model.SPPerson;
import com.webobjects.appserver.WOContext;

public class SPResetPasswordEmail extends SPComponent {
	private SPPerson _person;

	public SPResetPasswordEmail(WOContext context) {
		super(context);
	}

	public void setPerson(SPPerson person) {
		_person = person;
	}

	public SPPerson person() {
		return _person;
	}

	public String resetPasswordUrl() {
		return SPUtilities.resetPasswordUrl(_person, context());
	}

	@Override
	protected boolean isPageAccessAllowed() {
		return true;
	}
}