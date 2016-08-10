package com.webobjects.eocontrol;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSReflectionUtilities;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;
import com.webobjects.foundation._NSUtilities;

/**
 * Re-implemented to remove broken serialization of classDescription
 */
public class EOGenericRecord extends EOCustomObject {
	public static final Class<?> _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.eocontrol.EOGenericRecord");
	private static final long serialVersionUID = 1L;
	transient NSMutableDictionary __dictionary;

	@Deprecated
	public EOGenericRecord(EOEditingContext editingContext, EOClassDescription classDescription, EOGlobalID gid) {
		super(editingContext, classDescription, gid);
		__setClassDescription(classDescription);
	}

	public EOGenericRecord(EOClassDescription classDescription) {
		__setClassDescription(classDescription);
	}

	public EOGenericRecord() {
		__setClassDescription();
	}

	public static boolean usesDeferredFaultCreation() {
		return true;
	}

	private void __setClassDescription() {
		EOClassDescription cd = EOClassDescription.classDescriptionForClass(getClass());
		if (cd == null) {
			throw new IllegalStateException("Unabled to find an EOClassDescription for objects of " + getClass());
		}
		__setClassDescription(cd);
	}

	@Override
	public final void __setClassDescription(EOClassDescription classDescription) {
		if (classDescription == null) {
			throw new IllegalArgumentException("A class description of a generic record cannot be null");
		}
		__classDescription = classDescription;
		__dictionary = __classDescription._newDictionaryForProperties();
	}

	@Override
	public EOClassDescription classDescription() {
		EOClassDescription cd = __classDescription();
		if (cd == null) {
			__setClassDescription();
		}
		return __classDescription();
	}

	@Override
	public NSKeyValueCoding._KeyBinding _otherStorageBinding(String key) {
		boolean lazyBindingNeeded = false;
		EOClassDescription classDescription = classDescription();
		Class<?> objectClass = getClass();
		NSArray<String> cdAttributes = classDescription != null ? classDescription.attributeKeys() : null;

		if ((_usesDeferredFaultCreationForClass(objectClass)) && (cdAttributes != null)
				&& (!cdAttributes.containsObject(key))) {
			lazyBindingNeeded = true;
		}

		Class<?> enforcedNumberOrBooleanClass = null;
		Class<?> inferredValueClass = null;
		if (classDescription != null) {
			inferredValueClass = classDescription._enforcedKVCNumberClassForKey(key);
			if ((inferredValueClass != null) && (_NSUtilities._isClassANumberOrABoolean(inferredValueClass))) {
				enforcedNumberOrBooleanClass = inferredValueClass;
			}
		}

		if (inferredValueClass == null) {
			inferredValueClass = _NSReflectionUtilities._inferredValueClassForKey(objectClass, key, true);
			if ((inferredValueClass != null) && (_NSUtilities._isClassANumberOrABoolean(inferredValueClass))) {
				enforcedNumberOrBooleanClass = inferredValueClass;
			}
		}

		if ((__dictionary instanceof _EOMutableKnownKeyDictionary)) {
			NSKeyValueCoding._KeyBinding keyBinding = ((_EOMutableKnownKeyDictionary) __dictionary).initializer()
					._genericRecordKeyBindingForKey(key, lazyBindingNeeded, enforcedNumberOrBooleanClass);
			if (keyBinding != null) {
				return keyBinding;
			}
		}

		if (((cdAttributes != null) && (cdAttributes.containsObject(key)))
				|| ((classDescription != null) && ((classDescription.toOneRelationshipKeys().containsObject(key)) || (classDescription
						.toManyRelationshipKeys().containsObject(key))))) {
			return lazyBindingNeeded ? new _LazyDictionaryBinding(key, enforcedNumberOrBooleanClass)
					: new _DictionaryBinding(key, enforcedNumberOrBooleanClass);
		}
		return new NSKeyValueCoding._KeyBinding(null, key);
	}

	@Override
	public NSKeyValueCoding._KeyBinding _keyGetBindingForKey(String key) {
		Class<?> objectClass = getClass();
		_NSThreadsafeMutableDictionary mapTable = classDescription()._kvcMapForClass(objectClass)._getBindings;
		NSKeyValueCoding._KeyBinding keyBinding = (NSKeyValueCoding._KeyBinding) mapTable.objectForKey(key);
		if (keyBinding == null) {
			keyBinding = _createKeyGetBindingForKey(key);
			mapTable.setObjectForKey(keyBinding != null ? keyBinding : new NSKeyValueCoding._KeyBinding(objectClass,
					key), key);
		}
		return keyBinding;
	}

	@Override
	public NSKeyValueCoding._KeyBinding _keySetBindingForKey(String key) {
		Class<?> objectClass = getClass();
		_NSThreadsafeMutableDictionary mapTable = classDescription()._kvcMapForClass(objectClass)._setBindings;
		NSKeyValueCoding._KeyBinding keyBinding = (NSKeyValueCoding._KeyBinding) mapTable.objectForKey(key);
		if (keyBinding == null) {
			keyBinding = _createKeySetBindingForKey(key);
			mapTable.setObjectForKey(keyBinding != null ? keyBinding : new NSKeyValueCoding._KeyBinding(objectClass,
					key), key);
		}
		return keyBinding;
	}

	@Override
	public NSKeyValueCoding._KeyBinding _storedKeyGetBindingForKey(String key) {
		Class<?> objectClass = getClass();
		_NSThreadsafeMutableDictionary mapTable = classDescription()._kvcMapForClass(objectClass)._storedGetBindings;
		NSKeyValueCoding._KeyBinding keyBinding = (NSKeyValueCoding._KeyBinding) mapTable.objectForKey(key);
		if (keyBinding == null) {
			keyBinding = _createStoredKeyGetBindingForKey(key);
			mapTable.setObjectForKey(keyBinding != null ? keyBinding : new NSKeyValueCoding._KeyBinding(objectClass,
					key), key);
		}
		return keyBinding;
	}

	@Override
	public NSKeyValueCoding._KeyBinding _storedKeySetBindingForKey(String key) {
		Class<?> objectClass = getClass();
		_NSThreadsafeMutableDictionary mapTable = classDescription()._kvcMapForClass(objectClass)._storedSetBindings;
		NSKeyValueCoding._KeyBinding keyBinding = (NSKeyValueCoding._KeyBinding) mapTable.objectForKey(key);
		if (keyBinding == null) {
			keyBinding = _createStoredKeySetBindingForKey(key);
			mapTable.setObjectForKey(keyBinding != null ? keyBinding : new NSKeyValueCoding._KeyBinding(objectClass,
					key), key);
		}
		return keyBinding;
	}

	public static class _LazyDictionaryBinding extends EOGenericRecord._DictionaryBinding {
		public _LazyDictionaryBinding(String key) {
			super(key);
		}

		public _LazyDictionaryBinding(String key, Class<?> enforcedNumberOrBooleanClass) {
			super(key, enforcedNumberOrBooleanClass);
		}

		@Override
		public Object valueInObject(Object object) {
			Object value = super.valueInObject(object);
			return value != null ? ((EOGenericRecord) object).willReadRelationship(value) : null;
		}
	}

	public static class _DictionaryBinding extends NSKeyValueCoding._KeyBinding {
		private Class<?> _enforcedNumberOrBooleanClass;

		public _DictionaryBinding(String key) {
			this(key, null);
		}

		public _DictionaryBinding(String key, Class<?> enforcedNumberOrBooleanClass) {
			super(null, key);
			_enforcedNumberOrBooleanClass = enforcedNumberOrBooleanClass;
		}

		@Override
		public Object valueInObject(Object object) {
			EOGenericRecord genericRecord = (EOGenericRecord) object;
			genericRecord.willRead();

			Object value = genericRecord.__dictionary.objectForKey(_key);
			return value == NSKeyValueCoding.NullValue ? null : value;
		}

		@Override
		public void setValueInObject(Object value, Object object) {
			EOGenericRecord genericRecord = (EOGenericRecord) object;
			genericRecord.willChange();

			Object convertedValue = value;
			if (convertedValue == null) {
				convertedValue = NSKeyValueCoding.NullValue;
			} else if (_enforcedNumberOrBooleanClass != null) {
				try {
					convertedValue = _NSUtilities.convertNumberOrBooleanIntoCompatibleValue(convertedValue,
							_enforcedNumberOrBooleanClass);
				} catch (ClassCastException exception) {
					NSLog._conditionallyLogPrivateException(exception);
				}

			}

			genericRecord.__dictionary.setObjectForKey(convertedValue, _key);
		}
	}
}