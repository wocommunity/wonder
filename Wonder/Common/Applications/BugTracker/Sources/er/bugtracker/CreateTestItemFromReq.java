/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class CreateTestItemFromReq extends WOComponent {

    public CreateTestItemFromReq(WOContext aContext) {
        super(aContext);
    }

    public Bug bug;

    public WOComponent createTestItem() {
        ERXLocalizer localizer = ERXLocalizer.localizerForSession(session());
        EOEditingContext peer = new EOEditingContext(bug.editingContext().parentObjectStore());
        People user = (People)EOUtilities.localInstanceOfObject(peer,((Session)session()).getUser());

        String description = localizer.localizedTemplateStringForKeyWithObject("CreateTestItemFromReq.templateString", bug);
        TestItem testItem = user.createTestItemFromRequestWithDescription(bug, (Component)valueForKey("component"), description);

        EditPageInterface epi=(EditPageInterface)D2W.factory().pageForConfigurationNamed("CreateNewTestItemFromReq",session());
        epi.setObject(testItem);
        epi.setNextPage(context().page());

        return (WOComponent)epi;
    }
}