/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponentUtilities;
import er.extensions.components._private.ERXWOForm;

/**
 * Nice guy for performing actions when a popup item is selected.
 * 
 * @binding string
 * @binding list
 * @binding selectsItem
 * @binding popupName
 * @binding doNotAddOneToComputedIndex" defaults="Boolean
 */
public class ERXJSPopupSelector extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXJSPopupSelector.class);

	public ERXJSPopupSelector(WOContext aContext) {
		super(aContext);
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	public String onClickString() {
		String result = null;
		Object item = valueForBinding("selectsItem");
		NSArray list = (NSArray) valueForBinding("list");
		String popupName = (String) valueForBinding("popupName");
		if (list != null && item != null) {
			int index = list.indexOfObject(item);
			if (index == -1) {
				log.info(item + " could not be found in " + list);
			}
			// by default we assume that there is one more item on top of the
			// list (i.e. - none - or - pick one -)
			// when the relationship is mandatory, this is not the case
			boolean doNotAddOne = ERXComponentUtilities.booleanValueForBinding(this, "doNotAddOneToComputedIndex", false);
			if (!doNotAddOne)
				index++;
			String formName = ERXWOForm.formName(context(), "forms[2]");
			result = "javascript:window.document." + formName + "." + popupName + ".selectedIndex=" + index + "; return false;";
		}
		return result;
	}
}
