/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.ERXEC;

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
    
    protected NSArray _directtowebfiles;
    public NSArray directtowebfiles(){
        if(_directtowebfiles == null){
            _directtowebfiles = Framework.clazz.orderedFrameworks(ec);
        }
        return _directtowebfiles;
    }
    protected EOEnterpriseObject directtowebfile;
    protected EOEnterpriseObject _localUser;
    protected EOEnterpriseObject localUser() {
        if (_localUser==null) {
            _localUser=EOUtilities.localInstanceOfObject(ec, ((Session)session()).user());
        }
        return _localUser;
    }

    public WOComponent grabHat() {
        ec.revert();
        directtowebfile.addObjectToBothSidesOfRelationshipWithKey(localUser(),"owner");
        directtowebfile.takeValueForKey(new NSTimestamp(), "ownedSince");
        ec.saveChanges();
        return null;
    }

    public WOComponent returnHat() {
        ec.revert();
        EOEnterpriseObject localUser=EOUtilities.localInstanceOfObject(ec, ((Session)session()).user());
        directtowebfile.removeObjectFromBothSidesOfRelationshipWithKey(localUser,"owner");
        directtowebfile.takeValueForKey(null, "ownedSince");
        ec.saveChanges();
        return null;
    }
    
    public String bgColor() {
        EOEnterpriseObject owner=(EOEnterpriseObject)directtowebfile.valueForKey("owner");
        return owner!=null ? ( owner==localUser() ? "#CCFFCC" : "#FFCCCC" ) : "#FFFFFF";
    }
}
