package com.secretpal.model;

import java.util.UUID;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class SPMembership extends _SPMembership {
	public String personName() {
		return confirmed().booleanValue() ? person().name() : person().emailAddress();
	}
	
	public boolean canDelete(SPPerson currentPerson) {
		return !ERXEOControlUtilities.eoEquals(currentPerson, person()) && (currentPerson.admin().booleanValue() || ERXEOControlUtilities.eoEquals(group().owner(), currentPerson) || ERXEOControlUtilities.eoEquals(person(), currentPerson));
	}

	public static void declineInvitation(SPMembership membership) {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		SPMembership localMembership = membership.localInstanceIn(editingContext);
		localMembership.person().setEmailDeliveryFailure(Boolean.FALSE);
		localMembership.delete();
		editingContext.saveChanges();
	}
	
	public void acceptInvitation() {
		setConfirmed(Boolean.TRUE);
		setConfirmationCode(null);
		person().setEmailDeliveryFailure(Boolean.FALSE);
	}

	public void resetConfirmation() {
		setConfirmed(Boolean.FALSE);
		setConfirmationCode(UUID.randomUUID().toString());
		person().setEmailDeliveryFailure(Boolean.FALSE);
	}
}
