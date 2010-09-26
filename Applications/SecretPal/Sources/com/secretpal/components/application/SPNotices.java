package com.secretpal.components.application;

import com.secretpal.SPNoticeList;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class SPNotices extends SPComponent {
	public String _notice;

	public SPNotices(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public SPNoticeList noticeList() {
		return (SPNoticeList) valueForBinding("noticeList");
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		noticeList().clearNotices();
	}
}