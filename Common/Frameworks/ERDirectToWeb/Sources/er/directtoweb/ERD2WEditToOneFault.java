//
// ERD2WEditToOneFault.java: Class file for WO Component 'ERD2WEditToOneFault'
// Project ERDirectToWeb
//
// Created by giorgio on 13/07/05
//

package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

import er.extensions.*;

public class ERD2WEditToOneFault extends D2WEditToOneFault {
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WEditToOneFault.class);
    
    
    public ERD2WEditToOneFault(WOContext context) {
        super(context);
    }

    public WOComponent editRelationship() {
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
        return editPage != null ? editPage : super.editRelationship();
        
    }
}
