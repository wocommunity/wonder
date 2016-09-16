package er.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * MapClassDescription is an EOClassDescription that is made on the
 * fly based on a Map (NSDictionary, etc) so that ERRest can render
 * a dictionary as a node where the keys of the node are the keys
 * of the dictionary.
 *  
 * @author mschrag
 */
public class MapClassDescription extends EOClassDescription implements IERXNonEOClassDescription {
	private Map<String, ?> _map;

	public MapClassDescription() {
		this(new HashMap<>());
	}
	
	public MapClassDescription(Map<String, ?> map) {
		_map = map;
	}

	@Override
	public String entityName() {
		String entityName = (String) _map.get("entityName");
		if (entityName == null) {
		    if (_map instanceof NSDictionary) {
		        entityName = "NSDictionary";
		    }
		    else {
		        entityName = "HashMap";
		    }
		}
		return entityName;
	}

	protected boolean isAttribute(Class type) {
		return ERXRestUtils.isPrimitive(type);
	}

	protected boolean isToMany(Class type) {
		return List.class.isAssignableFrom(type);
	}

	@Override
	public NSArray attributeKeys() {
		NSMutableArray<String> attributes = new NSMutableArray<>();
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
		NSMutableArray<String> relationships = new NSMutableArray<>();
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
		NSMutableArray<String> relationships = new NSMutableArray<>();
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
		else {
			return ERXRestClassDescriptionFactory.classDescriptionForClass(Object.class, true);
		}
	}

	public Object createInstance() {
		return new HashMap<String, Object>(_map); // not much else we can do here ... fucking clone method.
	}
}