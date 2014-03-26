package com.webobjects.eoaccess;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.eocontrol.changeNotification.EOChangeNotificationOptions;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSRange;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation._NSStringUtilities;
import com.webobjects.foundation._NSUtilities;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

public class EOAttribute extends EOProperty implements EOPropertyListEncoding, EOSQLExpression.SQLValue {
	
	enum Characteristic {
		ExternalType("externalType"), ColumnName("columnName"), ReadOnly("readOnly"), ClassName("className"), ValueType("valueType"), Width("width"), Precision("precision"), Scale("scale"), WriteFormat("writeFormat"), ReadFormat("readFormat"), UserInfo("userInfo"), ServerTimeZone("serverTimeZone"), ValueFactoryClassName("valueFactoryClassName"), ValueFactoryMethodName("valueFactoryMethodName"), AdaptorValueConversionClassName("adaptorValueConversionClassName"), AdaptorValueConversionMethodName("adaptorValueConversionMethodName"), FactoryMethodArgumentType("factoryMethodArgumentType"), AllowsNull("allowsNull"), ParameterDirection("parameterDirection"), InternalInfo("_internalInfo");

		private String _externalName;

		Characteristic(String externalName) {
			_externalName = externalName;
		}

		public String externalName() {
			return _externalName;
		}

		public static Characteristic characteristicForName(String value) {
			String lower = value.toLowerCase();
			for (Characteristic c : values()) {
				if (c.externalName().equalsIgnoreCase(lower)) {
					return c;
				}
			}
			return null;
		}
	}

	public synchronized Map overwrittenCharacteristics() {
		if (_overwrittenCharacteristics == null) {
			Map list = new HashMap();
			for (Characteristic c : Characteristic.values()) {
				list.put(c, Boolean.FALSE);
			}
			_overwrittenCharacteristics = list;
		}
		return _overwrittenCharacteristics;
	}

	protected void _setOverrideForCharacteristic(Characteristic key) {
		overwrittenCharacteristics().put(key, Boolean.TRUE);
	}

	public boolean overridesPrototypeDefinitionForCharacteristic(Characteristic key) {
		Boolean value = (Boolean) overwrittenCharacteristics().get(key);
		return value == null ? false : value.booleanValue();
	}

	public void _updateFromPrototype() {
		if (_prototype == null)
			return;
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ExternalType))
			_setExternalType(_prototype.externalType());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ColumnName))
			_setColumnName(_prototype.columnName());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ReadOnly))
			_setReadOnly(_prototype.isReadOnly());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ClassName))
			_setClassName(_prototype.className());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ValueType))
			_setValueType(_prototype.valueType());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.Width))
			_setWidth(_prototype.width());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.Precision))
			_setPrecision(_prototype.precision());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.Scale))
			_setScale(_prototype.scale());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.WriteFormat))
			_setWriteFormat(_prototype.writeFormat());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ReadFormat))
			_setReadFormat(_prototype.readFormat());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ServerTimeZone))
			_setServerTimeZone(_prototype.serverTimeZone());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ValueFactoryClassName))
			_setValueFactoryClassName(_prototype.valueFactoryClassName());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ValueFactoryMethodName))
			_setValueFactoryMethodName(_prototype.valueFactoryMethodName());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.AdaptorValueConversionClassName))
			_setAdaptorValueConversionClassName(_prototype.adaptorValueConversionClassName());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.AdaptorValueConversionMethodName))
			_setAdaptorValueConversionMethodName(_prototype.adaptorValueConversionMethodName());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.FactoryMethodArgumentType))
			_setFactoryMethodArgumentType(_prototype.factoryMethodArgumentType());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.AllowsNull))
			_setAllowsNull(_prototype.allowsNull());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.ParameterDirection))
			_setParameterDirection(_prototype.parameterDirection());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.UserInfo))
			if (_prototype.userInfo() != null)
				_setUserInfo(_prototype.userInfo().immutableClone());
			else
				_setUserInfo(NSDictionary.emptyDictionary());
		if (!overridesPrototypeDefinitionForCharacteristic(Characteristic.InternalInfo))
			if (_prototype._internalInfo() != null)
				__setInternalInfo(_prototype._internalInfo().immutableClone());
			else
				__setInternalInfo(NSDictionary.emptyDictionary());
	}

	public void _resetPrototype() {
		if (_prototypeName != null) {
			_prototype = null;
			prototype();
		}
	}

	protected boolean _hasAnyOverrides() {
		boolean result = false;
		for (Iterator i = overwrittenCharacteristics().values().iterator(); i.hasNext();) {
			Boolean anEnum = (Boolean) i.next();
			result |= anEnum.booleanValue();
		}

		return result;
	}

	public EOAttribute() {
		_flags_isNonUpdateable = false;
		_flags_isNonUpdateableInitialized = false;
		_flags_allowsNull = true;
		_adaptorValueType = -1;
		_valueTypeClassName = "";
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public String toString() {
		return _toString(0);
	}

	public String _toString(int indent) {
		String indentString = "";
		for (int i = 0; i < indent; i++)
			indentString = new StringBuilder().append(indentString).append('\t').toString();

		StringBuilder aLog = new StringBuilder();
		aLog.append(new StringBuilder().append(indentString).append("<EOAttribute ").append(name()).append('\n').toString());
		String aKey = "";
		try {
			NSMutableDictionary aDictionary = new NSMutableDictionary();
			encodeIntoPropertyList(aDictionary);
			for (Enumeration enumerator = aDictionary.keyEnumerator(); enumerator.hasMoreElements(); aLog.append(new StringBuilder().append(indentString).append('\t').append(aKey).append(" : '").append(aDictionary.objectForKey(aKey)).append("'\n").toString()))
				aKey = (String) enumerator.nextElement();

		}
		catch (Exception exception) {
			aLog.append(new StringBuilder().append(indentString).append('\t').append(getClass().getName()).append(".toString: characteristics ").append(aKey).append(" exception ").append(exception).append('\n').toString());
		}
		aLog.append(new StringBuilder().append(indentString).append('>').toString());
		return aLog.toString();
	}

	@Override
	public EOEntity entity() {
		return _parent;
	}

	public EOStoredProcedure storedProcedure() {
		return _parentStoredProcedure;
	}

	public Object parent() {
		if (_parent != null)
			return _parent;

		return _parentStoredProcedure;
	}

	private EOModel _parentModel() {
		if (_parent != null)
			return _parent.model();
		if (_parentStoredProcedure != null)
			return _parentStoredProcedure.model();

		return null;
	}

	private void _parent_setIsEdited() {
		if (_parent != null)
			_parent._setIsEdited();
		else if (_parentStoredProcedure != null)
			_parentStoredProcedure._setIsEdited();
	}

	private NSArray _parent_primaryKeyAttributes() {
		if (_parent != null)
			return _parent.primaryKeyAttributes();

		return null;
	}

	private EOAttribute _parent_attributeNamed(String name) {
		if (_parent != null)
			return _parent.attributeNamed(name);

		return null;
	}

	private void _parent_removeAttribute(EOAttribute att) {
		if (_parent != null)
			_parent.removeAttribute(att);
	}

	public String prototypeName() {
		return _prototypeName;
	}

	public EOAttribute prototype() {
		if (_prototype == null && _prototypeName != null)
			if (_parent != null)
				_prototype = _parent.model().prototypeAttributeNamed(_prototypeName);
			else if (_parentStoredProcedure != null)
				_prototype = _parentStoredProcedure.model().prototypeAttributeNamed(_prototypeName);
		return _prototype;
	}

	public String externalType() {
		return _externalType;
	}

	public String columnName() {
		return _columnName;
	}

	public String definition() {
		return _definitionArray == null ? null : _definitionArray.valueForSQLExpression(null);
	}

	public boolean isFlattened() {
		if (_definitionArray == null)
			return false;
		int count;
		if ((count = _definitionArray.count()) < 2)
			return false;
		for (int index = 0; index < count - 1; index++) {
			Object property = _definitionArray.objectAtIndex(index);
			if (!(property instanceof EORelationship))
				return false;
		}

		return _definitionArray.lastObject() instanceof EOAttribute;
	}

	public boolean isDerived() {
		return _definitionArray != null;
	}

	public boolean isReadOnly() {
		return _flags_isReadOnly || isDerived() && !isFlattened();
	}

	public boolean _isPrimaryKeyClassProperty() {
		NSArray pkAtts = _parent_primaryKeyAttributes();
		if (pkAtts == null)
			return false;
		if (!pkAtts.containsObject(this))
			return false;

		return _parent.classProperties().containsObject(this);
	}

	public boolean _isNonUpdateable() {
		if (_flags_isNonUpdateableInitialized) {
			return _flags_isNonUpdateable;
		}
		_flags_isNonUpdateable = isReadOnly() || _isPrimaryKeyClassProperty();
		_flags_isNonUpdateableInitialized = true;
		return _flags_isNonUpdateable;
	}

	/**
	 * @return valueClassName
	 * @deprecated Method valueClassName is deprecated
	 */
	@Deprecated
	public String valueClassName() {
		return _valueClassName;
	}

	public String className() {
		return _className;
	}

	public String valueType() {
		return _valueType;
	}

	protected char _valueTypeChar() {
		if (_valueType != null && _valueType.length() == 1)
			return _valueType.charAt(0);

		return _VTUnknown;
	}

	public int width() {
		return _width;
	}

	public int precision() {
		return _precision;
	}

	public int scale() {
		return _scale;
	}

	public boolean allowsNull() {
		return _flags_allowsNull;
	}

	public String readFormat() {
		return _readFormat;
	}

	public String writeFormat() {
		return _writeFormat;
	}

	public int parameterDirection() {
		return _parameterDirection;
	}

	public NSDictionary userInfo() {
		return _userInfo;
	}

	public NSDictionary _internalInfo() {
		return _internalInfo;
	}

	public EOAttribute(NSDictionary plist, Object owner) {
		_flags_isNonUpdateable = false;
		_flags_isNonUpdateableInitialized = false;
		_name = (String) plist.objectForKey("name");
		_adaptorValueType = -1;
		_valueTypeClassName = "";
		setParent(owner);
		String string;
		if ((string = (String) plist.objectForKey("prototypeName")) != null)
			setPrototype(_parentModel().prototypeAttributeNamed(string));
		if ((string = (String) plist.objectForKey("externalType")) != null)
			setExternalType(string);
		if ((string = (String) plist.objectForKey("isReadOnly")) != null)
			setReadOnly(string.equals("Y"));
		if ((string = (String) plist.objectForKey("allowsNull")) != null || _prototypeName == null)
			setAllowsNull(string != null && string.equals("Y"));
		if ((string = (String) plist.objectForKey("valueType")) != null)
			setValueType(string);
		if ((string = (String) plist.objectForKey("valueClassName")) != null)
			setValueClassName(string);
		if ((string = (String) plist.objectForKey("className")) != null)
			setClassName(string);
		string = (String) plist.objectForKey("writeFormat");
		if (string == null) {
			string = (String) plist.objectForKey("updateFormat");
			if (string == null)
				string = (String) plist.objectForKey("insertFormat");
		}
		if (string != null)
			setWriteFormat(string);
		string = (String) plist.objectForKey("readFormat");
		if (string == null)
			string = (String) plist.objectForKey("selectFormat");
		if (string != null)
			setReadFormat(string);
		int num = _NSStringUtilities.integerFromPlist(plist, "maximumLength", -1);
		if (num == -1)
			num = _NSStringUtilities.integerFromPlist(plist, "width", -1);
		if (num != -1)
			setWidth(num);
		if ((string = (String) plist.objectForKey("factoryMethodArgumentType")) != null)
			_argumentType = _factoryMethodArgumentTypeFromString(string);
		if ((string = (String) plist.objectForKey("adaptorValueConversionClassName")) != null)
			setAdaptorValueConversionClassName(string);
		if ((string = (String) plist.objectForKey("adaptorValueConversionMethodName")) != null)
			setAdaptorValueConversionMethodName(string);
		if ((string = (String) plist.objectForKey("valueFactoryClassName")) != null)
			setValueFactoryClassName(string);
		if ((string = (String) plist.objectForKey("valueFactoryMethodName")) != null)
			setValueFactoryMethodName(string);
		num = _NSStringUtilities.integerFromPlist(plist, "precision", -1);
		if (num != -1)
			setPrecision(num);
		num = _NSStringUtilities.integerFromPlist(plist, "scale", -1);
		if (num != -1)
			setScale(num);
		if (_width == 0 && _externalType != null) {
			int leftParen = _externalType.indexOf("(");
			if (leftParen != -1) {
				int rightParen = _externalType.indexOf(")");
				if (rightParen != -1) {
					String sizeString = _externalType.substring(leftParen + 1, rightParen);
					int max;
					try {
						max = Integer.parseInt(sizeString);
					}
					catch (NumberFormatException e) {
						NSLog._conditionallyLogPrivateException(e);
						max = 0;
					}
					if (max != 0) {
						setWidth(max);
						setExternalType(_externalType.substring(0, leftParen));
					}
				}
			}
		}
		String tzName = (String) plist.objectForKey("serverTimeZone");
		if (tzName != null) {
			TimeZone tz = TimeZone.getTimeZone(tzName);
			setServerTimeZone(tz);
		}
		num = _NSStringUtilities.integerFromPlist(plist, "parameterDirection", -1);
		if (num != -1)
			setParameterDirection(num);
		Object val = plist.objectForKey("userInfo");
		if (val == null)
			val = plist.objectForKey("userDictionary");
		setUserInfo((NSDictionary) val);
		if ((val = plist.objectForKey("internalInfo")) != null)
			_setInternalInfo((NSDictionary) val);
	}

	private boolean shouldEncodeIvarWithPrototypeCharacteristic(int ivar, Characteristic key) {
		return ivar != 0 && (_prototype == null || overridesPrototypeDefinitionForCharacteristic(key));
	}

	private boolean shouldEncodeIvarWithPrototypeCharacteristic(Object ivar, Characteristic key) {
		return ivar != null && (_prototype == null || overridesPrototypeDefinitionForCharacteristic(key));
	}

	private boolean shouldEncodeScalarIvarWithPrototypeCharacteristic(int scalarIvar, Characteristic key) {
		return _prototype == null && scalarIvar != 0 || _prototype != null && overridesPrototypeDefinitionForCharacteristic(key);
	}

	public void encodeIntoPropertyList(NSMutableDictionary result) {
		if (_name != null)
			result.setObjectForKey(_name, "name");
		Object plist = prototypeName();
		if (plist != null)
			result.setObjectForKey(plist, "prototypeName");
		plist = columnName();
		if (plist != null) {
			if (shouldEncodeIvarWithPrototypeCharacteristic(_columnName, Characteristic.ColumnName))
				result.setObjectForKey(plist, "columnName");
		}
		else {
			plist = definition();
			if (plist != null)
				result.setObjectForKey(plist, "definition");
		}
		if (shouldEncodeIvarWithPrototypeCharacteristic(_externalType, Characteristic.ExternalType))
			result.setObjectForKey(_externalType, "externalType");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_valueType, Characteristic.ValueType))
			result.setObjectForKey(_valueType, "valueType");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_className, Characteristic.ClassName))
			result.setObjectForKey(_className, "className");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_writeFormat, Characteristic.WriteFormat))
			result.setObjectForKey(_writeFormat, "writeFormat");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_readFormat, Characteristic.ReadFormat))
			result.setObjectForKey(_readFormat, "readFormat");
		if (shouldEncodeScalarIvarWithPrototypeCharacteristic(_width, Characteristic.Width))
			result.setObjectForKey(String.valueOf(_width), "width");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_serverTimeZone, Characteristic.ServerTimeZone)) {
			plist = _serverTimeZone.getID();
			if (plist != null)
				result.setObjectForKey(plist, "serverTimeZone");
		}
		if (shouldEncodeIvarWithPrototypeCharacteristic(_valueFactoryClassName, Characteristic.ValueFactoryClassName))
			result.setObjectForKey(_valueFactoryClassName, "valueFactoryClassName");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_valueFactoryMethodName, Characteristic.ValueFactoryMethodName))
			result.setObjectForKey(_valueFactoryMethodName, "valueFactoryMethodName");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_adaptorValueConversionClassName, Characteristic.AdaptorValueConversionClassName))
			result.setObjectForKey(_adaptorValueConversionClassName, "adaptorValueConversionClassName");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_adaptorValueConversionMethodName, Characteristic.AdaptorValueConversionMethodName))
			result.setObjectForKey(_adaptorValueConversionMethodName, "adaptorValueConversionMethodName");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_argumentType, Characteristic.FactoryMethodArgumentType))
			result.setObjectForKey(_stringForFactoryMethodArgumentType(_argumentType), "factoryMethodArgumentType");
		if (shouldEncodeScalarIvarWithPrototypeCharacteristic(_precision, Characteristic.Precision))
			result.setObjectForKey(String.valueOf(_precision), "precision");
		if (shouldEncodeScalarIvarWithPrototypeCharacteristic(_scale, Characteristic.Scale))
			result.setObjectForKey(String.valueOf(_scale), "scale");
		if (shouldEncodeScalarIvarWithPrototypeCharacteristic(_flags_isReadOnly ? 1 : 0, Characteristic.ReadOnly))
			result.setObjectForKey(isReadOnly() ? "Y" : "N", "isReadOnly");
		if (shouldEncodeScalarIvarWithPrototypeCharacteristic(_flags_allowsNull ? 1 : 0, Characteristic.AllowsNull))
			result.setObjectForKey(allowsNull() ? "Y" : "N", "allowsNull");
		if (shouldEncodeScalarIvarWithPrototypeCharacteristic(_parameterDirection, Characteristic.ParameterDirection))
			result.setObjectForKey(String.valueOf(_parameterDirection), "parameterDirection");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_userInfo, Characteristic.UserInfo))
			result.setObjectForKey(_userInfo.clone(), "userInfo");
		if (shouldEncodeIvarWithPrototypeCharacteristic(_internalInfo, Characteristic.InternalInfo))
			result.setObjectForKey(_internalInfo.clone(), "internalInfo");
	}

	public void awakeWithPropertyList(NSDictionary plist) {
		Object encoding;
		String string;
		if ((string = (String) plist.objectForKey("definition")) != null) {
			_setDefinitionWithoutFlushingCaches(string);
			_parent_setIsEdited();
		}
		else if ((string = (String) plist.objectForKey("columnName")) != null)
			setColumnName(string);
		else if ((encoding = plist.objectForKey("externalName")) != null)
			if (encoding instanceof String)
				setColumnName((String) encoding);
			else if (encoding instanceof NSDictionary)
				_definitionArray = (_EOExpressionArray) _objectForPlist(encoding);
	}

	public boolean overridesPrototypeDefinitionForKey(String key) {
		Characteristic aCharacteristic = Characteristic.characteristicForName(key);
		return aCharacteristic == null ? false : overridesPrototypeDefinitionForCharacteristic(aCharacteristic);
	}

	public void setName(String name) {
		if (name.equals(_name))
			return;
		EOEntity entity = entity();
		EOModel model = null;
		EOModelGroup group = null;
		if (entity != null)
			model = entity.model();
		if (model != null)
			group = model.modelGroup();
		if (group != null)
			group.loadAllModelObjects();
		_name = name;
		_parent_setIsEdited();
	}

	public void setPrototype(EOAttribute prototype) {
		if (_prototype == prototype)
			return;
		_overwrittenCharacteristics = null;
		if (prototype == null) {
			_prototype = null;
			_prototypeName = null;
		}
		else if (_prototypeName == null || !_prototypeName.equals(prototype.name())) {
			_prototypeName = prototype.name();
			if (_prototypeName != null) {
				_prototype = _parentModel().prototypeAttributeNamed(_prototypeName);
				if (_prototype == null)
					_prototype = prototype;
				_updateFromPrototype();
			}
			else {
				_prototype = null;
				_prototypeName = null;
			}
		}
	}

	public void setReadOnly(boolean yn) {
		if (_flags_isReadOnly != yn) {
			if (!yn && isDerived() && !isFlattened())
				throw new IllegalArgumentException("Unable to remove read only on a derived not flattened attribute");
			_setReadOnly(yn);
			_setOverrideForCharacteristic(Characteristic.ReadOnly);
		}
	}

	private void _setReadOnly(boolean yn) {
		if (_flags_isReadOnly != yn) {
			if (!yn && isDerived() && !isFlattened())
				throw new IllegalArgumentException("Unable to remove read only on a derived not flattened attribute");
			_flags_isReadOnly = yn;
			_parent_setIsEdited();
			if (_parent != null)
				_parent._clearAttributesCaches();
			_flags_isNonUpdateableInitialized = false;
		}
	}

	public void setColumnName(String columnName) {
		if (columnName == null && _columnName == null) {
			return;
		}
		_setColumnName(columnName);
		_setOverrideForCharacteristic(Characteristic.ColumnName);
		return;
	}

	private void _setColumnName(String columnName) {
		if (columnName == null && _columnName == null) {
			return;
		}
		_definitionArray = null;
		_columnName = columnName;
		_parent_setIsEdited();
		return;
	}

	public void setDefinition(String definition) {
		if (definition == null && _definitionArray == null) {
			return;
		}
		_setDefinitionWithoutFlushingCaches(definition);
		_setValuesFromTargetAttribute();
		_parent_setIsEdited();
		return;
	}

	public void setExternalType(String string) {
		if (_externalType == null || !_externalType.equals(string)) {
			_setExternalType(string);
			_setOverrideForCharacteristic(Characteristic.ExternalType);
		}
	}

	private void _setExternalType(String string) {
		if (_externalType == null || !_externalType.equals(string)) {
			_externalType = string == null || string.length() <= 0 ? null : string;
			_parent_setIsEdited();
		}
	}

	public void setValueType(String string) {
		if (_valueType == null || !_valueType.equals(string)) {
			_setValueType(string);
			_setOverrideForCharacteristic(Characteristic.ValueType);
		}
	}

	private void _setValueType(String string) {
		if (_valueType == null || !_valueType.equals(string))
			_valueType = string == null || string.length() <= 0 ? null : string;
	}

	private String _javaNameForObjcName(String name) {
		if (name == null)
			return null;
		if (name.startsWith(CN_NSPrefix)) {
			if (name.equals(CN_NSString))
				return CN_JavaString;
			if (name.equals(CN_NSNumber))
				return CN_JavaNumber;
			if (name.equals(CN_NSDecimalNumber))
				return CN_JavaBigDecimal;
			if (name.equals(CN_NSCalendarDate))
				return CN_JavaNSTimestamp;
			if (name.equals(CN_NSGregorianDate))
				return CN_JavaNSTimestamp;
			if (name.equals(CN_NSData))
				return CN_JavaNSData;
		}
		else if (name.equals(""))
			return null;
		return name;
	}

	private String _objcNameForJavaName(String name) {
		if (name == null)
			return null;
		if (name.equals(CN_JavaString))
			return CN_NSString;
		if (name.equals(CN_JavaNumber))
			return CN_NSNumber;
		if (name.equals(CN_JavaBigDecimal))
			return CN_NSDecimalNumber;
		if (name.equals(CN_JavaNSTimestamp))
			return CN_NSCalendarDate;
		if (name.equals(CN_JavaNSData))
			return CN_NSData;
		if (name.equals(""))
			return null;

		return _NSStringUtilities.lastComponentInString(name, '.');
	}

	/**
	 * @param name
	 *            valueClassName
	 * @deprecated Method setValueClassName is deprecated
	 */
	@Deprecated
	public void setValueClassName(String name) {
		_valueClassName = name == null || name.length() <= 0 ? null : name;
		_className = _javaNameForObjcName(_valueClassName);
		_adaptorValueType = -1;
		_valueTypeClassName = "";
		_setOverrideForCharacteristic(Characteristic.ClassName);
	}

	public void setClassName(String name) {
		_setClassName(name);
		_setOverrideForCharacteristic(Characteristic.ClassName);
	}

	private void _setClassName(String name) {
		_className = name == null || name.length() <= 0 ? null : name;
		_valueClassName = _objcNameForJavaName(_className);
		_adaptorValueType = -1;
		_valueTypeClassName = "";
	}

	public void setWidth(int length) {
		_setWidth(length);
		_setOverrideForCharacteristic(Characteristic.Width);
	}

	private void _setWidth(int length) {
		_width = length;
	}

	public void setPrecision(int precision) {
		_setPrecision(precision);
		_setOverrideForCharacteristic(Characteristic.Precision);
	}

	private void _setPrecision(int precision) {
		_precision = precision;
	}

	public void setScale(int scale) {
		_setScale(scale);
		_setOverrideForCharacteristic(Characteristic.Scale);
	}

	private void _setScale(int scale) {
		_scale = scale;
	}

	public void setAllowsNull(boolean allowsNull) {
		if (allowsNull == _flags_allowsNull) {
			return;
		}
		_setAllowsNull(allowsNull);
		_setOverrideForCharacteristic(Characteristic.AllowsNull);
		return;
	}

	private void _setAllowsNull(boolean allowsNull) {
		if (allowsNull == _flags_allowsNull) {
			return;
		}
		_flags_allowsNull = allowsNull;
		return;
	}

	public void setWriteFormat(String string) {
		_setWriteFormat(string);
		_setOverrideForCharacteristic(Characteristic.WriteFormat);
	}

	private void _setWriteFormat(String string) {
		_writeFormat = _setNewFormatStringOld(string, _writeFormat);
	}

	public void setReadFormat(String string) {
		_setReadFormat(string);
		_setOverrideForCharacteristic(Characteristic.ReadFormat);
	}

	private void _setReadFormat(String string) {
		_readFormat = _setNewFormatStringOld(string, _readFormat);
	}

	public void setParameterDirection(int parameterDirection) {
		_setParameterDirection(parameterDirection);
		_setOverrideForCharacteristic(Characteristic.ParameterDirection);
	}

	private void _setParameterDirection(int parameterDirection) {
		_parameterDirection = parameterDirection;
	}

	public void setUserInfo(NSDictionary dictionary) {
		_setUserInfo(dictionary);
		_setOverrideForCharacteristic(Characteristic.UserInfo);
	}

	private void _setUserInfo(NSDictionary dictionary) {
		_userInfo = dictionary == null || dictionary.count() <= 0 ? null : (NSDictionary) dictionary.clone();
		_parent_setIsEdited();
	}

	protected void _setInternalInfo(NSDictionary dictionary) {
		__setInternalInfo(dictionary);
		_setOverrideForCharacteristic(Characteristic.InternalInfo);
	}

	private void __setInternalInfo(NSDictionary dictionary) {
		_internalInfo = dictionary == null || dictionary.count() <= 0 ? null : (NSDictionary) dictionary.clone();
		_parent_setIsEdited();
	}

	public void beautifyName() {
		setName(_EOStringUtil.nameForExternalNameSeparatorStringInitialCaps(name(), "_", false));
	}

	public TimeZone serverTimeZone() {
		if (_serverTimeZone == null)
			return TimeZone.getDefault();

		return _serverTimeZone;
	}

	public void setServerTimeZone(TimeZone tz) {
		_setServerTimeZone(tz);
		_setOverrideForCharacteristic(Characteristic.ServerTimeZone);
	}

	private void _setServerTimeZone(TimeZone tz) {
		_serverTimeZone = (TimeZone) tz.clone();
	}

	public Object newValueForBytes(byte bytes[], int length) {
		return newValueForImmutableBytes(bytes);
	}

	public Object newValueForImmutableBytes(byte bytes[]) {
		Class dataClass = NSData._CLASS;
		Object value = null;
		NSData data = null;
		if (_valueClass == null) {
			_valueClass = _NSUtilities.classWithName(className());//_valueClassName);
			if (_valueClass == null)
				_valueClass = dataClass;
		}
		if (_valueClass == dataClass || _argumentType == FactoryMethodArgumentIsData || _valueFactoryMethod == null) {
			data = new NSData(bytes, new NSRange(0, bytes.length), true);
			if (_valueClass == dataClass || _valueFactoryMethod == null)
				return data;
		}
		switch (_argumentType) {
		default:
			break;

		case FactoryMethodArgumentIsData:
			try {
				value = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), data);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			break;

		case FactoryMethodArgumentIsBytes:
			try {
				value = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), bytes);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			break;

		case FactoryMethodArgumentIsString:
			try {
				value = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), _NSStringUtilities.stringForBytes(bytes, CharEncoding.UTF_8));
				if (!NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDatabaseAccess))
					break;
				if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed))
					NSLog.debug.appendln(new RuntimeException("Deprecated implicit bytes->String conversion.  Assuming UTF-8 encoding."));
				else
					NSLog.debug.appendln("Deprecated implicit bytes->String conversion.  Assuming UTF-8 encoding.  Set debug level to NSLog.DebugLevelDetailed for more information.");
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			break;
		}
		return value;
	}

	/**
	 * @param bytes
	 * @param length
	 * @return newValueForBytesString
	 * @deprecated Method newValueForBytesString is deprecated
	 */
	@Deprecated
	public Object newValueForBytesString(byte bytes[], int length) {
		Class stringClass = String.class;
		Object value = null;
		Object value1 = null;
		if (_valueClass == null) {
			_valueClass = _NSUtilities.classWithName(className());//_valueClassName);
			if (_valueClass == null)
				_valueClass = stringClass;
		}
		if (_valueClass == stringClass) {
			String result = _NSStringUtilities.stringForBytes(bytes, 0, length, CharEncoding.UTF_8);
			if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDatabaseAccess))
				if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed))
					NSLog.debug.appendln(new RuntimeException("Deprecated implicit bytes->String conversion.  Assuming UTF-8 encoding."));
				else
					NSLog.debug.appendln("Deprecated implicit bytes->String conversion.  Assuming UTF-8 encoding.  Set debug level to NSLog.DebugLevelDetailed for more information.");
			return result;
		}
		if (_valueClass == stringClass || _argumentType == 1 || _valueFactoryMethod == null) {
			value = _NSStringUtilities.stringForBytes(bytes, 0, length, CharEncoding.UTF_8);
			if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDatabaseAccess))
				if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed))
					NSLog.debug.appendln(new RuntimeException("Deprecated implicit bytes->String conversion.  Assuming UTF-8 encoding."));
				else
					NSLog.debug.appendln("Deprecated implicit bytes->String conversion.  Assuming UTF-8 encoding.  Set debug level to NSLog.DebugLevelDetailed for more information.");
			if (_valueClass == stringClass || _valueFactoryMethod == null)
				return value;
		}
		switch (_argumentType) {
		case FactoryMethodArgumentIsData:
		default:
			break;

		case FactoryMethodArgumentIsString:
			try {
				value1 = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), value);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			value = value1;
			break;

		case FactoryMethodArgumentIsBytes:
			try {
				value1 = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), new Object[] { bytes, _NSUtilities.IntegerForInt(length) });
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			value = value1;
			break;
		}
		return value;
	}
	
	public Object newValueForDate(Object value) {
		if(valueFactoryMethod() != null) {
			if(!(value instanceof Date)) {
				throw new JDBCAdaptorException(new StringBuilder().append(value).append(" of type ").append(value.getClass().getName()).append(" is not a valid Date type.  You must use java.sql.Timestamp, java.sql.Date, or java.sql.Time").toString(), null);
			}
			Date date = (Date)value;
			//Call the custom factory method
			try {
				if(valueFactoryClass() != null) {
					Class<?> factoryClass = valueFactoryClass();
					return valueFactoryMethod().invoke(factoryClass, date);
				}
				Class<?> c = _NSUtilities.classWithName(className());
				return valueFactoryMethod().invoke(c, date);
			} catch(IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch(IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch(NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch(InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		} else {
			if(value instanceof Timestamp) {
				return new NSTimestamp((Timestamp)value);
			}
			if(value instanceof Date) {
				Date temp = (Date)value;
				return new NSTimestamp(temp.getTime());
			} else {
				throw new JDBCAdaptorException(new StringBuilder().append(value).append(" of type ").append(value.getClass().getName()).append(" is not a valid Date type.  You must use java.sql.Timestamp, java.sql.Date, or java.sql.Time").toString(), null);
			}
		}
	}

	public Object newValueForString(String str) {
		Class stringClass = String.class;
		Object value = null;
		if (_valueClass == null) {
			_valueClass = _NSUtilities.classWithName(className());//_valueClassName);
			if (_valueClass == null)
				_valueClass = stringClass;
		}
		if (_valueClass == stringClass || _valueFactoryMethod == null)
			return str;
		switch (_argumentType) {
		default:
			break;

		case FactoryMethodArgumentIsString:
			try {
				value = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), str);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			break;

		case FactoryMethodArgumentIsBytes:
			try {
				value = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), _NSStringUtilities.bytesForString(str, CharEncoding.UTF_8));
				if (!NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDatabaseAccess))
					break;
				if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed))
					NSLog.debug.appendln(new RuntimeException("Deprecated implicit String->bytes conversion.  Assuming UTF-8 encoding."));
				else
					NSLog.debug.appendln("Deprecated implicit String->bytes conversion.  Assuming UTF-8 encoding.  Set debug level to NSLog.DebugLevelDetailed for more information.");
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			break;

		case FactoryMethodArgumentIsData:
			try {
				value = _valueFactoryMethod.invoke(valueFactoryClass()==null?_valueClass:valueFactoryClass(), new NSData(str, CharEncoding.UTF_8));
				if (!NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDatabaseAccess))
					break;
				if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed))
					NSLog.debug.appendln(new RuntimeException("Deprecated implicit String->NSData conversion.  Assuming UTF-8 encoding."));
				else
					NSLog.debug.appendln("Deprecated implicit String->NSData conversion.  Assuming UTF-8 encoding.  Set debug level to NSLog.DebugLevelDetailed for more information.");
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			break;
		}
		return value;
	}
	
	public Class valueFactoryClass() {
		return _valueFactoryClass;
	}

	public String valueFactoryClassName() {
		return _valueFactoryClassName;
	}

	public String valueFactoryMethodName() {
		return _valueFactoryMethodName;
	}

	public NSSelector valueFactoryMethod() {
		return _valueFactoryMethod;
	}

	public Object adaptorValueByConvertingAttributeValue(Object value) {
		Object convertedValue = value;
		NSSelector conversionMethod = adaptorValueConversionMethod();
		Class conversionClass = adaptorValueConversionClass();
		if (conversionMethod != null)
			try {
				if (conversionClass != null) {
					convertedValue = conversionMethod.invoke(conversionClass,new Object[]{value});
				} else {
					convertedValue = conversionMethod.invoke(value);
				}
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		int valueType = adaptorValueType();
		
		//To preserve the illusion of consistency, convert the byte[] to an NSData here
		if(FactoryMethodArgumentIsBytes == factoryMethodArgumentType() && valueFactoryMethod() != null) {
			byte[] bytes = (byte[])convertedValue;
			convertedValue = new NSData(bytes);
		}
		
		//Make an additional check here to support custom date types.
		if(!(AdaptorDateType == valueType && conversionMethod != null && Date.class.isInstance(convertedValue))) {
			if (!valueClasses[valueType].isInstance(convertedValue) && (valueClasses[valueType] != Number.class || !(convertedValue instanceof Boolean))) {
				EOEntity parentEntity = (EOEntity) parent();
				String entityName = parentEntity == null ? "<unspecified>" : parentEntity.name();
				throw new IllegalArgumentException(new StringBuilder().append("EOAttribute adaptorValueByConvertingAttributeValue(Object): Unable to convert value of class ").append(convertedValue.getClass().getName()).append(" for attribute '").append(name()).append("' in entity '").append(entityName).append("' to adaptor type EOAttribute.Adaptor").append(valueTypeNames[valueType]).append("Type.  Check the signature of the conversion method ").append(className()).append('.').append(conversionMethod == null ? "NotFound" : conversionMethod.name()).append("().").toString());
			}
		}
		return convertedValue;
	}

	public String adaptorValueConversionMethodName() {
		return _adaptorValueConversionMethodName;
	}

	public NSSelector adaptorValueConversionMethod() {
		return _adaptorValueConversionMethod;
	}

	public String adaptorValueConversionClassName() {
		return _adaptorValueConversionClassName;
	}

	public Class adaptorValueConversionClass() {
		return _adaptorValueConversionClass;
	}

	public int adaptorValueType() {
		if (_adaptorValueType != -1)
			return _adaptorValueType;
		String className = className();//_valueClassName;
		if (className == null)
			className = CN_NSData;
		Class valueClass = _NSUtilities.classWithName(className);
		if (valueClass == null)
			throw new IllegalStateException(new StringBuilder().append("adaptorValueType: unable to load class named '").append(className).append("' for attribute ").append(name()).append(" on entity ").append(entity().name()).toString());
		if (valueFactoryMethodName() == null) {
			if (valueClass == String.class)
				_adaptorValueType = AdaptorCharactersType;
			else if (_NSUtilities._isClassANumberOrABoolean(valueClass))
				_adaptorValueType = AdaptorNumberType;
			else if (NSTimestamp.class.isAssignableFrom(valueClass))
				_adaptorValueType = AdaptorDateType;
			else if (NSData.class.isAssignableFrom(valueClass))
				_adaptorValueType = AdaptorBytesType;
		}
		else if (_argumentType == FactoryMethodArgumentIsString)
			_adaptorValueType = AdaptorCharactersType;
		else if (_argumentType == FactoryMethodArgumentIsData)
			_adaptorValueType = AdaptorBytesType;
		else if (_argumentType == FactoryMethodArgumentIsDate)
			_adaptorValueType = AdaptorDateType;
		else if (_argumentType == FactoryMethodArgumentIsBytes)
			_adaptorValueType = AdaptorBytesType;
		if (_adaptorValueType == -1)
			_adaptorValueType = AdaptorBytesType;
		return _adaptorValueType;
	}

	/**
	 * @return adaptorValueClass
	 * @deprecated use {@link #adaptorValueClass()}
	 */
	@Deprecated
	protected Class _adaptorValueClass() {
		return adaptorValueClass();
	}

	public Class adaptorValueClass() {
		switch (adaptorValueType()) {
		case AdaptorCharactersType:
			return String.class;

		case AdaptorDateType:
			return NSTimestamp.class;

		case AdaptorNumberType:
			switch (_valueTypeChar()) {
			case _VTBoolean:
				return Boolean.class;

			case _VTShort:
				return Short.class;

			case _VTInteger:
				return Integer.class;

			case _VTLong:
				return Long.class;

			case _VTFloat:
				return Float.class;

			case _VTDouble:
				return Double.class;

			case _VTBigDecimal:
				return BigDecimal.class;

			case _VTByte:
				return Byte.class;
			}
			if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelInformational))
				NSLog.debug.appendln(new StringBuilder().append("value type '").append(_valueTypeChar()).append("' on attribute \"").append(name()).append("\" of entity \"").append(entity().name()).append("\" is invalid.\nYou should fix your EOModel.").toString());
			return Integer.class;

		case AdaptorBytesType:
			if (adaptorValueConversionMethod() != null && factoryMethodArgumentType() == FactoryMethodArgumentIsString)
				return String.class;

			return NSData.class;
		}
		return Object.class;
	}

	public String valueTypeClassName() {
		if (_valueTypeClassName == null)
			_valueTypeClassName = "";
		if (_valueTypeClassName.length() == 0) {
			String className = _valueClassName == null ? NSData.class.getName() : _valueClassName;
			try {
				if (valueFactoryMethodName() == null) {
					Class valueClass = _NSUtilities.classWithName(className);
					if (valueClass != null) {
						if (_NSUtilities._isClassANumberOrABoolean(valueClass))
							switch (_valueTypeChar()) {
							case _VTByte:
								valueClass = Byte.class;
								break;

							case _VTShort:
								valueClass = Short.class;
								break;

							case _VTInteger:
								valueClass = Integer.class;
								break;

							case _VTLong:
								valueClass = Long.class;
								break;

							case _VTFloat:
								valueClass = Float.class;
								break;

							case _VTDouble:
								valueClass = Double.class;
								break;

							case _VTBigDecimal:
								valueClass = BigDecimal.class;
								break;

							case _VTBoolean:
								valueClass = Boolean.class;
								break;

							default:
								if (NSLog.debugLoggingAllowedForLevel(2))
									NSLog.debug.appendln(new StringBuilder().append("value type '").append(_valueTypeChar()).append("' on attribute \"").append(name()).append("\" of entity \"").append(entity().name()).append("\" is invalid.\nYou should fix your EOModel.").toString());
								valueClass = Integer.class;
								break;
							}
						className = valueClass.getName();
					}
				}
			}
			catch (Throwable exception) {
			}
			_valueTypeClassName = className;
		}
		return _valueTypeClassName;
	}

	public int factoryMethodArgumentType() {
		return _argumentType;
	}

	public void setFactoryMethodArgumentType(int argumentType) {
		_setFactoryMethodArgumentType(argumentType);
		_setOverrideForCharacteristic(Characteristic.FactoryMethodArgumentType);
	}

	private void _setFactoryMethodArgumentType(int argumentType) {
		_argumentType = argumentType;
		if (_valueFactoryMethodName != null)
			_valueFactoryMethod = new NSSelector(_valueFactoryMethodName, _classForArgumentType());
	}

	private Class[] _classForArgumentType() {
		if (_argumentType == FactoryMethodArgumentIsString)
			return _NSUtilities._StringClassArray;
		if (_argumentType == FactoryMethodArgumentIsData)
			return (new Class[] { NSData.class });
		if (_argumentType == FactoryMethodArgumentIsDate)
			return (new Class[] { Date.class });
		if (_argumentType == FactoryMethodArgumentIsBytes)
			return (new Class[] { byte[].class });

		return null;
	}
	
	public void setValueFactoryClassName(String factoryClassName) {
		_setValueFactoryClassName(factoryClassName);
		_setOverrideForCharacteristic(Characteristic.ValueFactoryClassName);
	}
	
	private void _setValueFactoryClassName(String factoryClassName) {
		if(factoryClassName != null && factoryClassName.length() != 0) {
			_valueFactoryClassName = factoryClassName;
			try {
				_valueFactoryClass = Class.forName(factoryClassName);
			} catch(ClassNotFoundException e) {
				_valueFactoryClassName = null;
			}
		} else {
			_valueFactoryClassName = null;
			_valueFactoryClass = null;
		}
	}

	public void setValueFactoryMethodName(String factoryMethodName) {
		_setValueFactoryMethodName(factoryMethodName);
		_setOverrideForCharacteristic(Characteristic.ValueFactoryMethodName);
	}

	private void _setValueFactoryMethodName(String factoryMethodName) {
		if (factoryMethodName != null && factoryMethodName.length() != 0) {
			if (factoryMethodName.endsWith(":")) {
				_valueFactoryMethodName = factoryMethodName.substring(0, factoryMethodName.length() - 1);
			} else {
				_valueFactoryMethodName = factoryMethodName;
			}
			_valueFactoryMethod = new NSSelector(_valueFactoryMethodName, _classForArgumentType());
		} else {
			_valueFactoryMethodName = null;
			_valueFactoryMethod = null;
		}
	}

	public void setAdaptorValueConversionClassName(String conversionClassName) {
		_setAdaptorValueConversionClassName(conversionClassName);
		_setOverrideForCharacteristic(Characteristic.AdaptorValueConversionClassName);
	}

	private void _setAdaptorValueConversionClassName(String conversionClassName) {
		if (conversionClassName != null && conversionClassName.length() != 0) {
			_adaptorValueConversionClassName = conversionClassName;
			try {
				_adaptorValueConversionClass = Class.forName(conversionClassName);
			} catch (ClassNotFoundException e) {
				_adaptorValueConversionClassName = null;
			}
		} else {
			_adaptorValueConversionClassName = null;
			_adaptorValueConversionClass = null;
		}
	}

	public void setAdaptorValueConversionMethodName(String conversionMethodName) {
		_setAdaptorValueConversionMethodName(conversionMethodName);
		_setOverrideForCharacteristic(Characteristic.AdaptorValueConversionMethodName);
	}

	private void _setAdaptorValueConversionMethodName(String conversionMethodName) {
		if (conversionMethodName != null && conversionMethodName.length() != 0) {
			_adaptorValueConversionMethodName = conversionMethodName;
			if(adaptorValueConversionClass()!=null) {
				Class valueClass = _NSUtilities.classWithName(className());					
				_adaptorValueConversionMethod = new NSSelector(_adaptorValueConversionMethodName, new Class[]{valueClass});
			} else {
				_adaptorValueConversionMethod = new NSSelector(_adaptorValueConversionMethodName, null);
			}
		} else {
			_adaptorValueConversionMethodName = null;
			_adaptorValueConversionMethod = null;
		}
	}

	public Object validateValue(Object valueP) throws com.webobjects.foundation.NSValidation.ValidationException {
		Object value = valueP;
		if (value == null || value == NSKeyValueCoding.NullValue) {
			if (allowsNull())
				return valueP;
			if (entity().primaryKeyAttributes().indexOfIdenticalObject(this) != -1)
				return valueP;

			throw new com.webobjects.foundation.NSValidation.ValidationException(new StringBuilder().append("The ").append(name()).append(" property of ").append(entity().name()).append(" is not allowed to be null.").toString(), this, name());
		}
		String className = className();
		if (className == null)
			return valueP;
		Class aClass = _NSUtilities.classWithName(className);
		if (aClass == null) {
			if (NSLog.debugLoggingAllowedForLevelAndGroups(2, 128L))
				NSLog.debug.appendln(new StringBuilder().append("Unable to find the class ").append(className).append(" in the Java runtime. Unable to validate. Please check your CLASSPATH, or the type for attribute ").append(name()).toString());
			return valueP;
		}
		if (!aClass.isInstance(value)) {
			value = _NSUtilities.tryToConvertIntoNumberOrBooleanValueClass(value, aClass);
			if (!aClass.isInstance(value)) {
				String valueString = value.toString();
				try {
					if (aClass == BigDecimal.class)
						value = new BigDecimal(valueString);
					else if (Number.class.isAssignableFrom(aClass))
						value = _EOStringUtil.numberWithStringType(valueString, _valueTypeChar());
				}
				catch (Exception e) {
					throw new com.webobjects.foundation.NSValidation.ValidationException(new StringBuilder().append("Error encountered converting value of class ").append(value.getClass().getName()).append(" to type specified in attribute '").append(name()).append("' of entity '").append(entity().name()).append('\'').toString(), this, name());
				}
			}
		}
		if (value == null)
			throw new com.webobjects.foundation.NSValidation.ValidationException(new StringBuilder().append("Error encountered converting null value to type specified in attribute '").append(name()).append("' of entity '").append(entity().name()).append('\'').toString(), this, name());
		int maxLen = width();
		if (maxLen != 0) {
			int currLength = 0;
			Object primitiveValue;
			try {
				primitiveValue = adaptorValueByConvertingAttributeValue(value);
			}
			catch (Exception e) {
				throw new com.webobjects.foundation.NSValidation.ValidationException(new StringBuilder().append("Error encountered converting value of class ").append(value.getClass().getName()).append(" to type specified in attribute '").append(name()).append("' of entity '").append(entity().name()).append("'. Conversion exception is : ").append(e.getMessage()).toString(), this, name());
			}
			if (primitiveValue instanceof String)
				currLength = ((String) primitiveValue).length();
			else if (primitiveValue instanceof NSData)
				currLength = ((NSData) primitiveValue).length();
			if (currLength > maxLen)
				throw new com.webobjects.foundation.NSValidation.ValidationException(new StringBuilder().append("The ").append(name()).append(" property of ").append(entity().name()).append(" exceeds maximum length of ").append(maxLen).append(" characters").toString(), this, name());
		}
		return value;
	}

	boolean referencesProperty(Object property) {
		if (_definitionArray == null)
			return false;

		return _definitionArray.referencesObject(property);
	}

	void setParent(Object parent) {
		if (parent == null) {
			_parent = null;
			_parentStoredProcedure = null;
		} else if (parent instanceof EOEntity) {
			_parent = (EOEntity) parent;
			_parentStoredProcedure = null;
		} else if (parent instanceof EOStoredProcedure) {
			_parent = null;
			_parentStoredProcedure = (EOStoredProcedure) parent;
		} else {
			throw new IllegalArgumentException(new StringBuilder().append("Invalid object of type ").append(parent.getClass().getName()).append(" passed to setParent (expected EOEntity or EOStoredProcedure").toString());
		}
	}

	void setEntity(EOEntity entity) {
		if (_parent == entity)
			return;
		if (_parent != null && this == _parent_attributeNamed(name()))
			_parent_removeAttribute(this);
		setParent(entity);
	}

	public void _setSourceToDestinationKeyMap(NSDictionary mapping) {
		_sourceToDestinationKeyMap = mapping;
	}

	public NSDictionary _sourceToDestinationKeyMap() {
		if (_sourceToDestinationKeyMap == null)
			_sourceToDestinationKeyMap = entity()._keyMapForRelationshipPath(relationshipPath());
		return _sourceToDestinationKeyMap;
	}

	@Override
	public String relationshipPath() {
		if (!isFlattened())
			return null;
		StringBuilder relPath = new StringBuilder();
		int iCount = _definitionArray.count() - 1;
		for (int i = 0; i < iCount; i++) {
			if (i > 0)
				relPath.append('.');
			relPath.append(((EORelationship) _definitionArray.objectAtIndex(i)).name());
		}

		return relPath.toString();
	}

	EOAttribute targetAttribute() {
		if (!isFlattened())
			return null;

		return (EOAttribute) _definitionArray.lastObject();
	}

	public String _setNewFormatStringOld(String newString, String old) {
		if (old == null || !old.equals(newString)) {
			_parent_setIsEdited();
			return newString == null || newString.length() <= 0 ? null : newString;
		}

		return old;
	}

	public int _factoryMethodArgumentTypeFromString(String string) {
		String aString = (string == null ? "" : string).toLowerCase();
		if (aString.equals(FactoryMethodArgumentIsBytesString.toLowerCase()))
			return FactoryMethodArgumentIsBytes;
		if (aString.equals(FactoryMethodArgumentIsStringString.toLowerCase()))
			return FactoryMethodArgumentIsString;
		if (aString.equals(FactoryMethodArgumentIsDateString.toLowerCase()))
			return FactoryMethodArgumentIsDate;
		return !aString.equals("EOFactoryMethodArgumentIsNSString".toLowerCase()) ? FactoryMethodArgumentIsData : FactoryMethodArgumentIsString;
	}

	public String _stringForFactoryMethodArgumentType(int type) {
		switch (type) {
		case FactoryMethodArgumentIsBytes:
			return FactoryMethodArgumentIsBytesString;

		case FactoryMethodArgumentIsString:
			return FactoryMethodArgumentIsStringString;

		case FactoryMethodArgumentIsData:
			return FactoryMethodArgumentIsDataString;

		case FactoryMethodArgumentIsDate:
			return FactoryMethodArgumentIsDateString;
		}
		return FactoryMethodArgumentIsDataString;
	}

	protected _EOExpressionArray _definitionArray() {
		return _definitionArray;
	}

	protected void _setDefinitionArray(_EOExpressionArray definitionArray) {
		if (_definitionArray == definitionArray) {
			return;
		}
		_definitionArray = definitionArray;
		_setValuesFromTargetAttribute();
		_removeFromEntityArraySelector(entity().primaryKeyAttributes(), _setPrimaryKeyAttributesSelector);
		_parent_setIsEdited();
		return;
	}

	protected void _setDefinitionWithoutFlushingCaches(String definition) {
		if (_parent != null) {
			_columnName = null;
			if (definition == null) {
				_definitionArray = null;
				return;
			}
			Object exprArray = _parent._parseDescriptionIsFormatArguments(definition, false, null);
			if (exprArray != null) {
				if (!(exprArray instanceof _EOExpressionArray))
					exprArray = new _EOExpressionArray(exprArray);
				exprArray = _normalizeDefinitionPath(exprArray, null);
				_definitionArray = (_EOExpressionArray) exprArray;
			}
			_removeFromEntityArraySelector(_parent_primaryKeyAttributes(), _setPrimaryKeyAttributesSelector);
		}
	}

	protected void _setValuesFromTargetAttribute() {
		if (isFlattened()) {
			EOAttribute property = (EOAttribute) _definitionArray.lastObject();
			setExternalType(property.externalType());
			setClassName(property.className());
			setValueType(property.valueType());
			setWidth(property.width());
			setAllowsNull(property.allowsNull());
			setReadFormat(property.readFormat());
			setWriteFormat(property.writeFormat());
			setReadOnly(property.isReadOnly());
			setParameterDirection(property.parameterDirection());
			setUserInfo(property.userInfo());
			_setInternalInfo(property._internalInfo());
			int adaptorDataType = property.adaptorValueType();
			if (adaptorDataType == AdaptorNumberType) {
				setPrecision(property.precision());
				setScale(property.scale());
			}
			else if (adaptorDataType == AdaptorDateType)
				setServerTimeZone(property.serverTimeZone());
			else if (adaptorDataType == AdaptorBytesType) {
				setValueFactoryClassName(property.valueFactoryClassName());
				setValueFactoryMethodName(property.valueFactoryMethodName());
				setAdaptorValueConversionClassName(property.adaptorValueConversionClassName());
				setAdaptorValueConversionMethodName(property.adaptorValueConversionMethodName());
				setFactoryMethodArgumentType(property.factoryMethodArgumentType());
			}
		}
	}

	protected Object _objectForPlist(Object plist) {
		if (plist instanceof String)
			return plist;
		if (!(plist instanceof NSDictionary))
			return null;
		NSDictionary pl = (NSDictionary) plist;
		String string;
		if ((string = (String) pl.objectForKey("name")) != null)
			return entity()._parsePropertyName(string);
		if ((string = (String) pl.objectForKey("path")) != null)
			return entity()._parsePropertyName(string);
		NSArray array;
		if ((array = (NSArray) pl.objectForKey("array")) == null)
			return null;
		_EOExpressionArray result = new _EOExpressionArray();
		if ((string = (String) pl.objectForKey("prefix")) != null)
			result.setPrefix(string);
		if ((string = (String) pl.objectForKey("infix")) != null)
			result.setInfix(string);
		if ((string = (String) pl.objectForKey("suffix")) != null)
			result.setSuffix(string);
		int count = array.count();
		for (int i = 0; i < count; i++) {
			Object object = array.objectAtIndex(i);
			if ((object = _objectForPlist(object)) != null)
				result.addObject(object);
		}

		return result;
	}

	protected void _removeFromEntityArraySelector(NSArray oldArray, NSSelector sel) {
		if (oldArray.indexOfObject(this) != -1) {
			NSMutableArray newArray = new NSMutableArray(oldArray);
			newArray.removeIdenticalObject(this);
			try {
				sel.invoke(entity(), newArray);
			} catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (InvocationTargetException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			} catch (NoSuchMethodException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}
	}

	protected Object _normalizeDefinitionPath(Object definition, NSArray attributePath) {
		NSArray path = null;
		if (attributePath != null)
			path = attributePath;
		else
			path = NSArray.emptyArray();
		if (definition instanceof String)
			return definition;
		Object nDefinition;
		if (definition instanceof EOAttribute) {
			EOAttribute attribute = (EOAttribute) definition;
			if (attribute == this)
				return null;
			if (attribute.isDerived()) {
				_EOExpressionArray attrDef = attribute._definitionArray();
				nDefinition = _normalizeDefinitionPath(attrDef, path);
				if (nDefinition == null)
					return null;
			} else if (path.count() == 0) {
				nDefinition = attribute;
			} else {
				_EOExpressionArray newPath = new _EOExpressionArray();
				newPath.setInfix(".");
				newPath.addObjectsFromArray(path);
				newPath.addObject(attribute);
				nDefinition = newPath;
			}
			return nDefinition;
		}
		int cnt;
		if (((_EOExpressionArray) definition)._isPropertyPath()) {
			_EOExpressionArray newPath = new _EOExpressionArray();
			newPath.setInfix(".");
			newPath.addObjectsFromArray(path);
			cnt = ((_EOExpressionArray) definition).count() - 1;
			for (int i = 0; i < cnt; i++)
				newPath.addObject(((_EOExpressionArray) definition).objectAtIndex(i));

			EOAttribute attribute = (EOAttribute) ((_EOExpressionArray) definition).lastObject();
			if (attribute.isDerived()) {
				_EOExpressionArray attrDef = attribute._definitionArray();
				nDefinition = _normalizeDefinitionPath(attrDef, newPath);
				if (nDefinition == null)
					return null;
			} else {
				newPath.addObject(attribute);
				nDefinition = newPath;
			}
			return nDefinition;
		}
		nDefinition = new _EOExpressionArray();
		cnt = ((_EOExpressionArray) definition).count();
		for (int i = 0; i < cnt; i++) {
			Object elem = ((_EOExpressionArray) definition).objectAtIndex(i);
			Object nElem = _normalizeDefinitionPath(elem, path);
			if (nElem == null)
				return null;
			if ((nElem instanceof _EOExpressionArray) && !((_EOExpressionArray) nElem)._isPropertyPath()) {
				int jcnt = ((_EOExpressionArray) nElem).count();
				for (int j = 0; j < jcnt; j++)
					((_EOExpressionArray) nDefinition).addObject(((_EOExpressionArray) nElem).objectAtIndex(j));

			} else {
				((_EOExpressionArray) nDefinition).addObject(nElem);
			}
		}

		return nDefinition;
	}

	public void _flushCache() {
	}

	public String valueForSQLExpression(EOSQLExpression context) {
		if (context != null)
			return context.sqlStringForAttribute(this);
		if (_definitionArray != null)
			return _definitionArray.valueForSQLExpression(context);

		return name();
	}

	protected EOAttribute(EOEntity entity, String definition) {
		this();
		_name = definition;
		setParent(entity);
		setDefinition(definition);
	}

	public static final int FactoryMethodArgumentIsData = 0;
	public static final int FactoryMethodArgumentIsString = 1;
	public static final int FactoryMethodArgumentIsBytes = 2;
	public static final int FactoryMethodArgumentIsDate = 3;
	public static final int AdaptorNumberType = 0;
	public static final int AdaptorCharactersType = 1;
	public static final int AdaptorBytesType = 2;
	public static final int AdaptorDateType = 3;
	public static final int Void = 0;
	public static final int InParameter = 1;
	public static final int OutParameter = 2;
	public static final int InOutParameter = 3;
	private static final NSSelector _setPrimaryKeyAttributesSelector;
	protected String _name;
	protected EOEntity _parent;
	protected EOStoredProcedure _parentStoredProcedure;
	protected String _prototypeName;
	protected EOAttribute _prototype;
	protected String _columnName;
	protected _EOExpressionArray _definitionArray;
	protected String _externalType;
	protected String _valueType;
	protected String _valueClassName;
	protected String _className;
	protected int _adaptorValueType;
	protected String _valueTypeClassName;
	protected String _readFormat;
	protected String _writeFormat;
	protected TimeZone _serverTimeZone;
	protected int _width;
	protected int _precision;
	protected int _scale;
	protected Class _valueClass;
	int _argumentType;
	protected String _valueFactoryClassName;
	protected String _valueFactoryMethodName;
	protected Class _valueFactoryClass;
	protected String _adaptorValueConversionMethodName;
	protected String _adaptorValueConversionClassName;
	protected Class _adaptorValueConversionClass;
	protected NSSelector _valueFactoryMethod;
	protected NSSelector _adaptorValueConversionMethod;
	protected boolean _flags_allowsNull;
	protected boolean _flags_isReadOnly;
	protected boolean _flags_isNonUpdateable;
	protected boolean _flags_isNonUpdateableInitialized;
	protected NSDictionary _sourceToDestinationKeyMap;
	protected int _parameterDirection;
	protected NSDictionary _userInfo;
	public NSDictionary _internalInfo;
	private static final String CN_JavaString = "java.lang.String";
	private static final String CN_JavaNumber = "java.lang.Number";
	private static final String CN_JavaBigDecimal = "java.math.BigDecimal";
	private static final String CN_JavaNSTimestamp = "com.webobjects.foundation.NSTimestamp";
	private static final String CN_JavaNSData = "com.webobjects.foundation.NSData";
	private static final String CN_NSString = "NSString";
	private static final String CN_NSNumber = "NSNumber";
	private static final String CN_NSDecimalNumber = "NSDecimalNumber";
	private static final String CN_NSCalendarDate = "NSCalendarDate";
	private static final String CN_NSGregorianDate = "NSGregorianDate";
	private static final String CN_NSData = "NSData";
	private static final String CN_NSPrefix = "NS";
	public static final char _VTByte = 'b';
	public static final char _VTShort = 's';
	public static final char _VTInteger = 'i';
	public static final char _VTLong = 'l';
	public static final char _VTFloat = 'f';
	public static final char _VTDouble = 'd';
	public static final char _VTBigDecimal = 'B';
	public static final char _VTBoolean = 'c';
	public static final char _VTDate = 'D';
	public static final char _VTTime = 't';
	public static final char _VTTimestamp = 'T';
	public static final char _VTString = 'S';
	public static final char _VTCharStream = 'C';
	public static final char _VTEncodedBytes = 'E';
	public static final char _VTCharTrimString = 'c';
	public static final char _VTUnknown = ' ';
	public static final char _VTCoerceDate = 'M';
	public static final String FactoryMethodArgumentIsBytesString = "EOFactoryMethodArgumentIsBytes";
	public static final String FactoryMethodArgumentIsStringString = "EOFactoryMethodArgumentIsString";
	public static final String FactoryMethodArgumentIsDataString = "EOFactoryMethodArgumentIsData";
	public static final String FactoryMethodArgumentIsDateString = "EOFactoryMethodArgumentIsDate";
	private Map _overwrittenCharacteristics;
	private static Class valueClasses[];
	private static String valueTypeNames[] = { "Number", "Characters", "Bytes", "Date" };

	static {
		_setPrimaryKeyAttributesSelector = new NSSelector("setPrimaryKeyAttributes", _NSUtilities._ArrayClassArray);
		valueClasses = (new Class[] { Number.class, String.class, NSData.class, NSTimestamp._CLASS });
	}

	public void setChangeNotificationOptions(
			EOChangeNotificationOptions changeNotificationOptions) {
		// AK: method from 5.4.3.1
		
	}

	public EOChangeNotificationOptions changeNotificationOptions() {
		// AK: method from 5.4.3.1
		return null;
	}
}
