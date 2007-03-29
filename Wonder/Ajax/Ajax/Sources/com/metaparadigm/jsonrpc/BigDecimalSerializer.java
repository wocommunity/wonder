package com.metaparadigm.jsonrpc;

import java.math.BigDecimal;

/**
 * Simple serializer class for BigDecimals. You should use it onyl for display purposes, as the conversion is based on floats.
 * @author ak
 *
 */

public class BigDecimalSerializer extends AbstractSerializer {

	private static final long serialVersionUID = 1L;
	private static Class _serializableClasses[];
	private static Class _JSONClasses[];

	public BigDecimalSerializer() {
	}

	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object jso) throws UnmarshallException {
		try {
			toNumber(clazz, jso);
		}
		catch (NumberFormatException e) {
			throw new UnmarshallException("not a number");
		}
		return ObjectMatch.OKAY;
	}

	public Object toNumber(Class clazz, Object jso) throws NumberFormatException {
		if (clazz == (java.math.BigDecimal.class)) {
			if (jso instanceof String) {
				return new BigDecimal((String) jso);
			}
			return new BigDecimal(((Number) jso).intValue());
		}
		return null;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object jso) throws UnmarshallException {
		try {
			if (jso == null || "".equals(jso)) {
				return null;
			}
			return toNumber(clazz, jso);
		}
		catch (NumberFormatException nfe) {
			throw new UnmarshallException("cannot convert object " + jso + " to type " + clazz.getName());
		}
	}

	public Object marshall(SerializerState state, Object o) {
		return o;
	}

	static {
		_serializableClasses = (new Class[] { java.math.BigDecimal.class });
		_JSONClasses = (new Class[] { java.math.BigDecimal.class, java.lang.String.class });
	}
}