package er.bugtracker;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import er.extensions.appserver.ERXSession;
import er.extensions.localization.ERXLocalizer;
import er.extensions.logging.ERXLogger;

/**
 * Holds a D2W list in a collapsible component.
 *
 * @binding closedLabelString 
 * @binding items 
 * @binding openedLabelString 
 * @binding entityName 
 * @binding bgcolor 
 * @binding pageConfiguration 
 * @binding noItemsString 
 * 
 * @created ak on Thu Aug 22 2002
 * @project BugTracker
 */

public class CollapsibleList extends WOComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(CollapsibleList.class.getClass().getName() + ".components");
    protected String openedLabelString;
    protected String closedLabelString;
	
    /**
     * Public constructor
     * @param context the context
     */
    public CollapsibleList(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    ERXLocalizer localizer() {
        return ((Session)session()).localizer();
    }
    String plurifiedString(String template, String entity, NSArray arr) {
        String localizedEntityName = localizer().localizedStringForKeyWithDefault(entity);
        return localizer().plurifiedStringWithTemplateForKey(template, localizedEntityName, arr.count(), session());
    }
    
    protected NSArray array() {
        return (NSArray)valueForBinding("items");
    }
    protected String entityName() {
        return (String)valueForBinding("entityName");
    }
    public String openedLabelString() {
        return plurifiedString((String)valueForBinding("openedLabelString"), entityName(), array());
    }
    public String closedLabelString() {
        return plurifiedString((String)valueForBinding("closedLabelString"), entityName(), array());
    }
}
