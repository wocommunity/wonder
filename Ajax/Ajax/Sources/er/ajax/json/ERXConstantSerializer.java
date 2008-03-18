package er.ajax.json;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXConstant;

/**
 * Serializes ERXConstants.
 * 
 * @author ak
 */
public class ERXConstantSerializer extends AbstractSerializer {

	protected static NSMutableDictionary publicAttributes = new NSMutableDictionary();

	private static Class[] _serializableClasses = new Class[] { ERXConstant.Constant.class };

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && ERXConstant.Constant.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object jso) {
		return null;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			Object object = jso.get("value");
			if (object == null) {
				throw new UnmarshallException("ERXConstant missing");
			}
			String javaClassName = jso.getString("javaClass");
			return ERXConstant.constantForClassNamed(object, javaClassName);

		}
		catch (Exception e) {
			throw new UnmarshallException("Failed to unmarshall ERXConstant.", e);
		}
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			ERXConstant.Constant obj = (ERXConstant.Constant) o;
			JSONObject result = new JSONObject();
			result.put("javaClass", obj.getClass().getName());
			result.put("value", obj.value());
			result.put("name", obj.name());
			return result;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall ERXConstant.", e);
		}
	}
}
