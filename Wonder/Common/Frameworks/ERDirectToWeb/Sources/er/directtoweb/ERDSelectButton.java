package er.directtoweb;

import com.webobjects.appserver.*;
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

    public WOComponent selectObjectAction() {
        SelectPageInterface parent = parentSelectPage();
        if(parent != null) {
            parent.setSelectedObject(object());
            return nextPageInPage((D2WPage)parent);
        }
        throw new IllegalStateException("This page is not an instance of SelectPageInterface. I can't select here.");
    }
}
