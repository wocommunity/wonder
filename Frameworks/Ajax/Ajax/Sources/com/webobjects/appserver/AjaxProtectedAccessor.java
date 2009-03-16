package com.webobjects.appserver;

public class AjaxProtectedAccessor {
	public static WOContext contextForRequest(WORequest request) {
		return request._context();
	}
}
