package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Select button to display in lists.
 *
 * @binding d2wContext the d2wContext
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDSelectButton extends ERDActionButton {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDSelectButton.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDSelectButton(WOContext context) {
        super(context);
    }

    protected ERD2WListPage selectingPage() {
        WOComponent p = parent();
        while(p != null) {
            if(p instanceof ERD2WListPage)
                return (ERD2WListPage)p;
            p = p.parent();
        }
        return null;
    }
    
    public WOComponent selectObjectAction() {
        ERD2WListPage parent = selectingPage();
        if(parent != null) {
            // HACK ak: this is just a temporary fix until I can think of something better
            context()._setCurrentComponent(parent);
            try {
                return parent.selectObjectAction();
            } finally {
                context()._setCurrentComponent(this);
            }
        }
        throw new IllegalStateException("This page is not an instance of ERD2WListPage. I can't select here.");
    }
}
