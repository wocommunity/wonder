package com.secretpal;

import com.secretpal.components.application.Main;
import com.secretpal.components.application.SPBacktrackErrorPage;
import com.secretpal.components.application.SPErrorPage;
import com.secretpal.components.application.SPSessionExpiredPage;
import com.secretpal.components.group.SPHomePage;
import com.secretpal.components.person.SPConfirmationPage;
import com.secretpal.components.person.SPForgotPasswordPage;
import com.secretpal.components.person.SPResetPasswordPage;
import com.secretpal.model.SPMembership;
import com.secretpal.model.SPPerson;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.eof.ERXEC;

public class DirectAction extends ERXDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public Session session() {
		return (Session) super.session();
	}

	@Override
	public WOActionResults defaultAction() {
		WOActionResults nextPage;
		if (session().currentPerson() != null) {
			nextPage = pageWithName(SPHomePage.class);
		} else {
			nextPage = pageWithName(Main.class);
		}
		return nextPage;
	}

	public WOActionResults adminAction() {
		return D2W.factory().defaultPage(session());
	}

	public WOActionResults forgotPasswordAction() {
		return pageWithName(SPForgotPasswordPage.class);
	}

	public WOActionResults errorAction() {
		return pageWithName(SPErrorPage.class);
	}

	public WOActionResults backtrackErrorAction() {
		return pageWithName(SPBacktrackErrorPage.class);
	}
	
	public WOActionResults sessionExpiredAction() {
		return pageWithName(SPSessionExpiredPage.class);
	}

	public WOActionResults loginAction() {
		session().logout();
		
		String emailAddress = request().stringFormValueForKey("emailAddress");
		String password = request().stringFormValueForKey("password");
		SPPerson person = SPPerson.fetchSPPerson(ERXEC.newEditingContext(), SPPerson.EMAIL_ADDRESS.is(emailAddress).and(SPPerson.PASSWORD.is(SPPerson.hashPassword(password))).and(SPPerson.PASSWORD.isNotNull()));
		WOActionResults nextPage;
		if (person != null) {
			session().setCurrentPerson(person);
			nextPage = pageWithName(SPHomePage.class);
		} else {
			session().errors().addNotice("There was no person found with that email address and password.");
			nextPage = pageWithName(Main.class);
		}
		return nextPage;
	}

	public WOActionResults confirmAction() {
		session().logout();

		WOActionResults nextPage;
		EOEditingContext editingContext = ERXEC.newEditingContext();
		String confirmationCode = request().stringFormValueForKey(SPUtilities.CONFIRMATION_CODE_KEY);
		if (confirmationCode == null || confirmationCode.trim().length() == 0) {
			session().errors().addNotice("You must provide a confirmation code to validate your membership.");
			nextPage = pageWithName(Main.class);
		} else {
			SPMembership membership = SPMembership.fetchSPMembership(editingContext, SPMembership.CONFIRMATION_CODE.is(confirmationCode));
			if (membership == null) {
				session().errors().addNotice("The validation code you are using is invalid. Please talk to the owner of your group to receive a new invite.");
				nextPage = pageWithName(Main.class);
			} else {
				SPConfirmationPage validationPage = pageWithName(SPConfirmationPage.class);
				validationPage.setMembership(membership);
				nextPage = validationPage;
			}
		}
		return nextPage;
	}
	
	@Override
	public WOActionResults logoutAction() {
		session().logout();
		return super.logoutAction();
	}

	public WOActionResults resetPasswordAction() {
		session().logout();
		
		WOActionResults nextPage;
		EOEditingContext editingContext = ERXEC.newEditingContext();
		String resetPasswordCode = request().stringFormValueForKey(SPUtilities.RESET_PASSWORD_CODE_KEY);
		if (resetPasswordCode == null || resetPasswordCode.trim().length() == 0) {
			session().errors().addNotice("You must provide a reset code to change your password.");
			nextPage = pageWithName(Main.class);
		} else {
			SPPerson person = SPPerson.fetchSPPerson(editingContext, SPPerson.PASSWORD.is(resetPasswordCode));
			if (person == null) {
				session().errors().addNotice("The reset code you are using is invalid.");
				nextPage = pageWithName(Main.class);
			} else {
				SPResetPasswordPage resetPasswordPage = pageWithName(SPResetPasswordPage.class);
				resetPasswordPage.setPerson(person);
				nextPage = resetPasswordPage;
			}
		}
		return nextPage;
	}
}
