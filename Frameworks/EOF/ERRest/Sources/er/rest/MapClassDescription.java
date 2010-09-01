package er.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * MapClassDescription is an EOClassDescription that is made on the
 * fly based on a Map (NSDictionary, etc) so that ERRest can render
 * a dictionary as a node where the keys of the node are the keys
 * of the dictionary.
 *  
 * @author mschrag
 */
public class MapClassDescription extends EOClassDescription {
	private Map<String, ?> _map;

	public MapClassDescription(Map<String, ?> map) {
		_map = map;
	}

	@Override
	public String entityName() {
		String entityName = (String) _map.get("entityName");
		if (entityName == null) {
			entityName = _map.getClass().getSimpleName();
		}
		return entityName;
	}

	protected boolean isAttribute(Class type) {
		return ERXRestUtils.isPrimitive(type);
	}

	protected boolean isToMany(Class type) {
		return List.class.isAssignableFrom(type) || NSArray.class.isAssignableFrom(type) /* 5.2 Blerg */;
	}

	@Override
	public NSArray attributeKeys() {
		NSMutableArray/*<String>*/ attributes = new NSMutableArray/*<String>*/();
		for (Map.Entry<String, ?> entry : _map.entrySet()) {
			Class type = entry.getValue().getClass();
			if (isAttribute(type)) {
				attributes.addObject(entry.getKey());
			}
		}
		return attributes;
	}

	@Override
	public NSArray toOneRelationshipKeys() {
		NSMutableArray/*<String>*/ relationships = new NSMutableArray/*<String>*/();
		for (Map.Entry<String, ?> entry : _map.entrySet()) {
			Class type = entry.getValue().getClass();
			if (!isAttribute(type) && !isToMany(type)) {
				relationships.addObject(entry.getKey());
			}
		}
		return relationships;
	}

	@Override
	public NSArray toManyRelationshipKeys() {
		NSMutableArray/*<String>*/ relationships = new NSMutableArray/*<String>*/();
		for (Map.Entry<String, ?> entry : _map.entrySet()) {
			Class type = entry.getValue().getClass();
			if (isToMany(type)) {
				relationships.addObject(entry.getKey());
			}
		}
		return relationships;
	}

	@Override
	public EOClassDescription classDescriptionForDestinationKey(String detailKey) {
		Object obj = _map.get(detailKey);
		if (obj != null) {
			Class type = obj.getClass();
			if (isToMany(type)) {
				return ERXRestClassDescriptionFactory.classDescriptionForClass(Object.class, true);
			}
			else {
				return ERXRestClassDescriptionFactory.classDescriptionForClass(type, false);
			}
		}
		return null;
	}

	public Object createInstance() {
		return new HashMap<String, Object>(_map); // not much else we can do here ... fucking clone method.
	}
}