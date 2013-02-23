package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.QueryOperatorComponent;
import com.webobjects.foundation.NSArray;

import er.extensions.localization.ERXLocalizer;

/**
 * Localized replacement for D2WQueryOperator.
 * @d2wKey queryOperators
 */
public class ERD2WQueryOperator extends QueryOperatorComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public NSArray queryOperators;
    public String currentOperator;
    
    public ERD2WQueryOperator(WOContext context) {
        super(context);
    }

    @Override
    public void reset() {
        super.reset();
        queryOperators = null;
    }

    public String currentDisplayString() {
        return (String)ERXLocalizer.currentLocalizer().valueForKeyPath("ERD2WQueryOperator." + currentOperator);
    }

    public NSArray queryOperators() {
        if(queryOperators == null) {
            queryOperators = (NSArray)d2wContext().valueForKey("queryOperators");
        }
        return queryOperators;
    }
}
