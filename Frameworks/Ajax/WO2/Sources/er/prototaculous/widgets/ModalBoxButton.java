package er.prototaculous.widgets;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXWOContext;
import er.prototaculous.widgets.ModalBox.Bindings;


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
 */
public class ModalBoxButton extends ModalBox {
	private static boolean _serializeForm = true;
	
    public ModalBoxButton(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
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
    	
    	if (hasBinding(Bindings.method)) params.add("method: '" + method() + "'");
    	if (shouldSerializeForm()) params.add("params: Form.serialize(" + _formString() + ")");
    	if (hasBinding(Bindings.title)) params.add("title: '" + title() + "'");
    		
    	return params.immutableClone();
    }
    
    public String method() {
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
    	return (hasBinding(Bindings.formID)) ? "'" + formID() + "'" : "this.form";
    }
    
	public String href() {
    	if (hasBinding(Bindings.action))
    		return (String) ERXWOContext.ajaxActionUrl(context());
    	else if (hasBinding(Bindings.directActionName)) {
    		String directActionName = (String) valueForBinding(Bindings.directActionName);
    		@SuppressWarnings("unchecked")  NSDictionary<String, Object> queryDictionary = (NSDictionary<String, Object>) valueForBinding(Bindings.queryDictionary);
    		
    		return context().directActionURLForActionNamed(directActionName, queryDictionary);
    	} else return null;
    }
    
    // R/R
	@Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if (aContext.senderID().equals(aContext.elementID())) {		// check to see if the request is coming from modalbox
    		if (hasBinding(Bindings.action)) {
        		aContext._setActionInvoked(true);
    			return (WOComponent) valueForBinding(Bindings.action);
    		}
    	} return null;
    }
    
    @Override
    public void awake() {
    	super.awake();
    	context()._setFormSubmitted(true);
    }
    
    @Override
    public void sleep() {
    	super.sleep();
    	context()._setFormSubmitted(false);
    }

	public WOActionResults dummy() {
		return null;
	}
}