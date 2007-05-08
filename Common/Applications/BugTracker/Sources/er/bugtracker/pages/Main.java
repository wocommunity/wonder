/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.pages;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.directtoweb.D2W;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

import er.bugtracker.People;
import er.bugtracker.Session;
import er.extensions.ERXCrypto;
import er.extensions.ERXUtilities;

public class Main extends WOComponent {

    public Main(WOContext aContext) {
        super(aContext);
    }

    public String username;
    public String password;
    public boolean rememberPassword;
    public String errorMessage;

    private WOComponent _nextPage;
    public WOComponent nextPage() {
        if ((_nextPage == null) && (_nextPageCallback != null)) {
            _nextPage = (WOComponent)_nextPageCallback.invoke(session());
        }
        return _nextPage;
    }

    private ERXUtilities.Callback _nextPageCallback;
    public ERXUtilities.Callback nextPageCallback() { return _nextPageCallback; }
    public void setNextPageCallback(ERXUtilities.Callback value) {
        // delay the next page creation as long as possible because Main's constructor calls refresh which
        // will do nothing if the sesion's user is null, which it will be until deep in the defaultPage action
        // below.
        _nextPageCallback = value;
        _nextPage = null;
    }

    public WOComponent defaultPage() {
        EOEditingContext editingContext;
        
        Session session = (Session)session();
        editingContext = session.defaultEditingContext();

        if (username==null || password==null) {
            errorMessage="Please specify both fields!";
            return null;
        }
        
        People  userObject = People.clazz.userWithUsernamePassword(editingContext, username, password);
        if(userObject == null) {
            errorMessage="Sorry login incorrect!";
            return null;
        }

        if (!userObject.isActiveAsBoolean()) {
            errorMessage="Sorry your account is inactive!";
            return null;
        }
        session.setUser(userObject);
        boolean isAdmin = userObject.isAdminAsBoolean();
        D2W.factory().setWebAssistantEnabled(isAdmin);
        String encryptedIDPrimaryKey = ERXCrypto.blowfishEncode(userObject.primaryKey());
        WOCookie loginCookie=WOCookie.cookieWithName("BTL", rememberPassword ?  encryptedIDPrimaryKey : "-");
        loginCookie.setExpires(NSTimestamp.DistantFuture);
        loginCookie.setPath("/");
        context().response().addCookie(loginCookie);
        WOComponent nextPage = nextPage();
        return ((nextPage == null) ? pageWithName("HomePage") : nextPage);
    }
}