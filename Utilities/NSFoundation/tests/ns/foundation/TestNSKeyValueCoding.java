package ns.foundation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ns.foundation.NSKeyValueCoding.DefaultImplementation;
import ns.foundation.NSKeyValueCoding.ErrorHandling;
import ns.foundation.NSKeyValueCoding.UnknownKeyException;
import ns.foundation.NSKeyValueCoding.Utility;
import ns.foundation.NSKeyValueCoding._KeyBinding;
import ns.foundation.NSKeyValueCoding._KeyBindingCreation;
import ns.foundation.NSKeyValueCoding._KeyBindingCreation._KeyBindingFactory;
import ns.foundation.NSKeyValueCoding._KeyBindingCreation._KeyBindingFactory.Callback;
import ns.foundation.noaccess.NoAccessClass;
import ns.foundation.overriddenaccess.OverriddenAccessClass;
import ns.foundation.protectedaccess.RestrictedClass;
import ns.foundation.protectedaccess.SubclassOfNoAccessClass;


@SuppressWarnings("unused")
public class TestNSKeyValueCoding extends BaseTestCase {

  private static final BigDecimal FORTY_TWO_BIG_DECIMAL = BigDecimal.valueOf(42L);
  private static final BigInteger FORTY_TWO_BIG_INTEGER = BigInteger.valueOf(42L);
  private static final String FORTY_TWO_STRING = "42";
  private static final Character FORTY_TWO_CHAR = (char)42;
  private static final Integer FORTY_TWO = Integer.valueOf(42);
  private static final Short FORTY_TWO_SHORT = FORTY_TWO.shortValue();
  private static final Long FORTY_TWO_LONG = Long.valueOf(FORTY_TWO.longValue());
  private static final Float FORTY_TWO_FLOAT = Float.valueOf(FORTY_TWO.floatValue());
  private static final Double FORTY_TWO_DOUBLE = Double.valueOf(FORTY_TWO.doubleValue());
  private static final Byte FORTY_TWO_BYTE = Byte.valueOf(FORTY_TWO.byteValue());

  public static class NSKeyValueCodingImpl implements NSKeyValueCoding {
    public Integer knownField;
    
    private Object _value;
    
    public Object knownMethod() {
      return FORTY_TWO;
    }
    
    @Override
    public Object valueForKey(String s) {
      return _value;
    }

    @Override
    public void takeValueForKey(Object obj, String s) {
      _value = obj;
    }
    
    public Object value() {
      return _value;
    }
    
    public void setValue(Object value) {
      _value = value;
    }
  }
  
  public static class ErrorHandlingImpl implements ErrorHandling {
    private Object _value;
    public int knownKey;
    
    @Override
    public Object handleQueryWithUnboundKey(String s) {
      return FORTY_TWO;
    }

    @Override
    public void handleTakeValueForUnboundKey(Object obj, String s) {
      _value = FORTY_TWO;
    }

    @Override
    public void unableToSetNullForKey(String s) {
      _value = FORTY_TWO;
    }
    
    public Object value() {
      return _value;
    }
    
    public void setValue(Object value) {
      _value = value;
    }
  };
  
  public static class NSKeyValueCodingDefaultImpl implements NSKeyValueCoding {
    private Object _value;
    
    public Object value() {
      return _value;
    }
    
    public void setValue(Object value) {
      _value = value;
    }
    
    @Override
    public Object valueForKey(String s) {
      return DefaultImplementation.valueForKey(this, s);
    }

    @Override
    public void takeValueForKey(Object obj, String s) {
      DefaultImplementation.takeValueForKey(this, obj, s);
    }
  }
  
  public static class ErrorHandlingDefaultImpl implements ErrorHandling {

    @Override
    public Object handleQueryWithUnboundKey(String s) {
      return DefaultImplementation.handleQueryWithUnboundKey(this, s);
    }

    @Override
    public void handleTakeValueForUnboundKey(Object obj, String s) {
      DefaultImplementation.handleTakeValueForUnboundKey(this, obj, s);
    }

    @Override
    public void unableToSetNullForKey(String s) {
      DefaultImplementation.unableToSetNullForKey(this, s);
    }
    
  }
  
  /*
   * Tests for NSKeyValueCoding.Utility
   */
  
  public void testUtility$nullValue() {
    assertEquals(NSKeyValueCoding.NullValue, Utility.nullValue());
  }
  
  public static class ValueForKey$knownMethod {
    public Object knownMethod() { return FORTY_TWO; }
  }
  
  public void testUtility$valueForKey$knownMethod() {
    Object obj = new ValueForKey$knownMethod();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod"));
  }
  
  public void testUtility$valueForKey$anonInnerClassKnownMethod() {
    Object obj = new Object() {
      public Object knownMethod() { return FORTY_TWO; }
    };
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod"));
  }

  public static class ValueForKey$knownField {
    public Integer knownField = FORTY_TWO;
  }
  
  public void testUtility$valueForKey$knownField() {
    Object obj = new ValueForKey$knownField();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField"));
  }
  

  public void testUtility$valueForKey$anonInnerClassKnownField() {
    Object obj = new Object() {
      public Integer knownField = FORTY_TWO;
    };
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField"));
  }

  public static class TakeValueForKey$knownMethod {
    private Integer _value;
    public Integer knownMethod() { return _value; };
    public void setKnownMethod(Integer value) { _value = value; };
  }
  
  public void testUtility$takeValueForKey$knownMethod() {
    Object obj = new TakeValueForKey$knownMethod();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownMethod");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod"));
  }
  
  public void testUtility$takeValueForKey$anonInnerClassKnownMethod() {
    Object obj = new Object() {
      private Integer _value;
      public Integer knownMethod() { return _value; };
      public void setKnownMethod(Integer value) { _value = value; };
    };
    Utility.takeValueForKey(obj, FORTY_TWO, "knownMethod");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod"));
  }

  public static class TakeValueForKey$knownField {
    public Integer knownField;
  }

  public void testUtility$takeValueForKey$knownField() {
    Object obj = new TakeValueForKey$knownField();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownField");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField"));
  }
  
  public void testUtility$takeValueForKey$anonInnerClassKnownField() {
    Object obj = new Object() {
      public Integer knownField;
    };
    Utility.takeValueForKey(obj, FORTY_TWO, "knownField");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField"));
  }
    
  public void testUtility$valueForKey$Map() {
    Map<Object, Object> map = new HashMap<Object, Object>();
    map.put("fortytwo", FORTY_TWO);
    assertEquals(FORTY_TWO, Utility.valueForKey(map, "fortytwo"));
    assertEquals(null, Utility.valueForKey(map, "unknownElement"));
    assertEquals(map.values(), Utility.valueForKey(map, "values"));
    assertEquals(map.keySet(), Utility.valueForKey(map, "keySet"));
    assertEquals(map.size(), Utility.valueForKey(map, "size"));
    assertEquals(map.entrySet(), Utility.valueForKey(map, "entrySet"));
    map.put("values", 1);
    map.put("keySet", 2);
    map.put("size", 3);
    map.put("entrySet", 4);
    assertEquals(1, Utility.valueForKey(map, "values"));
    assertEquals(2, Utility.valueForKey(map, "keySet"));
    assertEquals(3, Utility.valueForKey(map, "size"));
    assertEquals(4, Utility.valueForKey(map, "entrySet"));
  }

  public void testUtility$takeValueForKey$Map() {
    Map<Object, Object> map = new HashMap<Object, Object>();
    Utility.takeValueForKey(map, FORTY_TWO, "fortytwo");
    assertEquals(FORTY_TWO, map.get("fortytwo"));
    Utility.takeValueForKey(map, 1, "values");
    Utility.takeValueForKey(map, 2, "keySet");
    Utility.takeValueForKey(map, 3, "size");
    Utility.takeValueForKey(map, 4, "entrySet");
    assertEquals(1, Utility.valueForKey(map, "values"));
    assertEquals(2, Utility.valueForKey(map, "keySet"));
    assertEquals(3, Utility.valueForKey(map, "size"));
    assertEquals(4, Utility.valueForKey(map, "entrySet"));
  }
  
  public void testUtility$valueForKey$UnknownKey() {
    Object obj = new Object();
    try {
      Utility.valueForKey(obj, "unknownKey");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }
  }

  public void testUtility$takeValueForKey$UnknownKey() {
    Object obj = new Object();
    try {
      Utility.takeValueForKey(obj, null, "unknownKey");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }
  }
  
  public void testUtility$valueForKey$implementsNSKeyValueCoding() {
    NSKeyValueCodingImpl obj = new NSKeyValueCodingImpl() {
      @Override
      public void takeValueForKey(Object obj, String s) {
        fail("this method should not be called");
      }
    };
    obj.setValue(FORTY_TWO);
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "anyKey"));
  }

  public void testUtility$takeValueForKey$implementsNSKeyValueCoding() {
    NSKeyValueCodingImpl obj = new NSKeyValueCodingImpl() {
      @Override
      public Object valueForKey(String s) {
        fail("this method should not be called");
        return null;
      }
    };
    Utility.takeValueForKey(obj, FORTY_TWO, "anyKey");
    assertEquals(FORTY_TWO, obj.value());
  }
    
  public void testUtility$takeValueForKey$implementsKeyBindingCreation() {
    _KeyBindingCreation obj = new _KeyBindingCreation() {
      private Object _value; 
      
      @Override
      public _KeyBinding _keySetBindingForKey(String s) {
        return new _KeyBinding(Object.class, "knownKey") {
          @Override
          public void setValueInObject(Object value, Object object) {
            _value = value;
          }
        };
      }
      
      @Override
      public _KeyBinding _keyGetBindingForKey(String s) {
        return new _KeyBinding(Object.class, "knownKey") {
          @Override
          public Object valueInObject(Object object) {
            return _value;
          }
        };
      }
      
      @Override
      public _KeyBinding _createKeySetBindingForKey(String s) {
        return _keySetBindingForKey(s);
      }
      
      @Override
      public _KeyBinding _createKeyGetBindingForKey(String s) {
        return _keyGetBindingForKey(s);
      }
    };
    
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }
  
  public void testUtility$takeValueForKey$implementsCallBack() {
    Callback obj = new Callback() {
      private Object _value;
      
      public Object field;
      public Object method() { return null; }
      public void setMethod(Object obj) { }
      public Object methodValue() { return _value; }
      public Object fieldValue() { return field; }
      
      @Override
      public _KeyBinding _otherStorageBinding(String s) {
        return new _KeyBinding(Object.class, "otherStorage") {
          @Override
          public Object valueInObject(Object object) {
            return "otherStorage";
          }
        };
      }
      
      @Override
      public _KeyBinding _methodKeySetBinding(String s, String s1) {
        return new _KeyBinding(Object.class, "setMethod") {
          @Override
          public void setValueInObject(Object value, Object object) {
            _value = value;
          }
        };
      }
      
      @Override
      public _KeyBinding _methodKeyGetBinding(String s, String s1) {
        return new _KeyBinding(Object.class, "method") {
          @Override
          public Object valueInObject(Object object) {
            return _value;
          }
        };
      }
      
      @Override
      public _KeyBinding _fieldKeyBinding(String s, String s1) {
        return new _KeyBinding(Object.class, "field") {
          @Override
          public void setValueInObject(Object value, Object object) {
            _value = value;
          }
          
          @Override
          public Object valueInObject(Object object) {
            return _value;
          }
        };
      }
    };
    Utility.takeValueForKey(obj, FORTY_TWO, "method");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "methodValue"));
    Utility.takeValueForKey(obj, 24, "field");
    assertEquals(24, Utility.valueForKey(obj, "fieldValue"));

  }
  
  public void testUtility$handleQueryForUnboundKey() {
    Object obj = new Object();
    try {
      Utility.handleQueryWithUnboundKey(obj, "unknownKey");
      fail("Expected UnknownKeyException");
    } catch (UnknownKeyException e) {
    }
  }

  public void testUtility$handleTakeValueForUnboundKey() {
    Object obj = new Object();
    try {
      Utility.handleTakeValueForUnboundKey(obj, FORTY_TWO, "unknownKey");
      fail("Expected UnknownKeyException");
    } catch (UnknownKeyException e) {
    }
  }

  public void testUtility$unableToSetNullForKey() {
    Object obj = new Object();
    try {
      Utility.unableToSetNullForKey(obj, "unknownKey");
      fail("Expected UnknownKeyException");
    } catch (IllegalArgumentException e) {
    }
    
    ErrorHandlingImpl obj2 = new ErrorHandlingImpl();
    Utility.takeValueForKey(obj2, null, "knownKey");
    assertEquals(FORTY_TWO, obj2.value());
  }
  
  public void testUtility$handleQueryForUnboundKey$implementsErrorHandling() {
    ErrorHandlingImpl obj = new ErrorHandlingImpl() {
      @Override
      public void handleTakeValueForUnboundKey(Object obj, String s) {
        fail("this method should not be called");
      }

      @Override
      public void unableToSetNullForKey(String s) {
        fail("this method should not be called");
      }
    };
    assertEquals(FORTY_TWO, Utility.handleQueryWithUnboundKey(obj, "unknownKey"));
  }

  public void testUtility$handleTakeValueForUnboundKey$implementsErrorHandling() {
    ErrorHandlingImpl obj = new ErrorHandlingImpl() {
      @Override
      public Object handleQueryWithUnboundKey(String s) {
        fail("this method should not be called");
        return null;
      }

      @Override
      public void unableToSetNullForKey(String s) {
        fail("this method should not be called");
      }
    };
    Utility.handleTakeValueForUnboundKey(obj, null, "unknownKey");
    assertEquals(FORTY_TWO, obj.value());
  }

  public void testUtility$unableToSetNullForKey$implementsErrorHandling() {
    ErrorHandlingImpl obj = new ErrorHandlingImpl() {
      @Override
      public Object handleQueryWithUnboundKey(String s) {
        fail("this method should not be called");
        return null;
      }

      @Override
      public void handleTakeValueForUnboundKey(Object obj, String s) {
        fail("this method should not be called");
      }
    };
    Utility.unableToSetNullForKey(obj, "unknownKey");
    assertEquals(FORTY_TWO, obj.value());
  }
  
  /*
   * Tests for NSKeyValueCoding.DefaultImplementation
   */
  
  public void testDefaultImplementation$valueForKey() {
    NSKeyValueCodingDefaultImpl obj = new NSKeyValueCodingDefaultImpl();
    obj.setValue(FORTY_TWO);
    assertEquals(FORTY_TWO, obj.valueForKey("value"));
    try {
      obj.valueForKey("unknownKey");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e){
    }
  }

  public void testDefaultImplementation$takeValueForKey() {
    NSKeyValueCodingDefaultImpl obj = new NSKeyValueCodingDefaultImpl();
    obj.takeValueForKey(FORTY_TWO, "value");
    assertEquals(FORTY_TWO, obj.value());
    try {
      obj.takeValueForKey(FORTY_TWO, "unknownKey");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e){
    }
  }

  public void testDefaultImplementation$handleQueryForUnboundKey() {
    ErrorHandlingDefaultImpl obj = new ErrorHandlingDefaultImpl();
    try {
      obj.handleQueryWithUnboundKey("unknownKey");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }
  }
  
  public void testDefaultImplementation$handleTakeValueForUnboundKey() {
    ErrorHandlingDefaultImpl obj = new ErrorHandlingDefaultImpl();
    try {
      obj.handleTakeValueForUnboundKey(null, "unknownKey");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }
  }
  
  public void testDefaultImplementation$unableToSetNullForKey() {
    ErrorHandlingDefaultImpl obj = new ErrorHandlingDefaultImpl();
    try {
      obj.unableToSetNullForKey("unknownKey");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
  }
  
  /*
   * Tests for _KeyBinding creation
   */
  
  public static class KeyBindingTypeTest {
    public Integer knownField;

    public Byte byteObjectField;
    public byte byteField;
    public Character charObjectField;
    public char charField;
    public Short shortObjectField;
    public short shortField;
    public Integer integerField;
    public int intField;
    public Long longObjectField;
    public long longField;
    public Float floatObjectField;
    public float floatField;
    public Double doubleObjectField;
    public double doubleField;
    public Boolean booleanObjectField;
    public boolean booleanField;
    public BigInteger bigIntegerField;
    public BigDecimal bigDecimalField;
    public String stringField;
    
    public Integer knownMethod() { return FORTY_TWO; }
    
    public Byte byteObjectMethod() { return byteObjectField; }
    public void setByteObjectMethod(Byte value) { byteObjectField = value; } 
    public byte byteMethod() { return byteField; }
    public void setByteMethod(byte value) { byteField = value; }
    public Character charObjectMethod() { return charObjectField; }
    public void setCharObjectMethod(Character value) { charObjectField = value; }
    public char charMethod() { return charField; }
    public void setCharMethod(char value) { charField = value; }
    public Short shortObjectMethod() { return shortObjectField; }
    public void setShortObjectMethod(Short value) { shortObjectField = value; }  
    public short shortMethod() { return shortField; }
    public void setShortMethod(short value) { shortField = value; }
    public Integer integerMethod() { return integerField; }
    public void setIntegerMethod(Integer value) { integerField = value; }
    public int intMethod() { return intField; }
    public void setIntMethod(int value) { intField = value; }
    public Long longObjectMethod() { return longObjectField; }
    public void setLongObjectMethod(Long value) { longObjectField = value; }
    public long longMethod() { return longField; }
    public void setLongMethod(long value) { longField = value; }
    public Float floatObjectMethod() { return floatObjectField; }
    public void setFloatObjectMethod(Float value) { floatObjectField = value; }
    public float floatMethod() { return floatField; }
    public void setFloatMethod(float value) { floatField = value; }
    public Double doubleObjectMethod() { return doubleObjectField; }
    public void setDoubleObjectMethod(Double value) { doubleObjectField = value; }
    public double doubleMethod() { return doubleField; }
    public void setDoubleMethod(double value) { doubleField = value; }
    public Boolean booleanObjectMethod() { return booleanObjectField; }
    public void setBooleanObjectMethod(Boolean value) { booleanObjectField = value; }
    public boolean booleanMethod() { return booleanField; }
    public void setBooleanMethod(boolean value) { booleanField = value; }
    public BigInteger bigIntegerMethod() { return bigIntegerField; }
    public void setBigIntegerMethod(BigInteger value) { bigIntegerField = value; }
    public BigDecimal bigDecimalMethod() { return bigDecimalField; }
    public void setBigDecimalMethod(BigDecimal value) { bigDecimalField = value; }    
    public String stringMethod() { return stringField; }
    public void setStringMethod(String value) { stringField = value; }
  }

  /*
   * Tests for _KeyBinding Getter field bindings
   */

  public void testDefaultImplementation$_keyGetBindingForKey$knownField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "knownField");
    assertEquals(obj.getClass(), kb.targetClass());
    assertEquals("knownField", kb.key());  
    assertEquals(Integer.class, kb.valueType());
  }

  public void testDefaultImplementation$_keyGetBindingForKey$unknownField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "unknownField");
    assertEquals(obj.getClass(), kb.targetClass());
    assertEquals("unknownField", kb.key());
    assertEquals(Object.class, kb.valueType());
  }
  
  public void testDefaultImplementation$_keyGetBindingForKey$byteField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "byteObjectField");
    assertEquals(Byte.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.byteObjectField = FORTY_TWO_BYTE;
    assertEquals(FORTY_TWO_BYTE, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "byteField");
    assertEquals(Byte.TYPE, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.byteField = FORTY_TWO_BYTE;
    assertEquals(FORTY_TWO_BYTE, kb.valueInObject(obj));
  }
  
  public void testDefaultImplementation$_keyGetBindingForKey$charField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "charObjectField");
    assertEquals(Character.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.charObjectField = FORTY_TWO_CHAR;
    assertEquals(FORTY_TWO_CHAR, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "charField");
    assertEquals(Character.TYPE, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.charField = FORTY_TWO_CHAR;
    assertEquals(FORTY_TWO_CHAR, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$shortField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "shortObjectField");
    assertEquals(Short.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.shortObjectField = FORTY_TWO_SHORT;
    assertEquals(FORTY_TWO_SHORT, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "shortField");
    assertEquals(Short.class, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.shortField = FORTY_TWO_SHORT;
    assertEquals(FORTY_TWO_SHORT, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$integerField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "integerField");
    assertEquals(Integer.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.integerField = FORTY_TWO;
    assertEquals(FORTY_TWO, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "intField");
    assertEquals(Integer.class, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.intField = FORTY_TWO;
    assertEquals(FORTY_TWO, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$longField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "longObjectField");
    assertEquals(Long.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.longObjectField = FORTY_TWO_LONG;
    assertEquals(FORTY_TWO_LONG, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "longField");
    assertEquals(Long.class, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.longField = FORTY_TWO_LONG;
    assertEquals(FORTY_TWO_LONG, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$floatField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "floatObjectField");
    assertEquals(Float.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.floatObjectField = FORTY_TWO_FLOAT;
    assertEquals(FORTY_TWO_FLOAT, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "floatField");
    assertEquals(Float.class, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.floatField = FORTY_TWO_FLOAT;
    assertEquals(FORTY_TWO_FLOAT, kb.valueInObject(obj));
  }
  
  public void testDefaultImplementation$_keyGetBindingForKey$doubleField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "doubleObjectField");
    assertEquals(Double.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.doubleObjectField = FORTY_TWO_DOUBLE;
    assertEquals(FORTY_TWO_DOUBLE, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "doubleField");
    assertEquals(Double.class, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.doubleField = FORTY_TWO_DOUBLE;
    assertEquals(FORTY_TWO_DOUBLE, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$booleanField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "booleanObjectField");
    assertEquals(Boolean.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.booleanObjectField = true;
    assertEquals(Boolean.TRUE, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "booleanField");
    assertEquals(Boolean.class, kb.valueType());
    assertTrue(kb.isScalarProperty());
    obj.booleanField = true;
    assertEquals(Boolean.TRUE, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$stringField() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "stringField");
    assertEquals(String.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.stringField = FORTY_TWO_STRING;
    assertEquals(FORTY_TWO_STRING, kb.valueInObject(obj));
  }

  /*
   * Tests for _KeyBinding Setter field bindings
   */

  public void testDefaultImplementation$_keySetBindingForKey$knownField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "knownField");
    assertEquals(obj.getClass(), kb.targetClass());
    assertEquals("knownField", kb.key());
    assertEquals(Integer.class, kb.valueType());
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$byteField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "byteObjectField");
    assertEquals(Byte.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_BYTE, obj);
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "byteField");
    assertEquals(Byte.TYPE, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_BYTE, obj);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$integerField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "integerField");
    assertEquals(Integer.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO, obj);
    assertEquals(FORTY_TWO, obj.integerField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "intField");
    assertEquals(Integer.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO, obj);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$longObjectField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "longObjectField");
    assertEquals(Long.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_LONG, obj);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "longField");
    assertEquals(Long.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_LONG, obj);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$floatField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "floatObjectField");
    assertEquals(Float.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_FLOAT, obj);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "floatField");
    assertEquals(Float.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_FLOAT, obj);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$doubleField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "doubleObjectField");
    assertEquals(Double.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_DOUBLE, obj);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "doubleField");
    assertEquals(Double.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_DOUBLE, obj);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$booleanField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "booleanObjectField");
    assertEquals(Boolean.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(true, obj);
    assertTrue(obj.booleanObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "booleanField");
    assertEquals(Boolean.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(true, obj);
    assertTrue(obj.booleanField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$stringField() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "stringField");
    assertEquals(String.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_STRING, obj);
    assertEquals(FORTY_TWO_STRING, obj.stringField);
  }
  
  /*
   * Tests for _KeyBinding Getter method bindings
   */
  
  public void testDefaultImplementation$_keyGetBindingForKey$knownMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "knownMethod");
    assertEquals(obj.getClass(), kb.targetClass());
    assertEquals("knownMethod", kb.key());
    assertEquals(Integer.class, kb.valueType());
  }
  
  public void testDefaultImplementation$_keyGetBindingForKey$unknownMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "unknownMethod");
    assertEquals(obj.getClass(), kb.targetClass());
    assertEquals("unknownMethod", kb.key());
    assertEquals(Object.class, kb.valueType());
  }

  public void testDefaultImplementation$_keyGetBindingForKey$byteMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "byteObjectMethod");
    assertEquals(Byte.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.byteObjectField = FORTY_TWO_BYTE;
    assertEquals(FORTY_TWO_BYTE, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "byteMethod");
    assertEquals(Byte.TYPE, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.byteField = FORTY_TWO_BYTE;
    assertEquals(FORTY_TWO_BYTE, kb.valueInObject(obj));
  }
  
  public void testDefaultImplementation$_keyGetBindingForKey$charMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "charObjectMethod");
    assertEquals(Character.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.charObjectField = FORTY_TWO_CHAR;
    assertEquals(FORTY_TWO_CHAR, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "charMethod");
    assertEquals(Character.TYPE, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.charField = FORTY_TWO_CHAR;
    assertEquals(FORTY_TWO_CHAR, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$shortMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "shortObjectMethod");
    assertEquals(Short.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.shortObjectField = FORTY_TWO_SHORT;
    assertEquals(FORTY_TWO_SHORT, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "shortMethod");
    assertEquals(Short.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.shortField = FORTY_TWO_SHORT;
    assertEquals(FORTY_TWO_SHORT, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$integerMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "integerMethod");
    assertEquals(Integer.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.integerField = FORTY_TWO;
    assertEquals(FORTY_TWO, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "intMethod");
    assertEquals(Integer.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.intField = FORTY_TWO;
    assertEquals(FORTY_TWO, kb.valueInObject(obj));

  }

  public void testDefaultImplementation$_keyGetBindingForKey$longMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "longObjectMethod");
    assertEquals(Long.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.longObjectField = FORTY_TWO_LONG;
    assertEquals(FORTY_TWO_LONG, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "longMethod");
    assertEquals(Long.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.longField = FORTY_TWO_LONG;
    assertEquals(FORTY_TWO_LONG, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$floatMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "floatObjectMethod");
    assertEquals(Float.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.floatObjectField = FORTY_TWO_FLOAT;
    assertEquals(FORTY_TWO_FLOAT, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "floatMethod");
    assertEquals(Float.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.floatField = FORTY_TWO_FLOAT;
    assertEquals(FORTY_TWO_FLOAT, kb.valueInObject(obj));
  }
  
  public void testDefaultImplementation$_keyGetBindingForKey$doubleMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "doubleObjectMethod");
    assertEquals(Double.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.doubleObjectField = FORTY_TWO_DOUBLE;
    assertEquals(FORTY_TWO_DOUBLE, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "doubleMethod");
    assertEquals(Double.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.doubleField = FORTY_TWO_DOUBLE;
    assertEquals(FORTY_TWO_DOUBLE, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$booleanMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "booleanObjectMethod");
    assertEquals(Boolean.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.booleanObjectField = Boolean.TRUE;
    assertEquals(Boolean.TRUE, kb.valueInObject(obj));
    
    kb = DefaultImplementation._keyGetBindingForKey(obj, "booleanMethod");
    assertEquals(Boolean.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.booleanField = Boolean.TRUE;
    assertEquals(Boolean.TRUE, kb.valueInObject(obj));
  }

  public void testDefaultImplementation$_keyGetBindingForKey$stringMethod() {    
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keyGetBindingForKey(obj, "stringMethod");
    assertEquals(String.class, kb.valueType());
    assertFalse(kb.isScalarProperty());
    obj.stringField = FORTY_TWO_STRING;
    assertEquals(FORTY_TWO_STRING, kb.valueInObject(obj));
  }
  
  /*
   * Tests for _KeyBinding Setter method bindings
   */
  public void testDefaultImplementation$_keySetBindingForKey$byteMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "byteObjectMethod");
    assertEquals(Byte.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_BYTE, obj);
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);

    kb = DefaultImplementation._keySetBindingForKey(obj, "byteMethod");
    assertEquals(Byte.TYPE, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_BYTE, obj);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
  }

  public void testDefaultImplementation$_keySetBindingForKey$charMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "charObjectMethod");
    assertEquals(Character.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_CHAR, obj);
    assertEquals(FORTY_TWO_CHAR, obj.charObjectField);

    kb = DefaultImplementation._keySetBindingForKey(obj, "charMethod");
    assertEquals(Character.TYPE, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_CHAR, obj);
    assertEquals(FORTY_TWO_CHAR.charValue(), obj.charField);

  }

  public void testDefaultImplementation$_keySetBindingForKey$shortMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "shortObjectMethod");
    assertEquals(Short.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_SHORT, obj);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "shortMethod");
    assertEquals(Short.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_SHORT, obj);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);

  }

  public void testDefaultImplementation$_keySetBindingForKey$integerMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "integerMethod");
    assertEquals(Integer.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO, obj);
    assertEquals(FORTY_TWO, obj.integerField);

    kb = DefaultImplementation._keySetBindingForKey(obj, "intMethod");
    assertEquals(Integer.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO, obj);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
  }

  public void testDefaultImplementation$_keySetBindingForKey$longMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "longObjectMethod");
    assertEquals(Long.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_LONG, obj);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);

    kb = DefaultImplementation._keySetBindingForKey(obj, "longMethod");
    assertEquals(Long.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_LONG, obj);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);

  }

  public void testDefaultImplementation$_keySetBindingForKey$floatMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "floatObjectMethod");
    assertEquals(Float.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_FLOAT, obj);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "floatMethod");
    assertEquals(Float.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_FLOAT, obj);
    assertEquals(FORTY_TWO_FLOAT, obj.floatField);
  }

  public void testDefaultImplementation$_keySetBindingForKey$doubleMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "doubleObjectMethod");
    assertEquals(Double.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_DOUBLE, obj);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "doubleMethod");
    assertEquals(Double.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_DOUBLE, obj);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleField);
  }

  public void testDefaultImplementation$_keySetBindingForKey$booleanMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "booleanObjectMethod");
    assertEquals(Boolean.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(true, obj);
    assertTrue(obj.booleanObjectField);
    
    kb = DefaultImplementation._keySetBindingForKey(obj, "booleanMethod");
    assertEquals(Boolean.class, kb.valueType());
    assertTrue(kb.isScalarProperty());

    kb.setValueInObject(true, obj);
    assertTrue(obj.booleanField);
  }
  
  public void testDefaultImplementation$_keySetBindingForKey$stringMethod() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();

    _KeyBinding kb = DefaultImplementation._keySetBindingForKey(obj, "stringMethod");
    assertEquals(String.class, kb.valueType());
    assertFalse(kb.isScalarProperty());

    kb.setValueInObject(FORTY_TWO_STRING, obj);
    assertEquals(FORTY_TWO_STRING, obj.stringField);
  }
  
  public void testDefaultImplementation$TypeConversion$byte() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "bigDecimalField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BYTE, "bigIntegerField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(FORTY_TWO_BIG_DECIMAL, obj.bigDecimalField);
  }

  public void testDefaultImplementation$TypeConversion$short() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_SHORT, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(FORTY_TWO_BIG_DECIMAL, obj.bigDecimalField);
  }
  
  public void testDefaultImplementation$TypeConversion$integer() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(FORTY_TWO_BIG_DECIMAL, obj.bigDecimalField);
  }
  
  public void testDefaultImplementation$TypeConversion$long() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_LONG, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(FORTY_TWO_BIG_DECIMAL, obj.bigDecimalField);
  }
  
  public void testDefaultImplementation$TypeConversion$float() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_FLOAT, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(BigDecimal.valueOf(FORTY_TWO_DOUBLE), obj.bigDecimalField);
  }
  
  public void testDefaultImplementation$TypeConversion$double() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_DOUBLE, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(BigDecimal.valueOf(FORTY_TWO_DOUBLE), obj.bigDecimalField);
  }

  public void testDefaultImplementation$TypeConversion$bigInteger() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_INTEGER, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(FORTY_TWO_BIG_DECIMAL, obj.bigDecimalField);
  }

  public void testDefaultImplementation$TypeConversion$bigDecimal() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "byteField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "shortField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "integerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "intField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "longField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "floatField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "doubleField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "booleanField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, FORTY_TWO_BIG_DECIMAL, "bigDecimalField");
    assertEquals(FORTY_TWO_BYTE, obj.byteObjectField);
    assertEquals(FORTY_TWO_BYTE.byteValue(), obj.byteField);
    assertEquals(FORTY_TWO_SHORT, obj.shortObjectField);
    assertEquals(FORTY_TWO_SHORT.shortValue(), obj.shortField);
    assertEquals(FORTY_TWO, obj.integerField);
    assertEquals(FORTY_TWO.intValue(), obj.intField);
    assertEquals(FORTY_TWO_LONG, obj.longObjectField);
    assertEquals(FORTY_TWO_LONG.longValue(), obj.longField);
    assertEquals(FORTY_TWO_FLOAT, obj.floatObjectField);
    assertEquals(FORTY_TWO_FLOAT.floatValue(), obj.floatField);
    assertEquals(FORTY_TWO_DOUBLE, obj.doubleObjectField);
    assertEquals(FORTY_TWO_DOUBLE.doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(FORTY_TWO_BIG_INTEGER, obj.bigIntegerField);
    assertEquals(FORTY_TWO_BIG_DECIMAL, obj.bigDecimalField);
  }

  public void testDefaultImplementation$TypeConversion$boolean() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, true, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, true, "byteField");
    DefaultImplementation.takeValueForKey(obj, true, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, true, "shortField");
    DefaultImplementation.takeValueForKey(obj, true, "integerField");
    DefaultImplementation.takeValueForKey(obj, true, "intField");
    DefaultImplementation.takeValueForKey(obj, true, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, true, "longField");
    DefaultImplementation.takeValueForKey(obj, true, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, true, "floatField");
    DefaultImplementation.takeValueForKey(obj, true, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, true, "doubleField");
    DefaultImplementation.takeValueForKey(obj, true, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, true, "booleanField");
    DefaultImplementation.takeValueForKey(obj, true, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, true, "bigDecimalField");
    assertEquals(Byte.valueOf((byte)1), obj.byteObjectField);
    assertEquals((byte)1, obj.byteField);
    assertEquals(Short.valueOf((short)1), obj.shortObjectField);
    assertEquals((short)1, obj.shortField);
    assertEquals(Integer.valueOf(1), obj.integerField);
    assertEquals(1, obj.intField);
    assertEquals(Long.valueOf(1L), obj.longObjectField);
    assertEquals(1L, obj.longField);
    assertEquals(Float.valueOf(1), obj.floatObjectField);
    assertEquals(1.0f, obj.floatField);
    assertEquals(Double.valueOf(1), obj.doubleObjectField);
    assertEquals(1.0, obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(BigInteger.ONE, obj.bigIntegerField);
    assertEquals(BigDecimal.ONE, obj.bigDecimalField);
  }
  
  
  public void testDefaultImplementation$TypeConversion$null() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest() {
      {
        byteObjectField = Byte.MAX_VALUE;
        byteField = Byte.MAX_VALUE;
        shortObjectField = Short.MAX_VALUE;
        shortField = Short.MAX_VALUE;
        integerField = Integer.MAX_VALUE;
        intField = Integer.MAX_VALUE;
        longObjectField = Long.MAX_VALUE;
        longField = Long.MAX_VALUE;
        floatObjectField = Float.MAX_VALUE;
        floatField = Float.MAX_VALUE;
        doubleObjectField = Double.MAX_VALUE;
        doubleField = Double.MAX_VALUE;
        booleanObjectField = true;
        booleanField = true;
        bigIntegerField = BigInteger.ONE;
        bigDecimalField = BigDecimal.ONE;
      }
    };
    
    /* Test null assignment handling */
    DefaultImplementation.takeValueForKey(obj, null, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, null, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, null, "integerField");
    DefaultImplementation.takeValueForKey(obj, null, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, null, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, null, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, null, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, null, "bigIntegerField");
    DefaultImplementation.takeValueForKey(obj, null, "bigDecimalField");
    try {
      DefaultImplementation.takeValueForKey(obj, null, "byteField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    try {
      DefaultImplementation.takeValueForKey(obj, null, "shortField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    try {
      DefaultImplementation.takeValueForKey(obj, null, "intField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    try {
      DefaultImplementation.takeValueForKey(obj, null, "longField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    try {
      DefaultImplementation.takeValueForKey(obj, null, "floatField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    try {
      DefaultImplementation.takeValueForKey(obj, null, "doubleField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    try {
      DefaultImplementation.takeValueForKey(obj, null, "booleanField");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException e) {
    }
    assertEquals(null, obj.byteObjectField);
    assertEquals(Byte.MAX_VALUE, obj.byteField);
    assertEquals(null, obj.shortObjectField);
    assertEquals(Short.MAX_VALUE, obj.shortField);
    assertEquals(null, obj.integerField);
    assertEquals(Integer.MAX_VALUE, obj.intField);
    assertEquals(null, obj.longObjectField);
    assertEquals(Long.MAX_VALUE, obj.longField);
    assertEquals(null, obj.floatObjectField);
    assertEquals(Float.MAX_VALUE, obj.floatField);
    assertEquals(null, obj.doubleObjectField);
    assertEquals(Double.MAX_VALUE, obj.doubleField);
    assertEquals(null, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
    assertEquals(null, obj.bigIntegerField);
    assertEquals(null, obj.bigDecimalField);
  }
  
  public void testDefaultImplementation$TypeConversion$overflowLong() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    BigInteger bigNumber = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "byteField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "shortField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "integerField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "intField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, Long.MAX_VALUE, "booleanField");
    assertEquals(Byte.valueOf((byte)-1), obj.byteObjectField);
    assertEquals((byte)-1, obj.byteField);
    assertEquals(Short.valueOf((short)-1), obj.shortObjectField);
    assertEquals((short)-1, obj.shortField);
    assertEquals(Integer.valueOf(-1), obj.integerField);
    assertEquals(-1, obj.intField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
  }
  
  public void testDefaultImplementation$TypeConversion$overflowDouble() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "byteField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "shortField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "integerField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "intField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "longField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "floatField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, Double.MAX_VALUE, "booleanField");
    assertEquals(Byte.valueOf((byte)-1), obj.byteObjectField);
    assertEquals((byte)-1, obj.byteField);
    assertEquals(Short.valueOf((short)-1), obj.shortObjectField);
    assertEquals((short)-1, obj.shortField);
    assertEquals(Integer.valueOf(2147483647), obj.integerField);
    assertEquals(2147483647, obj.intField);
    assertEquals(Long.valueOf(9223372036854775807L), obj.longObjectField);
    assertEquals(9223372036854775807L, obj.longField);
    assertEquals(Float.valueOf(Float.POSITIVE_INFINITY), obj.floatObjectField);
    assertEquals(Float.POSITIVE_INFINITY, obj.floatField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
  }
  
  public void testDefaultImplementation$TypeConversion$overflowBigInteger() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    BigInteger bigNumber = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Byte.MAX_VALUE));
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, bigNumber, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "byteField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "shortField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "integerField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "intField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "longField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "floatField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "doubleField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "booleanField");
    assertEquals(Byte.valueOf((byte)126), obj.byteObjectField);
    assertEquals((byte)126, obj.byteField);
    assertEquals(Short.valueOf((short)126), obj.shortObjectField);
    assertEquals((short)126, obj.shortField);
    assertEquals(Integer.valueOf(126), obj.integerField);
    assertEquals(126, obj.intField);
    assertEquals(Long.valueOf(Long.MIN_VALUE + Byte.MAX_VALUE - 1L), obj.longObjectField);
    assertEquals(Long.MIN_VALUE + Byte.MAX_VALUE - 1L, obj.longField);
    assertEquals(Float.valueOf(bigNumber.toString()), obj.floatObjectField);
    assertEquals(Float.valueOf(bigNumber.toString()).floatValue(), obj.floatField);
    assertEquals(Double.valueOf(bigNumber.toString()), obj.doubleObjectField);
    assertEquals(Double.valueOf(bigNumber.toString()).doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
  }
  
  public void testDefaultImplementation$TypeConversion$overflowBigDecimal() {
    KeyBindingTypeTest obj = new KeyBindingTypeTest();
    
    BigDecimal bigNumber = BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.valueOf(Byte.MAX_VALUE));
    
    /* Test automatic type conversion */
    DefaultImplementation.takeValueForKey(obj, bigNumber, "byteObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "byteField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "shortObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "shortField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "integerField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "intField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "longObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "longField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "floatObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "floatField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "doubleObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "doubleField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "booleanObjectField");
    DefaultImplementation.takeValueForKey(obj, bigNumber, "booleanField");
    assertEquals(Byte.valueOf((byte)127), obj.byteObjectField);
    assertEquals((byte)127, obj.byteField);
    assertEquals(Short.valueOf((short)127), obj.shortObjectField);
    assertEquals((short)127, obj.shortField);
    assertEquals(Integer.valueOf(127), obj.integerField);
    assertEquals(127, obj.intField);
    assertEquals(Long.valueOf(127L), obj.longObjectField);
    assertEquals(127L, obj.longField);
    assertEquals(Float.valueOf(bigNumber.toString()), obj.floatObjectField);
    assertEquals(Float.valueOf(bigNumber.toString()).floatValue(), obj.floatField);
    assertEquals(Double.valueOf(bigNumber.toString()), obj.doubleObjectField);
    assertEquals(Double.valueOf(bigNumber.toString()).doubleValue(), obj.doubleField);
    assertEquals(Boolean.TRUE, obj.booleanObjectField);
    assertEquals(true, obj.booleanField);
  }
  
  /*
   * Tests for _KeyBinding creation search order.
   */
  
  public static class GetMethod$GetPrefix {
    public Integer getKnownKey() { return FORTY_TWO; }
    public Integer knownKey() { fail("this method should not be called"); return null; }
    public Integer isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _getKnownKey() { fail("this method should not be called"); return null; }
    public Integer _knownKey() { fail("this method should not be called"); return null; }
    public Integer _isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  public void testKeyBindingSearchOrder$GetMethod$GetPrefix() {
    Object obj = new GetMethod$GetPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetMethod$NoPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    public Integer knownKey() { return FORTY_TWO; }
    public Integer isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _getKnownKey() { fail("this method should not be called"); return null; }
    public Integer _knownKey() { fail("this method should not be called"); return null; }
    public Integer _isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  };
  public void testKeyBindingSearchOrder$GetMethod$NoPrefix() {
    Object obj = new GetMethod$NoPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetMethod$IsPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    public Integer isKnownKey() { return FORTY_TWO; };
    public Integer _getKnownKey() { fail("this method should not be called"); return null; }
    public Integer _knownKey() { fail("this method should not be called"); return null; }
    public Integer _isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$GetMethod$IsPrefix() {
    Object obj = new GetMethod$IsPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetMethod$UnderscoreGetPrefix {      
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _getKnownKey() { return FORTY_TWO; }
    public Integer _knownKey() { fail("this method should not be called"); return null; }
    public Integer _isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$GetMethod$UnderscoreGetPrefix() {
    Object obj = new GetMethod$UnderscoreGetPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetMethod$UnderscoreNoPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _getKnownKey() { fail("this method should not be called"); return null; }
    public Integer _knownKey() { return FORTY_TWO; }
    public Integer _isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$GetMethod$UnderscoreNoPrefix() {
    Object obj = new GetMethod$UnderscoreNoPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }
  
  public static class GetMethod$UnderscoreIsPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer _knownKey() { fail("this method should not be called"); return null; }
    public Integer _isKnownKey() { return FORTY_TWO; };
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  };
  
  public void testKeyBindingSearchOrder$GetMethod$UnderscoreIsPrefix() {
    Object obj = new GetMethod$UnderscoreIsPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetField$UnderscoreNoPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer _knownKey() { fail("this method should not be called"); return null; }
    protected Integer _isKnownKey() { fail("this method should not be called"); return null; };
    public Integer _knownKey = FORTY_TWO;
    public Integer _isKnownKey = 1;
    public Integer knownKey = 2;
    public Integer isKnownKey = 3;
  }
  
  public void testKeyBindingSearchOrder$GetField$UnderscoreNoPrefix() {
    Object obj = new GetField$UnderscoreNoPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetField$UnderscoreIsPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer _knownKey() { fail("this method should not be called"); return null; }
    protected Integer _isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _knownKey = 1;
    public Integer _isKnownKey = FORTY_TWO;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$GetField$UnderscoreIsPrefix() {
    Object obj = new GetField$UnderscoreIsPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  
  public static class GetField$NoPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer _knownKey() { fail("this method should not be called"); return null; }
    protected Integer _isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _knownKey = 1;
    protected Integer _isKnownKey = 2;
    public Integer knownKey = FORTY_TWO;
    public Integer isKnownKey = 4;
  };
  
  public void testKeyBindingSearchOrder$GetField$NoPrefix() {
    Object obj = new GetField$NoPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }

  public static class GetField$IsPrefix {
    protected Integer getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer knownKey() { fail("this method should not be called"); return null; }
    protected Integer isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _getKnownKey() { fail("this method should not be called"); return null; }
    protected Integer _knownKey() { fail("this method should not be called"); return null; }
    protected Integer _isKnownKey() { fail("this method should not be called"); return null; };
    protected Integer _knownKey = 1;
    protected Integer _isKnownKey = 2;
    protected Integer knownKey = 3;
    public Integer isKnownKey = FORTY_TWO;
  }
  
  public void testKeyBindingSearchOrder$GetField$IsPrefix() {
    Object obj = new GetField$IsPrefix();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }
  
  public static class SetMethod$SetPrefix {
    public Integer _value;
    public void setKnownKey(Integer value) { _value = value; }
    public void _setKnownKey(Integer value) { fail("this method should not be called"); }
    public void setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public void _setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$SetMethod$SetPrefix() {
    Object obj = new SetMethod$SetPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "value"));
  }

  public static class SetMethod$UserscoreSetPrefix {
    public Integer _value;
    protected void setKnownKey(Integer value) { fail("this method should not be called"); }
    public void _setKnownKey(Integer value) { _value = value; }
    public void setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public void _setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$SetMethod$UserscoreSetPrefix() {
    Object obj = new SetMethod$UserscoreSetPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "value"));
  }

  public static class SetMethod$SetIsPrefix {
    public Integer _value;
    protected void setKnownKey(Integer value) { fail("this method should not be called"); }
    protected void _setKnownKey(Integer value) { fail("this method should not be called"); }
    public void setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public void _setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$SetMethod$SetIsPrefix() {
    Object obj = new SetMethod$SetIsPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(null, Utility.valueForKey(obj, "value"));
  }

  public static class SetMethod$UserscoreSetIsPrefix {
    public Integer _value;
    protected void setKnownKey(Integer value) { fail("this method should not be called"); }
    protected void _setKnownKey(Integer value) { fail("this method should not be called"); }
    protected void setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public void _setIsKnownKey(Integer value) { fail("this method should not be called"); }
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  }
  
  public void testKeyBindingSearchOrder$SetMethod$UserscoreSetIsPrefix() {
    Object obj = new SetMethod$UserscoreSetIsPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(null, Utility.valueForKey(obj, "value"));
  }
  
  public static class SearchOrderSetFieldUserscoreNoPrefix {
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  };
  
  public void testKeyBindingSearchOrder$SetField$UserscoreNoPrefix() {
    SearchOrderSetFieldUserscoreNoPrefix obj = new SearchOrderSetFieldUserscoreNoPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, obj._knownKey);
  }

  public static class SearchOrderSetFieldUserscoreIsPrefix {
    protected Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  };
  
  public void testKeyBindingSearchOrder$SetField$UserscoreIsPrefix() {
    SearchOrderSetFieldUserscoreIsPrefix obj = new SearchOrderSetFieldUserscoreIsPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, obj._isKnownKey);
  }

  public static class SearchOrderSetField$NoPrefix {
    protected Integer _knownKey = 1;
    protected Integer _isKnownKey = 2;
    public Integer knownKey = 3;
    public Integer isKnownKey = 4;
  };
  
  public void testKeyBindingSearchOrder$SetField$NoPrefix() {
    SearchOrderSetField$NoPrefix obj = new SearchOrderSetField$NoPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, obj.knownKey);
  }
  
  public static class SearchOrderSetFieldIsPrefix {
    protected Integer _knownKey = 1;
    protected Integer _isKnownKey = 2;
    protected Integer knownKey = 4;
    public Integer isKnownKey = 3;
  };
  
  public void testKeyBindingSearchOrder$SetField$IsPrefix() {
    SearchOrderSetFieldIsPrefix obj = new SearchOrderSetFieldIsPrefix();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, obj.isKnownKey);
  }
  
  public static class BindingSearch$multipleSetters {
    public Integer getKnownKey() { return FORTY_TWO; }
    public void setKnownKey(Object value) { fail("This method should not be called"); }
    public void setKnownKey(Integer value) { }
  }
  
  public void testKeyBindingSearch$multipleSetters() {
    Object obj = new BindingSearch$multipleSetters();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
  }
  
  public static class BindingSearch$mixedObjectPrimitiveSetGet {
    private Object _value;
    public int getKnownKey() { return (Integer)_value; }
    public void setKnownKey(Integer value) { fail("This method should not be called"); }
    public void setKnownKey(int value) { _value = value; }
    public void setKnownKey(Boolean value) { fail("This method should not be called"); }

  }
  
  public void testKeyBindingSearch$mixedObjectPrimitiveSetGet() {
    Object obj = new BindingSearch$mixedObjectPrimitiveSetGet();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownKey"));
  }
  
  public static class BindingSearch$singleArgumentGetter {
    public Integer knownKey;
    public Integer getKnownKey(Integer value) { fail("This method should not be called"); return null; }
  }
  
  public void testKeyBindingSearch$singleArgumentGetter() {
    Object obj = new BindingSearch$singleArgumentGetter();
    Utility.valueForKey(obj, "knownKey");
  }
  
  public static class BindingSearchOrder$SetMethod$multipleArgumentSetter {
    public Integer knownKey;
    public Integer getKnownKey() { return FORTY_TWO; }
    public void setKnownKey(Integer value, Integer value2) { fail("This method should not be called"); }
  }
  
  public void testKeyBindingSearchOrder$SetMethod$multipleArgumentSetter() {
    Object obj = new BindingSearchOrder$SetMethod$multipleArgumentSetter();
    Utility.takeValueForKey(obj, FORTY_TWO, "knownKey");
  }

  public static class CustomFieldSearchOrder {
    public Integer _knownKey = 1;
    public Integer _isKnownKey = 2;
    public Integer knownKey = 4;
    public Integer isKnownKey = 3;
  }

  public void testKeyBindingCustomFieldSearchOrder() {
    CustomFieldSearchOrder obj = new CustomFieldSearchOrder();
    int[] lookupOrder = new int[] {
        _KeyBindingFactory.FieldLookup,
        _KeyBindingFactory.MethodLookup,
        _KeyBindingFactory.UnderbarMethodLookup,
        _KeyBindingFactory.UnderbarFieldLookup,
        _KeyBindingFactory.OtherStorageLookup };
    _KeyBinding binding = NSKeyValueCoding.DefaultImplementation._createKeyGetBindingForKey(obj, "knownKey", lookupOrder);
    assertEquals(4, binding.valueInObject(obj));
  }
  
  public void testKeyBindingRestrictedAccess$withProtectedAccessor() {
    RestrictedClass obj = new RestrictedClass();
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod"));
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField"));
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod2"));
    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField2"));
  }
  
  public void testKeyBindingRestrictedAccess$withoutProtectedAccessor() {
    NoAccessClass obj = new NoAccessClass();
    try {
      Utility.valueForKey(obj, "knownField");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }
    try {
      Utility.valueForKey(obj, "knownMethod");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }    
    try {
      Utility.valueForKey(obj, "knownField2");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }    
    try {
      Utility.valueForKey(obj, "knownMethod2");
      fail("UnknownKeyException expected");
    } catch (UnknownKeyException e) {
    }  
  }
  
//  public void testKeyBindingRestrictedAccess$overriddenProtectedAccessor() {
//    OverriddenAccessClass obj = new OverriddenAccessClass();
//    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod"));
//    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField"));
//    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownMethod2"));
//    assertEquals(FORTY_TWO, Utility.valueForKey(obj, "knownField2"));
//    
//    obj = new OverriddenAccessClass();
//    Utility.takeValueForKey(obj, null, "knownField");
//    Utility.takeValueForKey(obj, null, "knownField2");
//    assertEquals(24, Utility.valueForKey(obj, "knownField"));
//    assertEquals(24, Utility.valueForKey(obj, "knownField2"));
//    
//    obj = new OverriddenAccessClass();
//    Utility.takeValueForKey(obj, null, "knownMethod");
//    Utility.takeValueForKey(obj, null, "knownMethod2");
//    assertEquals(24, Utility.valueForKey(obj, "knownMethod"));
//    assertEquals(24, Utility.valueForKey(obj, "knownMethod2"));
//  }
  
  public void testKeyBindingRestrictedAccess$NoAccessSuperclass() {
    SubclassOfNoAccessClass obj = new SubclassOfNoAccessClass();
    assertEquals(42, Utility.valueForKey(obj, "knownField"));
  }
}
