package er.directtoweb.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.eof.ERXS;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Provides simple filtered grouping capability for display groups, like ebay or Solr.
 * To be put outside of the "is this list empty" conditional.
 * 
 * @binding displayGroup displayGroup to filter
 * @binding groupingKeys keys to group on
 * 
 * @author ak
 *
 */

/* sample CSS
<style>
.FacetFilterKeys {
    float: left;
}
.FacetFilterKey, .FacetFilterKey {
    padding-left: 1em;
}
.FacetFilterKey span {
    font-weight: bold;
}
.FacetFilterItem.Selected {
    font-weight: bold;
}
.ObjTable {
    width: 80%;
}
.ERMDEmptyList {
    margin-left: 20%;
}
</style>
*/

// TODO: doesn't account for changes in displayGroup or groupingKeys, no batching support, can lead to empty list, should maybe update count of selectable items (so you don't end up with an empty list), localization

public class ERDFacetFilter extends ERDCustomQueryComponent {
	
	public Object currentValue;
	public String currentKey;
	private NSArray<EOEnterpriseObject> _allObjects;
	
	private static String NONE = "(None)";
	
	private NSMutableDictionary<String, NSDictionary<Object, NSArray<EOEnterpriseObject>>> values = new NSMutableDictionary();
	private NSMutableDictionary<String, NSMutableArray<Object>> selectedValues = new NSMutableDictionary();

	public ERDFacetFilter(WOContext context) {
		super(context);
	}
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public NSArray<EOEnterpriseObject> allObjects() {
		if(_allObjects == null && allKeys() != null) {
			_allObjects = displayGroup().allObjects();
			values.removeAllObjects();
			selectedValues.removeAllObjects();
			for (String keyPath : allKeys()) {
				NSMutableDictionary<Object, NSArray<EOEnterpriseObject>> groupedObjects = ERXArrayUtilities.arrayGroupedByKeyPath(_allObjects, keyPath).mutableClone();
				NSArray<EOEnterpriseObject> nulls = groupedObjects.remove(ERXArrayUtilities.NULL_GROUPING_KEY);
				if(nulls != null) {
				    groupedObjects.setObjectForKey(nulls, NONE);
				}
				values.setObjectForKey(groupedObjects, keyPath);
				selectedValues.setObjectForKey(new NSMutableArray(), keyPath);
			}
		}
		return _allObjects;
	}
	
	public NSArray<String> allKeys() {
		return (NSArray<String>) valueForBinding("groupingKeys");
	}

	public NSArray valueList() {
		return ERXS.asc("toString").sorted(values.objectForKey(currentKey).allKeys());
	}
	
	public int currentValueCount() {
		return values.objectForKey(currentKey).objectForKey(currentValue).count();
	}

	public NSArray keyList() {
		allObjects();
		return values.allKeys();
	}

	public boolean isCurrentValueSelected() {
		NSMutableArray<Object> selected = selectedValues.objectForKey(currentKey);
		return selected.containsObject(currentValue);
	}
	
	public WOActionResults selectCurrentValue() {
		NSMutableArray<Object> selected = selectedValues.objectForKey(currentKey);
		if(selected.containsObject(currentValue)) {
			selected.removeObject(currentValue);
		} else {
			selected.addObject(currentValue);
		}
		updateSelectedValues();
		return null;
	}

	public void updateSelectedValues() {
		if (selectedValues.count() > 0) {
			NSMutableSet eos = new NSMutableSet(allObjects());
			for (String key : selectedValues.allKeys()) {
				NSMutableArray<Object> selection = selectedValues.objectForKey(key);
				if (selection.count() > 0) {
					NSMutableSet currentEos = new NSMutableSet();
					for (Object value : selection) {
						currentEos.addObjectsFromArray(values.objectForKey(key).objectForKey(value));
					}
					eos.intersectSet(currentEos);
				}
			}
			displayGroup().setObjectArray(eos.allObjects());
			displayGroup().setSortOrderings(displayGroup().sortOrderings());
		} else {
			displayGroup().setObjectArray(null);
		}
	}

	public boolean hasSelectedValues() {
	    if(currentKey != null) {
	        return selectedValues.objectForKey(currentKey).count() > 0;
	    }
	    for (NSArray values : selectedValues.values()) {
            if(values.count() > 0) {
                return true;
            }
        }
	    return false;
	}

	public WOActionResults clearSelectedValues() {
	    if(currentKey != null) {
	        selectedValues.objectForKey(currentKey).removeAllObjects();
	    } else {
	        for (NSMutableArray values : selectedValues.allValues()) {
                values.removeAllObjects();
            }
	    }
		updateSelectedValues();
		return null;
	}
	
	public String itemClassName() {
        return isCurrentValueSelected() ? "FacetFilterItem Selected" : "FacetFilterItem";
    }

    public String currentDisplayName() {
        return NONE == currentKey ? NONE : ERXStringUtilities.displayNameForKey(currentKey);
    }

}