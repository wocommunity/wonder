package com.secretpal;

import com.secretpal.model.SPPerson;

import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;

	private SPPerson _currentPerson;
	private SPNoticeList _errors;
	private SPNoticeList _notifications;

	public Session() {
		_errors = new SPNoticeList();
		_notifications = new SPNoticeList();
		setStoresIDsInCookies(true);
		setStoresIDsInURLs(false);
	}

	public SPNoticeList errors() {
		return _errors;
	}

	public SPNoticeList notifications() {
		return _notifications;
	}

	public void setCurrentPerson(SPPerson currentPerson) {
		_currentPerson = currentPerson;
	}

	public SPPerson currentPerson() {
		return _currentPerson;
	}
}
