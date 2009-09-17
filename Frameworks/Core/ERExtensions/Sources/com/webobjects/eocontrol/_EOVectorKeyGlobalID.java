package com.webobjects.eocontrol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

import com.webobjects.foundation._NSCollectionPrimitives;
import com.webobjects.foundation._NSUtilities;

/**
 * Rewrite of _EOVectorKeyGlobalID with better equals() handling (700% speed improvement)
 * @author ak
 *
 */

public class _EOVectorKeyGlobalID extends EOKeyGlobalID {

	public _EOVectorKeyGlobalID(String entityName, Object values[]) {
		super(entityName, _hashCode(entityName, values));
		_values = _NSCollectionPrimitives.copyArray(values);
	}

	public Object[] keyValues() {
		return _NSCollectionPrimitives.copyArray(_values);
	}

	public Object[] _keyValuesNoCopy() {
		return _values;
	}

	public int keyCount() {
		return _values.length;
	}

	public Object clone() {
		_EOVectorKeyGlobalID result = new _EOVectorKeyGlobalID(_literalEntityName(), _keyValuesNoCopy());
		_prepClone(result);
		return result;
	}

	public String toString() {
		StringBuffer result = new StringBuffer(_NSUtilities.shortClassName(this));
		result.append('[');
		result.append(entityName());
		result.append(' ');
		int i = 0;
		for (int c = _values.length; i < c; i++) {
			if (i > 0)
				result.append(',');
			Object target = _values[i];
			if (target != null) {
				result.append('(');
				result.append(target.getClass().getName());
				result.append(')');
			}
			result.append(target);
		}

		result.append(']');
		return result.toString();
	}

	public boolean equals(Object obj) {
		if (false) {
			if (obj == this)
				return true;
			if (!(obj instanceof _EOVectorKeyGlobalID))
				return false;
			_EOVectorKeyGlobalID other = (_EOVectorKeyGlobalID) obj;
			String entityName = _literalEntityName();
			String otherEntityName = other._literalEntityName();
			if (_values == other._values && entityName == otherEntityName)
				return true;
			if (hashCode() != other.hashCode())
				return false;
			if (!entityName.equals(otherEntityName))
				return false;
			if (_values == other._values)
				return true;
			if (_values.length != other._values.length)
				return false;
			int i = 0;
			for (int c = _values.length; i < c; i++)
				if (!_values[i].equals(other._values[i]))
					return false;

			return true;
		}
		else {
			if (obj == this)
				return true;
			if (!(obj instanceof _EOVectorKeyGlobalID))
				return false;
			_EOVectorKeyGlobalID other = (_EOVectorKeyGlobalID) obj;
			if (hashCode() != other.hashCode())
				return false;
			String entityName = _literalEntityName();
			String otherEntityName = other._literalEntityName();
			if (_values == other._values && entityName == otherEntityName)
				return true;
			if (!entityName.equals(otherEntityName))
				return false;
			if (_values == other._values)
				return true;
			if (_values.length != other._values.length)
				return false;
			int i = 0;
			for (int c = _values.length; i < c; i++)
				if (!_values[i].equals(other._values[i]))
					return false;

			return true;
		}
	}

	private static int _hashCode(String entityName, Object values[]) {
		int hashCode = entityName.hashCode();
		int i = 0;
		for (int c = values.length; i < c; i++) {
			Object temp = values[i];
			if (temp != null)
				hashCode ^= temp.hashCode();
		}

		return hashCode == 0 ? 42 : hashCode;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		fields.put("values", ((Object) (_values)));
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		_values = (Object[]) (Object[]) fields.get("values", ((Object) (new Object[0])));
	}

	static final long serialVersionUID = 657069455L;
	private static final String SerializationValuesFieldKey = "values";
	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.eocontrol._EOVectorKeyGlobalID");
	protected Object _values[];
	private static final ObjectStreamField serialPersistentFields[] = { new ObjectStreamField("values", ((Object) (new Object[0])).getClass()) };

}
