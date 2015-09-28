package com.secretpal.components.person;

import org.apache.commons.lang3.ObjectUtils;

import com.secretpal.components.application.Main;
import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPPerson;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXStringUtilities;

public class SPResetPasswordPage extends SPPage {
	private SPPerson _person;
	public String _password;
	public String _confirmPassword;

	public SPResetPasswordPage(WOContext context) {
		super(context);
	}

	public void setPerson(SPPerson person) {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		_person = person.localInstanceIn(editingContext);
	}

	public SPPerson person() {
		return _person;
	}

	@Override
	protected boolean isAuthenticationRequired() {
		return false;
	}

	public WOActionResults resetPassword() {
		if (session().errors().hasNotices()) {
			return null;
		}
		
		// MS: This password checking is some repetitive bullshit ... It needs to be somewhere more centralized, but
		// they're all just SLLIIGGHTTLY different. They also can't quite be in EO validation because we actually
		// ALLOW a null password, but just not when entered by a user.
		if (ObjectUtils.notEqual(_password, _confirmPassword)) {
			_password = null;
			_confirmPassword = null;
			session().errors().addNotice("Your password confirmation didn't match.");
			return null;
		}
		
		if (ERXStringUtilities.nullForEmptyString(_password) == null) {
			session().errors().addNotice("You must set a password before logging in.");
			return null;
		}

		_person.setPlainTextPassword(_password);
		_person.editingContext().saveChanges();
		
		session().notifications().addNotice("Your password has been changed. Please login again.");

		return pageWithName(Main.class);
	}
}
