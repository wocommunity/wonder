package com.secretpal.components.event;

import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPEvent;
import com.secretpal.model.SPGroup;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;

public class SPEventNewPage extends SPPage {
	private SPGroup _group;
	private SPEvent _event;

	public SPEventNewPage(WOContext context) {
		super(context);
	}

	@Override
	protected void checkAccess() throws SecurityException {
		super.checkAccess();
		if (!_group.canEdit(session().currentPerson().localInstanceIn(_group.editingContext()))) {
			throw new SecurityException("You do not have permission to add events to this group.");
		}
	}

	public void setGroup(SPGroup group) {
		_group = group;
	}
	
	public SPGroup group() {
		return _group;
	}

	public void setEvent(SPEvent event) {
		_event = event;
	}

	public SPEvent event() {
		if (_event == null) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			_event = SPEvent.createSPEvent(editingContext, Boolean.TRUE, "New Event", _group.localInstanceIn(editingContext));
		}
		return _event;
	}

	public WOActionResults addEvent() {
		if (session().errors().hasNotices()) {
			return null;
		}
		
		_event.editingContext().saveChanges();
		SPEventPage eventPage = pageWithName(SPEventPage.class);
		eventPage.setEvent(_event);
		return eventPage;
	}
}