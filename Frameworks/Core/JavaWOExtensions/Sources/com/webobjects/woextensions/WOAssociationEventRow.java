/*
 * WOAssociationEventRow.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOEvent;

public class WOAssociationEventRow extends WOEventRow {
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
