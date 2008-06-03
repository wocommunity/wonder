package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WCustomComponent;
import com.webobjects.foundation.NSArray;

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
    private static final Logger log = Logger.getLogger(ERD2WFlyOverCustomComponent.class);

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
}
