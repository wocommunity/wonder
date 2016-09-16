package er.rest.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXRestClassDescriptionFactory;

public class ERXRestSchema {
	public static NSDictionary<String, Object> schemaForEntityNamed(String entityName, ERXKeyFilter filter) {
		NSMutableDictionary<String, Object> schema = new NSMutableDictionary<>();
		schema.setObjectForKey(entityName, "name");
		
		NSDictionary<String, Object> properties = ERXRestSchema.schemaPropertiesForEntityNamed(entityName, filter, new HashSet<>());
		schema.setObjectForKey(properties, "properties");
		return schema;
	}
	
	@SuppressWarnings("unchecked")
	protected static NSDictionary<String, Object> schemaPropertiesForEntityNamed(String entityName, ERXKeyFilter filter, Set<String> entities) {
		if (entities.contains(entityName)) {
			return null;
		}
		entities.add(entityName);
		
		NSMutableDictionary<String, Object> properties = new NSMutableDictionary<>();

		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName);
		
		EOEntity entity = null;
		if (classDescription instanceof EOEntityClassDescription) {
			entity = ((EOEntityClassDescription) classDescription).entity();
		}

		for (String attributeName : classDescription.attributeKeys()) {
			ERXKey<Object> key = new ERXKey<>(attributeName);
			if (filter.matches(key, ERXKey.Type.Attribute)) {
				EOAttribute attribute = null;
				if (entity != null) {
					attribute = entity.attributeNamed(key.key());
				}
				NSMutableDictionary<String, Object> property = new NSMutableDictionary<>();

				boolean optional = attribute != null && attribute.allowsNull();
				property.setObjectForKey(optional, "optional");

				Class<?> attributeClass = classDescription.classForAttributeKey(key.key());
				if (String.class.isAssignableFrom(attributeClass)) {
					property.setObjectForKey("string", "type");
					if (attribute != null) {
						int width = attribute.width();
						if (width > 0) {
							if (!optional) {
								property.setObjectForKey(1, "minLength");
							}
							property.setObjectForKey(width, "maxLength");
						}
					}
				}
				else if (Date.class.isAssignableFrom(attributeClass)) {
					property.setObjectForKey("string", "type");
					property.setObjectForKey("date-time", "format");
				}
				else if (Integer.class.isAssignableFrom(attributeClass)) {
					property.setObjectForKey("integer", "type");
				}
				else if (BigDecimal.class.isAssignableFrom(attributeClass)) {
					property.setObjectForKey("number", "type");
				}
				else if (Number.class.isAssignableFrom(attributeClass)) {
					property.setObjectForKey("number", "type");
				}
				else if (Boolean.class.isAssignableFrom(attributeClass)) {
					property.setObjectForKey("boolean", "type");
				}
				else {
					NSLog.out.appendln("Unknown schema type '" + attributeClass.getName() + "' for entity '" + entityName + "'");
					property.setObjectForKey("any", "type");
				}

				properties.setObjectForKey(property, key.key());
			}
		}

		for (String toOneRelationshipName : classDescription.toOneRelationshipKeys()) {
			ERXKey<Object> key = new ERXKey<>(toOneRelationshipName);
			if (filter.matches(key, ERXKey.Type.ToOneRelationship)) {
				EOClassDescription destinationClassDescription = classDescription.classDescriptionForDestinationKey(key.key());
				ERXKeyFilter destinationFilter = filter._filterForKey(key);
				NSDictionary<String, Object> destinationSchema = ERXRestSchema.schemaPropertiesForEntityNamed(destinationClassDescription.entityName(), destinationFilter, entities);
				if (destinationSchema != null) {
					properties.setObjectForKey(destinationSchema, key.key());
				}
				else {
					// MS: Recursive reference to an entity .... wtf do we do.
				}
				/*
				 * HashMap property = new HashMap(); property.put("$ref", relationship.destinationEntity().name());
				 * properties.put(relationship.name(), property);
				 */
			}
		}

		for (String toManyRelationshipName : classDescription.toManyRelationshipKeys()) {
			ERXKey<Object> key = new ERXKey<>(toManyRelationshipName);
			if (filter.matches(key, ERXKey.Type.ToManyRelationship)) {
				EOClassDescription destinationClassDescription = classDescription.classDescriptionForDestinationKey(key.key());
				ERXKeyFilter destinationFilter = filter._filterForKey(key);
				NSDictionary<String, Object> destinationSchema = ERXRestSchema.schemaPropertiesForEntityNamed(destinationClassDescription.entityName(), destinationFilter, entities);
				if (destinationSchema != null) {
					properties.setObjectForKey(destinationSchema, key.key());
				}
				else {
					// MS: Recursive reference to an entity .... wtf do we do.
				}
			}
		}
		
		entities.remove(entityName);
		return properties;
	}

}
