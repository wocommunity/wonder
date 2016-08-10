package er.extensions.foundation;

import java.io.File;
import java.io.IOException;

import junit.framework.AssertionFailedError;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.erxtest.ERXTestCase;

public class ERXPropertiesTest extends ERXTestCase {
  public void test() throws IOException {
    ERXProperties._Properties properties = new ERXProperties._Properties();
    properties.load(new File("ERXPropertiesTest0.properties"));
    assertEquals("ERXPropertiesTest0", properties.getProperty("key0"));
    assertEquals("ERXPropertiesTest0", properties.getProperty("key1"));
    assertEquals("ERXPropertiesTest2", properties.getProperty("key2"));
    assertEquals("ERXPropertiesTest0", properties.getProperty("key3"));
    assertEquals("ERXPropertiesTest2", properties.getProperty("key4"));
    assertEquals("ERXPropertiesTest0", properties.getProperty("key5"));
    assertEquals("ERXPropertiesTest1", properties.getProperty("key6"));
    assertEquals("ERXPropertiesTest3", properties.getProperty("key7"));
    assertEquals("ERXPropertiesTest3", properties.getProperty("key8"));
    assertEquals("ERXPropertiesTest3", properties.getProperty("key9"));
  }

  private static final String Key = "testKey";
  
  protected static void _setProperty(String key, String value) {
    ERXProperties.setStringForKey(value, key);
  }
  
  protected static void removePropertyForKey(String key) {
    ERXProperties.removeKey(key);
  }
  
  protected static void setCachingEnabled(boolean cachingEnabled) {
    
  }
  public void testGetProperty() {
    // pretty much tested by testStringForKey
  }
  
  public void testCache() {
    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    // MS: Check null twice -- first time = parsed, second time = cached
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "(\"a\", \"b\", \"c\")");
    assertSame(ERXProperties.arrayForKey(ERXPropertiesTest.Key), ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertEquals(new NSArray<String>(new String[] { "a", "b", "c" }), ERXProperties.arrayForKey(ERXPropertiesTest.Key));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "()");
    assertEquals(NSArray.EmptyArray, ERXProperties.arrayForKey(ERXPropertiesTest.Key));

    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
  }
  
//  public void testAppSpecificProperties() {
//    ERXPropertiesTest._setProperty("name", "Name");
//    ERXPropertiesTest._setProperty("name.Test", "NameTest");
//    assertEquals("Name", ERXProperties.stringForKey("name"));
//    ERXProperties._setAppName("Test");
//    assertEquals("NameTest", ERXProperties.stringForKey("name"));
//  }
  
  public void testArrayForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, null));
    NSArray<String> defaultValue = new NSArray<String>(new String[] { "a" });
    assertSame(defaultValue, ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, null));
    assertSame(defaultValue, ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "()");
    assertEquals(NSArray.EmptyArray, ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertEquals(NSArray.EmptyArray, ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, null));
    
    NSArray<String> value = new NSArray<String>(new String[] { "a", "b", "c" });
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "(\"a\", \"b\", \"c\")");
    assertEquals(value, ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertEquals(value, ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, null));
    assertNotSame(value, ERXProperties.arrayForKey(ERXPropertiesTest.Key));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "a");
    assertEquals(new NSArray<String>("a"), ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "('a')");
      ERXProperties.arrayForKeyWithDefault(ERXPropertiesTest.Key, null);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "(\"a\", \"b\", \"c\")");
    assertSame(ERXProperties.arrayForKey(ERXPropertiesTest.Key), ERXProperties.arrayForKey(ERXPropertiesTest.Key));
    assertEquals(value, ERXProperties.arrayForKey(ERXPropertiesTest.Key));
  }
  
//  public void testSetForKey() {
//    ERXPropertiesTest.setCachingEnabled(false);
//    
//    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
//    assertNull(ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, null));
//    NSSet<String> defaultValue = new NSSet<String>(new String[] { "a" });
//    assertSame(defaultValue, ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//    
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
//    assertNull(ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, null));
//    assertSame(defaultValue, ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//    
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "()");
//    assertEquals(NSSet.EmptySet, ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertEquals(NSSet.EmptySet, ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, null));
//    
//    NSSet<String> value = new NSSet<String>(new String[] { "a", "b", "c" });
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "(\"a\", \"b\", \"c\")");
//    assertEquals(value, ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertEquals(value, ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, null));
//    assertNotSame(value, ERXProperties.setForKey(ERXPropertiesTest.Key));
//
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "a");
//    assertEquals(new NSSet<String>("a"), ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//
//    try {
//      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "('a')");
//      ERXProperties.setForKeyWithDefault(ERXPropertiesTest.Key, null);
//      throw new AssertionFailedError("This should have failed.");
//    }
//    catch (IllegalArgumentException e) {
//      // expected
//    }
//
//    ERXPropertiesTest.setCachingEnabled(true);
//    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
//    assertNull(ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.setForKey(ERXPropertiesTest.Key));
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "(\"a\", \"b\", \"c\")");
//    assertSame(ERXProperties.setForKey(ERXPropertiesTest.Key), ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertEquals(value, ERXProperties.setForKey(ERXPropertiesTest.Key));
//  }

  public void testBooleanForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(false, ERXProperties.booleanForKey(ERXPropertiesTest.Key));
    boolean defaultValue = true;
    assertEquals(defaultValue, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertEquals(false, ERXProperties.booleanForKey(ERXPropertiesTest.Key));
    assertEquals(defaultValue, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "yes");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "y");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "YES");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "Y");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "true");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "TRUE");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "no");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "n");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "NO");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "N");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "false");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "FALSE");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "0");
    assertEquals(false, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, true));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "1");
    assertEquals(true, ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
      ERXProperties.booleanForKeyWithDefault(ERXPropertiesTest.Key, false);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(false, ERXProperties.booleanForKey(ERXPropertiesTest.Key));
    assertEquals(false, ERXProperties.booleanForKey(ERXPropertiesTest.Key));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "yes");
    assertEquals(true, ERXProperties.booleanForKey(ERXPropertiesTest.Key));
    assertSame(new Boolean(ERXProperties.booleanForKey(ERXPropertiesTest.Key)), new Boolean(ERXProperties.booleanForKey(ERXPropertiesTest.Key)));
  }

//  public void testDataForKey() {
//    ERXPropertiesTest.setCachingEnabled(false);
//    
//    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
//    assertNull(ERXProperties.dataForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, null));
//    NSData defaultValue = new NSData(new byte[] { 1 });
//    assertSame(defaultValue, ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//    
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
//    assertNull(ERXProperties.dataForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, null));
//    assertSame(defaultValue, ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//    
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "<>");
//    assertEquals(NSData.EmptyData, ERXProperties.dataForKey(ERXPropertiesTest.Key));
//    assertEquals(NSData.EmptyData, ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, null));
//    
//    NSData value = new NSData(new byte[] { 80, 100, 120 });
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "<506478>");
//    assertEquals(value, ERXProperties.dataForKey(ERXPropertiesTest.Key));
//    assertEquals(value, ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, null));
//    assertNotSame(value, ERXProperties.dataForKey(ERXPropertiesTest.Key));
//
//    try {
//      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
//      ERXProperties.dataForKeyWithDefault(ERXPropertiesTest.Key, null);
//      throw new AssertionFailedError("This should have failed.");
//    }
//    catch (IllegalArgumentException e) {
//      // expected
//    }
//
//    ERXPropertiesTest.setCachingEnabled(true);
//    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
//    assertNull(ERXProperties.setForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.setForKey(ERXPropertiesTest.Key));
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "<506478>");
//    assertSame(ERXProperties.dataForKey(ERXPropertiesTest.Key), ERXProperties.dataForKey(ERXPropertiesTest.Key));
//    assertEquals(value, ERXProperties.dataForKey(ERXPropertiesTest.Key));
//  }

  public void testDictionaryForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, null));
    NSDictionary defaultValue = new NSDictionary();
    assertSame(defaultValue, ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertNull(ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, null));
    assertSame(defaultValue, ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "{}");
    assertEquals(NSDictionary.EmptyDictionary, ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    assertEquals(NSDictionary.EmptyDictionary, ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, null));
    
    NSDictionary value = new NSDictionary("value", "key");
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "{key=value;}");
    assertEquals(value, ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    assertEquals(value, ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, null));
    assertNotSame(value, ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
      ERXProperties.dictionaryForKeyWithDefault(ERXPropertiesTest.Key, null);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "{key=value;}");
    assertSame(ERXProperties.dictionaryForKey(ERXPropertiesTest.Key), ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
    assertEquals(value, ERXProperties.dictionaryForKey(ERXPropertiesTest.Key));
  }
  
  public void testDoubleForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(new Double(0.0d), new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)));
    double defaultValue = 15.0;
    assertEquals(new Double(defaultValue), new Double(ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertEquals(new Double(0.0d), new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)));
    assertEquals(new Double(defaultValue), new Double(ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "0");
    assertEquals(new Double(0.0d), new Double(ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "1");
    assertEquals(new Double(1.0d), new Double(ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5.0");
    assertEquals(new Double(5.0d), new Double(ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "-5.0");
    assertEquals(new Double(-5.0d), new Double(ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
      ERXProperties.doubleForKeyWithDefault(ERXPropertiesTest.Key, defaultValue);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(new Double(0.0d), new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)));
    assertEquals(new Double(0.0d), new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5000.0");
    assertEquals(new Double(5000.0d), new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)));
    assertEquals(new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)), new Double(ERXProperties.doubleForKey(ERXPropertiesTest.Key)));
  }

  public void testFloatForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(new Float(0.0f), new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)));
    float defaultValue = 15.0f;
    assertEquals(new Float(defaultValue), new Float(ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertEquals(new Float(0.0f), new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)));
    assertEquals(new Float(defaultValue), new Float(ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "0");
    assertEquals(new Float(0.0f), new Float(ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "1");
    assertEquals(new Float(1.0f), new Float(ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5.0");
    assertEquals(new Float(5.0f), new Float(ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "-5.0");
    assertEquals(new Float(-5.0f), new Float(ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue)));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
      ERXProperties.floatForKeyWithDefault(ERXPropertiesTest.Key, defaultValue);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(new Float(0.0f), new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)));
    assertEquals(new Float(0.0f), new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5000.0");
    assertEquals(new Float(5000.0f), new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)));
    assertEquals(new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)), new Float(ERXProperties.floatForKey(ERXPropertiesTest.Key)));
  }

  public void testIntForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(0, ERXProperties.intForKey(ERXPropertiesTest.Key));
    int defaultValue = 15;
    assertEquals(defaultValue, ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertEquals(0, ERXProperties.intForKey(ERXPropertiesTest.Key));
    assertEquals(defaultValue, ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "0");
    assertEquals(0, ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "1");
    assertEquals(1, ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5");
    assertEquals(5, ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "-5");
    assertEquals(-5, ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5.0");
      ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
      ERXProperties.intForKeyWithDefault(ERXPropertiesTest.Key, defaultValue);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(0, ERXProperties.intForKey(ERXPropertiesTest.Key));
    assertEquals(0, ERXProperties.intForKey(ERXPropertiesTest.Key));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5000");
    assertEquals(5000, ERXProperties.intForKey(ERXPropertiesTest.Key));
    assertEquals(ERXProperties.intForKey(ERXPropertiesTest.Key), ERXProperties.intForKey(ERXPropertiesTest.Key));
  }
  
//  public void testClassForKey() {
//    ERXPropertiesTest.setCachingEnabled(false);
//    
//    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
//    assertNull(ERXProperties.classForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.classForKeyWithDefault(ERXPropertiesTest.Key, null));
//    Class defaultValue = NSSet.class;
//    assertSame(defaultValue, ERXProperties.classForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//    
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
//    assertNull(ERXProperties.classForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.classForKeyWithDefault(ERXPropertiesTest.Key, null));
//    assertSame(defaultValue, ERXProperties.classForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
//    
//    Class value = NSData.class;
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, value.getName());
//    assertEquals(value, ERXProperties.classForKey(ERXPropertiesTest.Key));
//    assertEquals(value, ERXProperties.classForKeyWithDefault(ERXPropertiesTest.Key, null));
//    // Class objects are singleton within a classloader
//    // assertNotSame(value, ERXProperties.classForKey(ERXPropertiesTest.Key));
//
//    try {
//      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
//      ERXProperties.classForKeyWithDefault(ERXPropertiesTest.Key, null);
//      throw new AssertionFailedError("This should have failed.");
//    }
//    catch (IllegalArgumentException e) {
//      // expected
//    }
//
//    ERXPropertiesTest.setCachingEnabled(true);
//    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
//    assertNull(ERXProperties.classForKey(ERXPropertiesTest.Key));
//    assertNull(ERXProperties.classForKey(ERXPropertiesTest.Key));
//    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, value.getName());
//    assertSame(ERXProperties.classForKey(ERXPropertiesTest.Key), ERXProperties.classForKey(ERXPropertiesTest.Key));
//    assertEquals(value, ERXProperties.classForKey(ERXPropertiesTest.Key));
//  }

  public void testLongForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(0, ERXProperties.longForKey(ERXPropertiesTest.Key));
    long defaultValue = 15;
    assertEquals(defaultValue, ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertEquals(0, ERXProperties.longForKey(ERXPropertiesTest.Key));
    assertEquals(defaultValue, ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "0");
    assertEquals(0, ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "1");
    assertEquals(1, ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5");
    assertEquals(5, ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "-5");
    assertEquals(-5, ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5.0");
      ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "randomString");
      ERXProperties.longForKeyWithDefault(ERXPropertiesTest.Key, defaultValue);
      throw new AssertionFailedError("This should have failed.");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertEquals(0, ERXProperties.longForKey(ERXPropertiesTest.Key));
    assertEquals(0, ERXProperties.longForKey(ERXPropertiesTest.Key));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "5000");
    assertEquals(5000, ERXProperties.longForKey(ERXPropertiesTest.Key));
    assertEquals(ERXProperties.longForKey(ERXPropertiesTest.Key), ERXProperties.longForKey(ERXPropertiesTest.Key));
  }

  public void setPropertiesFromArgv() {
  }

  public void testStringForKey() {
    ERXPropertiesTest.setCachingEnabled(false);
    
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.stringForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.stringForKeyWithDefault(ERXPropertiesTest.Key, null));
    String defaultValue = "defaultValue";
    assertSame(defaultValue, ERXProperties.stringForKeyWithDefault(ERXPropertiesTest.Key, defaultValue));
    
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
    assertEquals("", ERXProperties.stringForKey(ERXPropertiesTest.Key));
    assertEquals("", ERXProperties.stringForKeyWithDefault(ERXPropertiesTest.Key, null));
    
    String value = "value";
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, new String("value"));
    assertEquals(value, ERXProperties.stringForKey(ERXPropertiesTest.Key));
    assertEquals(value, ERXProperties.stringForKeyWithDefault(ERXPropertiesTest.Key, null));
    assertNotSame(value, ERXProperties.stringForKey(ERXPropertiesTest.Key));

    ERXPropertiesTest.setCachingEnabled(true);
    ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
    assertNull(ERXProperties.stringForKey(ERXPropertiesTest.Key));
    assertNull(ERXProperties.stringForKey(ERXPropertiesTest.Key));
    ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, value);
    // MS: we don't bother to cache string values
    //assertSame(ERXProperties.stringForKey(ERXPropertiesTest.Key), ERXProperties.stringForKey(ERXPropertiesTest.Key));
    assertEquals(ERXProperties.stringForKey(ERXPropertiesTest.Key), ERXProperties.stringForKey(ERXPropertiesTest.Key));
    assertEquals(value, ERXProperties.stringForKey(ERXPropertiesTest.Key));
  }
  
  public void testHasKey() {
	  ERXPropertiesTest.setCachingEnabled(false);
	  ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
	  assertFalse(ERXProperties.hasKey(ERXPropertiesTest.Key));
	  assertFalse(ERXProperties.hasKey(ERXPropertiesTest.Key, true));
	  assertFalse(ERXProperties.hasKey(ERXPropertiesTest.Key, false));
	  
	  ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "");
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key));
	  assertFalse(ERXProperties.hasKey(ERXPropertiesTest.Key, true));
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key, false));
	  
	  ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "foo");
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key));
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key, true));
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key, false));
	  
	  ERXPropertiesTest.setCachingEnabled(true);
	  ERXPropertiesTest.removePropertyForKey(ERXPropertiesTest.Key);
	  assertFalse(ERXProperties.hasKey(ERXPropertiesTest.Key));
	  assertFalse(ERXProperties.hasKey(ERXPropertiesTest.Key));

	  ERXPropertiesTest._setProperty(ERXPropertiesTest.Key, "foo");
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key));
	  assertTrue(ERXProperties.hasKey(ERXPropertiesTest.Key));
  }
}
