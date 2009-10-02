/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

// Stateless confirm panel
/**
 * Stateless implementation of JSConfirmPanel.*<br />
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

    public ERXJSConfirmPanel(WOContext aContext) {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }
    
    public String confirmMessage() { return "return confirm('" +
        ERXStringUtilities.escapeJavascriptApostrophes((String)valueForBinding("confirmMessage")) + "')"; }
}
