//
// ERDInspectButton.java: Class file for WO Component 'ERDInspectButton'
// Project ERDirectToWeb
//
// Created by bposokho on Mon Jan 06 2003
//
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import er.extensions.*;

public class ERDInspectButton extends ERDActionButton {

    public ERDInspectButton(WOContext context) {
        super(context);
    }
    
    public WOComponent inspectObjectAction() {
    	EOEditingContext context = ERXEC.newEditingContext();
    	//CHECKME ak: I don't remember why we would use a local instance when we just want to inspect...
    	context.lock();
    	try {
    		EOEnterpriseObject localObject = EOUtilities.localInstanceOfObject(context, object());
    		String configuration = (String)valueForBinding("inspectConfigurationName");
    		InspectPageInterface epi = (InspectPageInterface)D2W.factory().pageForConfigurationNamed(configuration, session());
    		epi.setObject(localObject);
    		epi.setNextPage(context().page());
    		context.hasChanges(); // Ensuring it survives.
    		return (WOComponent)epi;
    	} finally {
    		context.unlock();
    	}
    }

}
