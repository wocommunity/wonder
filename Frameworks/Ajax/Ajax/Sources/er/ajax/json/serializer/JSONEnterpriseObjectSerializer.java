package er.ajax.json.serializer;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.jabsorb.serializer.impl.BeanSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.json.client.IJSONEnterpriseObject;

/**
 * JSONEnterpriseObjectSerializer turns EO's from a JSON server into IJSONEnterpriseObject stubs of the EO's (and back
 * again).
 * 
 * @author mschrag
 */
public class JSONEnterpriseObjectSerializer extends AbstractSerializer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected static NSMutableDictionary publicAttributes = new NSMutableDictionary();

	private static Class[] _serializableClasses = new Class[] { IJSONEnterpriseObject.class };

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && IJSONEnterpriseObject.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object jso) {
		return null;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String gid = jso.getString("gid");
			jso.put("globalID", gid);
			jso.remove("gid");

			String javaClassName = jso.getString("javaClass");
			Class javaClass = Class.forName(javaClassName);

			BeanSerializer beanSerializer = new BeanSerializer();
			beanSerializer.setOwner(ser);
			Object obj = beanSerializer.unmarshall(state, javaClass, jso);
			state.setSerialized(o, obj);
			return obj;
		}
		catch (Exception e) {
			throw new UnmarshallException("Failed to unmarshall EO.", e);
		}
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			IJSONEnterpriseObject eo = (IJSONEnterpriseObject) o;
			JSONObject obj = new JSONObject();
			obj.put("javaClass", o.getClass().getName());
			obj.put("gid", eo.globalID());
			//JSONObject eoData = new JSONObject();
			//obj.put("eo", eoData);
			return obj;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall EO.", e);
		}
	}
}
