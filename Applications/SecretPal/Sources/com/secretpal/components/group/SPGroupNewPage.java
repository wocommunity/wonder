package com.secretpal.components.group;

import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPGroup;
import com.secretpal.model.SPMembership;
import com.secretpal.model.SPPerson;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;

public class SPGroupNewPage extends SPPage {
	private SPGroup _group;
	public SPMembership _membership;
	public String _invites;

	public SPGroupNewPage(WOContext context) {
		super(context);
	}

	public SPGroup group() {
		if (_group == null) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			SPPerson currentPerson = session().currentPerson().localInstanceIn(editingContext);
			_group = SPGroup.createSPGroup(editingContext, "New Group", currentPerson);
			SPMembership.createSPMembership(editingContext, Boolean.TRUE, Boolean.TRUE, _group, currentPerson);
		}
		return _group;
	}

	public WOActionResults addGroup() {
		if (session().errors().hasNotices()) {
			return null;
		}
		
		_group.editingContext().saveChanges();
		SPGroupEditPage groupPage = pageWithName(SPGroupEditPage.class);
		groupPage.setGroup(_group);
		return groupPage;
	}
}