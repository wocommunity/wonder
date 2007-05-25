/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.pages;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.bugtracker.Framework;
import er.bugtracker.People;
import er.bugtracker.Session;
import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;

public class OnlineHatSystem extends WOComponent {

    public OnlineHatSystem(WOContext aContext) {
        super(aContext);
    }

    private EOEditingContext ec = ERXEC.newEditingContext();
    
    public void awake() {
        ec.lock();
    }
    
    public void sleep() {
        ec.unlock();
    }
    
    private NSArray _frameworks;
    public NSArray frameworks(){
        if(_frameworks == null){
            _frameworks = Framework.clazz.orderedFrameworks(ec);
        }
        return _frameworks;
    }
    public Framework currentFramework;
 
    public WOComponent grabHat() {
        ec.revert();
        currentFramework.grabHat();
        ec.saveChanges();
        return null;
    }

    public WOComponent returnHat() {
        ec.revert();
        currentFramework.releaseHat();
        ec.saveChanges();
        return null;
    }
    
    public String bgColor() {
        People owner = currentFramework.owner();
        boolean isOwner = ERXEOControlUtilities.eoEquals(owner, People.clazz.currentUser(ec));
        return owner!=null ? ( isOwner ? "#CCFFCC" : "#FFCCCC" ) : "#FFFFFF";
    }
}
