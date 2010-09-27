package com.secretpal;

import com.secretpal.components.application.SPBacktrackErrorPage;
import com.secretpal.components.application.SPErrorPage;
import com.secretpal.components.application.SPSessionExpiredPage;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXRedirect;

public class Application extends ERXApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
		setAllowsConcurrentRequestHandling(true);
	}
	
	@Override
	public WOResponse handlePageRestorationErrorInContext(WOContext context) {
		WOResponse response;
		if (context != null && AjaxUtils.isAjaxRequest(context.request())) {
			ERXRedirect redirect = pageWithName(ERXRedirect.class, context);
			redirect.setDirectActionName("backtrackError");
			response = redirect.generateResponse();
		} else {
			response = pageWithName(SPBacktrackErrorPage.class, context).generateResponse();
		}
		return response;
	}

	@Override
	public WOResponse handleException(Exception exception, WOContext context) {
		WOResponse response;
		if (context != null && AjaxUtils.isAjaxRequest(context.request())) {
			ERXRedirect redirect = pageWithName(ERXRedirect.class, context);
			redirect.setDirectActionName("error");
			response = redirect.generateResponse();
		} else {
			SPErrorPage errorPage = pageWithName(SPErrorPage.class, context);
			errorPage.setException(exception);
			response = errorPage.generateResponse();
		}
		return response;
	}

	@Override
	public WOResponse handleSessionRestorationErrorInContext(WOContext context) {
		WOResponse response;
		if (context != null && AjaxUtils.isAjaxRequest(context.request())) {
			ERXRedirect redirect = pageWithName(ERXRedirect.class, context);
			redirect.setDirectActionName("sessionExpired");
			response = redirect.generateResponse();
		} else {
			response = pageWithName(SPSessionExpiredPage.class, context).generateResponse();
		}
		return response;
	}
}
