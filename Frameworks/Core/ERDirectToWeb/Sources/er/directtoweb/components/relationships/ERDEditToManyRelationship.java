package er.directtoweb.components.relationships;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Convenient way to add, edit, remove or delete objects from a to-many relationship. 
 * Best used for owned relationships with not much info in them.
 * <p>
 * Displays a list of edit configurations, one for each related object, along
 * with a check box that handles the selection for the Delete and Remove buttons.
 * If the keypath is not directly off the root object, then the last part will be 
 * selected as the relationship. Meaning you can edit a customer and have the keypath
 * being <code>lastPurchase.items</code>
 * <p>
 * A know bug is when you add object and have validation failures, the failure display may
 * end up with the wrong object.
 * 
 * @binding object eo to edit
 * @binding key keypath to the relationship
 * @binding destinationEntityName entity name for the destination of the relationship
 * @binding hasRemove true if the objects can also be removed (as opposed to just being deleted)
 * @binding inspectConfigurationName page configuration to use for the edit component
 * @binding task should be "edit"
 */
public class ERDEditToManyRelationship extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERDEditToManyRelationship.class);

    public int index;
    public int objectsToAdd = 1;
    public EOEnterpriseObject currentObject;

    protected NSMutableArray selectedObjects;
    
    /**
     * Public constructor.
     * @param context the context
     */
    public ERDEditToManyRelationship(WOContext context) {
        super(context);
    }

    public String relationshipName() {
        return ERXStringUtilities.lastPropertyKeyInKeyPath(key());
    }
    
    public String destinationEntityName() {
        return (String)valueForBinding("destinationEntityName");
    }
    
    public String pageConfiguration() {
        return (String)valueForBinding("inspectConfigurationName");
    }
    
    @Override
    public String task() {
        return (String)valueForBinding("task");
    }
    
    public boolean isEditing() {
        return "edit".equals(task());
    }

    public NSArray objects() {
        return (NSArray) objectKeyPathValue();
    }

    private EOEnterpriseObject relationshipObject() {
        EOEnterpriseObject object = object();
        if(!relationshipName().equals(key())) {
            object = (EOEnterpriseObject)object.valueForKeyPath(ERXStringUtilities.keyPathWithoutLastProperty(key()));
        }
        return object;
    }
    
    public void removeObject(EOEnterpriseObject objectToRemove) {
        relationshipObject().removeObjectFromBothSidesOfRelationshipWithKey(objectToRemove, relationshipName());
    }

    public void deleteObject(EOEnterpriseObject objectToRemove) {
        removeObject(objectToRemove);
        if(objectToRemove.editingContext() != null) {
            // AK: this is a bit tricky... when we delete and a delete can fail,
            // WOApp tries to validate the delete at the and
            // of the current RR loop and this triggers an exception
            // that can't be caught in the page itself
            // so what we do here is delete the object, try to validate and
            // is that fails, restore the EC. This isn't sufficient either -
            // as reverting deletes doesn't really work well -
            // but preferable to simply showing up and exception page.
            try {
                objectToRemove.editingContext().deleteObject(objectToRemove);
                objectToRemove.validateForDelete();
            } catch (NSValidation.ValidationException ex) {
                validationFailedWithException(ex, objectToRemove, key());
                object().editingContext().revert();
             }
        }
    }

    public EOEnterpriseObject addObject() {
        return ERXEOControlUtilities.createAndAddObjectToRelationship(object().editingContext(), relationshipObject(), relationshipName(), destinationEntityName(), null);
    }

    public WOComponent removeObjectsAction() {
        for(Enumeration objects = selectedObjects().objectEnumerator(); objects.hasMoreElements(); ) {
            EOEnterpriseObject eo = (EOEnterpriseObject)objects.nextElement();
            removeObject(eo);
        }
        return context().page();
    }

    public WOComponent deleteObjectsAction() {
        for(Enumeration objects = selectedObjects().objectEnumerator(); objects.hasMoreElements(); ) {
            EOEnterpriseObject eo = (EOEnterpriseObject)objects.nextElement();
            deleteObject(eo);
        }
        return context().page();
    }

    public WOComponent addObjectsAction() {
        for(int i = 0; i < objectsToAdd; i++) {
            addObject();
        }
        return context().page();
    }
    
    public boolean isSelected() {
        return selectedObjects().containsObject(currentObject);
    }
    
    public void setIsSelected(boolean selected) {
        if(selected && !selectedObjects().containsObject(currentObject)) {
            selectedObjects().addObject(currentObject);
        } else if(!selected && !selectedObjects().containsObject(currentObject)) {
            selectedObjects().removeObject(currentObject);
        }
    }
    
    public NSMutableArray selectedObjects() {
        if(selectedObjects == null) {
            selectedObjects = new NSMutableArray();
        }
        return selectedObjects;
    }
    
    public void setSelectedObjects(NSMutableArray value) {
        selectedObjects = value;
    }
}
