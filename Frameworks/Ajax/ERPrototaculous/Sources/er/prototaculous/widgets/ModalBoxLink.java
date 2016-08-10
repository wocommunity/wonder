package er.prototaculous.widgets;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;


/**
 * Encapsulation of http://www.wildbit.com/labs/modalbox/ (a re-implementation of AjaxModalDialog)  
 * Extends the api of WOHyperlink 
 * i.e api compatible with WOHyperlink
 * 
 * @author mendis
 *
 */
public class ModalBoxLink extends ModalBox {
    public ModalBoxLink(WOContext context) {
        super(context);
    }
    
    /*
     * API or bindings common to light window subcomponents
     */
    public static interface Bindings extends ModalBox.Bindings {
    	public static final String href = "href";
    	public static final String pageName = "pageName";
    }
    
    // accessors
    public String onClick() {
    	return "Modalbox.show(this.href, " + options() + "); return false;";
    }
    
    // accessors
    @Override
    protected NSArray<String> _options() {
    	NSMutableArray<String> params = new NSMutableArray<String>(super._options());
    	
    	if (hasBinding(Bindings.title)) params.add("title: this.title");
    		
    	return params.immutableClone();
    }
    
    @SuppressWarnings("unchecked")
	public String href() {
    	if (hasBinding(Bindings.href))
    		return (String) valueForBinding(Bindings.href);
    	else if (hasBinding(Bindings.action) || hasBinding(Bindings.pageName))
    		return context().componentActionURL(application().ajaxRequestHandlerKey());
    	else if (hasBinding(Bindings.directActionName)) {
    		String directActionName = (String) valueForBinding(Bindings.directActionName);
    		NSDictionary<String, Object> queryDictionary = (NSDictionary<String, Object>) valueForBinding(Bindings.queryDictionary);
    		
    		return context().directActionURLForActionNamed(directActionName, queryDictionary);
    	} else return null;
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    @Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if (aContext.senderID().equals(aContext.elementID())) {		// check to see if the request is coming from modalbox
    		//x-requested-with=[XMLHttpRequest]
    		if (hasBinding(Bindings.action))
    			return (WOComponent) valueForBinding(Bindings.action);
    		else if (hasBinding(Bindings.pageName)) {
    			String pageName = (String) valueForBinding(Bindings.pageName);
    			return pageWithName(pageName);
    		} 
    	} return null;
    }
}