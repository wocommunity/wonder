package er.extensions.components._ajax;

import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXTabPanel;

/**
 * An XHTML Ajax based Tab Panel
 * 
 * It's worth noting that because this is an ajax tab that the contents of the tab must be contained within this tab contents in order to be updated.
 * 
 * @see ERXTabPanel for bindings
 * 
 * @binding		container		This is a required binding that says which container to perform an Ajax.Updater on when the tab is clicked.
 */
public class ERXAjaxTabPanel extends ERXTabPanel {
	public String containerID;

    public ERXAjaxTabPanel(WOContext context) {
        super(context);
    }
    
    // accessors
    public String containerID() {
    	if (containerID == null) {
    		if (hasBinding("container")) containerID = (String) valueForBinding("id");
    		else containerID = ERXWOContext.safeIdentifierName(context(), false);
    	} return containerID;
    }
}