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

public class SPProfilePage extends SPPage {
	public SPPerson _person;
	public String _currentPassword;
	public String _password;
	public String _confirmPassword;

	public SPProfilePage(WOContext context) {
		super(context);
		EOEditingContext editingContext = ERXEC.newEditingContext();
		_person = session().currentPerson().localInstanceIn(editingContext);
	}

	public WOActionResults updateProfile() {
		if (session().errors().hasNotices()) {
			return null;
		}
		
		if (ERXStringUtilities.nullForEmptyString(_password) != null) {
			// MS: This password checking is some repetitive bullshit ... It needs to be somewhere more centralized, but
			// they're all just SLLIIGGHTTLY different. They also can't quite be in EO validation because we actually
			// ALLOW a null password, but just not when entered by a user.
			if (ObjectUtils.notEqual(SPPerson.hashPassword(_currentPassword), _person.password())) {
				_currentPassword = null;
				session().errors().addNotice("Your current password does not match the password you provided.");
				return null;
			}
			
			if (ERXStringUtilities.nullForEmptyString(_password) == null) {
				session().errors().addNotice("Your must set a new passwrd.");
				return null;
			}
			
			if (ObjectUtils.notEqual(_password, _confirmPassword)) {
				_password = null;
				_confirmPassword = null;
				session().errors().addNotice("Your password confirmation didn't match.");
				return null;
			}
			
			_person.setPlainTextPassword(_password);
		}
		
		_person.editingContext().saveChanges();
		session().notifications().addNotice("Your profile has been updated.");
		return pageWithName(Main.class);
	}
}
