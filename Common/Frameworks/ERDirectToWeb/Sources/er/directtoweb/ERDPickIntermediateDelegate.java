/**
 * 
 */
package er.directtoweb;

import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

/**
 * Nextpage delegate that handles creating or deleting an intermediate object when you finished with a pick page.
 * This is usefull when you have a to-many that is not flattened, like an invoice with a "lineItem" to-many to
 * products. You set this up via:
 * 
 * @author ak
 *
 */
public class ERDPickIntermediateDelegate implements NextPageDelegate {
    EOEnterpriseObject eo;
    String relationshipName;
    String pickRelationshipName;
    WOComponent nextPage;

    /**
     * Constructs the delegate
     * @param eo
     * @param relationshipName
     * @param pickRelationshipName
     * @param nextPage
     */
    public ERDPickIntermediateDelegate(EOEnterpriseObject eo, String relationshipName,  
    		String pickRelationshipName, WOComponent nextPage) {
        this.eo = eo;
        this.relationshipName = relationshipName;
        this.nextPage = nextPage;
        this.pickRelationshipName = pickRelationshipName;
    }
    
    public WOComponent nextPage(WOComponent sender) {
        EOEditingContext ec = eo.editingContext();
        ec.lock();
        try {
            NSArray selectedObjects;
            if(sender instanceof ERDPickPageInterface) {
            	selectedObjects = ((ERDPickPageInterface)sender).selectedObjects();
            } else {
            	selectedObjects = (NSArray)sender.valueForKeyPath("selectedObjects");
            }
            NSArray relatedObjects = (NSArray)eo.valueForKeyPath(relationshipName);
            for(Enumeration e = relatedObjects.objectEnumerator(); e.hasMoreElements(); ) {
                EOEnterpriseObject relatedObject = (EOEnterpriseObject)e.nextElement();
                EOEnterpriseObject pickedObject = (EOEnterpriseObject)relatedObject.valueForKey(pickRelationshipName);
                if(!selectedObjects.containsObject(pickedObject)) {
                    eo.removeObjectFromBothSidesOfRelationshipWithKey(relatedObject, relationshipName);
                    ec.deleteObject(relatedObject);
                }
            }
            NSArray pickedObjects = (NSArray)eo.valueForKeyPath(relationshipName +"." + pickRelationshipName +".@unique");
            EOEntity entity = ERXEOAccessUtilities.destinationEntityForKeyPath(ERXEOAccessUtilities.entityForEo(eo), relationshipName);
            for(Enumeration e = selectedObjects.objectEnumerator(); e.hasMoreElements(); ) {
                EOEnterpriseObject selectedObject = (EOEnterpriseObject)e.nextElement();
                if(!pickedObjects.containsObject(selectedObject)) {
                    EOEnterpriseObject relatedObject = ERXEOControlUtilities.createAndInsertObject(ec, entity.name());
                    relatedObject.addObjectToBothSidesOfRelationshipWithKey(selectedObject, pickRelationshipName);
                    eo.addObjectToBothSidesOfRelationshipWithKey(relatedObject, relationshipName);
                }
            }
        } finally {
            ec.unlock();
        }
        
        return nextPage;
    }
}