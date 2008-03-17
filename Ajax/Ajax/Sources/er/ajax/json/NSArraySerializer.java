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

package er.ajax.json;

import java.util.Enumeration;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Transforms NSArray between JavaScript and Java.
 * 
 * @author Jean-François Veillette <jfveillette@os.ca>
 * @version $Revision$, $Date$ <br>
 *          &copy; 2005 OS communications informatiques, inc. Tous droits réservés.
 */
public class NSArraySerializer extends AbstractSerializer {
	private static Class[] _serializableClasses = new Class[] { NSArray.class, NSMutableArray.class };
	private static Class[] _JSONClasses = new Class[] { JSONObject.class };

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONArray.class) && NSArray.class.isAssignableFrom(clazz)));
	}

	/**
	 * @see com.metaparadigm.jsonrpc.Serializer#doTryToUnmarshall(java.lang.Class, java.lang.Object)
	 */
	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}
			if (!(java_class.equals("com.webobjects.foundation.NSArray") || java_class.equals("com.webobjects.foundation.NSMutableArray"))) {
				throw new UnmarshallException("not a NSArray");
			}

			JSONArray jsonlist = jso.getJSONArray("nsarray");
			if (jsonlist == null) {
				throw new UnmarshallException("nsarray missing");
			}
			int i = 0;
			ObjectMatch m = new ObjectMatch(-1);
			try {
				for (; i < jsonlist.length(); i++) {
					m = ser.tryUnmarshall(state, null, jsonlist.get(i)).max(m);
				}
			}
			catch (UnmarshallException e) {
				throw new UnmarshallException("element " + i + " " + e.getMessage());
			}
			return m;
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSArray.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.metaparadigm.jsonrpc.Serializer#doUnmarshall(java.lang.Class, java.lang.Object)
	 */
	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		try {
			JSONObject jso = (JSONObject) o;
			String java_class = jso.getString("javaClass");
			if (java_class == null) {
				throw new UnmarshallException("no type hint");
			}
			NSMutableArray al = null;
			if (java_class.equals("com.webobjects.foundation.NSArray") || java_class.equals("com.webobjects.foundation.NSMutableArray")) {
				al = new NSMutableArray();
			}
			else {
				throw new UnmarshallException("not a NSArray");
			}
			JSONArray jsonlist = jso.getJSONArray("nsarray");
			if (jsonlist == null) {
				throw new UnmarshallException("nsarray missing");
			}
			int i = 0;
			try {
				for (; i < jsonlist.length(); i++) {
					al.addObject(ser.unmarshall(state, null, jsonlist.get(i)));
				}
			}
			catch (UnmarshallException e) {
				throw new UnmarshallException("element " + i + " " + e.getMessage());
			}
			return al;
		}
		catch (JSONException e) {
			throw new UnmarshallException("Failed to unmarshall NSArray.", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.metaparadigm.jsonrpc.Serializer#doMarshall(java.lang.Object)
	 */
	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		try {
			// TODO Auto-generated method stub
			NSArray array = (NSArray) o;
			JSONObject obj = new JSONObject();
			JSONArray arr = new JSONArray();
			obj.put("javaClass", o.getClass().getName());
			obj.put("nsarray", arr);
			int index = 0;
			try {
				for (Enumeration e = array.objectEnumerator(); e.hasMoreElements(); index++) {
					arr.put(ser.marshall(state, o, e.nextElement(), Integer.valueOf(index)));
				}
			}
			catch (MarshallException e) {
				throw new MarshallException("element " + index + " " + e.getMessage());
			}
			return obj;
		}
		catch (JSONException e) {
			throw new MarshallException("Failed to marshall NSArray.", e);
		}
	}
}
