/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import java.util.Enumeration;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.calendar.ERPublishCalendarPage;
import er.calendar.ERSimpleEvent;
import er.corebusinesslogic.ERCoreBusinessLogic;
import er.directtoweb.ERD2WDirectAction;
import er.extensions.crypting.ERXCrypto;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXUtilities;

public class DirectAction extends ERD2WDirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults calendarAction() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            ERPublishCalendarPage calendar = (ERPublishCalendarPage) pageWithName("ERPublishCalendarPage");
            calendar.setCalendarName("Release Map");
            NSMutableArray releases = new NSMutableArray();
            for (Enumeration e = Release.clazz.allObjects(ec).objectEnumerator(); e.hasMoreElements();) {
                final Release element = (Release) e.nextElement();
                ERSimpleEvent event = new ERSimpleEvent(element.dateDue(), null, element.name(), element.primaryKey());
                releases.addObject(event);
            }
            calendar.addEventsFromArray(releases);
            return calendar;
        } finally {
            ec.unlock();
        }
    }
    public WOComponent pingAction() {
        WOComponent result = null;
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            //EOUtilities.rawRowsForSQL(ec, (String) EOModelGroup.defaultGroup().modelNames().lastObject(), "select count(*) from PRIORITY", null);
            People user = People.clazz.anyUser(ec);
            ERCoreBusinessLogic.setActor(user);
            Bug bug = Bug.clazz.createAndInsertObject(ec);
            bug.setSubject("Test");
     //       bug.setTextDescription("Test");
            bug.setComponent(Component.clazz.allObjects(ec).lastObject());
            ec.saveChanges();
            bug.setSubject("Test");
            ec.saveChanges();
            bug.setSubject("Test1");
            bug.setOwner(People.clazz.anyUser(ec));
            ec.saveChanges();
            ec.deleteObject(bug);
            ec.saveChanges();
            result = pageWithName("ERXSuccess");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            ERCoreBusinessLogic.setActor(null);
            ec.unlock();
        }
        return result;
    }

    public WOComponent pingxAction() {
        WOComponent result = null;
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            EOUtilities.rawRowsForSQL(ec, EOModelGroup.defaultGroup().modelNames().lastObject(), "select count(*) from PRIORITY", null);
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
            String clearPrimaryKey = ERXCrypto.crypterForAlgorithm(ERXCrypto.BLOWFISH).decrypt(encryptedPrimaryKey);
            if (clearPrimaryKey != null) {
                clearPrimaryKey = clearPrimaryKey.trim();
                ec.lock();
                try {
                    Integer clearPrimaryKeyInt = Integer.valueOf(clearPrimaryKey);
                    user = People.clazz.objectWithPrimaryKeyValue(ec, clearPrimaryKeyInt);
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
        error.setNextPage(WOApplication.application().pageWithName("HomePage", session.context()));
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
                Integer bugId = Integer.valueOf(numberFromRequest);
                Bug bug =  Bug.clazz.objectWithPrimaryKeyValue(ec, bugId);
                if (bug == null) {
                    result = errorPage("Bug not found", session);
                } else {
                    result = Factory.bugTracker().inspectBug(bug);
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
        /*
        if (u != null) {
            ((Session) session()).setUser(u);
            return (WOComponent) successComponent.invoke(session());
        } else {
            Main loginPage = (Main) pageWithName("HomePage");
            loginPage.setNextPageCallback(successComponent);
            result = loginPage;
        }*/
        ((Session) session()).setUser(u);
        return (WOComponent) successComponent.invoke(session());
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

    @Override
    public WOActionResults defaultAction() {
        // the reason for this redirect is that the session cookie and the
        // 'remember my login cookie'
        // are in xxx/cgi-bin/WebObjects/x.woa domain
        // if you hit xxx.cgi-bin/WebObjects/x, you do not get them!..
        WORedirect page = (WORedirect) pageWithName("WORedirect");
        page.setUrl(session().context().directActionURLForActionNamed("entrance", null));
        return page;
    }

    @Override
    public WOActionResults logoutAction() {
    	WORedirect redirect = (WORedirect) pageWithName("WORedirect");
    	redirect.setUrl(context().directActionURLForActionNamed("entrance", null));
    	WOResponse response = redirect.generateResponse();

    	WOCookie loginCookie = new WOCookie("BTL", "-");
    	loginCookie.setExpires(NSTimestamp.DistantFuture);
    	loginCookie.setPath("/");
    	response.addCookie(loginCookie);
    	
    	if (existingSession() != null) {
    		existingSession().terminate();
		}

    	return response;
    }

}
