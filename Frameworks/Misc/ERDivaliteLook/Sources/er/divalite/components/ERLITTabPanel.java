package er.divalite.components;

import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXTabPanel;

/**
 * An XHTML based Tab Panel
 * 
 * @see ERXTabPanel for bindings
 * 
 */
public class ERLITTabPanel extends ERXTabPanel {
	public String containerID;
	
	public ERLITTabPanel(WOContext c) {
		super(c);
	}
    
    // accessors
    public String containerID() {
    	if (containerID == null) {
    		containerID = ERXWOContext.safeIdentifierName(context(), false);
    	} return containerID;
    }
}
