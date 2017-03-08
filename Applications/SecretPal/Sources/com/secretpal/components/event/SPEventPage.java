package com.secretpal.components.event;

import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPEvent;
import com.secretpal.model.SPMembership;
import com.secretpal.model.SPPerson;
import com.secretpal.model.SPSecretPal;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

public class SPEventPage extends SPPage {
	private SPEvent _event;
	public SPPerson _currentPerson;
	public SPMembership _membership;

	public SPPerson _secretPal;
	public NSArray<SPPerson> _secretPals;
	private NSMutableSet<SPMembership> _expandedMemberships;

	public SPEventPage(WOContext context) {
		super(context);
		_expandedMemberships = new NSMutableSet<>();
	}

	protected void _expandSecretPals() {
		_expandedMemberships.removeAllObjects();
		for (SPPerson secretPal : _secretPals) {
			SPMembership secretPalMembership = _event.group().membershipForPerson(secretPal);
			if (secretPalMembership != null) {
				_expandedMemberships.addObject(secretPalMembership);
			}
		}
		if (_currentPerson.desires().count() == 0) {
			_expandedMemberships.addObject(_event.group().membershipForPerson(_currentPerson));
		}
	}

	public void setEvent(SPEvent event) {
		_event = event;
		_currentPerson = session().currentPerson().localInstanceIn(_event.editingContext());
		_secretPals = SPSecretPal.RECEIVER.arrayValueInObject(event.secretPalsForPerson(_currentPerson));
		_expandSecretPals();
	}

	public SPEvent event() {
		return _event;
	}
	
	public boolean isMembershipSecretPal() {
		return _membership != null && _secretPals.containsObject(_membership.person());
	}

	public boolean isExpanded() {
		return _expandedMemberships.containsObject(_membership);
	}

	public void setExpanded(boolean expanded) {
		if (expanded) {
			_expandedMemberships.addObject(_membership);
		} else {
			_expandedMemberships.removeObject(_membership);
		}
	}
	
	public boolean canEdit() {
		return _event.canEdit(session().currentPerson().localInstanceIn(_event.editingContext()));
	}

	public WOActionResults expandSecretPals() {
		_expandSecretPals();
		return null;
	}
}