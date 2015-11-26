//
// ERXAjaxContext.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//
package er.extensions.appserver.ajax;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.jspservlet.WOServletContext;

/**
 * <span class="en">
 * ERXAjaxContext provides the overrides necessary methods for partial form
 * submits to work. If you want to use the Ajax framework without using other
 * parts of Project Wonder (i.e. ERXSession or ERXApplication), you should steal
 * all of the code in ERXAjaxSession, ERXAjaxApplication, and ERXAjaxContext.
 * </span>
 * 
 * <span class="ja">
 * ERXAjaxContext は部分的なフォーム・サブミットの機能を提供します。
 * </span>
 * 
 * @author mschrag
 */
public class ERXAjaxServletContext extends WOServletContext {
	
	public ERXAjaxServletContext(WORequest request) {
		super(request);
	}

	/*
	 * NOTE: This class was copied directly from ERXAjaxContext.  Keep it in sync with that.
	 */
	@Override
	public boolean wasFormSubmitted() {
		boolean wasFormSubmitted = super.wasFormSubmitted();
		if (wasFormSubmitted) {
			WORequest request = request();
			String partialSubmitSenderID = ERXAjaxApplication.partialFormSenderID(request);
			if (partialSubmitSenderID != null) {
				// TODO When explicitly setting the "name" binding on an input, 
				// the following will fail in the takeValuesFromRequest phase.
				String elementID = elementID();
				if (!partialSubmitSenderID.equals(elementID) 
						&& !partialSubmitSenderID.startsWith(elementID + ",") 
						&& !partialSubmitSenderID.endsWith("," + elementID) 
						&& !partialSubmitSenderID.contains("," + elementID + ",")) {
					String ajaxSubmitButtonID = ERXAjaxApplication.ajaxSubmitButtonName(request);
					if (ajaxSubmitButtonID == null || !ajaxSubmitButtonID.equals(elementID)) {
						wasFormSubmitted = false;
					}
				}
			}
		}
		return wasFormSubmitted;
	}
	
}
