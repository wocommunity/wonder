//
// ERD2WEditToManyFault.java: Class file for WO Component 'ERD2WEditToManyFault'
// Project ERDirectToWeb
//
// Created by max on Tue Jan 07 2003
//
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

import er.extensions.*;

public class ERD2WEditToManyFault extends com.webobjects.directtoweb.D2WEditToManyFault {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WEditToManyFault.class);

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WEditToManyFault(WOContext context) {
        super(context);
    }

    /**
     * Edits the relationship values of the object.
     * @return edit page
     */
    public WOComponent editValues() {
        WOComponent editPage = null;
        String editRelationshipConfigurationName = (String)d2wContext().valueForKey("editRelationshipConfigurationName");
        if (editRelationshipConfigurationName != null && editRelationshipConfigurationName.length() > 0) {
            editPage = D2W.factory().pageForConfigurationNamed(editRelationshipConfigurationName, session());
            if (editPage instanceof EditRelationshipPageInterface) {
                ((EditRelationshipPageInterface)editPage).setMasterObjectAndRelationshipKey(object(),propertyKey());
                ((EditRelationshipPageInterface)editPage).setNextPage(context().page());
            } else {
                log.warn("Unsupported relationship editing page: " + editPage.getClass().getName());
            }
        }
        return editPage != null ? editPage : super.editValues();
    }
}