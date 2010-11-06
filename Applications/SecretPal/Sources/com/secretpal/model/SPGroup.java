package com.secretpal.model;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOControlUtilities;

public class SPGroup extends _SPGroup {
	private static Logger log = Logger.getLogger(SPGroup.class);

	public SPMembership membershipForPerson(SPPerson person) {
		NSArray<SPMembership> memberships = memberships(SPMembership.PERSON.is(person));
		return memberships.count() == 0 ? null : memberships.lastObject();
	}

	public boolean canEdit(SPPerson currentPerson) {
		return currentPerson.admin().booleanValue() || ERXEOControlUtilities.eoEquals(owner(), currentPerson);
	}

	public SPMembership invite(String emailAddress) {
	  String cleanedEmailAddress = emailAddress.replaceAll("[()'\\[\\]<> ]", "");
		EOEditingContext editingContext = editingContext();
		SPPerson person = SPPerson.fetchSPPerson(editingContext, SPPerson.EMAIL_ADDRESS.likeInsensitive(cleanedEmailAddress));
		if (person == null) {
			int atIndex = cleanedEmailAddress.indexOf('@');
			String name = atIndex == -1 ? cleanedEmailAddress : cleanedEmailAddress.substring(0, atIndex);
			person = SPPerson.createSPPerson(editingContext, Boolean.FALSE, cleanedEmailAddress, Boolean.FALSE, name);
		}
		SPMembership membership = membershipForPerson(person);
		if (membership == null) {
			membership = SPMembership.createSPMembership(editingContext, Boolean.FALSE, Boolean.FALSE, this, person);
		}
		return membership;
	}
}
