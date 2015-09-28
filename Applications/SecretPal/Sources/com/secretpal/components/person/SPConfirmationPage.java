package com.secretpal.components.person;

import org.apache.commons.lang3.ObjectUtils;

import com.secretpal.components.application.Main;
import com.secretpal.components.application.SPPage;
import com.secretpal.components.group.SPHomePage;
import com.secretpal.model.SPMembership;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXStringUtilities;

public class SPConfirmationPage extends SPPage {
	private SPMembership _membership;
	public String _password;
	public String _confirmPassword;

	public SPConfirmationPage(WOContext context) {
		super(context);
	}

	public void setMembership(SPMembership membership) {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		_membership = membership.localInstanceIn(editingContext);
	}

	public SPMembership membership() {
		return _membership;
	}

	@Override
	protected boolean isAuthenticationRequired() {
		return false;
	}

	public WOActionResults accept() {
		if (session().errors().hasNotices()) {
			return null;
		}
		
		WOActionResults nextPage;

		// MS: This password checking is some repetitive bullshit ... It needs to be somewhere more centralized, but
		// they're all just SLLIIGGHTTLY different. They also can't quite be in EO validation because we actually
		// ALLOW a null password, but just not when entered by a user.
		if (_password != null) {
			if (ObjectUtils.notEqual(_password, _confirmPassword)) {
				_password = null;
				_confirmPassword = null;
				session().errors().addNotice("Your password confirmation didn't match.");
				return null;
			}

			_membership.person().setPlainTextPassword(_password);
		}

		if (ERXStringUtilities.nullForEmptyString(_membership.person().password()) == null) {
			session().errors().addNotice("You must set your password before accepting this invitation.");
			return null;
		}

		_membership.acceptInvitation();

		try {
			_membership.editingContext().saveChanges();
			session().setCurrentPerson(_membership.person());
			nextPage = pageWithName(SPHomePage.class);
		} catch (Throwable t) {
			session().errors().addNotice(t.getMessage());
			nextPage = null;
		}

		return nextPage;
	}

	public WOActionResults decline() {
		SPMembership.declineInvitation(_membership);
		session().notifications().addNotice("Thank you. You have declined your invitation.");
		return pageWithName(Main.class);
	}
}
