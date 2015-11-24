package er.jquery.support;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOGenericContainer;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * An ajax version of WOGenericContainer (only no support for invokeAction and formValue(s) bindings)
 * i.e support for jQuery
 * 
 * @see WOGenericContainer
 * 
 * @binding id
 * @binding	ajax			If the it is an Ajax update container. The default is false
 * @binding elementName		Defaults to "div"
 * @binding class
 * 
 * @author mendis
 * 
 * Note: it overloads the HTML5 ref attribute for the data-updateUrl.
 */
public class WXGenericContainer extends WOComponent {
	public boolean _omitTags;
	
    public WXGenericContainer(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    /*
     * Bindings/API of component
     */
    public static interface Bindings {
    	public static final String elementName = "elementName";
    	public static final String id = "id";
    	public static final String ajax = "ajax";
    	public static final String omitTags = "omitTags";
    }
    
    // accessors
    public String elementName() {
    	return (_elementName() == null) ? "div" : _elementName();
    }
    
    private String _elementName() {
    	return (String) valueForBinding(Bindings.elementName);
    }
    
    private boolean isAjax() {
    	return ERXComponentUtilities.booleanValueForBinding(this, Bindings.ajax, false);
    }
    
    public String id() {
    	return (_id() == null) ? ERXWOContext.safeIdentifierName(context(), false) : ERXStringUtilities.safeIdentifierName(_id());
    }
    
    private String _id() {
    	return (String) valueForBinding(Bindings.id);
    }
    
    public String ref() {
    	return isAjax() ? context().componentActionURL(WOApplication.application().componentRequestHandlerKey()) : null;
    }
    
    public boolean omitTags() {
    	return ERXComponentUtilities.booleanValueForBinding(this, Bindings.omitTags, _omitTags);
    }
    
    // action
    public WOActionResults invokeAction() {
    	context().setActionInvoked(true);
    	_setIsPage(true);
    	_omitTags = true;
    	return this;
    }
    
    //R&R
    @Override
    public void awake() {
    	super.awake();
    	_omitTags = false;
    }
}
