package ognl.helperfunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCoding.ValueAccessor;
import com.webobjects.foundation.NSKeyValueCoding._BooleanFieldBinding;
import com.webobjects.foundation.NSKeyValueCoding._BooleanMethodBinding;
import com.webobjects.foundation.NSKeyValueCoding._FieldBinding;
import com.webobjects.foundation.NSKeyValueCoding._KeyBinding;
import com.webobjects.foundation.NSKeyValueCoding._MethodBinding;
import com.webobjects.foundation.NSKeyValueCoding._NumberFieldBinding;
import com.webobjects.foundation.NSKeyValueCoding._NumberMethodBinding;
import com.webobjects.foundation.NSKeyValueCoding._ReflectionKeyBindingCreation.Callback;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation._NSReflectionUtilities;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;
import com.webobjects.foundation._NSThreadsafeMutableSet;
import com.webobjects.foundation._NSUtilities;

/**
 * WOHelperFunctionClassKeyValueCoding is basically just like NSKVC except that
 * it operates ONLY on Classes, and not on Objects.  This is required if you have
 * a keypath with evaluates to null, and you need to figure out the type of the
 * Helper to pass the null value to.
 * 
 * @author mschrag
 */
public class WOHelperFunctionClassKeyValueCoding {
	public static class _ReflectionKeyBindingCreation {
		public static _KeyBinding _NotAvailableIndicator = new _KeyBinding(null, null);
		private static final _NSThreadsafeMutableDictionary _bindingStorageMapTable = new _NSThreadsafeMutableDictionary(new NSMutableDictionary(256));
		public static final int _ValueForKeyLookupOrder[] = { 0, 1, 3, 2, 4 };
		public static final int _StoredValueForKeyLookupOrder[] = { 1, 3, 2, 4, 0 };

		private static class _BindingStorage {
			_KeyBinding _keyGetBindings[];
			_KeyBinding _keySetBindings[];

			public _BindingStorage() {

				_keyGetBindings = new _KeyBinding[4];
				_keySetBindings = new _KeyBinding[4];
			}
		}

		public static _KeyBinding _fieldKeyBinding(Class objectClass, String key, String fieldName) {
			ValueAccessor valueAccessor = ValueAccessor._valueAccessorForClass(objectClass);
			boolean publicFieldOnly = valueAccessor == null;
			Field field = _NSReflectionUtilities._fieldForClass(objectClass, fieldName, publicFieldOnly);
			if (field != null) {
				Class valueClass = _NSUtilities.classObjectForClass(field.getType());
				if (_NSUtilities._isClassANumber(valueClass)) {
					return new _NumberFieldBinding(objectClass, key, field, valueClass, valueAccessor);
				}
				if (_NSUtilities._isClassABoolean(valueClass)) {
					return new _BooleanFieldBinding(objectClass, key, field, valueAccessor);
				}
				return new _FieldBinding(objectClass, key, field, valueAccessor);
			}
			return null;
		}

		  private static final Class _noArgumentTypes[] = new Class[0];

		  // MS: slurped in from _NSReflectionUtilities because the original won't return a typed keypath for an abstract method, which
		  // means you don't get type information when you bind a helper function to an interface method
		  public static Method _methodForClass(Class objectClass, String methodName, Class argumentTypes[], boolean publicMethodOnly) {
			  Method method = null;
			  if (publicMethodOnly) {
				  try {
					  method = objectClass.getMethod(methodName, argumentTypes == null ? _noArgumentTypes : argumentTypes);
				  }
				  catch (NoSuchMethodException exception) {
					  NSLog._conditionallyLogPrivateException(exception);
				  }
				  catch (SecurityException exception) {
					  NSLog._conditionallyLogPrivateException(exception);
					  method = null;
				  }
		    }
		    else {
		    	do {
		    		if (objectClass == _NSUtilities._ObjectClass || method != null) {
		    			break;
		    		}
		    		try {
		    			method = objectClass.getDeclaredMethod(methodName, argumentTypes == null ? _noArgumentTypes : argumentTypes);
		    		}
		    		catch (NoSuchMethodException exception) {
		    			NSLog._conditionallyLogPrivateException(exception);
		    		}
		    		catch (SecurityException exception) {
		    			NSLog._conditionallyLogPrivateException(exception);
		    			method = null;
		    		}
		    		if (method == null) {
		    			objectClass = objectClass.getSuperclass();
		    		}
		    	} while (true);
		    }
			  if (method != null) {
				  int modifiers = method.getModifiers();
				  if (Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers)/* || Modifier.isAbstract(modifiers)*/) {
					  return null;
				  }
			  }
			  return method;
		  }
	
		public static _KeyBinding _methodKeyGetBinding(Class objectClass, String key, String methodName) {
			ValueAccessor valueAccessor = ValueAccessor._valueAccessorForClass(objectClass);
			boolean publicMethodOnly = valueAccessor == null;
			Method method = WOHelperFunctionClassKeyValueCoding._ReflectionKeyBindingCreation._methodForClass(objectClass, methodName, null, publicMethodOnly);
			if (method != null) {
				Class valueClass = _NSUtilities.classObjectForClass(method.getReturnType());
				if (_NSUtilities._isClassANumber(valueClass)) {
					return new _NumberMethodBinding(objectClass, key, method, valueClass, valueAccessor);
				}
				if (_NSUtilities._isClassABoolean(valueClass)) {
					return new _BooleanMethodBinding(objectClass, key, method, valueAccessor);
				}
				return new _MethodBinding(objectClass, key, method, valueAccessor);
			}
			return null;
		}

		public static _KeyBinding _methodKeySetBinding(Class objectClass, String key, String methodName) {
			ValueAccessor valueAccessor = ValueAccessor._valueAccessorForClass(objectClass);
			boolean publicMethodOnly = valueAccessor == null;
			Method method = _NSReflectionUtilities._methodWithOneArgumentOfUnknownType(objectClass, methodName, key, publicMethodOnly, true, null, true);
			if (method != null) {
				Class valueClass = _NSUtilities.classObjectForClass(method.getParameterTypes()[0]);
				if (_NSUtilities._isClassANumber(valueClass)) {
					return new _NumberMethodBinding(objectClass, key, method, valueClass, valueAccessor);
				}
				if (_NSUtilities._isClassABoolean(valueClass)) {
					return new _BooleanMethodBinding(objectClass, key, method, valueAccessor);
				}
				return new _MethodBinding(objectClass, key, method, valueAccessor);
			}
			return null;
		}

		private static _KeyBinding _createKeyBindingForKey(Class objectClass, String key, int lookupOrder[], boolean trueForSetAndFalseForGet) {
			if (key == null || key.length() == 0) {
				return null;
			}
			boolean canAccessFieldsDirectlyTestPerformed = false;
			boolean canAccessFieldsDirectly = false;
			_KeyBinding lookupBinding = new _KeyBinding(objectClass, key);
			_BindingStorage bindingStorage = (_BindingStorage) _bindingStorageMapTable.objectForKey(lookupBinding);
			if (bindingStorage == null) {
				bindingStorage = new _BindingStorage();
				_bindingStorageMapTable.setObjectForKey(bindingStorage, lookupBinding);
			}
			// MS: We just can't support callbacks without the original object
			// ... I think this is PROBABLY OK for our purposes.
			Callback keyBindingCreationCallbackObject = null;
			// (object instanceof Callback) ? (Callback) object : null;
			_KeyBinding keyBindings[] = trueForSetAndFalseForGet ? bindingStorage._keySetBindings : bindingStorage._keyGetBindings;
			for (int i = 0; i < lookupOrder.length; i++) {
				int lookup = lookupOrder[i];
				_KeyBinding keyBinding = lookup < 0 || lookup > 3 ? null : keyBindings[lookup];
				if (keyBinding == null) {
					switch (lookup) {
					case 0:

						StringBuilder methodNameBuffer = new StringBuilder(key.length() + 3);

						methodNameBuffer.append(trueForSetAndFalseForGet ? "set" : "get");

						methodNameBuffer.append(Character.toUpperCase(key.charAt(0)));

						methodNameBuffer.append(key.substring(1));

						String methodName = methodNameBuffer.toString();

						if (trueForSetAndFalseForGet) {
							keyBinding = keyBindingCreationCallbackObject == null ? _methodKeySetBinding(objectClass, key, methodName) : keyBindingCreationCallbackObject._methodKeySetBinding(key, methodName);
						}
						else {
							keyBinding = keyBindingCreationCallbackObject == null ? _methodKeyGetBinding(objectClass, key, methodName) : keyBindingCreationCallbackObject._methodKeyGetBinding(key, methodName);
							if (keyBinding == null) {
								keyBinding = keyBindingCreationCallbackObject == null ? _methodKeyGetBinding(objectClass, key, key) : keyBindingCreationCallbackObject._methodKeyGetBinding(key, key);
							}
							if (keyBinding == null) {
								methodNameBuffer.setLength(0);
								methodNameBuffer.append("is");
								methodNameBuffer.append(Character.toUpperCase(key.charAt(0)));
								methodNameBuffer.append(key.substring(1));
								methodName = methodNameBuffer.toString();
								keyBinding = keyBindingCreationCallbackObject == null ? _methodKeyGetBinding(objectClass, key, methodName) : keyBindingCreationCallbackObject._methodKeyGetBinding(key, methodName);
							}
						}
						break;
					case 1:

						StringBuilder underbarMethodNameBuffer = new StringBuilder(key.length() + 4);

						underbarMethodNameBuffer.append(trueForSetAndFalseForGet ? "_set" : "_get");

						underbarMethodNameBuffer.append(Character.toUpperCase(key.charAt(0)));

						underbarMethodNameBuffer.append(key.substring(1));

						String underbarMethodName = underbarMethodNameBuffer.toString();

						if (trueForSetAndFalseForGet) {
							keyBinding = keyBindingCreationCallbackObject == null ? _methodKeySetBinding(objectClass, key, underbarMethodName) : keyBindingCreationCallbackObject._methodKeySetBinding(key, underbarMethodName);
						}
						else {
							keyBinding = keyBindingCreationCallbackObject == null ? _methodKeyGetBinding(objectClass, key, underbarMethodName) : keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName);
							if (keyBinding == null) {
								underbarMethodNameBuffer.setLength(0);
								underbarMethodNameBuffer.append('_');
								underbarMethodNameBuffer.append(key);
								underbarMethodName = underbarMethodNameBuffer.toString();
								keyBinding = keyBindingCreationCallbackObject == null ? _methodKeyGetBinding(objectClass, key, underbarMethodName) : keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName);
							}
							if (keyBinding == null) {
								underbarMethodNameBuffer.setLength(0);
								underbarMethodNameBuffer.append("_is");
								underbarMethodNameBuffer.append(Character.toUpperCase(key.charAt(0)));
								underbarMethodNameBuffer.append(key.substring(1));
								underbarMethodName = underbarMethodNameBuffer.toString();
								keyBinding = keyBindingCreationCallbackObject == null ? _methodKeyGetBinding(objectClass, key, underbarMethodName) : keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName);
							}
						}
						break;
					case 2:

						if (!canAccessFieldsDirectlyTestPerformed) {
							canAccessFieldsDirectlyTestPerformed = true;
							canAccessFieldsDirectly = NSKeyValueCoding._ReflectionKeyBindingCreation._canAccessFieldsDirectlyForClass(objectClass);
						}

						if (canAccessFieldsDirectly) {
							keyBinding = keyBindingCreationCallbackObject == null ? _fieldKeyBinding(objectClass, key, key) : keyBindingCreationCallbackObject._fieldKeyBinding(key, key);
							if (keyBinding == null) {
								StringBuilder fieldNameBuffer = new StringBuilder(key.length() + 2);
								fieldNameBuffer.append("is");
								fieldNameBuffer.append(Character.toUpperCase(key.charAt(0)));
								fieldNameBuffer.append(key.substring(1));
								String fieldName = fieldNameBuffer.toString();
								keyBinding = keyBindingCreationCallbackObject == null ? _fieldKeyBinding(objectClass, key, fieldName) : keyBindingCreationCallbackObject._fieldKeyBinding(key, fieldName);
							}
						}
						break;
					case 3:

						if (!canAccessFieldsDirectlyTestPerformed) {
							canAccessFieldsDirectlyTestPerformed = true;
							canAccessFieldsDirectly = NSKeyValueCoding._ReflectionKeyBindingCreation._canAccessFieldsDirectlyForClass(objectClass);
						}

						if (canAccessFieldsDirectly) {
							StringBuilder underbarFieldNameBuffer = new StringBuilder(key.length() + 3);
							underbarFieldNameBuffer.append('_');
							underbarFieldNameBuffer.append(key);
							String underbarFieldName = underbarFieldNameBuffer.toString();
							keyBinding = keyBindingCreationCallbackObject == null ? _fieldKeyBinding(objectClass, key, underbarFieldName) : keyBindingCreationCallbackObject._fieldKeyBinding(key, underbarFieldName);
							if (keyBinding == null) {
								underbarFieldNameBuffer.setLength(0);
								underbarFieldNameBuffer.append("_is");
								underbarFieldNameBuffer.append(Character.toUpperCase(key.charAt(0)));
								underbarFieldNameBuffer.append(key.substring(1));
								underbarFieldName = underbarFieldNameBuffer.toString();
								keyBinding = keyBindingCreationCallbackObject == null ? _fieldKeyBinding(objectClass, key, underbarFieldName) : keyBindingCreationCallbackObject._fieldKeyBinding(key, underbarFieldName);
							}
						}
						break;
					case 4:

						keyBinding = keyBindingCreationCallbackObject == null ? null : keyBindingCreationCallbackObject._otherStorageBinding(key);
						break;
					}
					if (keyBinding == null) {
						keyBinding = _NotAvailableIndicator;
					}
					if (lookup == 2 || lookup == 3) {
						bindingStorage._keySetBindings[lookup] = bindingStorage._keyGetBindings[lookup] = keyBinding;
					}
					else if (lookup == 0 || lookup == 1) {
						keyBindings[lookup] = keyBinding;
					}
				}
				if (keyBinding != null && keyBinding != _NotAvailableIndicator) {
					return keyBinding;
				}
			}
			return null;
		}

		public static _KeyBinding _createKeyGetBindingForKey(Class objectClass, String key, int lookupOrder[]) {
			return _createKeyBindingForKey(objectClass, key, lookupOrder, false);
		}

		public static _KeyBinding _createKeySetBindingForKey(Class objectClass, String key, int lookupOrder[]) {
			return _createKeyBindingForKey(objectClass, key, lookupOrder, true);
		}
	}

	public static class DefaultImplementation {
		private static final _NSThreadsafeMutableSet _keyGetBindings = new _NSThreadsafeMutableSet(new NSMutableSet(256));
		private static final _NSThreadsafeMutableSet _keySetBindings = new _NSThreadsafeMutableSet(new NSMutableSet(256));

		public static void _flushCaches() {
			_keyGetBindings.removeAllObjects();
			_keySetBindings.removeAllObjects();
		}

		public static _KeyBinding _keyGetBindingForKey(Class objectClass, String key) {
			_KeyBinding keyBinding = (_KeyBinding) _keyGetBindings.member(new _KeyBinding(objectClass, key));
			if (keyBinding == null) {
				//keyBinding = (object instanceof _KeyBindingCreation) ? ((_KeyBindingCreation) object)._createKeyGetBindingForKey(key) : _createKeyGetBindingForKey(objectClass, key);
				keyBinding = _createKeyGetBindingForKey(objectClass, key);
				if (keyBinding == null) {
					keyBinding = new _KeyBinding(objectClass, key);
				}
				_keyGetBindings.addObject(keyBinding);
			}
			return keyBinding;
		}

		public static _KeyBinding _keySetBindingForKey(Class objectClass, String key) {
			_KeyBinding keyBinding = (_KeyBinding) _keySetBindings.member(new _KeyBinding(objectClass, key));
			if (keyBinding == null) {
				//keyBinding = (object instanceof _KeyBindingCreation) ? ((_KeyBindingCreation) object)._createKeySetBindingForKey(key) : _createKeySetBindingForKey(objectClass, key);
				keyBinding = _createKeySetBindingForKey(objectClass, key);
				if (keyBinding == null) {
					keyBinding = new _KeyBinding(objectClass, key);
				}
				_keySetBindings.addObject(keyBinding);
			}
			return keyBinding;
		}

		public static _KeyBinding _createKeyGetBindingForKey(Class objectClass, String key) {
			_KeyBinding keyBinding = _ReflectionKeyBindingCreation._createKeyGetBindingForKey(objectClass, key, _ReflectionKeyBindingCreation._ValueForKeyLookupOrder);
			return keyBinding;
		}

		public static _KeyBinding _createKeySetBindingForKey(Class objectClass, String key) {
			return _ReflectionKeyBindingCreation._createKeySetBindingForKey(objectClass, key, _ReflectionKeyBindingCreation._ValueForKeyLookupOrder);
		}

		public static _KeyBinding keyGetBindingForKeyPath(Class objectClass, String keyPath) {
			if (keyPath == null) {
				return null;
			}

			int index = keyPath.indexOf('.');
			if (index < 0) {
				return WOHelperFunctionClassKeyValueCoding.DefaultImplementation._keyGetBindingForKey(objectClass, keyPath);
			}
			String key = keyPath.substring(0, index);
			_KeyBinding keyBinding = WOHelperFunctionClassKeyValueCoding.DefaultImplementation._keyGetBindingForKey(objectClass, key);
			return keyBinding != null ? WOHelperFunctionClassKeyValueCoding.DefaultImplementation.keyGetBindingForKeyPath(keyBinding.valueType(), keyPath.substring(index + 1)) : null;
		}

		DefaultImplementation() {
			throw new IllegalStateException("Cannot instantiate an instance of class " + getClass().getName());
		}
	}
}
