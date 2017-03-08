package er.rest;

import java.util.List;
import java.util.Map;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/* 5.2 Blerg */
/**
 * NSDictionaryClassDescription is an EOClassDescription that is made on the
 * fly based on a Map (NSDictionary, etc) so that ERRest can render
 * a dictionary as a node where the keys of the node are the keys
 * of the dictionary.
 *  
 * @author mschrag
 */
public class NSDictionaryClassDescription extends EOClassDescription implements IERXNonEOClassDescription {
	private NSDictionary<String, Object> _map;

	public NSDictionaryClassDescription() {
		this(NSDictionary.emptyDictionary());
	}
	
	public NSDictionaryClassDescription(NSDictionary<String, Object> map) {
		_map = map;
	}

	@Override
	public String entityName() {
		String entityName = (String) _map.objectForKey("entityName");
		if (entityName == null) {
			entityName = "NSDictionary";
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
	public NSArray<String> attributeKeys() {
		NSMutableArray<String> attributes = new NSMutableArray<>();
		for (Map.Entry<String, Object> entry : _map.entrySet()) {
			Class type = entry.getValue().getClass();
			if (isAttribute(type)) {
				attributes.addObject(entry.getKey());
			}
		}
		return attributes;
	}

	@Override
	public NSArray<String> toOneRelationshipKeys() {
		NSMutableArray<String> relationships = new NSMutableArray<>();
		for (Map.Entry<String, Object> entry : _map.entrySet()) {
			Class type = entry.getValue().getClass();
			if (!isAttribute(type) && !isToMany(type)) {
				relationships.addObject(entry.getKey());
			}
		}
		return relationships;
	}

	@Override
	public NSArray<String> toManyRelationshipKeys() {
		NSMutableArray<String> relationships = new NSMutableArray<>();
		for (Map.Entry<String, Object> entry : _map.entrySet()) {
			Class type = entry.getValue().getClass();
			if (isToMany(type)) {
				relationships.addObject(entry.getKey());
			}
		}
		return relationships;
	}

	@Override
	public EOClassDescription classDescriptionForDestinationKey(String detailKey) {
		Object obj = _map.objectForKey(detailKey);
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

	@Override
	public Object createInstance() {
		return _map.mutableClone();
	}
}
