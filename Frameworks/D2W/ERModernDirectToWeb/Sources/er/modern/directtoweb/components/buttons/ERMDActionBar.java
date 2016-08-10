package er.modern.directtoweb.components.buttons;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDActionBar;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Subclass of ERDActionBar that uses ERModern stylable buttons
 * and wraps the buttons in an ActionList UL.
 * 
 * @author david
 */
public class ERMDActionBar extends ERDActionBar {
	
    public ERMDActionBar(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean isStateless() {
    	return false;
    }

	public String buttonCssClass() {
		String cssClass = "Button BranchButton";
		String branchName = branchName();
		if (branchName != null) {
			cssClass = cssClass + " " + ERXStringUtilities.capitalize(branchName) + "BranchButton";
		}
		return cssClass;
	}
	
	public String actionListCssClass() {
		String cssClass = "ActionList ActionBarActionList";
		String pageConfiguration = (String)d2wContext().valueForKey("pageConfiguration"); 
		if (pageConfiguration != null) {
			cssClass = cssClass + " " + pageConfiguration + "ActionBarActionList";
		}
		return cssClass;
	}
    
}
