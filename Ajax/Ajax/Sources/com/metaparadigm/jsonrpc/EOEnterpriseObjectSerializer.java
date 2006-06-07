package com.metaparadigm.jsonrpc;

import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

/**
 * La classe EOEnterpriseObjectSerializer s'occupe de la conversion des objets paramêtres de type 
 * <code>EOEnterpriseObject</code> entre le monde Javascript et le monde Java.
 *
 * @author	john
 * @author	Jean-François Veillette <jfveillette@os.ca>
 * @version	$Revision$, $Date$
 */
public class EOEnterpriseObjectSerializer extends AbstractSerializer {

	protected static NSMutableDictionary exposedKeyAttributeDictionary = null;

	private static Class[] _serializableClasses = new Class[] { EOEnterpriseObject.class};

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() { return _serializableClasses; }
	public Class[] getJSONClasses() { return _JSONClasses; }

	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) ||
				((jsonClazz == null || jsonClazz == JSONArray.class) &&
						EOEnterpriseObject.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object jso) {
		return null;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object jso) {
		return null;
	}

	public Object marshall(SerializerState state, Object o) throws MarshallException {
		EOEnterpriseObject eo = (EOEnterpriseObject)o;
		JSONObject obj = new JSONObject();
		JSONObject eoData = new JSONObject();
		obj.put("javaClass", o.getClass().getName());
		obj.put("eo", eoData);
		String keyPath = null;
		try {
			addAttributes(state, eo, eoData);
		} catch (MarshallException e) {
		    throw new MarshallException("element " + keyPath + " " + e.getMessage());
		}
		return obj;
	}

	/**
     * This copies the attributes from the source EOEnterpriseObject to the destination.  Only attributes which are class properties are copied.
     * However if an attribute is a class property and also used in a relationship it is assumed to be an exposed 
     * primary or forign key and not copied.  Such attributes are set to null.  
     * See exposedKeyAttributeNames for details on how this is determined.  It can be used when creating custom 
     * implementations of the duplicate() method in EOCopyable.
     *
     * @param source the EOEnterpriseObject to copy attribute values from
     * @param destination the EOEnterpriseObject to copy attribute values to
     */
   public void addAttributes(SerializerState state, EOEnterpriseObject source, JSONObject destination) throws MarshallException {
      NSArray keys = exposedKeyAttributeNames(source);
       for  (Enumeration e = source.attributeKeys().objectEnumerator(); e.hasMoreElements(); ) {
           String attributeName = (String)e.nextElement();
           if (!keys.containsObject(attributeName)) {
               destination.put(attributeName, ser.marshall(state, source.storedValueForKey(attributeName)));
           }
       }
   }

   /**
     * Returns an array of attribute names from the EOEntity of source that are used in the primary key, or in forming relationships.  
     * These can be presumed to be exposed primary or foreign keys and handled accordingly when copying an object.
     *
     * @param source the EOEnterpriseObject to copy attribute values from
     * @return an array of attribute names from the EOEntity of source that are used in forming relationships.
     **/
   public static NSArray exposedKeyAttributeNames(EOEnterpriseObject source) {
       // These are cached on EOEntity name as an optimization.
       if (exposedKeyAttributeDictionary == null) {
           exposedKeyAttributeDictionary = new NSMutableDictionary();
       }

       EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
       NSArray exposedKeyAttributeNames = (NSArray) exposedKeyAttributeDictionary.objectForKey(entity.name());

       if (exposedKeyAttributeNames == null) {
           NSMutableSet keyNames = new NSMutableSet();
           keyNames.addObjectsFromArray(entity.primaryKeyAttributeNames());

           Enumeration relationshipEnumerator = entity.relationships().objectEnumerator();
           while (relationshipEnumerator.hasMoreElements()) {
               EORelationship relationship = (EORelationship)relationshipEnumerator.nextElement();
               keyNames.addObjectsFromArray((NSArray)relationship.sourceAttributes().valueForKey("name"));
           }

           NSSet publicAttributeNames = new NSSet(source.attributeKeys());
           exposedKeyAttributeNames = publicAttributeNames.setByIntersectingSet(keyNames).allObjects();
           exposedKeyAttributeDictionary.setObjectForKey(exposedKeyAttributeNames, entity.name());
       }

       return exposedKeyAttributeNames;
   }
}
