package er.prototaculous.widgets;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Encapsulates http://www.stickmanlabs.com/lightwindow 2.0
 *
 * Extending the api of WOHyperlink. Only additional bindings:
 *
 * @binding type	 // LightWindow lightwindow_type
 * 
 * @author mendis
 */
public class LightWindowLink extends LightWindow {	
    public LightWindowLink(WOContext context) {
        super(context);
    }
    
    /*
     * API or bindings
     */
    public static interface Bindings extends LightWindow.Bindings {
    	public static final String href = "href";
    	public static final String pageName = "pageName";
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    // accessors
    public String classString() {
    	String classString = hasBinding("class") ? " " + (String) valueForBinding("class") : "";
    	return "lightwindow" + classString;
    }
    
    @SuppressWarnings("unchecked")
	public String href() {
    	if (hasBinding(Bindings.href))
    		return (String) valueForBinding(Bindings.href);
    	else if (hasBinding(Bindings.action) || hasBinding(Bindings.pageName))
    		return context().componentActionURL(application().ajaxRequestHandlerKey());
    	else if (hasBinding(Bindings.directActionName)) {
    		String directActionName = (String) valueForBinding(Bindings.directActionName);
    		NSDictionary queryDictionary = (NSDictionary) valueForBinding(Bindings.queryDictionary);
    		
    		return context().directActionURLForActionNamed(directActionName, queryDictionary);
    	} else return null;
    }
    
    /*
     * Note: these params differ from the button options
     */
    private NSArray<String> _params() {
    	NSArray<String> params = new NSMutableArray<String>();
    	
    	// add the page type
    	if (hasBinding(Bindings.pageName) || hasBinding(Bindings.action) || hasBinding(Bindings.directActionName)) {
    		String _type = (hasBinding(Bindings.type)) ? (String) valueForBinding(Bindings.type) : type;
        	params.add("lightwindow_type=" + _type);		// external type, though could be page, etc.
    	} 
    	
    	if (hasBinding(Bindings.formID)) params.add("lightwindow_form=" + formID());
    	if (hasBinding(Bindings.height)) params.add("lightwindow_height=" + valueForBinding(Bindings.height));
    	if (hasBinding(Bindings.width)) params.add("lightwindow_width=" + valueForBinding(Bindings.width));

    	return params.immutableClone();
    }
    
    public String params() {
    	return _params().componentsJoinedByString(",");
    }
    
    // R/R
    @Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if (aContext.senderID().equals(aContext.elementID())) {		// check to see if the request is coming from modalbox
    		if (hasBinding(Bindings.action))
    			return (WOComponent) valueForBinding(Bindings.action);
    		else if (hasBinding(Bindings.pageName)) {
    			String pageName = (String) valueForBinding(Bindings.pageName);
    			return pageWithName(pageName);
    		} 
    	} return null;
    }
}
