//
// ERD2WEditToManyFault.java: Class file for WO Component 'ERD2WEditToManyFault'
// Project ERDirectToWeb
//
// Created by max on Tue Jan 07 2003
//
package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WEditToManyFault;
import com.webobjects.directtoweb.EditRelationshipPageInterface;

/**
 * Enhanced to-many component, which provides the means to specify which edit-relationship page gets chosen.
 * @d2wKey editIcon
 * @d2wKey allowCollapsing
 * @d2wKey size
 * @d2wKey editRelationshipConfigurationName
 */
public class ERD2WEditToManyFault extends D2WEditToManyFault {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = LoggerFactory.getLogger(ERD2WEditToManyFault.class);

    /**
     * Public constructor
     * @param context current context
     */
    public ERD2WEditToManyFault(WOContext context) {
        super(context);
    }

    /**
     * Edits the relationship values of the object. You can set the 
     * key <code>editRelationshipConfigurationName</code> to return a EditRelationshipPageInterface.
     * 
     * @return page to edit the relationship
     */
    @Override
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