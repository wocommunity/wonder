package er. bugtracker;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Class for BugTracker Component CollapsibleList.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Thu Aug 22 2002
 * @project BugTracker
 */

public class CollapsibleList extends WOComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(CollapsibleList.class,"components");
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
    
    NSArray array() {
        return (NSArray)valueForBinding("items");
    }
    String entityName() {
        return (String)valueForBinding("entityName");
    }
    public String openedLabelString() {
        return plurifiedString((String)valueForBinding("openedLabelString"), entityName(), array());
    }
    public String closedLabelString() {
        return plurifiedString((String)valueForBinding("closedLabelString"), entityName(), array());
    }
}
