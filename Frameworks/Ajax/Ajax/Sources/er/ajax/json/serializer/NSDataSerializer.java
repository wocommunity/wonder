package er.ajax.json.serializer;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Transforms NSData between JavaScript and Java.
 */
public class NSDataSerializer extends AbstractSerializer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static Class[] _serializableClasses = new Class[] { NSData.class, NSMutableData.class };
	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONArray.class) && NSData.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}

			String string = jso.getString("bytes");
			NSData data = (NSData) NSPropertyListSerialization.propertyListFromString(string);
			if (NSData.class.isAssignableFrom(clazz)) {
				return ObjectMatch.OKAY;
			}
			throw new UnmarshallException("invalid class " + clazz);
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSData.", e);
		}
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}

			String string = jso.getString("bytes");
			NSData data = (NSData) NSPropertyListSerialization.propertyListFromString(string);
			if (NSMutableData.class.equals(clazz)) {
				NSMutableData mutableData = new NSMutableData(data);
				state.setSerialized(o, mutableData);
				return mutableData;
			}
			else if (NSData.class.equals(clazz)) {
				state.setSerialized(o, data);
				return data;
			}
			throw new UnmarshallException("invalid class " + clazz);
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSData.", e);
		}

	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			String bytes;

			if (o instanceof NSData) {
				bytes = NSPropertyListSerialization.stringFromPropertyList(o);
			}
			else {
				throw new MarshallException("cannot marshall date using class " + o.getClass());
			}
			JSONObject obj = new JSONObject();
			if (ser.getMarshallClassHints()) {
				obj.put("javaClass", o.getClass().getName());
			}
			obj.put("bytes", bytes);
			return obj;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall NSData.", e);
		}
	}
}
