/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import org.apache.log4j.Category;
import er.wrox.User;
import er.extensions.ERXSession;
import er.extensions.ERXUtilities;
import er.extensions.ERXCrypto;

public class Session extends ERXSession {

    /////////////////////////////////// log4j category ///////////////////////////////////
    public static final Category cat = Category.getInstance("wrox.application.Session");

    public Session() {
        setStoresIDsInCookies(true);
    }

    public boolean javaScriptEnabled() { return true; }
    
    protected String _look;
    public String look() { return _look; }
    public void setLook(String look) { _look = look; }
    
    protected User _user;
    /** @TypeInfo wrox.eo.User */
    public User user() { return _user; }
    public void setUser(User user) {
        _user = (User)ERXUtilities.localInstanceOfObject(defaultEditingContext(), user);
    }

    public void awake() {
        super.awake();
        if (user() != null)
            User.setActor(user());
    }

    public void sleep() {
        User.setActor(null);
        super.sleep();
    }

    public String loginUser(String username, String password) {
        String errorMessage = null;
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            NSArray potentialUsers = EOUtilities.objectsMatchingKeyAndValue(defaultEditingContext(), "User", "username", username);
            if (potentialUsers.count() == 1) {
                User user = (User)potentialUsers.lastObject();
                if (user.password().equals(ERXCrypto.shaEncode(password))) {
                    setUser(user);
                    cat.info("Login: " + user.fullName());
                }
            }
            if (user() == null) {
                errorMessage = "Sorry, we could not find your account!<BR>Please note that usernames and passwords are case sensitive.";
                if (potentialUsers.count() > 1)
                    cat.warn("Found multiple Users for the username: " + username + " found users: " + potentialUsers);                
            }
        } else {
            errorMessage = "Please specify <b>both</b> fields!";
        }
        return errorMessage;
    }

    public NSArray groups() { return EOUtilities.objectsForEntityNamed(defaultEditingContext(), "Group"); }
    public NSArray users() { return EOUtilities.objectsForEntityNamed(defaultEditingContext(), "User"); }

    public EOArrayDataSource usersDataSource() { return ERXUtilities.dataSourceForArray(users()); }
    public EOArrayDataSource groupsDataSource() { return ERXUtilities.dataSourceForArray(groups()); }
}
