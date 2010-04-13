package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

import er.directtoweb.components.ERDCustomComponent;

/**
 * Component for table header cell. Contains the property label and a sort order controll
 * 
 * @binding displayGroup
 * 
 * @d2wKey propertyIsSortable
 * @d2wKey sortOrderComponentName
 * @d2wKey displayNameForProperty
 * @d2wKey sortKeyForList
 * @d2wKey sortCaseInsensitive
 * 
 * @author davidleber
 *
 */
public class ERMD2WTableHeader extends ERDCustomComponent {
	
	public static interface Keys {
		 public static final String displayGroup = "displayGroup";
	}
	
    public ERMD2WTableHeader(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }

    public WODisplayGroup displayGroup() {
    	return (WODisplayGroup)valueForBinding(Keys.displayGroup);
    }
}