/*
 * WOAssociationEventRow.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

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

        return keyPath+" = "+binding+" : "+((isPush) ? "push" : "pull");
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
