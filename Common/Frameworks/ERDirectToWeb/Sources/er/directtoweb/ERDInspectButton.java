//
// ERDInspectButton.java: Class file for WO Component 'ERDInspectButton'
// Project ERDirectToWeb
//
// Created by bposokho on Mon Jan 06 2003
//
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class ERDInspectButton extends ERDActionButton {

    public ERDInspectButton(WOContext context) {
        super(context);
    }

    public WOComponent inspectObjectAction() {

        WOComponent returnedValue = null;
        String configuration = (String) valueForBinding("inspectConfigurationName");
        InspectPageInterface epi = (InspectPageInterface) D2W.factory().pageForConfigurationNamed(configuration, session());
        epi.setNextPage(context().page());

        boolean useExistingEditingContext = ERXValueUtilities.booleanValue(valueForBinding("useExistingEditingContext"));

        if (useExistingEditingContext) {
            // We just want to use the object's exiting editing context to inspect it
            epi.setObject(object());
            returnedValue = (WOComponent) epi;
        } else {
            EOEditingContext context = ERXEC.newEditingContext();
            //CHECKME ak: I don't remember why we would use a local instance when we just want to inspect...
            context.lock();
            try {
                EOEnterpriseObject localObject = EOUtilities.localInstanceOfObject(context, object());
                epi.setObject(localObject);
                context.hasChanges(); // Ensuring it survives.
                returnedValue = (WOComponent) epi;
            } finally {
                context.unlock();
            }
        }
        return returnedValue;
    }
}
