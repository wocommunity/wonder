package com.secretpal;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class SPNoticeList {
	private NSMutableArray<String> _notices;

	public SPNoticeList() {
		_notices = new NSMutableArray<>();
	}
	
	public synchronized boolean hasNotices() {
		return _notices.count() > 0;
	}

	public synchronized void addNotice(String error) {
		_notices.addObject(error);
	}

	public synchronized void clearNotices() {
		_notices.removeAllObjects();
	}

	public synchronized NSArray<String> notices() {
		return _notices.immutableClone();
	}
}
