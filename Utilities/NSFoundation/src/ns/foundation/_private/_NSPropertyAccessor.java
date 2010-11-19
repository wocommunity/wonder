package ns.foundation._private;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import ns.foundation.NSKeyValueCoding;
import ns.foundation.NSKeyValueCoding.ValueAccessor;
import ns.foundation.NSKeyValueCoding._KeyBinding;
import ns.foundation.NSKeyValueCoding._KeyBindingCreation.Callback;
import ns.foundation.NSLog;
import ns.foundation._NSStringUtilities;
import ns.foundation._NSUtilities;

public class _NSPropertyAccessor {
  private static final String[] FIELD_SEARCH_PREFIX = new String[] { "_", "_is", null, "is" };
  private static final String[] METHOD_GET_SEARCH_PREFIX = new String[] { "get", null, "is", "_get", "_", "_is" };
  private static final String[] METHOD_SET_SEARCH_PREFIX = new String[] { "set", null, "is", "_set", "_", "_is" };
  private static Map<Class<?>, Map<String, _KeyBinding>> _keyBindingCache = new ConcurrentHashMap<Class<?>, Map<String,_KeyBinding>>();
  private static ClassPool ctPool;
  private Object targetObject;
  private Class<?> targetClass;
  
  static {
    ctPool = ClassPool.getDefault();
  }
  
  public _NSPropertyAccessor(Object object) {
    targetObject = object;
    targetClass = object.getClass();
  }
  
  public _KeyBinding bindingForKey(String key, int[] lookupOrder, boolean trueForSetAndFalseForGet) {
    _KeyBinding keyBinding = null;
    if (_keyBindingCache.containsKey(targetClass)) {
      keyBinding = _keyBindingCache.get(targetClass).get(key);
    }
    if (keyBinding == null) {
      keyBinding = _createKeyBindingForKey(key, lookupOrder, trueForSetAndFalseForGet);
    }
    return keyBinding;
  }

  public static _KeyBinding _createKeyBindingForKey(Object object, String key, int lookupOrder[], boolean trueForSetAndFalseForGet) {
    return new _NSPropertyAccessor(object).bindingForKey(key, lookupOrder, trueForSetAndFalseForGet);
  }

  @SuppressWarnings("unchecked")
  private synchronized _KeyBinding _createKeyBindingForKey(String key, int lookupOrder[], boolean trueForSetAndFalseForGet) {
    
    if(key == null || key.length() == 0)
      return null;
    
    Callback keyBindingCreationCallbackObject = (targetObject instanceof Callback) ? (Callback)targetObject : null;
    ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(targetClass);
    boolean publicMethodOnly = valueAccessor == null;

    try {
      String targetClassName = targetClass.getName();
      CtClass ctAccessor = ctPool.getCtClass(_KeyBinding.class.getName());
      CtClass wrapper = ctPool.makeClass(targetClassName + "$" + key + "$KVCWrapper");
      if (targetClass.getPackage().getName().startsWith("java.")) {
        wrapper.setName("ns" + wrapper.getName());
      }
      wrapper.setSuperclass(ctAccessor);
      CtConstructor c = new CtConstructor(new CtClass[0], wrapper);
      c.setBody("super(" + targetClassName +".class, \"" + key + "\");");
      wrapper.addConstructor(c);
      
      /* Generate getter */
      CtMethod getter = null;
      CtClass valueType = null;
      
      CtClass ctclass = ctPool.getCtClass(targetClass.getName());

      
      for (String prefix : METHOD_GET_SEARCH_PREFIX) {
        String target = null;

        for (CtMethod method : ctclass.getMethods()) {
          if (method.getParameterTypes().length != 0
              || ( publicMethodOnly && !AccessFlag.isPublic(method.getModifiers()))
              || (AccessFlag.isPrivate(method.getModifiers()))) {
            continue;
          }
          if (method.getName().equals(prefixedKey(prefix, key))) {
            target = method.getName();
            valueType = method.getReturnType();
            break;
          }
        }
        if (target != null) {
          _KeyBinding keyBinding = null;
          if(trueForSetAndFalseForGet) {
            keyBinding = keyBindingCreationCallbackObject == null ? null : keyBindingCreationCallbackObject._methodKeySetBinding(key, target);
          } else {
            keyBinding = keyBindingCreationCallbackObject == null ? null : keyBindingCreationCallbackObject._methodKeyGetBinding(key, target);
          }
          if (keyBinding != null) {
            return keyBinding;
          }
          
          getter = CtMethod.make("public Object valueInObject(Object object) {" 
              + "return " + box(valueType, unbox(ctclass, "object") + "." + target + "()") + "; }", wrapper);
          break;
        }
      }
      
      if (getter == null) {
        for (String prefix : FIELD_SEARCH_PREFIX) {
          String target = null;

          for (CtField field : ctclass.getFields()) {
            if ((publicMethodOnly && !AccessFlag.isPublic(field.getModifiers())
                || AccessFlag.isPrivate(field.getModifiers()))) {
              continue;
            }
            if (field.getName().equals(prefixedKey(prefix, key))) {
              target = field.getName();
              valueType = field.getType();
              break;
            }
          }

          if (target != null) {
            _KeyBinding keyBinding = null;
            if(trueForSetAndFalseForGet) {
              keyBinding = keyBindingCreationCallbackObject == null ? null : keyBindingCreationCallbackObject._fieldKeyBinding(key, target);
            } else {
              keyBinding = keyBindingCreationCallbackObject == null ? null : keyBindingCreationCallbackObject._fieldKeyBinding(key, target);
            }
            if (keyBinding != null) {
              //return keyBinding;
            }
            getter = CtMethod.make("public Object valueInObject(Object object) {" 
                + "return " + box(valueType, unbox(ctclass, "object") +  "." + target) + "; }", wrapper);
            break;
          }
        }
      }
     
      /* Generate setter */
      CtMethod setter = null;

      for (String prefix : METHOD_SET_SEARCH_PREFIX) {
        String target = null;

        for (CtMethod method : ctclass.getMethods()) {
          if (method.getParameterTypes().length != 1) {
            continue;
          }

          CtClass currentValueType = method.getParameterTypes()[0];
          if ((valueType != null && !currentValueType.equals(valueType)) 
              || (publicMethodOnly && !AccessFlag.isPublic(method.getModifiers()))
              || AccessFlag.isPrivate(method.getModifiers())) {
            continue;
          }

          if (method.getName().equals(prefixedKey(prefix, key))) {
            target = method.getName();
            break;
          }
        }

        if (target != null) {
          StringBuffer code = new StringBuffer ("public void setValueInObject(Object value, Object object) {");
          if (valueType.isPrimitive()) {
            code.append("if (value == null) {");
            code.append(NSKeyValueCoding.Utility.class.getName() + ".unableToSetNullForKey(object,\"" + target + "\");");
            code.append("return; }");
          }
          code.append(unbox(ctclass, "object") + "." + target + "("+ convert(valueType, "value") +"); }");
          setter = CtMethod.make(code.toString(), wrapper);
          break;
        }
      }
      if (setter == null) {
        for (String prefix : FIELD_SEARCH_PREFIX) {
          String target = null;
          for (CtField field : ctclass.getFields()) {
            if ((publicMethodOnly && !AccessFlag.isPublic(field.getModifiers()))
                || AccessFlag.isPrivate((field.getModifiers()))) {
              continue;
            }

            if (field.getName().equals(prefixedKey(prefix, key))) {
              target = field.getName();
              break;
            }
          }

          if (target != null) {
            StringBuffer code = new StringBuffer("public void setValueInObject(Object value, Object object) {"); 
            if (valueType.isPrimitive()) {
              code.append("if (value == null) {");
              code.append(NSKeyValueCoding.Utility.class.getName() + ".unableToSetNullForKey(object,\"" + target + "\");");
              code.append("return; }");
            }
            code.append("((" + targetClass.getName() +")object)." + target + " = "+ convert(valueType, "value") + "; }");
            setter = CtMethod.make(code.toString(), wrapper);
            break;
          }
        }
      }
      
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
        if (valueType.isPrimitive()) {
          code.append("return true;");
        } else {
          code.append("return false;");
        }
        code.append("}");
        CtMethod sp = CtMethod.make(code.toString(), wrapper);
        wrapper.addMethod(sp);
      }
      
      if (getter != null || setter != null) {
        if (getter != null) {
          wrapper.addMethod(getter);
        }
        if (setter != null) {
          wrapper.addMethod(setter);
        }

        Class<_KeyBinding> wrapperClass = wrapper.toClass();
        _KeyBinding binding = wrapperClass.newInstance();
        if (! _keyBindingCache.containsKey(targetClass)) {
          _keyBindingCache.put(targetClass, new HashMap<String, _KeyBinding>());
        }
        _keyBindingCache.get(targetClass).put(key, binding);
        return binding;
      }
    } catch (NotFoundException e) {
      NSLog._conditionallyLogPrivateException(e);
    } catch (CannotCompileException e) {
      NSLog._conditionallyLogPrivateException(e);
    } catch (InstantiationException e) {
      NSLog._conditionallyLogPrivateException(e);
    } catch (IllegalAccessException e) {
      NSLog._conditionallyLogPrivateException(e);
    }
    return null;
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
    String converted = "(" + _NSUtilities.class.getName() 
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
    boolean isPrimitive = type.isPrimitive();
    if (isPrimitive)
      return ((CtPrimitiveType)type).getWrapperName();
    return type.getName();
  }
  
  private String prefixedKey(String prefix, String key) {
    if ("_".equals(prefix)) {
      return prefix + key;
    }
    if (prefix != null) {
      return prefix + _NSStringUtilities.capitalizedString(key);
    }
    return key;
  }
}
