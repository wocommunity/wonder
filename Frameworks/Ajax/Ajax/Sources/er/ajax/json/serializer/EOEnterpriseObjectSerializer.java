package er.ajax.json.serializer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * La classe EOEnterpriseObjectSerializer s'occupe de la conversion des objets paramêtres de type
 * <code>EOEnterpriseObject</code> entre le monde Javascript et le monde Java.
 * 
 * @property er.ajax.json.EOEditingContextFactory
 * @property er.ajax.json.[entityName].canInsert
 * @property er.ajax.json.[currentEntity.name].attributes
 * @property er.ajax.json.[currentEntity.name].writableAttributes
 * @property er.ajax.json.[currentEntity.name]relationships
 *
 * @author john
 * @author <a href="mailto:jfveillette@os.ca">Jean-François Veillette</a>
 */
public class EOEnterpriseObjectSerializer extends AbstractSerializer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected static final NSMutableDictionary<String, NSArray<String>> readableAttributeNames = new NSMutableDictionary<String, NSArray<String>>();
	protected static final NSMutableDictionary<String, NSArray<String>> writableAttributeNames = new NSMutableDictionary<String, NSArray<String>>();
	protected static final NSMutableDictionary<String, NSArray<String>> includedRelationshipNames = new NSMutableDictionary<String, NSArray<String>>();

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

	protected boolean _canSerialize(Class clazz, Class jsonClazz) {
		return super.canSerialize(clazz, jsonClazz);
	}

	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && EOEnterpriseObject.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object jso) {
		return null;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			if (o == null) {
				throw new UnmarshallException("eo missing");
			}
			JSONObject eoDict = (JSONObject) o;
			if(eoDict.has("eo")) {
				eoDict.getJSONObject("eo");
			}
			String gidString = eoDict.getString("gid");
			if (gidString == null) {
				throw new UnmarshallException("gid missing");
			}
			String parts[] = gidString.split("/");
			String ecid = parts[0];
			String entityName = parts[1];
	
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
				String type = null;
				String pk = null;
				if (parts.length > 2) {
					type = parts[2];
					pk = parts[3];
				}
				EOEnterpriseObject eo;
				EOGlobalID gid;
				if(pk != null && pk.length() > 0) {
					if ("T".equals(type)) {
						byte[] bytes = ERXStringUtilities.hexStringToByteArray(pk);
						gid = EOTemporaryGlobalID._gidForRawBytes(bytes);
						eo = ec.objectForGlobalID(gid);
					}
					else {
						pk = ERXStringUtilities.urlDecode(pk);
						gid = ERXEOControlUtilities.globalIDForString(ec, entityName, pk);
						eo = ec.faultForGlobalID(gid, ec);
					}
				}
				else if (_canInsert(entityName)) {
					eo = ERXEOControlUtilities.createAndInsertObject(ec, entityName);
				}
				else {
					eo = null;
				}
				if (eo != null) {
					NSArray<String> attributeNames = _writableAttributeNames(eo);
					NSArray<String> relationshipNames = _includedRelationshipNames(eo);
					for (Iterator iterator = eoDict.keys(); iterator.hasNext();) {
						String key = (String) iterator.next();
						if(!("javaClass".equals(key) || "gid".equals(key))) {
							Object value = eoDict.get(key);
							Object obj = ser.unmarshall(state, null, value);
							if (attributeNames.containsObject(key)) {
								if (obj == null && !relationshipNames.containsObject(key) && (eo.toOneRelationshipKeys().containsObject(key) || eo.toManyRelationshipKeys().containsObject(key))) { 
									// ignore nulls for non-included relationships 
								}
								else {
									eo.takeValueForKey(obj, key);
								}
							}
						}
					}
				}
				state.setSerialized(o, eo);
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
			String type;
			String pkStr;
			EOGlobalID gid = ec.globalIDForObject(eo);
			if (gid instanceof EOTemporaryGlobalID) {
				type = "T";
				byte[] bytes = ((EOTemporaryGlobalID)gid)._rawBytes();
				pkStr = ERXStringUtilities.byteArrayToHexString(bytes);
			}
			else {
				type = "K";
				pkStr = ERXEOControlUtilities.primaryKeyStringForObject(eo);
				pkStr = ERXStringUtilities.urlEncode(pkStr);
			}
			obj.put("gid", ecid + "/" + eo.entityName() +  "/" + type + "/" + pkStr);

			addAttributes(state, eo, obj);
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
	 * @param state
	 *            object that holds the sate of the serialization
	 * @param source
	 *            the EOEnterpriseObject to copy attribute values from
	 * @param destination
	 *            the EOEnterpriseObject to copy attribute values to
	 * @throws MarshallException if conversion failed
	 */
	public void addAttributes(SerializerState state, EOEnterpriseObject source, JSONObject destination) throws MarshallException {
		boolean useEO = false;
		try {
			JSONObject eoData = destination;
			if(useEO) {
				destination = new JSONObject();
				destination.put("eo", eoData);
				state.push(source, eoData, "eo");
			}
			EOClassDescription cd = source.classDescription();
			NSArray<String> attributeNames = _readableAttributeNames(source);
			NSArray<String> relationshipNames = _includedRelationshipNames(source);
			
			for (Enumeration e = attributeNames.objectEnumerator(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				Object jsonValue;
				if(cd.toManyRelationshipKeys().containsObject(key)) {
					if (relationshipNames.containsObject(key)) {
						Object value = source.valueForKey(key);
						jsonValue = ser.marshall(state, source, value, key);
					}
					else {
//						JSONObject rel = new JSONObject();
//						rel.put("javaClass", "com.webobjects.eocontrol.EOArrayFault");
//						rel.put("sourceGlobalID", destination.get("gid"));
//						rel.put("relationshipName", key);
//						jsonValue = rel;
						jsonValue = null;
					}
				} else if (cd.toOneRelationshipKeys().containsObject(key)) {
					if (relationshipNames.containsObject(key)) {
						Object value = source.valueForKey(key);
						jsonValue = ser.marshall(state, source, value, key);
					}
					else {
//						JSONObject rel = new JSONObject();
//						rel.put("javaClass", "com.webobjects.eocontrol.EOFault");
//						rel.put("sourceGlobalID", destination.get("gid"));
//						rel.put("relationshipName", key);
//						jsonValue = rel;
						jsonValue = null;
					}
				} else {
					Object value = source.valueForKey(key);
					jsonValue = ser.marshall(state, source, value, key);
				}
				if (JSONSerializer.CIRC_REF_OR_DUPLICATE == jsonValue) {
					destination.put(key, JSONObject.NULL);
				}
				else {
					destination.put(key, jsonValue);
				}
			}
			_addCustomAttributes(state, source, destination);
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall EO.", e);
		}
		finally {
			if(useEO) {
				state.pop();
			}
		}
	}

	protected void _addCustomAttributes(SerializerState state, EOEnterpriseObject source, JSONObject destination) throws MarshallException {
		// DO NOTHING
	}

	/**
	 * Override to return whether or not a new entity can be inserted.
	 * @param entityName name of an entity
	 * 
	 * @return <code>true</code> if entity is insertable
	 */
	protected boolean _canInsert(String entityName) {
		return ERXProperties.booleanForKeyWithDefault("er.ajax.json." + entityName + ".canInsert", false);
	}

	/**
	 * Override to return the appropriate attribute names.
	 * @param eo enterprise object
	 * 
	 * @return array of attribute names
	 */
	protected NSArray<String> _readableAttributeNames(EOEnterpriseObject eo) {
		return EOEnterpriseObjectSerializer.readableAttributeNames(eo);
	}

	/**
	 * Override to return the appropriate attribute names.
	 * @param eo enterprise object
	 * 
	 * @return array of attribute names
	 */
	protected NSArray<String> _writableAttributeNames(EOEnterpriseObject eo) {
		return EOEnterpriseObjectSerializer.writableAttributeNames(eo);
	}

	/**
	 * Override to return the appropriate relationship names.
	 * @param eo enterprise object
	 * 
	 * @return array of relationship names
	 */
	protected NSArray<String> _includedRelationshipNames(EOEnterpriseObject eo) {
		return EOEnterpriseObjectSerializer.includedRelationshipNames(eo);
	}
	
	/**
	 * Returns an array of attribute names from the EOEntity of source that should be marshalled to the client.
	 * 
	 * @param source
	 *            the EOEnterpriseObject to copy attribute values from
	 * @return an array of attribute names from the EOEntity of source that should be marshalled
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	public static NSArray<String> readableAttributeNames(EOEnterpriseObject source) {
		// These are cached on EOEntity name as an optimization.

		EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
		NSArray<String> attributeNames = EOEnterpriseObjectSerializer.readableAttributeNames.objectForKey(entity.name());
		//AK: should use clientProperties from EM
		if (attributeNames == null) {
			EOEntity currentEntity = entity;
			while (attributeNames == null && currentEntity != null) {
				attributeNames = (NSArray<String>)ERXProperties.arrayForKey("er.ajax.json." + currentEntity.name() + ".attributes");
				currentEntity = currentEntity.parentEntity();
			}
			if (attributeNames == null) {
				//publicAttributes = source.attributeKeys();
				//publicAttributeSet.addObjectsFromArray(publicAttributes);
				//NSArray classProperties = entity.classPropertyNames();
				//publicAttributeNames = publicAttributeSet.setByIntersectingSet(new NSSet(classProperties)).allObjects();
				attributeNames = entity.clientClassPropertyNames();
			}
			EOEnterpriseObjectSerializer.readableAttributeNames.setObjectForKey(attributeNames, entity.name());
		}

		return attributeNames;
	}
	
	/**
	 * Returns an array of attribute names from the EOEntity of source that should be marshalled from the client.
	 * 
	 * @param source
	 *            the EOEnterpriseObject
	 * @return an array of attribute names from the EOEntity of source that should be unmarshalled
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	public static NSArray<String> writableAttributeNames(EOEnterpriseObject source) {
		// These are cached on EOEntity name as an optimization.

		EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
		NSArray<String> writableNames = EOEnterpriseObjectSerializer.writableAttributeNames.objectForKey(entity.name());
		//AK: should use clientProperties from EM
		if (writableNames == null) {
			EOEntity currentEntity = entity;
			while (writableNames == null && currentEntity != null) {
				writableNames = (NSArray<String>)ERXProperties.arrayForKey("er.ajax.json." + currentEntity.name() + ".writableAttributes");
				currentEntity = currentEntity.parentEntity();
			}
			if (writableNames == null) {
				//publicAttributes = source.attributeKeys();
				//publicAttributeSet.addObjectsFromArray(publicAttributes);
				//NSArray classProperties = entity.classPropertyNames();
				//publicAttributeNames = publicAttributeSet.setByIntersectingSet(new NSSet(classProperties)).allObjects();
				writableNames = entity.clientClassPropertyNames();
			}
			EOEnterpriseObjectSerializer.writableAttributeNames.setObjectForKey(writableNames, entity.name());
		}

		return writableNames;
	}
	
	/**
	 * Returns an array of relationships on this EO that should be included in its marshalled output as
	 * the actual destination objects rather than just faults.
	 * 
	 * @param source
	 *            the EOEnterpriseObject being marhsalled
	 * @return an array of relationships that should be included in the marshalling
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	public static NSArray<String> includedRelationshipNames(EOEnterpriseObject source) {
		// These are cached on EOEntity name as an optimization.

		EOEntity entity = EOUtilities.entityForObject(source.editingContext(), source);
		NSArray<String> relationshipNames = EOEnterpriseObjectSerializer.includedRelationshipNames.objectForKey(entity.name());
		if (relationshipNames == null) {
			EOEntity currentEntity = entity;
			while (relationshipNames == null && currentEntity != null) {
				relationshipNames = (NSArray<String>)ERXProperties.arrayForKey("er.ajax.json." + currentEntity.name() + ".relationships");
				currentEntity = currentEntity.parentEntity();
			}
			if (relationshipNames == null) {
				relationshipNames = entity.classDescriptionForInstances().toOneRelationshipKeys();
			}
			EOEnterpriseObjectSerializer.includedRelationshipNames.setObjectForKey(relationshipNames, entity.name());
		}

		return relationshipNames;
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

	private static Map<EOEditingContext, String> _contexts = new WeakHashMap<>();

	@SuppressWarnings("unchecked")
	public static Map<EOEditingContext, String> contexts() {
		Map<EOEditingContext, String> contexts;
		WOSession session = ERXSession.anySession();
		if (session == null) {
			contexts = _contexts;
		}
		else {
			contexts = (Map<EOEditingContext, String>) session.objectForKey("_jsonContexts");
			if (contexts == null) {
				contexts = new HashMap<>();
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
			id = UUID.randomUUID().toString();
			contexts.put(ec, id);
			return id;
		}
	}

	@SuppressWarnings("unchecked")
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
