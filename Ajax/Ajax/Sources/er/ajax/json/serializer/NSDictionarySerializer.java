/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id$
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package er.ajax.json.serializer;

import java.util.Enumeration;
import java.util.Iterator;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Transforms NSDictionaries between JavaScript and Java.
 * 
 * @author Jean-François Veillette <jfveillette@os.ca>
 * @version $Revision$, $Date$ <br>
 *          &copy; 2005 OS communications informatiques, inc. Tous droits réservés.
 */
public class NSDictionarySerializer extends AbstractSerializer {

	private static Class[] _serializableClasses = new Class[] { NSDictionary.class, NSMutableDictionary.class };

	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && NSDictionary.class.isAssignableFrom(clazz)));
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}
			if (!(java_class.equals("com.webobjects.foundation.NSDictionary") || java_class.equals("com.webobjects.foundation.NSMutableDictionary"))) {
				throw new UnmarshallException("not a NSDictionary");
			}
			JSONObject jsonmap = jso.getJSONObject("dict");
			if (jsonmap == null) {
				throw new UnmarshallException("dict missing");
			}
			ObjectMatch m = new ObjectMatch(-1);
			Iterator i = jsonmap.keys();
			String key = null;
			try {
				while (i.hasNext()) {
					key = (String) i.next();
					m = ser.tryUnmarshall(state, null, jsonmap.get(key)).max(m);
				}
			}
			catch (UnmarshallException e) {
				throw new UnmarshallException("key " + key + " " + e.getMessage());
			}
			return m;
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSDictionary.", e);
		}
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}
			NSMutableDictionary abmap = null;
			if (java_class.equals("com.webobjects.foundation.NSDictionary") || java_class.equals("com.webobjects.foundation.NSMutableDictionary")) {
				abmap = new NSMutableDictionary();
			}
			else {
				throw new UnmarshallException("not a NSDictionary");
			}
			JSONObject jsonmap = jso.getJSONObject("dict");
			if (jsonmap == null) {
				throw new UnmarshallException("dict missing");
			}
			Iterator i = jsonmap.keys();
			String key = null;
			try {
				while (i.hasNext()) {
					key = (String) i.next();
					Object value = ser.unmarshall(state, null, jsonmap.get(key));
					abmap.setObjectForKey(value, key);
				}
			}
			catch (UnmarshallException e) {
				throw new UnmarshallException("key " + key + " " + e.getMessage());
			}
			return abmap;
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSDictionary.", e);
		}
	}

	/**
	 * @see com.metaparadigm.jsonrpc.Serializer#doMarshall(java.lang.Object)
	 */
	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			NSDictionary map = (NSDictionary) o;
			JSONObject obj = new JSONObject();
			JSONObject mapdata = new JSONObject();
			obj.put("javaClass", o.getClass().getName());
			obj.put("dict", mapdata);
			Object key = null;
			try {
				for (Enumeration e = map.keyEnumerator(); e.hasMoreElements();) {
					key = e.nextElement();
					Object value = ser.marshall(state, o, map.objectForKey(key), key);
					mapdata.put(String.valueOf(key), value);
				}
			}
			catch (MarshallException e) {
				throw new MarshallException("map key " + key + " " + e.getMessage());
			}
			return obj;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall NSDictionary.", e);
		}
	}
}
