package er.prototaculous.widgets;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Encapsulation of http://www.wildbit.com/labs/modalbox/ (a re-implementation of AjaxModalDialog)
 * This component extends api of WOSubmitButton
 * i.e api compatible with WOSubmitButton
 * 
 * @author mendis
 * 
 * @binding 	method 			The method of the modalbox/Ajax request. i.e 'post' or 'get'
 * @binding 	serializeForm	If you do not want to process form data set to false
 * 								NOTE: there appears to be a limitation as to how much can be serialized in WO + Ajax. Results vary - IE being the worst :)
 * 								Default is true
 * 
 * FIXME		Form value taking
 */
public class ModalBoxButton extends ModalBox {
	private static boolean _serializeForm = true;
	
    public ModalBoxButton(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    @Override
    public void reset() {
    	super.reset();
    	_serializeForm = true;
    }
    
    /*
     * API or bindings
     */
    public static interface Bindings extends ModalBox.Bindings {
    	public static final String formID = "formID";
    	public static final String method = "method";
    	public static final String serializeForm = "serializeForm";

    }
    
    // accessors
    public String onClick() {
    	return "Modalbox.show('" + href() + "', " + options() + "); return false;";
    }
    
    @Override
    protected NSArray<String> _options() {
    	NSMutableArray<String> params = new NSMutableArray<String>(super._options());
    	
    	params.add("method: '" + method() + "'");
    	if (shouldSerializeForm()) params.add("params: " + _formString() + ".serialize(true)");
    	    if (hasBinding(Bindings.title)) params.add("title: '" + title() + "'");
    		
    	return params.immutableClone();
    }
    
    public String method() {
    	return (_method() != null) ? _method() : "post";
    }
    
    private String _method() {
    	return (String) valueForBinding(Bindings.method);
    }
    
    public String formID() {
    	return (String) valueForBinding(Bindings.formID);
    }
    
    public boolean shouldSerializeForm() {
    	return (hasBinding(Bindings.serializeForm)) ? _serializeForm() : _serializeForm;
    }
    
    private Boolean _serializeForm() {
    	return (Boolean) valueForBinding(Bindings.serializeForm);
    }
    
    private String _formString() {
    	return (hasBinding(Bindings.formID)) ? "$('" + formID() + "')" : "this.form";
    }
    
	public String href() {
    	if (hasBinding(Bindings.action))
    		return context().componentActionURL(application().ajaxRequestHandlerKey());
    	else if (hasBinding(Bindings.directActionName)) {
    		String directActionName = (String) valueForBinding(Bindings.directActionName);
    		@SuppressWarnings("unchecked")  NSDictionary<String, Object> queryDictionary = (NSDictionary<String, Object>) valueForBinding(Bindings.queryDictionary);
    		
    		return context().directActionURLForActionNamed(directActionName, queryDictionary);
    	} else return null;
    }
    
    // actions
    public WOActionResults invokeAction() {
		if (hasBinding(Bindings.action)) {
			WOActionResults action = (WOActionResults) valueForBinding(Bindings.action);
			if (action instanceof WOComponent)  ((WOComponent) action)._setIsPage(true);	// cache is pageFrag cache
			return action;
		} else return context().page();
    }
}
