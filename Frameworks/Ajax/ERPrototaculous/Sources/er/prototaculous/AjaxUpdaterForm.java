package er.prototaculous;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WOForm;

import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXProperties;


/**
 * Form with Ajax.Updater onsubmit:
 * @see WOForm for bindings (href and multipleSubmit not supported - It's assumed to be default)
 * 
 * @binding showForm															@see ERXOptionalForm
 * 
 * @property er.extensions.ERXWOForm.addDefaultSubmitButtonDefault 				@see ERXWOForm except by default it is used
 * 
 * @author mendis
 */
public class AjaxUpdaterForm extends AjaxUpdater {
	public boolean addDefaultSubmitButtonDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXWOForm.addDefaultSubmitButtonDefault", true);

    public AjaxUpdaterForm(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }    
	
    // accessors
    private String _containerString() {
    	String container = container();
    	return (container != null) ? "'" + container + "'": "this";
    }
    
    public String onSubmit() {
    	return "new Ajax.Updater(" + _containerString() + ", '" + href() + "', {parameters: $(this).serialize(true)}); return false;";
    }
    
	public boolean _omitTags;
    
    public String href() {    // action
		return context().componentActionURL(application().ajaxRequestHandlerKey());
    }
    
    public String classString() {
    	String classString = "AjaxUpdaterForm";
    	classString += (_class() != null) ? " " + _class() : ""; 
    	return classString;
    }
    
    private String _class() {
    	return (String) valueForBinding("class");
    }
    
    private String _elementID;
    public String elementID() {
    	if (_elementID == null) _elementID = context().elementID();
    	return _elementID;
    }
    
    /**
     * Determines if a form tag should be shown.
     * This defaults to true.
     * 
     * @return if a form should be displayed.
     */
    public boolean showForm() {
        return ERXComponentUtilities.booleanValueForBinding(this, "showForm", true);
    }
    
    public boolean omitTags() {
    	return !showForm();
    }
    
    // actions
    public WOActionResults invokeAction() {
		if (hasBinding(Bindings.action))  {
			WOActionResults action = action();
			if (action instanceof WOComponent)  ((WOComponent) action)._setIsPage(true);	// cache is pageFrag cache
			return action;
		} else {
	    	_setIsPage(true);
	    	_omitTags = true;
	    	return this;
		}
    }
    
    //R&R
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
        String forceFormSubmittedElementID = (String) request.formValueForKey("_forceFormSubmitted");
        boolean forceFormSubmitted = forceFormSubmittedElementID != null && forceFormSubmittedElementID.equals(context.elementID());
        boolean _wasFormSubmitted = context.wasFormSubmitted();
        
        if (showForm()) {
        	if (forceFormSubmitted) context.setFormSubmitted(true);
        	super.takeValuesFromRequest(request, context);
        	if (forceFormSubmitted) context.setFormSubmitted(_wasFormSubmitted);
        } else super.takeValuesFromRequest(request, context);
	}
}