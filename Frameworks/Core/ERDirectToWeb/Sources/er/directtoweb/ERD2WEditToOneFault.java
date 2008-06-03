//
// ERD2WEditToOneFault.java: Class file for WO Component 'ERD2WEditToOneFault'
// Project ERDirectToWeb
//
// Created by giorgio on 13/07/05
//

package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WEditToOneFault;
import com.webobjects.directtoweb.EditRelationshipPageInterface;

public class ERD2WEditToOneFault extends D2WEditToOneFault {
    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WEditToOneFault.class);
    
    
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
