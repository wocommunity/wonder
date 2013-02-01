package er.extensions.woextensions._ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.woextensions.WOCollapsibleComponentContent;

/**
 * Ajax/XHTML collapsible component
 * @see WOCollapsibleComponentContent
 * 
 * @binding container
 * 
 * @author dchonen
 */
public class WOAjaxCollapsibleComponentContent extends WOCollapsibleComponentContent {

	
    public WOAjaxCollapsibleComponentContent(WOContext context) {
        super(context);
    }
    
    public String toggleButtonClass() {
    	return isVisible() ? "open" : "closed";
    }
    
    // actions (ajax)
    @Override
    public WOComponent toggleVisibilityClicked()  {
        super.toggleVisibilityClicked();
        return this;
    }
}
