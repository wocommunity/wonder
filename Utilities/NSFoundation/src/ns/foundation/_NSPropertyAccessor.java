package ns.foundation;

import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import ns.foundation.NSKeyValueCoding._KeyBinding;
import ns.foundation.NSKeyValueCoding._KeyBindingCreation._KeyBindingFactory;
import ns.foundation.NSKeyValueCoding._KeyBindingCreation._KeyBindingFactory._BindingStorage;

public class _NSPropertyAccessor {
  private static final _KeyBinding  _NotAvailableIndicator = new NSKeyValueCoding._KeyBinding(null, null);
  private static final ConcurrentHashMap<_KeyBinding, _BindingStorage>  _bindingStorageMapTable     = new ConcurrentHashMap<_KeyBinding, _BindingStorage>(256);
  private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

  private static int counter = 0;  
  private ClassPool ctPool;
  private Object targetObject;
  private Class<?> targetClass;
  private CtClass targetCtClass;
  

  public _NSPropertyAccessor(Object object) {
    targetObject = object;
    targetClass = object.getClass();
    ctPool = new ClassPool();
    ctPool.appendSystemPath();
    try {
      targetCtClass = ctPool.getCtClass(targetClass.getName());
    } catch (NotFoundException e) {
      throw new NSForwardException(e);
    }

  }
  
  public static void _flushCaches() {
    _bindingStorageMapTable.clear();
  }
  
  public static boolean _canAccessFieldsDirectlyForClass(Class<?> objectClass) {
    //return _NSReflectionUtilities._staticBooleanMethodValue("canAccessFieldsDirectly", null, null, objectClass, NSKeyValueCoding.class, true);
    return true;
  }

  protected static class _LegacyCompatibleKeyBinding extends _KeyBinding {
    private final _KeyBinding _delegate;
    private Class<?> _valueType;
    
    _LegacyCompatibleKeyBinding(_KeyBinding keyBinding) {
      super(keyBinding.targetClass(), keyBinding.key());
      _delegate = keyBinding;
    }

    @Override
    public boolean isScalarProperty() {
      return _delegate.isScalarProperty();
    }
    
    @Override
    public void setValueInObject(Object value, Object object) {
      _delegate.setValueInObject(value, object);
    }
    
    @Override
    public Class<?> valueType() {
      if (_valueType == null) {
        Class<?> valueType = _delegate.valueType();
        if (valueType.isPrimitive() && _NSUtilities._isClassANumberOrABoolean(valueType)) {
          valueType = _NSUtilities.classObjectForClass(valueType);
        }
        _valueType = valueType;
      }
      return _valueType;
    }
    
    @Override
    public Object valueInObject(Object object) {
      return _delegate.valueInObject(object);
    }
    
    @Override
    public String toString() {
      return _delegate.toString();
    }
  }
  
  public static _KeyBinding _createKeyBindingForKey(Object object, String key, int lookupOrder[], boolean trueForSetAndFalseForGet) {
    _KeyBinding keyBinding = new _NSPropertyAccessor(object)._createKeyBindingForKey(key, lookupOrder, trueForSetAndFalseForGet);
    return keyBinding == null ? null : new _LegacyCompatibleKeyBinding(keyBinding);
  }

  private NSKeyValueCoding._KeyBinding _createKeyBindingForKey(String key, int[] lookupOrder, boolean trueForSetAndFalseForGet) {
    if ((key == null) || (key.length() == 0)) {
      return null;
    }

    Class<?> objectClass = targetObject.getClass();

    boolean canAccessFieldsDirectlyTestPerformed = false;
    boolean canAccessFieldsDirectly = false;

    // we use a KeyBinding as key for the _BindingStorage object map table since it gives us exactly what we need: a class and a key - but we have to create a new lookup key binding to avoid synchronizing the read lookup (and we need a new instance for the write access)
    NSKeyValueCoding._KeyBinding lookupBinding = new NSKeyValueCoding._KeyBinding(objectClass, key);
    _BindingStorage bindingStorage = _bindingStorageMapTable.get(lookupBinding);
    if (bindingStorage == null) {
      bindingStorage = new _KeyBindingFactory._BindingStorage();
      _bindingStorageMapTable.put(lookupBinding, bindingStorage);
    }

    _KeyBindingFactory.Callback keyBindingCreationCallbackObject = (targetObject instanceof _KeyBindingFactory.Callback) ? (_KeyBindingFactory.Callback) targetObject
        : null;
    NSKeyValueCoding._KeyBinding keyBindings[] = (trueForSetAndFalseForGet) ? bindingStorage._keySetBindings : bindingStorage._keyGetBindings;
    for (int i = 0; i < lookupOrder.length; i++) {
      int lookup = lookupOrder[i];
      NSKeyValueCoding._KeyBinding keyBinding = ((lookup >= _KeyBindingFactory.MethodLookup) && (lookup <= _KeyBindingFactory.UnderbarFieldLookup)) ? keyBindings[lookup] : null;
      if (keyBinding == null) {
        Class<?> valueType = null;
        if (trueForSetAndFalseForGet) {
          _KeyBinding getKeyBinding = _createKeyBindingForKey(key, lookupOrder, false);
          valueType = getKeyBinding != null ? getKeyBinding.valueType() : null;
        }

        switch (lookup) {
          case _KeyBindingFactory.MethodLookup:
            String methodName = prefixedKey((trueForSetAndFalseForGet) ? "set" : "get", key);

            if (trueForSetAndFalseForGet) {
              // look up 'setKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeySetBinding(key, methodName) 
                  : _methodKeySetBinding(key, methodName, valueType);
            } else {
              // look up 'getKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, methodName)
                  : _methodKeyGetBinding(key, methodName);

              if (keyBinding == null) {
                // look up 'key'
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, key)
                    : _methodKeyGetBinding(key, key);
              }

              if (keyBinding == null) {
                // look up 'isKey'
                methodName = new String(prefixedKey("is", key));
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, methodName)
                    : _methodKeyGetBinding(key, methodName);
              }
            }
            break;
          case _KeyBindingFactory.UnderbarMethodLookup:
            String underbarMethodName = prefixedKey((trueForSetAndFalseForGet) ? "_set" : "_get", key);

            if (trueForSetAndFalseForGet) {
              // look up '_setKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeySetBinding(key, underbarMethodName)
                  : _methodKeySetBinding(key, underbarMethodName, valueType);
            } else {
              // look up '_getKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName)
                  : _methodKeyGetBinding(key, underbarMethodName);

              if (keyBinding == null) {
                // look up '_key'
                underbarMethodName = prefixedKey("_", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName)
                    : _methodKeyGetBinding(key, underbarMethodName);
              }

              if (keyBinding == null) {
                // look up '_isKey'
                underbarMethodName = prefixedKey("_is", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName)
                    : _methodKeyGetBinding(key, underbarMethodName);
              }
            }
            break;
          case _KeyBindingFactory.FieldLookup:
            if (!canAccessFieldsDirectlyTestPerformed) {
              canAccessFieldsDirectlyTestPerformed = true;
              canAccessFieldsDirectly = _canAccessFieldsDirectlyForClass(objectClass);
            }
            if (canAccessFieldsDirectly) {
              // look up 'key'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, key)
                  : _fieldKeyBinding(key, key);

              if (keyBinding == null) {
                // look up 'isKey'
                String fieldName = prefixedKey("is", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, fieldName)
                    : _fieldKeyBinding(key, fieldName);
              }
            }
            break;
          case _KeyBindingFactory.UnderbarFieldLookup:
            if (!canAccessFieldsDirectlyTestPerformed) {
              canAccessFieldsDirectlyTestPerformed = true;
              canAccessFieldsDirectly = _canAccessFieldsDirectlyForClass(objectClass);
            }
            if (canAccessFieldsDirectly) {
              // look up '_key'
              String underbarFieldName = prefixedKey("_", key);
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, underbarFieldName)
                  : _fieldKeyBinding(key, underbarFieldName);

              if (keyBinding == null) {
                // look up '_isKey'
                underbarFieldName = prefixedKey("_is", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, underbarFieldName)
                    : _fieldKeyBinding(key, underbarFieldName);
              }
            }
            break;
          case _KeyBindingFactory.OtherStorageLookup:
            keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._otherStorageBinding(key) : null;
            break;
        }

        if (keyBinding == null) {
          keyBinding = _NotAvailableIndicator;
        }
        if ((lookup == _KeyBindingFactory.FieldLookup) || (lookup == _KeyBindingFactory.UnderbarFieldLookup)) {
          // set and get bindings are the same for fields (but not for methods since the name of set and get methods are actually different)
          bindingStorage._keySetBindings[lookup] = bindingStorage._keyGetBindings[lookup] = keyBinding;
        } else if ((lookup == _KeyBindingFactory.MethodLookup) || (lookup == _KeyBindingFactory.UnderbarMethodLookup)) {
          keyBindings[lookup] = keyBinding;
        }
      }

      if ((keyBinding != null) && (keyBinding != _NotAvailableIndicator)) {
        return keyBinding;
      }
    }
    return null;
  }

  public NSKeyValueCoding._KeyBinding _fieldKeyBinding(String key, String fieldName) {
    Class<?> objectClass = targetObject.getClass();
    NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(objectClass);
    boolean publicFieldOnly = (valueAccessor == null);

    try {
      CtField field = targetCtClass.getField(fieldName);
      if ((publicFieldOnly && !AccessFlag.isPublic(field.getModifiers()))
          || AccessFlag.isPrivate((field.getModifiers()))) {
        return null;
      } 
      
      CtClass valueType = field.getType();
      CtClass wrapper = _keyBindingClassForMember(key, field);

      CtMethod getter = CtMethod.make("public Object valueInObject(Object object) {" 
          + "return " + box(valueType, unbox(targetCtClass, "object") +  "." + fieldName) + "; }", wrapper);
      
      StringBuffer code = new StringBuffer("public void setValueInObject(Object value, Object object) {"); 
      if (valueType.isPrimitive()) {
        code.append("if (value == null) {");
        code.append(NSKeyValueCoding.Utility.class.getName() + ".unableToSetNullForKey(object,\"" + key + "\");");
        code.append("return; }");
      }
      code.append("((" + field.getDeclaringClass().getName() +")object)." + fieldName + " = "+ convert(valueType, "value") + "; }");
      CtMethod setter = CtMethod.make(code.toString(), wrapper);

      wrapper.addMethod(getter);
      wrapper.addMethod(setter);
      _addMethodsForValueType(wrapper, valueType, valueType.isPrimitive());
      
      @SuppressWarnings("unchecked")
      Class<_KeyBinding> wrapperClass = wrapper.toClass(classLoader, getClass().getProtectionDomain());
      _KeyBinding binding = wrapperClass.newInstance();
      return binding;
    } catch (NotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }
  
  public NSKeyValueCoding._KeyBinding _methodKeyGetBinding(String key, String methodName) {
    Class<?> objectClass = targetObject.getClass();
    NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(objectClass);
    boolean publicMethodOnly = (valueAccessor == null);
    
    try {
      CtMethod method = null;
      for (CtMethod target : targetCtClass.getMethods()) {
        if (!target.getName().equals(methodName)
            || target.getParameterTypes().length != 0) {
          continue;
        }
        if (( publicMethodOnly && !AccessFlag.isPublic(target.getModifiers()))
            || (AccessFlag.isPrivate(target.getModifiers()))) {
          continue;
        }
        method = target;
        break;
      }
      if (method == null) {
        return null;
      }
      CtClass valueType = method.getReturnType();
      CtClass wrapper = _keyBindingClassForMember(key, method);
      CtMethod getter = CtMethod.make("public Object valueInObject(Object object) {" 
          + "return " + box(valueType, unbox(method.getDeclaringClass(), "object") + "." + methodName + "()") + "; }", wrapper);
      
      wrapper.addMethod(getter);
      _addMethodsForValueType(wrapper, valueType, false);

      @SuppressWarnings("unchecked")
      Class<_KeyBinding> wrapperClass = wrapper.toClass(classLoader, getClass().getProtectionDomain());
      _KeyBinding binding = wrapperClass.newInstance();
      return binding;
    } catch (NotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }

  public NSKeyValueCoding._KeyBinding _methodKeySetBinding(String key, String methodName, Class<?> targetValueType) {
    Class<?> objectClass = targetObject.getClass();
    NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(objectClass);
    boolean publicMethodOnly = (valueAccessor == null);
    if (targetValueType == null) {
      targetValueType = Object.class;
    }
    try {
      CtMethod method = null;
      for (CtMethod target : targetCtClass.getMethods()) {
        if (!target.getName().equals(methodName) || target.getParameterTypes().length != 1) {
          continue;
        }

        if ((publicMethodOnly && !AccessFlag.isPublic(target.getModifiers()))
            || AccessFlag.isPrivate(target.getModifiers())) {
          continue;
        }
        CtClass clazz = target.getParameterTypes()[0];
        if (clazz.getName().equals(targetValueType.getName())) {
          method = target;
          break;
        }
        else if (boxedTypeName(clazz).equals(_NSUtilities.classObjectForClass(targetValueType).getName())) {
          method = target;
        } 
        else if (method == null) {
          method = target;
        }
      }
      
      if (method == null) {
        return null;
      }
      CtClass valueType = method.getParameterTypes()[0];
      StringBuffer code = new StringBuffer ("public void setValueInObject(Object value, Object object) {");
      if (valueType.isPrimitive()) {
        code.append("if (value == null) {");
        code.append(NSKeyValueCoding.Utility.class.getName() + ".unableToSetNullForKey(object,\"" + key + "\");");
        code.append("return; }");
      }
      code.append(unbox(method.getDeclaringClass(), "object") + "." + methodName + "("+ convert(valueType, "value") +"); }");
      CtClass wrapper = _keyBindingClassForMember(key, method);
      CtMethod setter = CtMethod.make(code.toString(), wrapper);

      wrapper.addMethod(setter);
      _addMethodsForValueType(wrapper, valueType, valueType.isPrimitive());
      
      @SuppressWarnings("unchecked")
      Class<_KeyBinding> wrapperClass = wrapper.toClass(classLoader, getClass().getProtectionDomain());
      _KeyBinding binding = wrapperClass.newInstance();
      return binding;
    } catch (NotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }
  
  private CtClass _keyBindingClassForMember(String key, CtMember member) {
    try {
      String declaringClassName = member.getDeclaringClass().getName();
      CtClass ctAccessor = ctPool.getCtClass(_KeyBinding.class.getName());
      String wrapperName = declaringClassName + "$" + key + "$KVCWrapper" + counter++; 
      if (targetClass.getPackage().getName().startsWith("java.")) {
        wrapperName = "com.webobjects.kvc." + wrapperName;
      }
      CtClass wrapper = ctPool.makeClass(wrapperName, ctAccessor);
      CtConstructor c = new CtConstructor(new CtClass[0], wrapper);
      c.setBody("super(" + targetCtClass.getName() +".class, \"" + key + "\");");
      wrapper.addConstructor(c);
      return wrapper;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }
  
  private void _addMethodsForValueType(CtClass wrapper, CtClass valueType, boolean isScalar) throws CannotCompileException {
    if (valueType != null) {
      StringBuffer code = new StringBuffer("public Class valueType() {");
      if (valueType.isPrimitive()) {
        code.append("return " + boxedTypeName(valueType) + ".TYPE;");
      } else {
        code.append("return " + valueType.getName() + ".class;");
      }
      code.append("}");
      CtMethod vt = CtMethod.make(code.toString(), wrapper);
      wrapper.addMethod(vt);
      
      code = new StringBuffer("public boolean isScalarProperty() {");
      code.append(isScalar ? "return true;" :"return false;");
      code.append("}");
      CtMethod sp = CtMethod.make(code.toString(), wrapper);
      wrapper.addMethod(sp);
    }
  }
  
  private String box(CtClass type, String arg) {
    boolean isPrimitive = type.isPrimitive();
    if (isPrimitive && (type == CtClass.intType))
      return "(Integer.valueOf((int)" + arg + "))";
    if (isPrimitive && (type == CtClass.longType))
      return "(Long.valueOf((long)" + arg + "))";
    if (isPrimitive && (type == CtClass.shortType))
      return "(Short.valueOf((short)" + arg + "))";
    if (isPrimitive && (type == CtClass.floatType))
      return "(Float.valueOf((float)" + arg + "))";
    if (isPrimitive && (type == CtClass.doubleType))
      return "(Double.valueOf((double)" + arg + "))";
    if (isPrimitive && (type == CtClass.charType))
      return "(Character.valueOf((char)" + arg + "))";
    if (isPrimitive && (type == CtClass.byteType))
      return "(Byte.valueOf((byte)" + arg + "))";
    if (isPrimitive && (type == CtClass.booleanType))
      return "(Boolean.valueOf((boolean)" + arg + "))";
    return "((" + type.getName() + ") " + arg + ")";
  }

  private String unbox(CtClass type, String arg) {
    boolean isPrimitive = type.isPrimitive();
    if (isPrimitive && (type == CtClass.intType))
      return "((Number)" + arg + ").intValue()";
    if (isPrimitive && (type == CtClass.longType))
      return "((Number)" + arg + ").longValue()";
    if (isPrimitive && (type == CtClass.shortType))
      return "((Number)" + arg + ").shortValue()";
    if (isPrimitive && (type == CtClass.floatType))
      return "((Number)" + arg + ").floatValue()";
    if (isPrimitive && (type == CtClass.doubleType))
      return "((Number)" + arg + ").doubleValue()";
    if (isPrimitive && (type == CtClass.charType))
      return "((Character)" + arg + ").charValue()";
    if (isPrimitive && (type == CtClass.byteType))
      return "((Number)" + arg + ").byteValue()";
    if (isPrimitive && (type == CtClass.booleanType))
      return "((Boolean)" + arg + ").booleanValue()";
    return "((" + type.getName() + ") " + arg + ")";
  }
  
  private String convert(CtClass type, String arg) {
    String converted = "(" + _NSPropertyUtilities.class.getName() 
          + ".convertObjectIntoCompatibleValue(" + arg + "," + boxedTypeName(type) + ".class" + "))";
    boolean isPrimitive = type.isPrimitive();
    if (isPrimitive && (type == CtClass.intType))
      return "((Number)" + converted + ").intValue()";
    if (isPrimitive && (type == CtClass.longType))
      return "((Number)" + converted + ").longValue()";
    if (isPrimitive && (type == CtClass.shortType))
      return "((Number)" + converted + ").shortValue()";
    if (isPrimitive && (type == CtClass.floatType))
      return "((Number)" + converted + ").floatValue()";
    if (isPrimitive && (type == CtClass.doubleType))
      return "((Number)" + converted + ").doubleValue()";
    if (isPrimitive && (type == CtClass.charType))
      return "((Character)" + converted + ").charValue()";
    if (isPrimitive && (type == CtClass.byteType))
      return "((Number)" + converted + ").byteValue()";
    if (isPrimitive && (type == CtClass.booleanType))
      return "((Boolean)" + converted + ").booleanValue()";
    if (_NSPropertyUtilities._isCtClassABoolean(type))
      return "((Boolean)" + converted + ")";
    if (_NSPropertyUtilities._isCtClassANumber(type))
      return "((Number)" + converted + ")";
    return "((" + type.getName() + ") " + converted + ")";
  }
  
  private String boxedTypeName(CtClass type) {
    if (type.isPrimitive())
      return ((CtPrimitiveType)type).getWrapperName();
    return type.getName();
  }
  
  private String prefixedKey(String prefix, String key) {
    if (prefix == null) {
      return key;
    }
    StringBuffer sb = new StringBuffer(prefix.length() + key.length());
    sb.append(prefix);
    if ("_".equals(prefix)) {
      return sb.append(key).toString();
    }
    return sb.append(_NSStringUtilities.capitalizedString(key)).toString();
  }
}
