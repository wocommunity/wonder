package er.modern.directtoweb.components.repetitions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Special list page repetition for hierarchical object structures that extends
 * ERMDListPageRepetition. Allows recursive access to a relationship specified
 * via the "nestedRelationship" key.
 * 
 * As an example, you could use this for a hierarchy of locations (think
 * "continent > country > city > quarter"), with "containedLocations" as the
 * nestedRelationship key. This will allow a "drill-down" access to locations.
 * 
 * @binding displayGroup
 * @binding d2wContext
 * 
 * @d2wKey baseClassForObjectRow
 * @d2wKey componentName
 * @d2wKey displayNameForProperty
 * @d2wKey extraListComponentName
 * @d2wKey justification
 * @d2wKey nestedRelationship
 * @d2wKey object
 * @d2wKey propertyIsSortable 
 * @d2wKey showNestedRelationshipEditor
 * @d2wKey sortCaseInsensitive
 * @d2wKey sortKeyForList

 * @author fpeters
 */
public class ERMDNestingListPageRepetition extends ERMDListPageRepetition {

    private static final long serialVersionUID = 1L;

    public interface Keys {

        public static String nestedRelationship = "nestedRelationship";

        public static String showNestedRelationshipEditor = "showNestedRelationshipEditor";

    }

    public ERMDNestingListPageRepetition(WOContext context) {
        super(context);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NSDictionary childRelationshipBindings() {
        NSArray masterObjectAndRelationshipKey = new NSArray(d2wContext()
                .valueForKeyPath("object"), d2wContext().valueForKey(
                Keys.nestedRelationship));
        return new NSMutableDictionary("masterObjectAndRelationshipKey",
                masterObjectAndRelationshipKey);
    }

    public D2WContext childContext() {
        D2WContext c = ERD2WContext.newContext(d2wContext());
        c.setPropertyKey((String) d2wContext().valueForKey(Keys.nestedRelationship));
        c.takeValueForKey("ERDEditRelationship", "componentName");
        return c;
    }

    public boolean hasLeftActions() {
        boolean hasLeftActions = leftActions() != null && leftActions().count() > 0;
        return hasLeftActions;
    }

    public WOActionResults toggleNestedRelationshipEditor() {
        // use globalID hash as unique key for this object
        Boolean showNestedRelationshipEditor = (Boolean) d2wContext().valueForKeyPath(
                Keys.showNestedRelationshipEditor + uniqueObjectID());
        if (showNestedRelationshipEditor == null) {
            // default to hide
            showNestedRelationshipEditor = Boolean.FALSE;
        }
        d2wContext().takeValueForKeyPath(!showNestedRelationshipEditor,
                Keys.showNestedRelationshipEditor + uniqueObjectID());
        return null;
    }

    public Boolean hasNestedRelationship() {
        return d2wContext().valueForKey(Keys.nestedRelationship) != null;
    }

	public boolean showNestedRelationshipEditor() {
        // use globalID hash as unique key for this object
		return ERXValueUtilities.booleanValue(d2wContext().valueForKeyPath(Keys.
				showNestedRelationshipEditor + uniqueObjectID()));
    }

    /**
     * @return the column count for the nested td's colspan
     */
    public int columnCount() {
        int columnCount = displayPropertyKeyCount();
        if (d2wContext().valueForKey(Keys.nestedRelationship) != null) {
        	// additional column for toggle action
        columnCount++;
        }
        if (hasLeftActions()) {
            columnCount++;
        }
        if (hasRightActions()) {
            columnCount++;
        }
        return columnCount;
    }

    /**
     * Computes a unique ID, based on the nesting level. Depending on the object
     * graph, a unique EOGlobalID may be insufficient as an EO might appear more
     * than once.
     * 
     * @return unique ID for the nested update container
     */
    public String idForNestedUpdateContainer() {
        String idForNestedUpdateContainer = (String) d2wContext().valueForKey(
                "idForRepetitionContainer");
        WOComponent ancestor = parent();
        int recursionCounter = 0;
        while (ancestor != null && ancestor.parent() != null) {
            ancestor = ancestor.parent();
            if (name().equals(ancestor.name())) {
                recursionCounter++;
            }
        }
        idForNestedUpdateContainer = idForNestedUpdateContainer + "_l" + recursionCounter
                + "_" + uniqueObjectID();
        return idForNestedUpdateContainer;
    }

    /**
     * @return a unique ID, based on the D2WContext's object value
     */
    private String uniqueObjectID() {
        EOEnterpriseObject object = (EOEnterpriseObject) d2wContext().valueForKey(
                "object");
        String uniqueID = String.valueOf(object.hashCode());
        return uniqueID;
    }

    /*
     * Overridden to remove LastObjRow class, as the nestedRowClass will always
     * be last.
     */
    public String objectRowClass() {
        String objectRowClass = super.objectRowClass();
        objectRowClass = objectRowClass.replace("LastObjRow", "");
        return objectRowClass;
    }

    /**
     * Gets class list from object row class, but removes any FirstObjRow
     * occurrence, as the nested row can never be the first row.
     * 
     * @return nested row class
     */
    public String nestedRowClass() {
        String nestedRowClass = super.objectRowClass();
        nestedRowClass = nestedRowClass.replace("FirstObjRow", "");
        nestedRowClass = nestedRowClass.concat(" NestedObjRow");
        return nestedRowClass;
    }

    public boolean hasChildren() {
        EOEnterpriseObject object = (EOEnterpriseObject) d2wContext().valueForKey(
                "object");
		boolean hasChildren = ERXValueUtilities.booleanValue(object.valueForKeyPath(
				d2wContext().valueForKey(Keys.nestedRelationship) + ".@count"));
    	return hasChildren; 
    }
    
    /*
	 * The remainder handles the icon toggling – the same could be achieved by simply
	 * putting the icons in a dependent update container, but that would
	 * potentially introduce a lot of update containers.
	 */

    public String idForDoOpenIcon() {
    	return idForNestedUpdateContainer() + "_open";
    }
    
    public String idForDoCloseIcon() {
    	return idForNestedUpdateContainer() + "_close";
    }

	public String toggleIconsScript() {
		String toggleIconsScript = "function(){$('" + idForDoOpenIcon() + 
				"', '" + idForDoCloseIcon() + "').invoke('toggle');}";
		return toggleIconsScript;
	}
	
	public String initialStyleForDoOpenIcon() {
		return showNestedRelationshipEditor() ? "display:none;" : "";
	}
	
	public String initialStyleForDoCloseIcon() {
		return showNestedRelationshipEditor() ? "" : "display:none;";
	}

}