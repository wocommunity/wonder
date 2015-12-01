/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;


// Simple class that extends the ObjectSaveDelegate to provide a confirm page before saving.
/**
 * Extends ObjectSaverDelegate to provide a confirm page.
 */

public class ERDConfirmObjectDelegate implements NextPageDelegate {

    protected String _confirmPageName;
    protected EOEnterpriseObject _eo;
    protected EOEditingContext _ec;
    protected NextPageDelegate _cancelDelegate;
    protected NextPageDelegate _confirmDelegate;

    public ERDConfirmObjectDelegate(EOEnterpriseObject eo, String confirmPageName, NextPageDelegate confirmDelegate, NextPageDelegate cancelDelegate) {
        _eo = eo;
        _confirmPageName = confirmPageName;
        _confirmDelegate = confirmDelegate;
        _cancelDelegate = cancelDelegate;
        if (_eo != null) {
            _ec = _eo.editingContext();
            if (_confirmPageName == null)
                _confirmPageName = "Confirm" + _eo.entityName();
        }
    }

    public ERDConfirmObjectDelegate(EOEnterpriseObject eo, String confirmPageName, WOComponent confirmPage, WOComponent cancelPage) {
        this(eo, confirmPageName, new ERDPageDelegate(confirmPage), new ERDPageDelegate(cancelPage));
    }

    public ERDConfirmObjectDelegate(EOEnterpriseObject eo, String confirmPageName, WOComponent nextPage) {
        this(eo, confirmPageName, nextPage, nextPage);
    }    

    public ERDConfirmObjectDelegate(EOEnterpriseObject eo, String confirmPageName, NextPageDelegate delegate) {
        this(eo, confirmPageName, delegate, delegate);
    }

    public WOComponent nextPage(WOComponent sender) {
        ConfirmPageInterface cpi = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed(_confirmPageName, sender.session());
        ((D2WPage)cpi).setObject(_eo);
        cpi.setConfirmDelegate(_confirmDelegate);
        cpi.setCancelDelegate(_cancelDelegate);
        return (WOComponent)cpi;
    }
}
