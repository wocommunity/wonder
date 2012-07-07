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

public class ERDFacetFilter extends ERDCustomQueryComponent {
	
	public Object currentValue;
	public String currentKey;
	private NSArray<EOEnterpriseObject> _allObjects;
	
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
			_allObjects = (NSArray<EOEnterpriseObject>) displayGroup().allObjects();
			values.removeAllObjects();
			selectedValues.removeAllObjects();
			for (String keyPath : allKeys()) {
				NSDictionary<Object, NSArray<EOEnterpriseObject>> groupedObjects = ERXArrayUtilities.arrayGroupedByKeyPath(_allObjects, keyPath);
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
		return selectedValues.objectForKey(currentKey).count() > 0;
	}

	public WOActionResults clearSelectedValues() {
		selectedValues.objectForKey(currentKey).removeAllObjects();
		updateSelectedValues();
		return null;
	}
}