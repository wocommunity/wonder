/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXLogger;

public abstract class ERD2WMessagePage extends ERD2WPage implements ERDMessagePageInterface, ERDBranchInterface {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.templates.ERD2WMessagePage");
    
    protected String _message;
    protected String _title;
    protected WOComponent _cancelPage;
    protected WOComponent _nextPage;
    protected NextPageDelegate _cancelDelegate;
    protected NextPageDelegate _nextDelegate;

    /**
     * Public constructor
     * @param c current context
     */
    public ERD2WMessagePage(WOContext c) {
        super(c);
    }
    
    public WOComponent cancelAction() { return (cancelDelegate() != null) ? cancelDelegate().nextPage(this) : cancelPage(); }
    public WOComponent confirmAction() { return errorMessages.count()==0 ? nextPageAction() : null; }
    public WOComponent nextPageAction() { return nextPageDelegate() != null ? nextPageDelegate().nextPage(this) : nextPage(); }
    
    public void setMessage(String message) { _message = message; }
    public String message() { return _message; }

    public void setTitle(String title) { _title = title; }
    public String title() { return _title; }
    
    public void setCancelPage(WOComponent cancelPage) { _cancelPage = cancelPage; }
    public WOComponent cancelPage() { return _cancelPage; }

    public void setCancelDelegate(NextPageDelegate cancelDelegate) { _cancelDelegate = cancelDelegate; }
    public NextPageDelegate cancelDelegate() { return _cancelDelegate; }

    public void setConfirmDelegate(NextPageDelegate confirmPageDelegate) { setNextPageDelegate(confirmPageDelegate); }
    public NextPageDelegate confirmDelegate() { return nextPageDelegate(); }

    public void setConfirmPage(WOComponent confirmPage) { setNextPage(confirmPage); }
    public WOComponent confirmPage() { return nextPage(); }

    public void setNextPageDelegate(NextPageDelegate nextDelegate) { _nextDelegate = nextDelegate; }
    public NextPageDelegate nextPageDelegate() { return _nextDelegate; }    
    
    public void setNextPage(WOComponent nextPage) { _nextPage = nextPage; }
    public WOComponent nextPage() { return _nextPage; }


    public String titleForPage() {
        return title() != null ? title() : (String)d2wContext().valueForKey("messageTitleForPage");
    }

    //---------------- Branch Delegate Support --------------------//
    /** holds the chosen branch */
    protected NSDictionary _branch;
    
    /**
     * Cover method for getting the choosen branch.
     * @return user choosen branch.
     */
    public NSDictionary branch() { return _branch; }
    
    /**
     * Sets the user choosen branch.
     * @param branch choosen by user.
     */
    public void setBranch(NSDictionary branch) { _branch = branch; }

    /**
     * Implementation of the {@link ERDBranchDelegate ERDBranchDelegate}.
     * Gets the user selected branch name.
     * @return user selected branch name.
     */
    // ENHANCEME: Should be localized
    public String branchName() { return (String)branch().valueForKey("branchName"); }
    
    /**
     * Calculates the branch choices for the current 
     * poage. This method is just a cover for calling
     * the method <code>branchChoicesForContext</code>
     * on the current {@link ERDBranchDelegate ERDBranchDelegate}.
     * @return array of branch choices
     */
    public NSArray branchChoices() {
        NSArray branchChoices = null;
        if (nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface) {
            branchChoices = ((ERDBranchDelegateInterface)nextPageDelegate()).branchChoicesForContext(d2wContext());
        } else {
            log.error("Attempting to call branchChoices on a page with a delegate: " + nextPageDelegate() + " that doesn't support the ERDBranchDelegateInterface!");
        }
        return branchChoices;
    }
    
    /**
     * Determines if this message page should display branch choices.
     * @return if the current delegate supports branch choices.
     */
    public boolean hasBranchChoices() {
        return nextPageDelegate() != null && nextPageDelegate() instanceof ERDBranchDelegateInterface;
    }
}
