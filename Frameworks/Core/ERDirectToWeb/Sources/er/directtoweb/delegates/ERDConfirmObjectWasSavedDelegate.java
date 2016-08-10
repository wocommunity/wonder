/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.interfaces.ERDObjectSaverInterface;

/**
 * Determines if the user wants the changes saved if so provides a confirm page, if note uses cancel delegate.
 */

public class ERDConfirmObjectWasSavedDelegate extends ERDConfirmObjectDelegate {

    public ERDConfirmObjectWasSavedDelegate(EOEnterpriseObject eo, String confirmPageName, NextPageDelegate confirmDelegate, NextPageDelegate cancelDelegate) {
        super(eo, confirmPageName, confirmDelegate, cancelDelegate);
    }

    public ERDConfirmObjectWasSavedDelegate(EOEnterpriseObject eo, String confirmPageName, WOComponent confirmPage, WOComponent cancelPage) {
        super(eo, confirmPageName, new ERDPageDelegate(confirmPage), new ERDPageDelegate(cancelPage));
    }

    public ERDConfirmObjectWasSavedDelegate(EOEnterpriseObject eo, String confirmPageName, WOComponent nextPage) {
        super(eo, confirmPageName, nextPage, nextPage);
    }

    public ERDConfirmObjectWasSavedDelegate(EOEnterpriseObject eo, String confirmPageName, NextPageDelegate delegate) {
        super(eo, confirmPageName, delegate, delegate);
    }

    @Override
    public WOComponent nextPage(WOComponent sender) {
        boolean wasSaved = true;
        if (sender instanceof ERDObjectSaverInterface)
            wasSaved = ((ERDObjectSaverInterface)sender).objectWasSaved();
        // Just one more check that the eo is not null and has an ec.
        if (wasSaved)
            wasSaved = _eo !=null && _eo.editingContext() != null;
        return wasSaved ? super.nextPage(sender) : _cancelDelegate.nextPage(sender);
    }
}
