package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Delete button for repetitions. 
 *
 * @binding object
 * @binding dataSource
 * @binding d2wContext
 * @binding trashcanExplanation
 * @binding noTrashcanExplanation
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDDeleteButton extends ERDActionButton {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDDeleteButton.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDDeleteButton(WOContext context) {
        super(context);
    }

    public boolean canDelete() {
        return object() != null && object() instanceof ERXGuardedObjectInterface ? ((ERXGuardedObjectInterface)object()).canDelete() : true;
    }

    public WOComponent deleteObjectAction() {
        ConfirmPageInterface nextPage = (ConfirmPageInterface)D2W.factory().pageForConfigurationNamed((String)valueForBinding("confirmDeleteConfigurationName"), session());
        nextPage.setConfirmDelegate(new ERDDeletionDelegate(object(), dataSource(), context().page()));
        nextPage.setCancelDelegate(new ERDPageDelegate(context().page()));
        D2WPage d2wPage = ((D2WPage)nextPage);
        
        String message = ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject("ERDTrashcan.confirmDeletionMessage", d2wContext()); 

        nextPage.setMessage(message);
        d2wPage.setObject(object());
        return (WOComponent) nextPage;
    }

    public String onMouseOverTrashcan() {
        return hasBinding("trashcanExplanation") ? "self.status='" + valueForBinding("trashcanExplanation") + "'; return true" : "";
    }

    public String onMouseOverNoTrashcan() {
        return hasBinding("noTrashcanExplanation") ? "self.status='" + valueForBinding("noTrashcanExplanation") + "'; return true" : "";
    }    
}
