/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;


/**
 * Similiar to the message page, except this one has the choice Yes or No.<br />
 * As this functionality is already integrated in ERD2WMessagePage, you should use this instead.
 * @deprecated use subclass of {@link ERD2WMessagePage} instead, also, the name is wrong
 * @d2wKey pageWrapperName
 */
@Deprecated
public class ERDQuestionPage extends ERD2WMessagePage implements ConfirmPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDQuestionPage(WOContext context) {
        super(context);
    }
    
    // D2W compatibility
    public void setOkDelegate(NextPageDelegate okDelegate) { setConfirmDelegate(okDelegate); }
    public void setOkNextPage(WOComponent page) { setNextPage(page); }
    public void setCancelNextPage(WOComponent page) { setCancelPage(page); }

    /** @deprecated use confirmAction() */
    @Deprecated
    public WOComponent okClicked() {
        return confirmAction();        
    }

    /** @deprecated use cancelAction() */
    @Deprecated
    public WOComponent cancelClicked() {
        return cancelAction();        
    }
}
