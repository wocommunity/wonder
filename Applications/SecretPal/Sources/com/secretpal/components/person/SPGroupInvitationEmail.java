package com.secretpal.components.person;

import com.secretpal.SPUtilities;
import com.secretpal.components.application.SPComponent;
import com.secretpal.model.SPMembership;
import com.webobjects.appserver.WOContext;

public class SPGroupInvitationEmail extends SPComponent {
	private SPMembership _membership;

	public SPGroupInvitationEmail(WOContext context) {
		super(context);
	}

	public void setMembership(SPMembership membership) {
		_membership = membership;
	}

	public SPMembership membership() {
		return _membership;
	}

	@Override
	protected boolean isPageAccessAllowed() {
		return true;
	}

	public String confirmationUrl() {
		return SPUtilities.confirmationUrl(_membership, context());
	}
}