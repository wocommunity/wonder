//
// ERXWOContext.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//
package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

/**
 * ERXAjaxContext provides the overrides necessary methods for partial form
 * submits to work.
 * 
 * @author mschrag
 */
public class ERXAjaxContext extends WOContext {
	public ERXAjaxContext(WORequest request) {
		super(request);
	}

	@Override
	public boolean _wasFormSubmitted() {
		boolean wasFormSubmitted = super._wasFormSubmitted();
		if (wasFormSubmitted) {
			WORequest request = request();
			String partialSubmitSenderID = ERXAjaxApplication.partialFormSenderID(request);
			if (partialSubmitSenderID != null) {
				String elementID = elementID();
				if (!partialSubmitSenderID.equals(elementID) && !partialSubmitSenderID.startsWith(elementID + ",") && !partialSubmitSenderID.endsWith("," + elementID) && !partialSubmitSenderID.contains("," + elementID + ",")) {
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
