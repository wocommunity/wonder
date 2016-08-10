package er.ajax.json.serializer;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXConstant;

/**
 * Serializes ERXConstants.
 * 
 * @author ak
 */
public class ERXConstantSerializer extends AbstractSerializer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected static NSMutableDictionary publicAttributes = new NSMutableDictionary();

	private static Class[] _serializableClasses = new Class[] { ERXConstant.Constant.class };

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	@Override
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
			ERXConstant.Constant constant = ERXConstant.constantForClassNamed(object, javaClassName);
			state.setSerialized(o, constant);
			return constant;
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
