package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Displays a fly-over that shows a table with hidden property keys. It's very cool in lists when
 * you don't have enough space for all the items you'd want to show.
 *
 * @d2wKey propertyKey the key value to show as label (optional)
 * @d2wKey label the string to show as label (optional)
 * @d2wKey customComponentName the component to use as display for the propertyKey when no label was given (optional)
 * @d2wKey displayPropertyKeys the keys value to show in the table
 *
 * @created ak on Tue Feb 10 2004
 * @project ERDirectToWeb
 */

public class ERD2WFlyOverCustomComponent extends D2WCustomComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WFlyOverCustomComponent.class,"components");

    protected String _linkId;
    protected String _spanId;

    /**
        * Public constructor
     * @param context the context
     */
    public ERD2WFlyOverCustomComponent(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public void reset() {
        super.reset();
        _linkId = null;
        _spanId = null;
    }

    public NSArray displayPropertyKeys() {
        return (NSArray)d2wContext().valueForKey("displayPropertyKeys");
    }
    public D2WContext d2wContext() {
    	return (D2WContext)valueForBinding("localContext");
    }
    
    public String label() {
        return (String)d2wContext().valueForKey("label");
    }
    public boolean hasLabel() {
        return label() != null;
    }
    
    public String id() {
        return ERXStringUtilities.replaceStringByStringInString(".", "_", context().elementID());
    }

    public String linkId() {
        if(_linkId == null) {
            _linkId = "link_" + id();
        }
        return _linkId;
    }

    public String spanId() {
        if(_spanId == null) {
            _spanId = "span_" + id();
        }
        return _spanId;
    }

    public String showString() {
        return "var style=document.getElementById('"+spanId()+"').style; style.visibility='visible';";
    }
    public String hideString() {
        return "var style=document.getElementById('"+spanId()+"').style; style.visibility='hidden';";
    }
}
