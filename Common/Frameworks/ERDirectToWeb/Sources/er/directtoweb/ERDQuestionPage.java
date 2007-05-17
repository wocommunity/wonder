/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;

/**
 * Similiar to the message page, except this one has the choice Yes or No.<br />
 * As this functionality is already integrated in ERD2WMessagePage, you should use this instead.
 * @deprecated subclass ERD2WMessagePage instead, also, the name is wrong
 */
//DELETEME

public class ERDQuestionPage extends ERD2WMessagePage implements ConfirmPageInterface {

    public ERDQuestionPage(WOContext context) {
        super(context);
    }
    
    // D2W compatibility
    public void setOkDelegate(NextPageDelegate okDelegate) { setConfirmDelegate(okDelegate); }
    public void setOkNextPage(WOComponent page) { setNextPage(page); }
    public void setCancelNextPage(WOComponent page) { setCancelPage(page); }

    /** @deprecated, use confirmAction() */
    public WOComponent okClicked() {
        return confirmAction();        
    }

    /** @deprecated, use cancelAction() */
    public WOComponent cancelClicked() {
        return cancelAction();        
    }
}
