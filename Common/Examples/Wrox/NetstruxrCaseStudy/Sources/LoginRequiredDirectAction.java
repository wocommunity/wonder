/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* LoginRequiredDirectAction.java created by max on Sat 08-Sep-2001 */
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

public class LoginRequiredDirectAction extends WODirectAction {

    public static final Category cat = Category.getInstance("er.application.LoginRequiredDirectAction");
    
    public static String bindingsFromURI(String uri) {
        int index = uri.indexOf("?");
        return index != -1? uri.substring(index+1) : null;
    }

    public LoginRequiredDirectAction(WORequest r) { super(r); }

    public WOActionResults performActionNamed(String actionName) {
        WOActionResults result;
        cat.debug("In performActionNamed: " + actionName);
        if (existingSession() != null)
            cat.debug("Has Session");
        if (((Session)session()).user() != null) {
            cat.debug("Has user: " + ((Session)session()).user());
            result = super.performActionNamed(actionName);
        } else {
            cat.debug("No user");
            LoginPage p=(LoginPage)pageWithName("LoginPage");
            p.takeValueForKey(bindingsFromURI(request().uri()), "rbindings");
            p.takeValueForKey((actionName == null) ? null : getClass().getName()+"/"+actionName, "raction");
            result=p;
        }
        return result;
    }

    // This is an action that only users who have logged into the system
    // Note here you will always have access to the session's user and the form values.
    public WOActionResults superSecretAction() { return pageWithName("SuperSecretPage"); }
}
