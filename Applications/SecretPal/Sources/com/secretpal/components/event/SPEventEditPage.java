package com.secretpal.components.event;

import com.secretpal.components.application.SPPage;
import com.secretpal.components.group.SPGroupPage;
import com.secretpal.model.SPEvent;
import com.secretpal.model.SPGroup;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;

public class SPEventEditPage extends SPPage {
	private SPEvent _event;

	public SPEventEditPage(WOContext context) {
		super(context);
	}

	@Override
	protected void checkAccess() throws SecurityException {
		super.checkAccess();
		if (!_event.canEdit(session().currentPerson().localInstanceIn(_event.editingContext()))) {
			throw new SecurityException("You do not have permission to edit this event.");
		}
	}

	public void setEvent(SPEvent event) {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		_event = event.localInstanceIn(editingContext);
	}

	public SPEvent event() {
		return _event;
	}

	public WOActionResults reassignSecretPals() {
		try {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			_event.localInstanceIn(editingContext).reassignSecretPals();
			editingContext.saveChanges();
			
			session().notifications().addNotice("Secret pals have been assigned for this event.");
			
			SPEventPage eventPage = pageWithName(SPEventPage.class);
			eventPage.setEvent(_event);
			return eventPage;
		}
		catch (IllegalStateException e) {
			session().errors().addNotice(e.getMessage());
			return null;
		}
	}
	
	public WOActionResults saveEvent() {
		if (session().errors().hasNotices()) {
			return null;
		}
		
		_event.editingContext().saveChanges();
		SPEventPage eventPage = pageWithName(SPEventPage.class);
		eventPage.setEvent(_event);
		return eventPage;
	}

	public WOActionResults deleteEvent() {
		SPGroup group = _event.group();
		_event.delete();
		_event.editingContext().saveChanges();
		SPGroupPage groupPage = pageWithName(SPGroupPage.class);
		groupPage.setGroup(group);
		return groupPage;
	}
}