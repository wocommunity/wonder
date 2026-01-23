package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WEditToManyFault;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * An alternative/simplified edit to many fault component for D2W
 * displaying the the toMany relationship in a <ul></ul> with add/remove functionality
 * 
 * @author mendis
 */
public class ERD2WEditToManyFaultList extends D2WEditToManyFault {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERD2WEditToManyFaultList(WOContext arg0) {
		super(arg0);
	}

	public static Logger log = LoggerFactory.getLogger(ERD2WEditToManyFaultList.class);
    
    // accessors
    public String addBoxID() {
    	return id() + "_add";
    }
    
    public String removeBoxID() {
        String primaryKeyString = ERXEOControlUtilities.primaryKeyStringForObject(browserItem);
        return ERXStringUtilities.safeIdentifierName(browserItem.entityName() + primaryKeyString);
    }
    
    public Object toOneDescription() {
        EOEnterpriseObject anEO = browserItem;
        if(anEO != null) {
            String keyWhenRelationship = keyWhenRelationship();
            if(keyWhenRelationship == null || keyWhenRelationship.equals("userPresentableDescription"))
                return anEO.userPresentableDescription();
            else
                return anEO.valueForKeyPath(keyWhenRelationship);
        } else {
            return null;
        }
    }
    
    public String id() {
    	return (String) d2wContext().valueForKey("id");
    }

    // actions    
    public WOComponent toOneAction() {
        EOEnterpriseObject anEO = browserItem;
        if(anEO == null) {
            return null;
        } else {
            InspectPageInterface inspectPage = (InspectPageInterface) D2W.factory().pageForConfigurationNamed("Inspect" + anEO.entityName(), session());
            inspectPage.setObject(anEO);
            inspectPage.setNextPage(context().page());
            return (WOComponent)inspectPage;
        }
    }
    
    public void removeFromToManyRelationshipAction() {
        EOEnterpriseObject anEO = browserItem;
        object().removeObjectFromBothSidesOfRelationshipWithKey(anEO, propertyKey());
    }
    
    @Override
    public WOComponent editValues() {
        String targetEntityName = relationship().destinationEntity().name();
        EditRelationshipPageInterface editPage = D2W.factory().editRelationshipPageForEntityNamed(targetEntityName, session());
        editPage.setMasterObjectAndRelationshipKey(object(), propertyKey());
        editPage.setNextPage(context().page());
        return (WOComponent)editPage;
    }
}
