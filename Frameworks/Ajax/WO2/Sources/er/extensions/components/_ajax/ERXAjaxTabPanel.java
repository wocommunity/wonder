package er.extensions.components._ajax;

import com.webobjects.appserver.*;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXTabPanel;

/**
 * An XHTML Ajax based Tab Panel
 * 
 * It's worth noting that because this is an ajax tab that the contents of the tab must be contained within this tab contents in order to be updated.
 * 
 * @binding tabs: a list of objects representing the tabs
 * @binding tabNameKey: a string containing a key to apply to tabs to get the title of the tab
 * @binding selectedTab: contains the selected tab
 * @binding submitActionName: if this binding is non null, tabs will contain a submit button instead of a regular hyperlink and the action pointed to by the binding will be called
 */
public class ERXAjaxTabPanel extends ERXTabPanel {
	public String containerID;

    public ERXAjaxTabPanel(WOContext context) {
        super(context);
    }
    
    // accessors
    public String containerID() {
    	if (containerID == null) {
    		containerID = ERXWOContext.safeIdentifierName(context(), false);
    	} return containerID;
    }
}