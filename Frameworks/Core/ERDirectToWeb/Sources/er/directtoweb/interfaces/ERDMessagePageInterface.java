/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.interfaces;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;

// This interface is a super-set of both the confirm and error page interfaces.
// Depending on what the task is the different method names make sense.
/**
 * Super set of all D2W message interfaces, confirm and error.
 */

public interface ERDMessagePageInterface extends ConfirmPageInterface, ErrorPageInterface, InspectPageInterface {

    // Next page delegate interface
    public void setNextPageDelegate(NextPageDelegate npd);
    public NextPageDelegate nextPageDelegate();

    public void setCancelPage(WOComponent cp);
    public WOComponent cancelPage();

    public void setTitle(String title);
    public String title();

    public EOEnterpriseObject object();

    public EODataSource dataSource();
    public void setDataSource(EODataSource ds);
}
