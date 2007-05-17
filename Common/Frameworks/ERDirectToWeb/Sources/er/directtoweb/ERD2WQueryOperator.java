package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.QueryOperatorComponent;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXLocalizer;

/**
 * Localized replacement for D2WQueryOperator.
 */

public class ERD2WQueryOperator extends QueryOperatorComponent {

    public NSArray queryOperators;
    public String currentOperator;
    
    public ERD2WQueryOperator(WOContext context) {
        super(context);
    }

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
