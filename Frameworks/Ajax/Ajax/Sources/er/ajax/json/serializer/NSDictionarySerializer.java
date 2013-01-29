package er.ajax.json.serializer;

import java.util.Enumeration;
import java.util.Iterator;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Serialises NSDictionaries
 * 
 * TODO: if this serialises a superclass does it need to also specify the subclasses?
 */
public class NSDictionarySerializer extends AbstractSerializer {
	/**
	 * Unique serialisation id.
	 */
	private final static long serialVersionUID = 2;

	/**
	 * Classes that this can serialise.
	 */
	private static Class[] _serializableClasses = new Class[] { NSDictionary.class, NSMutableDictionary.class };

	/**
	 * Classes that this can serialise to.
	 */
	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && NSDictionary.class.isAssignableFrom(clazz)));
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		NSDictionary dictionary = (NSDictionary) o;
		JSONObject obj = new JSONObject();
		JSONObject dictionarydata = new JSONObject();
		if (ser.getMarshallClassHints()) {
			try {
				obj.put("javaClass", o.getClass().getName());
			}
			catch (JSONException e) {
				throw new MarshallException("javaClass not found!");
			}
		}
		try {
			obj.put("nsdictionary", dictionarydata);
			state.push(o, dictionarydata, "nsdictionary");
		}
		catch (JSONException e) {
			throw new MarshallException("Could not add nsdictionary to object: " + e.getMessage());
		}
		Object key = null;
		try {
			Enumeration keyEnum = dictionary.allKeys().objectEnumerator();
			while (keyEnum.hasMoreElements()) {
				key = keyEnum.nextElement();
				Object value = dictionary.objectForKey(key);
				String keyString = key.toString(); // only support String keys

				Object json = ser.marshall(state, dictionarydata, value, keyString);

				// omit the object entirely if it's a circular reference or duplicate
				// it will be regenerated in the fixups phase
				if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
					dictionarydata.put(keyString, json);
				}
			}
		}
		catch (MarshallException e) {
			throw (MarshallException) new MarshallException("nsdictionary key " + key + " " + e.getMessage()).initCause(e);
		}
		catch (JSONException e) {
			throw (MarshallException) new MarshallException("nsdictionary key " + key + " " + e.getMessage()).initCause(e);
		}
		finally {
			state.pop();
		}
		return obj;
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		JSONObject jso = (JSONObject) o;
		String java_class;
		try {
			java_class = jso.getString("javaClass");
		}
		catch (JSONException e) {
			throw new UnmarshallException("Could not read javaClass", e);
		}
		if (java_class == null) {
			throw new UnmarshallException("no type hint");
		}
		if (!(java_class.equals("com.webobjects.foundation.NSDictionary") || java_class.equals("com.webobjects.foundation.NSMutableDictionary"))) {
			throw new UnmarshallException("not an NSDictionary");
		}
		JSONObject jsondictionary;
		try {
			jsondictionary = jso.getJSONObject("nsdictionary");
		}
		catch (JSONException e) {
			throw new UnmarshallException("Could not read dictionary: " + e.getMessage(), e);
		}
		if (jsondictionary == null) {
			throw new UnmarshallException("nsdictionary missing");
		}
		ObjectMatch m = new ObjectMatch(-1);
		Iterator i = jsondictionary.keys();
		String key = null;
		state.setSerialized(o, m);
		try {
			while (i.hasNext()) {
				key = (String) i.next();
				m.setMismatch(ser.tryUnmarshall(state, null, jsondictionary.get(key)).max(m).getMismatch());
			}
		}
		catch (UnmarshallException e) {
			throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
		}
		catch (JSONException e) {
			throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
		}
		return m;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		JSONObject jso = (JSONObject) o;
		String java_class;
		try {
			java_class = jso.getString("javaClass");
		}
		catch (JSONException e) {
			throw new UnmarshallException("Could not read javaClass", e);
		}
		if (java_class == null) {
			throw new UnmarshallException("no type hint");
		}
		boolean immutableClone = false;
		NSMutableDictionary abdictionary;
		if (java_class.equals("com.webobjects.foundation.NSDictionary")) {
			abdictionary = new NSMutableDictionary();
			immutableClone = true;
		}
		else if (java_class.equals("com.webobjects.foundation.NSMutableDictionary")) {
			abdictionary = new NSMutableDictionary();
		}
		else {
			throw new UnmarshallException("not an NSDictionary");
		}
		JSONObject jsondictionary;
		try {
			jsondictionary = jso.getJSONObject("nsdictionary");
		}
		catch (JSONException e) {
			throw new UnmarshallException("Could not read dictionary: " + e.getMessage(), e);
		}
		if (jsondictionary == null) {
			throw new UnmarshallException("nsdictionary missing");
		}
		Iterator i = jsondictionary.keys();
		String key = null;
		try {
			while (i.hasNext()) {
				key = (String) i.next();
				Object value = ser.unmarshall(state, null, jsondictionary.get(key));
				if (value != null) {
					abdictionary.setObjectForKey(value, key);
				}
				else {
					abdictionary.setObjectForKey(NSKeyValueCoding.NullValue, key);
				}
			}
			NSDictionary finalDictionary = abdictionary;
			if (immutableClone) {
				finalDictionary = abdictionary.immutableClone();
			}
			state.setSerialized(o, finalDictionary);
			return finalDictionary;
		}
		catch (UnmarshallException e) {
			throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
		}
		catch (JSONException e) {
			throw new UnmarshallException("key " + key + " " + e.getMessage(), e);
		}
	}

}