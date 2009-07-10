package er.prototaculous;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.prototaculous.widgets.ModalBoxLink;

/**
 * An on DOM load variant of the AjaxModalBox
 * 
 * @see ModalBoxLink for api
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
    }
    
    // accessors
    public String scriptString() {
    	return "document.observe('dom:loaded', function() { Modalbox.show(" + ref() + ", " + options() + "); })";
    }
    
    // RM: FIXME: could probably move up to parent
	public String ref() {
    	if (hasBinding(Bindings.showID))
    		return (String) valueForBinding(Bindings.showID);
    	else return href();
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