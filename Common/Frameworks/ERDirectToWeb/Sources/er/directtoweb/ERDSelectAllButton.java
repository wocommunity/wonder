package er.directtoweb;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Class for DirectToWeb Component ERDSelectAllButton.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Fri Sep 05 2003
 * @project ERDirectToWeb
 */

public class ERDSelectAllButton extends ERDActionButton {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDSelectAllButton.class,"components");
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDSelectAllButton(WOContext context) {
        super(context);
    }

    /** Utility to return the enclosing pick page, if there is one. */
    protected ERDPickPageInterface parentPickPage() {
        return (ERDPickPageInterface)enclosingPageOfClass(ERDPickPageInterface.class);
    }

    /** Selects all objects. */
    public WOComponent selectAllAction() {
        ERDPickPageInterface parent = parentPickPage();
        if(parent != null) {
            NSMutableArray selectedObjects = new NSMutableArray();
            NSArray list = displayGroup().allObjects();
            if(displayGroup().qualifier() != null) {
                list = EOQualifier.filteredArrayWithQualifier(list, displayGroup().qualifier());
            }
            for (Enumeration e=list.objectEnumerator();e.hasMoreElements();) {
                selectedObjects.addObject(e.nextElement());
            }
            parent.setSelectedObjects(selectedObjects);
        }
        return null;
    }
}
