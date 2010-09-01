package er.rest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOGenericRecord._DictionaryBinding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/* 5.2 Blerg */
/**
 * NSDictionaryClassDescription is an EOClassDescription that is made on the
 * fly based on a Map (NSDictionary, etc) so that ERRest can render
 * a dictionary as a node where the keys of the node are the keys
 * of the dictionary.
 *  
 * @author mschrag
 */
public class NSDictionaryClassDescription extends EOClassDescription {
	private NSDictionary _map;

	public NSDictionaryClassDescription(NSDictionary map) {
		_map = map;
	}

	@Override
	public String entityName() {
		String entityName = (String) _map.objectForKey("entityName");
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
		for (Enumeration keyEnum = _map.keyEnumerator(); keyEnum.hasMoreElements(); ) {
			Object key = keyEnum.nextElement();
			Object value = _map.objectForKey(key);
			Class type = value.getClass();
			if (isAttribute(type)) {
				attributes.addObject(key);
			}
		}
		return attributes;
	}

	@Override
	public NSArray toOneRelationshipKeys() {
		NSMutableArray/*<String>*/ relationships = new NSMutableArray/*<String>*/();
		for (Enumeration keyEnum = _map.keyEnumerator(); keyEnum.hasMoreElements(); ) {
			Object key = keyEnum.nextElement();
			Object value = _map.objectForKey(key);
			Class type = value.getClass();
			if (!isAttribute(type) && !isToMany(type)) {
				relationships.addObject(key);
			}
		}
		return relationships;
	}

	@Override
	public NSArray toManyRelationshipKeys() {
		NSMutableArray/*<String>*/ relationships = new NSMutableArray/*<String>*/();
		for (Enumeration keyEnum = _map.keyEnumerator(); keyEnum.hasMoreElements(); ) {
			Object key = keyEnum.nextElement();
			Object value = _map.objectForKey(key);
			Class type = value.getClass();
			if (isToMany(type)) {
				relationships.addObject(key);
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
		return null;
	}

	public Object createInstance() {
		return _map.clone();
	}
}