package er.ajax.json.serializer;

import java.util.Enumeration;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.eocontrol._EOCheapCopyArray;
import com.webobjects.eocontrol._EOCheapCopyMutableArray;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

/**
 * Serialises NSArrays
 * 
 * TODO: if this serialises a superclass does it need to also specify the subclasses?
 */
public class NSArraySerializer extends AbstractSerializer {
	/**
	 * Unique serialisation id.
	 */
	private final static long serialVersionUID = 2;

	/**
	 * Classes that this can serialise.
	 */
	private static Class[] _serializableClasses = new Class[] { NSArray.class, NSMutableArray.class, _EOCheapCopyArray.class, _EOCheapCopyMutableArray.class };

	/**
	 * Classes that this can serialise to.
	 */
	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && NSArray.class.isAssignableFrom(clazz)));
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		NSArray nsarray = (NSArray) o;
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();

		// TODO: this same block is done everywhere.
		// Have a single function to do it.
		if (ser.getMarshallClassHints()) {
			try {
				obj.put("javaClass", o.getClass().getName());
			}
			catch (JSONException e) {
				throw new MarshallException("javaClass not found!");
			}
		}
		try {
			obj.put("nsarray", arr);
			state.push(o, arr, "nsarray");
		}
		catch (JSONException e) {
			throw new MarshallException("Error setting nsarray: " + e);
		}
		int index = 0;
		try {
			Enumeration e = nsarray.objectEnumerator();
			while (e.hasMoreElements()) {
				Object json = ser.marshall(state, arr, e.nextElement(), Integer.valueOf(index));
				if (JSONSerializer.CIRC_REF_OR_DUPLICATE != json) {
					arr.put(json);
				}
				else {
					// put a slot where the object would go, so it can be fixed up properly in the fix up phase
					arr.put(JSONObject.NULL);
				}
				index++;
			}
		}
		catch (MarshallException e) {
			throw (MarshallException) new MarshallException("element " + index).initCause(e);
		}
		finally {
			state.pop();
		}
		return obj;
	}

	// TODO: try unMarshall and unMarshall share 90% code. Put in into an
	// intermediate function.
	// TODO: Also cache the result somehow so that an unmarshall
	// following a tryUnmarshall doesn't do the same work twice!
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
		
		Class klass;
		try {
			klass = Class.forName(java_class);
		} catch (ClassNotFoundException cnfe) {
			throw new UnmarshallException("Could not find class named: " + java_class);
		}
		if (!NSArray.class.isAssignableFrom(klass)) {
			throw new UnmarshallException("not an NSArray");
		}
		
		JSONArray jsonNSArray;
		try {
			jsonNSArray = jso.getJSONArray("nsarray");
		}
		catch (JSONException e) {
			throw new UnmarshallException("Could not read nsarray: " + e.getMessage(), e);
		}
		if (jsonNSArray == null) {
			throw new UnmarshallException("nsarray missing");
		}
		int i = 0;
		ObjectMatch m = new ObjectMatch(-1);
		state.setSerialized(o, m);
		try {
			for (; i < jsonNSArray.length(); i++) {
				m.setMismatch(ser.tryUnmarshall(state, null, jsonNSArray.get(i)).max(m).getMismatch());
			}
		}
		catch (UnmarshallException e) {
			throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
		}
		catch (JSONException e) {
			throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
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
		NSMutableArray al = new NSMutableArray();
		boolean immutableClone = true;
		
		Class klass;
		try {
			klass = Class.forName(java_class);
		} catch (ClassNotFoundException cnfe) {
			throw new UnmarshallException("Could not find class named: " + java_class);
		}

		if (NSMutableArray.class.isAssignableFrom(klass)){
			immutableClone = false;
		} else if (!NSArray.class.isAssignableFrom(klass)) {
			throw new UnmarshallException("not an NSArray");
		}

		JSONArray jsonNSArray;
		try {
			jsonNSArray = jso.getJSONArray("nsarray");
		}
		catch (JSONException e) {
			throw new UnmarshallException("Could not read nsarray: " + e.getMessage(), e);
		}
		if (jsonNSArray == null) {
			throw new UnmarshallException("nsarray missing");
		}
		int i = 0;
		try {
			for (; i < jsonNSArray.length(); i++) {
				Object obj = ser.unmarshall(state, null, jsonNSArray.get(i));
				if (obj != null) {
					al.addObject(obj);
				}
				else {
					al.addObject(NSKeyValueCoding.NullValue);
				}
			}
			NSArray finalArray = al;
			if (immutableClone) {
				finalArray = al.immutableClone();
			}
			state.setSerialized(o, finalArray);
			return finalArray;
		}
		catch (UnmarshallException e) {
			throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
		}
		catch (JSONException e) {
			throw new UnmarshallException("element " + i + " " + e.getMessage(), e);
		}
	}

}
