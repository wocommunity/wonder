//
// ERD2WEditToOneFault.java: Class file for WO Component 'ERD2WEditToOneFault'
// Project ERDirectToWeb
//
// Created by giorgio on 13/07/05
//

package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WEditToOneFault;
import com.webobjects.directtoweb.EditRelationshipPageInterface;

/**
 * @d2wKey editIcon
 * @d2wKey disabled
 * @d2wKey editRelationshipConfigurationName
 */
public class ERD2WEditToOneFault extends D2WEditToOneFault {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = LoggerFactory.getLogger(ERD2WEditToOneFault.class);
    
    
    public ERD2WEditToOneFault(WOContext context) {
        super(context);
    }

    @Override
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
