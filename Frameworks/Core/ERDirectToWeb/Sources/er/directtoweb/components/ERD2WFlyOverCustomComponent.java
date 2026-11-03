package er.directtoweb.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author ak on Tue Feb 10 2004
 */
public class ERD2WFlyOverCustomComponent extends D2WCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERD2WFlyOverCustomComponent.class);

    /**
        * Public constructor
     * @param context the context
     */
    public ERD2WFlyOverCustomComponent(WOContext context) {
        super(context);
    }

    @Override
    public boolean isStateless() { return true; }

    @Override
    public NSArray displayPropertyKeys() {
        return (NSArray)d2wContext().valueForKey("displayPropertyKeys");
    }
    
    @Override
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
