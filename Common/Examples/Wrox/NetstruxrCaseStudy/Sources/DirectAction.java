/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.wrox.*;
import er.extensions.ERXEC;
import er.extensions.ERXUtilities;
import er.directtoweb.ERDObjectWasCreatedDelegate;
import er.extensions.ERXExtensions;
import er.directtoweb.ERDPageNameDelegate;
import org.apache.log4j.Logger;

public class DirectAction extends WODirectAction {

    /* logging support */
    public static final Logger log = Logger.getLogger("er.application.DirectAction");
    
    public DirectAction(WORequest aRequest) { super(aRequest); }

    public WOActionResults defaultAction() { return pageWithName("LoginPage"); }

    public WOActionResults loginAction() {
        WOActionResults result=null;
        String raction = (String)request().formValueForKey("raction");
        String rbindings = (String)request().formValueForKey("rbindings");
        String username = (String)request().formValueForKey("username");
        String password = (String)request().formValueForKey("password");
        String errorMessage = ((Session)session()).loginUser(username, password);
        log.debug("Attempt to log in with username: " + username + " error message: " + errorMessage);
        // Login logic - if the user is logged into the system then errorMessage will be null.
        if (errorMessage != null) {
            // Error happened, prompt user for correct information.
            LoginPage p=(LoginPage)pageWithName("LoginPage");
            p.takeValueForKey(errorMessage, "errorMessage");
            p.takeValueForKey(username, "username");       
            if (raction != null) {
                p.takeValueForKey(raction, "raction");
                p.takeValueForKey(rbindings, "rbindings");
            }
            result=p;
        } else if (raction != null && raction.length() > 0) {
            // Has a redirect.  Redirecting to the action.
            log.debug("Login Ok, has raction: " + raction);
            result = redirectToActionWithQueryString(session().context(), raction, null, rbindings);
        } else {
            // No redirect just take them to their home page.
            log.debug("Login Ok");            
            result = pageWithName("Home");
        }
        return result;
    }

    public WOActionResults signUpAction() {
        EOEditingContext peer = ERXEC.newEditingContext();
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("EditSignUpUser", session());
        er.wrox.User user = (er.wrox.User)ERXUtilities.createEO("User", peer);
        epi.setObject(user);
        epi.setNextPageDelegate(new ERDObjectWasCreatedDelegate(user,
                                                             new SignUpHomePageDelegate(user),
                                                             new ERDPageNameDelegate("LoginPage")));
        return (WOComponent)epi;
    }

    public static class SignUpHomePageDelegate implements NextPageDelegate {
        protected er.wrox.User user;
        protected EOEditingContext ec;
        public SignUpHomePageDelegate(er.wrox.User u) {
            user = u;
            if (user != null)
                ec = user.editingContext();
        }

        public WOComponent nextPage(WOComponent sender) {
            // SetUser handles pulling the user into the defaultEditingContext().
            ((Session)sender.session()).setUser(user);
            return sender.pageWithName("Home");
        }
    }
    
    // Simple utility method used to generate a WORedirect with a given query string
    public static WORedirect redirectToActionWithQueryString(WOContext context, String actionName, NSDictionary bindings, String queryString) {
        WORedirect page=(WORedirect)WOApplication.application().pageWithName("WORedirect", context);
        String url=context.directActionURLForActionNamed(actionName, bindings);
        if ((queryString != null) && (!queryString.equals(""))) {
            url += (url.indexOf("?") == -1 ? "?" : "&") + queryString;
        }
        page.setUrl(url);
        log.debug("Set url to: " + url);
        return page;
    }

    public WOActionResults logoutAction() {
        if (existingSession() != null)
            existingSession().terminate();
        return pageWithName("LogoutPage");
    }
}
