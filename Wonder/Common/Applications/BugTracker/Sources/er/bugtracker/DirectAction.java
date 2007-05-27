/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;

import er.bugtracker.pages.Main;
import er.directtoweb.ERD2WDirectAction;
import er.extensions.ERXCrypto;
import er.extensions.ERXEC;
import er.extensions.ERXUtilities;

public class DirectAction extends ERD2WDirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOComponent pingAction() {
        WOComponent result = null;
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EOUtilities.rawRowsForSQL(ec, (String) EOModelGroup.defaultGroup().modelNames().lastObject(), "select count(*) from PRIORITY", null);
            result = pageWithName("ERXSuccess");
        } catch (Exception e) {
            
        } finally {
            ec.unlock();
        }
        return result;
    }

    public static People userFromRequest(WORequest r, EOEditingContext ec) {
        People user = People.clazz.currentUser(ec);
        if(user != null) {
            return user;
        }
        // This gets us the encrypted ID stored for the login cookie
        String encryptedPrimaryKey = r.cookieValueForKey("BTL");
        if (encryptedPrimaryKey != null && !encryptedPrimaryKey.equals("") && !encryptedPrimaryKey.equals("-")) {
            String clearPrimaryKey = ERXCrypto.blowfishDecode(encryptedPrimaryKey);
            if (clearPrimaryKey != null) {
                clearPrimaryKey = clearPrimaryKey.trim();
                ec.lock();
                try {
                    Integer clearPrimaryKeyInt = new Integer(clearPrimaryKey);
                    user = (People) People.clazz.objectWithPrimaryKeyValue(ec, clearPrimaryKeyInt);
                } catch (NumberFormatException NFe) {
                    // WOApplication.application().logString(NFe.toString());
                } finally {
                    ec.unlock();
                }
            }
        }
        return user;
    }

    private static WOComponent errorPage(String message, Session session) {
        ErrorPageInterface error = D2W.factory().errorPage(session);
        error.setMessage(message);
        error.setNextPage(WOApplication.application().pageWithName("Main", session.context()));
        return (WOComponent) error;
    }

    public static class BugActionCallback implements ERXUtilities.Callback {
        
        public String numberFromRequest;

        public BugActionCallback(String numFromReq) {
            super();
            numberFromRequest = numFromReq;
        }

        public Object invoke(Object ctx) {
            Session session = (Session) ctx;
            WOComponent result = null;
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                Integer bugId = new Integer(numberFromRequest);
                Bug bug =  (Bug)Bug.clazz.objectWithPrimaryKeyValue(ec, bugId);
                if (bug == null) {
                    result = errorPage("Bug not found", session);
                } else {
                    result = Factory.bugTracker().editBug(bug);
                }
            } catch (NumberFormatException nfe) {
                result = errorPage("Invalid Request", session);
            } catch (Exception e) {
                result = errorPage("Bug Not Found", session);
            } finally {
                ec.unlock();
            }
            return result;
        }
    }

    public WOComponent bugAction() {
        BugActionCallback result = new BugActionCallback((String) request().formValueForKey("number"));
        return entranceTemplate(result);
    }

    private WOComponent entranceTemplate(ERXUtilities.Callback successComponent) {
        WOComponent result = null;
        People u = userFromRequest(request(), session().defaultEditingContext());
        if (u != null) {
            ((Session) session()).setUser(u);
            return (WOComponent) successComponent.invoke(session());
        } else {
            Main loginPage = (Main) pageWithName("Main");
            loginPage.setNextPageCallback(successComponent);
            result = loginPage;
        }
        return result;
    }

    public static class EntranceActionCallback implements ERXUtilities.Callback {
        public Object invoke(Object ctx) {
            return WOApplication.application().pageWithName("HomePage", ((Session) ctx).context());
        }
    }

    public WOComponent entranceAction() {
        EntranceActionCallback result = new EntranceActionCallback();
        return entranceTemplate(result);
    }

    public WOComponent homeAction() {
        EntranceActionCallback result = new EntranceActionCallback();
        return entranceTemplate(result);
    }

    public WOActionResults defaultAction() {
        // the reason for this redirect is that the session cookie and the
        // 'remember my login cookie'
        // are in xxx/cgi-bin/WebObjects/x.woa domain
        // if you hit xxx.cgi-bin/WebObjects/x, you do not get them!..
        WORedirect page = (WORedirect) pageWithName("WORedirect");
        page.setUrl(session().context().directActionURLForActionNamed("entrance", null));
        return page;
    }

    public WOActionResults logoutAction() {
        WORedirect redirectPage = (WORedirect) pageWithName("WORedirect");
        redirectPage.setUrl("/");
        if (existingSession() != null)
            existingSession().terminate();
        return redirectPage;
    }
}
