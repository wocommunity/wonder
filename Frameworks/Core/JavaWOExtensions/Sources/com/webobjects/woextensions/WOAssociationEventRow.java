/*
 * WOAssociationEventRow.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOEvent;

public class WOAssociationEventRow extends WOEventRow {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public WOAssociationEventRow(WOContext aContext)  {
        super(aContext);
    }

    public String declarationName()
    {
        return ((WOAssociation.Event)event).declarationName();
    }

    public String bindingName()
    {
        String binding, keyPath;
        boolean isPush;

        binding = ((WOAssociation.Event)event).bindingName();
        keyPath = ((WOAssociation.Event)event).keyPath();
        isPush = ((WOAssociation.Event)event).isPush();
        String pushText = ((isPush) ? "push" : "pull");
        if (!keyPath.equals(binding)) {
            return keyPath+" = "+binding+" : "+ pushText;
        } else {
            return keyPath + " : "+ pushText;
        }
    }

    @Override
    public String hyperlinkTitle()
    {
        return declarationName()+" : "+ event.signatureOfType(WOEvent.ComponentSignature);
    }

    public String backgroundColor()
    {
        if (((WOAssociation.Event)event).isPush())
            return "#cccccc";
        else
            return "#eeeeee";
    }
}
