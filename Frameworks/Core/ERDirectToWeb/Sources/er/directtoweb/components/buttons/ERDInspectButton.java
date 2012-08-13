//
// ERDInspectButton.java: Class file for WO Component 'ERDInspectButton'
// Project ERDirectToWeb
//
// Created by bposokho on Mon Jan 06 2003
//
package er.directtoweb.components.buttons;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXValueUtilities;

public class ERDInspectButton extends ERDActionButton {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
