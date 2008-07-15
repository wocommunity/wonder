package er.ajax.json.serializer;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;

public class NSTimestampSerializer extends AbstractSerializer {
	private final static long serialVersionUID = 1;

	private static Class[] _serializableClasses = new Class[] { NSTimestamp.class };

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}
			if (!(java_class.equals("com.webobjects.foundation.NSTimestamp"))) {
				throw new UnmarshallException("not a NSTimestamp");
			}
			long time = jso.getLong("time");
			String tz = jso.getString("tz");
			return ObjectMatch.OKAY;
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSTimestamp.", e);
		}
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			long time = jso.getLong("time");
			String tz = jso.getString("tz");

			if (jso.has("javaClass")) {
				try {
					clazz = Class.forName(jso.getString("javaClass"));
				}
				catch (ClassNotFoundException cnfe) {
					throw new UnmarshallException(cnfe.getMessage());
				}
			}
			if (NSTimestamp.class.equals(clazz)) {
				NSTimestamp timestamp = new NSTimestamp(time, NSTimeZone.getTimeZone(tz));
				state.setSerialized(o, timestamp);
				return timestamp;
			}
			throw new UnmarshallException("invalid class " + clazz);
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSTimestamp.", e);
		}
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			long time;
			String tz;

			if (o instanceof NSTimestamp) {
				time = ((NSTimestamp) o).getTime();
				tz = ((NSTimestamp) o).timeZone().getID();
			}
			else {
				throw new MarshallException("cannot marshall date using class " + o.getClass());
			}
			JSONObject obj = new JSONObject();
			if (ser.getMarshallClassHints()) {
				obj.put("javaClass", o.getClass().getName());
			}
			obj.put("time", time);
			obj.put("tz", tz);
			return obj;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall NSTimestamp.", e);
		}
	}

}
