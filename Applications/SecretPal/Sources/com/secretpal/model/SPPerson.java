package com.secretpal.model;

import java.util.UUID;

import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSValidation;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class SPPerson extends _SPPerson {
	private static Logger log = Logger.getLogger(SPPerson.class);

	public static String hashPassword(String password) {
		return ERXStringUtilities.md5Hex(password, CharEncoding.UTF_8);
	}
	
	public void setPlainTextPassword(String password) {
		setPassword(SPPerson.hashPassword(password));
	}
	
	public boolean anyMembershipsConfirmed() {
		return memberships(SPMembership.CONFIRMED.isTrue()).count() > 0;
	}
	
	public void resetPassword() {
		setPassword(UUID.randomUUID().toString());
	}
	
	public String resetPasswordCode() {
		return password();
	}
	
	public NSArray<SPWish> desires() {
		return wishes(SPWish.SUGGESTED_BY.is(SPWish.SUGGESTED_FOR));
	}

	@Override
	public NSArray<SPWish> suggestions() {
		return wishes(SPWish.SUGGESTED_BY.isNot(SPWish.SUGGESTED_FOR));
	}
	
	public String validateEmailAddress(String emailAddress) {
		SPPerson existingPerson = SPPerson.fetchSPPerson(editingContext(), SPPerson.EMAIL_ADDRESS.likeInsensitive(emailAddress));
		if (existingPerson != null && !ERXEOControlUtilities.eoEquals(existingPerson, this)) {
			throw new NSValidation.ValidationException("There is already a user with the email address '" + existingPerson.emailAddress() + "'.");
		}
		return emailAddress;
	}
}
