package er.prototaculous.widgets;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXStringUtilities;


/**
 * An on DOM load variant of the AjaxModalBox
 * 
 * @see AjaxModalBoxLink for api
 * 
 * @author mendis
 *
 */
public class ModalBoxOnLoad extends ModalBoxLink {
    public ModalBoxOnLoad(WOContext context) {
        super(context);
    }
    
    /*
     * API or bindings
     */
    public static interface Bindings extends ModalBoxLink.Bindings {
    	public static final String showID = "showID";
    	public static final String id = "id";
    }
    
    // accessors
    public String scriptString() {
    	return isAjaxRequest() ? _scriptString() : "document.observe('dom:loaded', function() { " + _scriptString() + " })";
    }
    
    private String _scriptString() {
    	return "Modalbox.show(" + ref() + ", " + options() + ");";
    }
    
    // RM: FIXME: could probably move up to parent
	public String ref() {
    	if (hasBinding(Bindings.showID))
    		return (String) valueForBinding(Bindings.showID);
    	else return href();
    }
	
	public String id() {
		return (_id() != null) ? _id() : ERXStringUtilities.safeIdentifierName(context().elementID());
	}
	
	private String _id() {
		return (String) valueForBinding(Bindings.id);
	}
	
	private boolean isAjaxRequest() {
		return ERXAjaxApplication.isAjaxRequest(context().request());
	}
	
	@Override
    protected NSArray<String> _options() {
    	NSMutableArray<String> params = new NSMutableArray<String>(super._options());
    	
    	if (hasBinding(Bindings.title)) {
    		params.remove("title: this.title");
    		params.add("title: '" + valueForBinding("title") + "'");
    	}
    		
    	return params.immutableClone();
    }
}