package er.ajax.json;

import java.util.Enumeration;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol._EOIntegralKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

import er.extensions.ERXEC;
import er.extensions.ERXProperties;

/**
 * La classe EOEnterpriseObjectSerializer s'occupe de la conversion des objets paramêtres de type
 * <code>EOEnterpriseObject</code> entre le monde Javascript et le monde Java.
 * 
 * @author john
 * @author Jean-François Veillette <jfveillette@os.ca>
 * @version $Revision$, $Date$
 */
public class EOEnterpriseObjectSerializer extends AbstractSerializer {

	protected static NSMutableDictionary publicAttributes = new NSMutableDictionary();

	private static Class[] _serializableClasses = new Class[] { EOEnterpriseObject.class };

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	private EOEditingContextFactory _editingContextFactory;

	public EOEnterpriseObjectSerializer() {
		String editingContextFactory = ERXProperties.stringForKey("er.ajax.json.EOEditingContextFactory");
		if (editingContextFactory == null) {
			_editingContextFactory = new ERXECEditingContextFactory();
		}
		else {
			try {
				_editingContextFactory = (EOEditingContextFactory) Class.forName(editingContextFactory).newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to initialize EOEnterpriseObjectSerializer.", e);
			}
		}
	}

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && EOEnterpriseObject.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object jso) {
		return null;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			JSONObject eoDict = jso.getJSONObject("eo");
			if (eoDict == null) {
				throw new UnmarshallException("eo missing");
			}
			String entityName = eoDict.getString("_entityName");
			JSONArray gid = eoDict.getJSONArray("_gid");
			int gidLength = gid.length();
			Object[] keyValues = new Object[gidLength];
			for (int i = 0; i < gidLength; i++) {
				keyValues[i] = gid.get(i);
			}
			EOEnterpriseObject eo;
			EOKeyGlobalID keyGlobalID = EOKeyGlobalID.globalIDWithEntityName(entityName, keyValues);
			EOEditingContext editingContext = _editingContextFactory.newEditingContext();
			editingContext.lock();
			try {
				eo = editingContext.faultForGlobalID(keyGlobalID, editingContext);
			}
			finally {
				editingContext.unlock();
			}
			return eo;
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall EO.", e);
		}
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			EOEnterpriseObject eo = (EOEnterpriseObject) o;
			JSONObject obj = new JSONObject();
			JSONObject eoData = new JSONObject();
			obj.put("javaClass", o.getClass().getName());
			obj.put("eo", eoData);
			String keyPath = null;
			try {
				addAttributes(state, eo, eoData);
			}
			catch (MarshallException e) {
				throw new MarshallException("element " + keyPath + " " + e.getMessage());
			}
			return obj;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall EO.", e);
		}
	}

	/**
	 * This copies the attributes from the source EOEnterpriseObject to the destination. Only attributes which are class
	 * properties are copied. However if an attribute is a class property and also used in a relationship it is assumed
	 * to be an exposed primary or foreign key and not copied. Such attributes are set to null. See
	 * exposedKeyAttributeNames for details on how this is determined. It can be used when creating custom
	 * implementations of the duplicate() method in EOCopyable.
	 * 
	 * @param source
	 *            the EOEnterpriseObject to copy attribute values from
	 * @param destination
	 *            the EOEnterpriseObject to copy attribute values to
	 */
	public void addAttributes(SerializerState state, EOEnterpriseObject source, JSONObject destination) throws MarshallException {
		try {
			EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);

			NSArray publicAttributeNames = EOEnterpriseObjectSerializer.publicAttributeNames(source);
			for (Enumeration e = source.attributeKeys().objectEnumerator(); e.hasMoreElements();) {
				String attributeName = (String) e.nextElement();
				if (publicAttributeNames.containsObject(attributeName)) {
					destination.put(attributeName, ser.marshall(state, source, source.storedValueForKey(attributeName), attributeName));
				}
			}

			destination.put("_entityName", entity.name());
			EOGlobalID gid = source.editingContext().globalIDForObject(source);
			if (gid instanceof EOKeyGlobalID) {
				EOKeyGlobalID key = (EOKeyGlobalID) gid;
				Object[] keyValues = key._keyValuesNoCopy();
				destination.put("_gid", ser.marshall(state, source, keyValues, "_gid"));
			}
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall EO.", e);
		}
	}

	/**
	 * Returns an array of attribute names from the EOEntity of source that are used in the primary key, or in forming
	 * relationships. These can be presumed to be exposed primary or foreign keys and handled accordingly when copying
	 * an object.
	 * 
	 * @param source
	 *            the EOEnterpriseObject to copy attribute values from
	 * @return an array of attribute names from the EOEntity of source that are used in forming relationships.
	 */
	public static NSArray publicAttributeNames(EOEnterpriseObject source) {
		// These are cached on EOEntity name as an optimization.

		EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
		NSArray publicAttributeNames = (NSArray) EOEnterpriseObjectSerializer.publicAttributes.objectForKey(entity.name());

		if (publicAttributeNames == null) {
			NSMutableSet publicAttributeSet = new NSMutableSet();
			NSArray publicAttributes = ERXProperties.arrayForKey("er.ajax.json." + entity.name() + ".attributes");
			if (publicAttributes != null) {
				publicAttributeSet.addObjectsFromArray(publicAttributes);
			}
			else {
				publicAttributes = source.attributeKeys();
				publicAttributeSet.addObjectsFromArray(publicAttributes);
				NSArray classProperties = entity.classPropertyNames();
				publicAttributeNames = publicAttributeSet.setByIntersectingSet(new NSSet(classProperties)).allObjects();
			}
			EOEnterpriseObjectSerializer.publicAttributes.setObjectForKey(publicAttributeNames, entity.name());
		}

		return publicAttributeNames;
	}

	public static interface EOEditingContextFactory {
		public EOEditingContext newEditingContext();
	}

	public static class ERXECEditingContextFactory implements EOEnterpriseObjectSerializer.EOEditingContextFactory {
		public EOEditingContext newEditingContext() {
			return ERXEC.newEditingContext();
		}
	}

	public static class SadEditingContextFactory implements EOEnterpriseObjectSerializer.EOEditingContextFactory {
		public EOEditingContext newEditingContext() {
			return new EOEditingContext();
		}
	}
}
