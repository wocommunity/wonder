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

public class ERDInspectButton extends ERDCustomEditComponent {

    public ERDInspectButton(WOContext context) {
        super(context);
    }

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public WOComponent inspect() {
        EOEditingContext context = ERXEC.newEditingContext();
        EOEnterpriseObject localObject = EOUtilities.localInstanceOfObject(context, object());
        String configuration = (String)valueForBinding("inspectConfigurationNameForEntity");
        InspectPageInterface epi = (InspectPageInterface)D2W.factory().pageForConfigurationNamed(configuration, session());
        epi.setObject(localObject);
        epi.setNextPage(context().page());
        context.hasChanges(); // Ensuring it survives.
        return (WOComponent)epi;
    }

}
