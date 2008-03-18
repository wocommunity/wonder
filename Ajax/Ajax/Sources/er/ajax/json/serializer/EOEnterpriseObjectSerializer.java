package er.ajax.json.serializer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXProperties;
import er.extensions.ERXRandomGUID;
import er.extensions.ERXSession;
import er.extensions.ERXStringUtilities;

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
			String gidString = jso.getString("gid");
			if (gidString == null) {
				throw new UnmarshallException("gid missing");
			}
			String parts[] = gidString.split("/");
			String ecid = parts[0];
			String entityName = parts[1];
			EOGlobalID keyGlobalID;
	
			EOEditingContext ec = null;
			if(ecid != null) {
				ec = editingContextForKey(ecid);
			}
			if(ec == null) {
				ec = _editingContextFactory.newEditingContext();
				registerEditingContext(ec);
			}
			ec.lock();
			try {
				String pk = parts.length > 2 ? parts[2] : null;
				EOEnterpriseObject eo;
				if(pk != null && pk.length() > 0) {
					pk = ERXStringUtilities.urlDecode(pk);
					keyGlobalID = ERXEOControlUtilities.globalIDForString(ec, entityName, pk);
					eo = ec.faultForGlobalID(keyGlobalID, ec);
				} else {
					eo = ERXEOControlUtilities.createAndInsertObject(ec, entityName);
				}
				for (Iterator iterator = eoDict.keys(); iterator.hasNext();) {
					String key = (String) iterator.next();
					Object value = eoDict.get(key);
					eo.takeValueForKey(value, key);
				}
				return eo;
			}
			finally {
				ec.unlock();
			}
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall EO.", e);
		}
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			EOEnterpriseObject eo = (EOEnterpriseObject) o;
			JSONObject obj = new JSONObject();
			obj.put("javaClass", o.getClass().getName());

			EOEditingContext ec = eo.editingContext();
			String ecid = registerEditingContext(ec);
			String pk = ERXStringUtilities.urlEncode(ERXEOControlUtilities.primaryKeyStringForObject(eo));
			obj.put("gid", ecid + "/" + eo.entityName() +  "/" + pk);
			String keyPath = null;

			JSONObject eoData = new JSONObject();
			obj.put("eo", eoData);
			state.push(o, eoData, "eo");
			try {
				addAttributes(state, eo, eoData);
			}
			catch (MarshallException e) {
				throw new MarshallException("element " + keyPath + " " + e.getMessage());
			}
			finally {
				state.pop();
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

			NSArray publicAttributeNames = EOEnterpriseObjectSerializer.publicAttributeNames(source);
			for (Enumeration e = source.attributeKeys().objectEnumerator(); e.hasMoreElements();) {
				String attributeName = (String) e.nextElement();
				if (publicAttributeNames.containsObject(attributeName)) {
					Object value = ser.marshall(state, source, source.storedValueForKey(attributeName), attributeName);
					if (JSONSerializer.CIRC_REF_OR_DUPLICATE == value)
						destination.put(attributeName, JSONObject.NULL);
					else
						destination.put(attributeName, value);
				}
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
		//AK: should use clientProperties from EM
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

	private static Map<EOEditingContext, String> _contexts = new WeakHashMap<EOEditingContext, String>();

	public static Map<EOEditingContext, String> contexts() {
		Map<EOEditingContext, String> contexts;
		ERXSession session = ERXSession.session();
		if (session == null) {
			contexts = _contexts;
		}
		else {
			contexts = (Map<EOEditingContext, String>) session.objectForKey("_jsonContexts");
			if (contexts == null) {
				contexts = new HashMap<EOEditingContext, String>();
				session.setObjectForKey(contexts, "_jsonContexts");
			}
		}
		return contexts;
	}
	
	public static String registerEditingContext(EOEditingContext ec) {
		Map<EOEditingContext, String> contexts = contexts();
		synchronized (contexts) {
			String id = contexts.get(ec);
			if (id != null) {
				return id;
			}
			id = ERXRandomGUID.newGid();
			contexts.put(ec, id);
			return id;
		}
	}

	public static EOEditingContext editingContextForKey(String key) {
		Map<EOEditingContext, String> contexts = contexts();
		synchronized (contexts) {
			for (Iterator iterator = contexts.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<EOEditingContext, String> entry = (Map.Entry<EOEditingContext, String>) iterator.next();
				if(entry.getValue().equals(key)) {
					return entry.getKey();
				}
			}
			return null;
		}
	}
	
}
