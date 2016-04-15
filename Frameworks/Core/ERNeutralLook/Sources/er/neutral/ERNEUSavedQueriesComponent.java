package er.neutral;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

import er.directtoweb.components.misc.ERDSavedQueriesComponent;

/**
 * This Component will store the forms values in the displayGroup of a Query Page into user preferences
 * under a saved name.  The list of saved queries will be available in the popup for future use.
 *
 * Preference key = "SavedQueryFor"+pageConfiguration
 *
 * The ERNEUQueryPage has been modified to switch in the value of the rule keyPath = "savedQueryComponentName"
 * So, if you want this component to appear on your query page, create a rule defining savedQueryComponentName="ERNEUSavedQueriesComponent"
 *
 * User: dscheck
 */

public class ERNEUSavedQueriesComponent extends ERDSavedQueriesComponent {
    //  For backward compatibility we need er.neutral.ERNEUSavedQueriesComponent.SavedQuery
    //  So that previously saved preferences can still be decoded
    //  The EOKeyValueUnarchiver uses the class name stored in the saved dictionary to instantiate the destination class

    static class SavedQuery extends ERDSavedQueriesComponent.SavedQuery {
        public SavedQuery() {
            super();
        }

        public SavedQuery(String name, WODisplayGroup displayGroup) {
            super(name, displayGroup);
        }
    }

    public ERNEUSavedQueriesComponent(WOContext context) {
        super(context);
    }
    
}
