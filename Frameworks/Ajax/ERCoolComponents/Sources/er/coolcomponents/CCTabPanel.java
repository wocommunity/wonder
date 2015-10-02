package er.coolcomponents;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.components.ERXClickToOpenSupport;
import er.extensions.components.ERXTabPanel;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Tab panel that uses CCSubmitLinkButtons. Allows denial of tab switching. Useful when validation failures occur.
 * 
 * @binding tabs a list of objects representing the tabs
 * @binding tabNameKey a string containing a key to apply to tabs to get the title of the tab
 * @binding selectedTab contains the selected tab
 * @binding tabClass CSS class to use for the selected tab
 * @binding nonSelectedTabClass CSS class to use for the unselected tabs
 * @binding submitActionName if this binding is non null, tabs will contain a submit button instead of a regular hyperlink and the action
 * @binding useFormSubmit true, if the form should be submitted before switching, allows denial of switches
 * @binding id CSS id for the wrapper div
 */
public class CCTabPanel extends ERXTabPanel {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public CCTabPanel(WOContext context) {
        super(context);
    }
    
    public boolean dontSubmitForm() {
    	return !ERXValueUtilities.booleanValue(valueForBinding("useFormSubmit"));
    }
    
    @Override
    public Object nonSelectedTabClass() {
    	Object tabClass = super.nonSelectedTabClass();
        if ("nonSelectedTab".equals(tabClass)) {
        	tabClass = "Tab TPTab";
        }
        return tabClass;
    }
    
    @Override
    public Object tabClass() {
    	Object tabClass = super.tabClass();
        if ("tab".equals(tabClass)) {
        	tabClass = "Tab TPTab TPTab_Selected";
        }
        return tabClass;
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {

    	//
    	// Since this component is not derived from an ERXComponent subclass
    	// use this boilerplate code to pull in clickToOpen support.
    	//
    	boolean clickToOpenEnabled = clickToOpenEnabled(response, context);
    	ERXClickToOpenSupport.preProcessResponse(response, context, clickToOpenEnabled);
    	try {
    		super.appendToResponse(response, context);
    	}
    	finally {
    		ERXClickToOpenSupport.postProcessResponse(getClass(), response, context, clickToOpenEnabled);
    	}
    }
    
	/**
	 * Returns whether or not click-to-open should be enabled for this
	 * component. By default this returns ERXClickToOpenSupport.isEnabled().
	 * 
	 * @param response
	 *            the response
	 * @param context
	 *            the context
	 * @return whether or not click-to-open is enabled for this component
	 */
	public boolean clickToOpenEnabled(WOResponse response, WOContext context) {
		return ERXClickToOpenSupport.isEnabled();
	}
    
}

