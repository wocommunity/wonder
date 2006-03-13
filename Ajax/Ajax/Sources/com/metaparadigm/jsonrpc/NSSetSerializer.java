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

package com.metaparadigm.jsonrpc;

import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSMutableSet;
import java.util.Enumeration;
import java.util.Iterator;
import org.json.JSONObject;

/**
 * Transforms NSSets between JavaScript and Java.
 *
 * @author	Jean-François Veillette <jfveillette@os.ca>
 * @version $, $Date $
 * <br>&copy; 2005 OS Communications Informatiques, inc. Tous droits réservés.
 */
class NSSetSerializer extends AbstractSerializer
{
    private static Class[] _serializableClasses = new Class[] { NSSet.class, NSMutableSet.class };
    private static Class[] _JSONClasses = new Class[] { JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }

    public boolean canSerialize(Class clazz, Class jsonClazz)
    {
		return (super.canSerialize(clazz, jsonClazz) ||
				((jsonClazz == null || jsonClazz == JSONObject.class) &&
				 NSSet.class.isAssignableFrom(clazz)));
    }

    public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
		throws UnmarshallException
    {
		JSONObject jso = (JSONObject)o;
		String java_class = jso.getString("javaClass");
		if(java_class == null)
			throw new UnmarshallException("no type hint");	
		if(!(java_class.equals("com.webobjects.foundation.NSSet") ||
			 java_class.equals("com.webobjects.foundation.NSMutableSet")))
			throw new UnmarshallException("not a Set");
		JSONObject jsonset = jso.getJSONObject("set");
		if(jsonset == null)
			throw new UnmarshallException("set missing");

		ObjectMatch m = new ObjectMatch(-1);

		Iterator i = jsonset.keys();
		String key = null;

		try {
			while(i.hasNext()) {
				key = (String)i.next();
				m = ser.tryUnmarshall(state, null, jsonset.get(key)).max(m);
			}
		} catch (UnmarshallException e) {
			throw new UnmarshallException ("key " + key + " " + e.getMessage());
		}
		return m;
    }

    public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		JSONObject jso = (JSONObject)o;
		String java_class = jso.getString("javaClass");
		if(java_class == null)
			throw new UnmarshallException("no type hint");	
		NSMutableSet abset = null;
		if(java_class.equals("com.webobjects.foundation.NSSet") ||
		   java_class.equals("com.webobjects.foundation.NSMutableSet")) {
			abset = new NSMutableSet();
		} else {
			throw new UnmarshallException("not a Set");
		}
		JSONObject jsonset = jso.getJSONObject("set");

		if(jsonset == null)
			throw new UnmarshallException("set missing");

		Iterator i = jsonset.keys();
		String key = null;

		try {
			while(i.hasNext()) {
				key = (String)i.next();
				Object setElement = jsonset.get(key);
                Object unmarshalledObject = ser.unmarshall(state, null, setElement);
				abset.addObject(unmarshalledObject);
			}
		} catch (UnmarshallException e) {
			throw new UnmarshallException("key " + i + e.getMessage());
		}
		return abset;
    }

    public Object marshall(SerializerState state, Object o) throws MarshallException {
		NSSet set = (NSSet)o;

		JSONObject obj = new JSONObject();
		JSONObject setdata = new JSONObject();
		obj.put("javaClass", o.getClass().getName());
		obj.put("set", setdata);
		String key = null;
		Enumeration i = set.objectEnumerator();

		try {
			while(i.hasMoreElements()) {
				Object value = i.nextElement();
				setdata.put(key, ser.marshall(state, value));
			}
		} catch (MarshallException e) {
			throw new MarshallException("set key " + key + e.getMessage());
		}
		return obj;
    }
 }
