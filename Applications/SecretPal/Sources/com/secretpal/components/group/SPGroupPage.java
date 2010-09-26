package com.secretpal.components.group;

import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPEvent;
import com.secretpal.model.SPGroup;
import com.webobjects.appserver.WOContext;

public class SPGroupPage extends SPPage {
	private SPGroup _group;
	public SPEvent _event;

	public SPGroupPage(WOContext context) {
		super(context);
	}

	public void setGroup(SPGroup group) {
		_group = group;
	}

	public SPGroup group() {
		return _group;
	}
	
	public boolean canEdit() {
		return _group.canEdit(session().currentPerson().localInstanceIn(_group.editingContext()));
	}
}