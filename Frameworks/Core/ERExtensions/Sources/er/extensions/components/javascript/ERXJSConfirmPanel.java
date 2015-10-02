/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Stateless implementation of JSConfirmPanel.
 *
 * @binding hyperlinkMessage
 * @binding action
 * @binding confirmMessage
 * @binding title title of the link
 * @binding id id of the link
 * @binding class class of the link
 * @binding style style of the link
 * @binding disabled whether or not this link is disabled
 */
public class ERXJSConfirmPanel extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXJSConfirmPanel(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean isStateless() { return true; }

    public String confirmMessage() { return "return confirm('" +
        ERXStringUtilities.escapeJavascriptApostrophes((String)valueForBinding("confirmMessage")) + "')"; }
}
