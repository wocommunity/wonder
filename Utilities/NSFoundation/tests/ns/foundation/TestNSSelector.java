package ns.foundation;

import java.lang.reflect.InvocationTargetException;

import ns.foundation.NSForwardException;
import ns.foundation.NSSelectable;
import ns.foundation.NSSelector;

public class TestNSSelector extends BaseTestCase {
  private static final String TEST_EXCEPTION = "testException";
  private static final String TEST_METHOD = "test";
  private boolean invoked = false;

  public class TestSelectable implements NSSelectable {

    public TestSelectable() {
    }

    public void test() {
      invoked = true;
    }
    
    public void setInvoked(Boolean value) {
      invoked = value;
    }
    
    public void testException() throws IllegalArgumentException {
      throw new IllegalArgumentException(TEST_EXCEPTION);
    }
    
    public boolean testBoolean(boolean value) { return value; }
    public byte testByte(byte value) { return value; }
    public char testChar(char value) { return value; }
    public double testDouble(double value) { return value; }
    public float testFloat(float value) { return value; }
    public int testInteger(int value) { return value; }
    public long testLong(long value) { return value; }
    public short testShort(short value) { return value; }
    public int[] testIntArray(int[] value) { return value; }
  }

  public void testNSSelectorBinding() {
    NSSelector<?> selector = new NSSelector<Object>(TEST_METHOD);
    assertTrue(selector instanceof NSSelector<?>);
  }

  public void testNSSelectorNullBinding() {
    try {
      new NSSelector<Object>(null);
    } catch (IllegalArgumentException e) {
      return;
    }
    fail("null selector name should not be allowed");
  }
  
  public void testNSSelectorBindingParams() {
    NSSelector<?> selector = new NSSelector<Object>("setInvoked", new Class[] { Boolean.class });
    assertTrue(selector.parameterTypes() != null);
    assertEquals(1, selector.parameterTypes().length);
  }

  public void testInvokeSelectable() throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    NSSelector<?> selector = new NSSelector<Object>(TEST_METHOD);
    invoked = false;
    selector.invoke(new TestSelectable());
    assertTrue(invoked);
  }

  public void testInvokeSelectableMissingMethod() throws IllegalArgumentException, InvocationTargetException, IllegalAccessException {
    try {
      NSSelector<?> selector = new NSSelector<Object>("missingMethod");
      invoked = false;
      selector.invoke(new TestSelectable());
    } catch (NoSuchMethodException e) {
      assertFalse(invoked);
      return;
    }
    fail("failed to throw nosuchmethod exception on missing method");
  }
  
  public void testInvokeSelectableArgs() throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    NSSelector<?> selector = new NSSelector<Object>("setInvoked", new Class[] { Boolean.class } );
    invoked = false;
    selector.invoke(new TestSelectable(), new Object[] { true });
    assertTrue(invoked);
  }
  
  public void testSafeInvokeSelector() {
    NSSelector<?> selector = new NSSelector<Object>("setInvoked", new Class[] { Boolean.class } );
    invoked = false;
    NSSelector._safeInvokeSelector(selector, new TestSelectable(), new Object[] { true });
    assertTrue(invoked);
  }

  public void testSafeInvokeSelectorMissingMethod() {
    try {
      NSSelector<?> selector = new NSSelector<Object>("missingMethod");
      invoked = false;
      NSSelector._safeInvokeSelector(selector, new TestSelectable(), new Object[] { true });
    } catch (IllegalArgumentException e) {
      assertFalse(invoked);
      return;
    }
    fail("missing method did not throw illegal argument exception");
  }

  public void testSafeInvokeSelectorNullTarget() {
    try {
      NSSelector<?> selector = new NSSelector<Object>(TEST_METHOD);
      invoked = false;
      NSSelector._safeInvokeSelector(selector, null, new Object[0]);
    } catch (Exception e) {
      assertFalse(invoked);
      return;
    }
    fail("missing method did not throw illegal argument exception");
  }

  public void testInvokeClassCache() {
      NSSelector<?> selector = new NSSelector<Object>("setInvoked", new Class[] { Boolean.class });
      NSSelector._safeInvokeSelector(selector, new TestSelectable(), new Object[] { true });
      assertTrue(invoked);
      invoked = false;
      NSSelector._safeInvokeSelector(selector, new TestSelectable(), new Object[] { true });
      assertTrue(invoked);
  }

  public void testSafeInvokeSelectorNotSelectable() {
    try {
      NSSelector<?> selector = new NSSelector<Object>("setInvoked", new Class[] { Boolean.class });
      NSSelector._safeInvokeSelector(selector, new Object(), new Object[] { true });
    } catch (IllegalArgumentException e) {
      return;
    }
    fail("missing method did not throw class cast exception");
  }
  
  
  public void testSafeInvokeSelectorRethrowsException() {
    try {
      NSSelector<?> selector = new NSSelector<Object>(TEST_EXCEPTION);
      NSSelector._safeInvokeSelector(selector, new TestSelectable(), (Object[])null);
    } catch (NSForwardException e) {
      return;
    } catch (IllegalArgumentException e) {
      return;
    }
    fail("missing method did not throw class cast exception");
  }
  
  public void testPrimitiveParams() throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    NSSelectable target = new TestSelectable();
    NSSelector<Object> selector;

    Object obj = null;
    selector = new NSSelector<Object>("testBoolean", new Class[] { boolean.class });
    obj = selector.invoke(target, new Object[] { Boolean.valueOf(false) });
    assertTrue(obj instanceof Boolean);
    assertFalse((Boolean)obj);
    selector = new NSSelector<Object>("testByte", new Class[] { byte.class });
    obj = selector.invoke(target, new Object[] { Integer.valueOf(0).byteValue() });
    assertTrue(obj instanceof Byte);
    assertEquals((byte)0, (byte)(Byte)obj);
    selector = new NSSelector<Object>("testChar", new Class[] { char.class });
    obj = selector.invoke(target, new Object[] { Character.valueOf((char)0) });
    assertTrue(obj instanceof Character);
    assertEquals((char)0, (char)(Character)obj);
    selector = new NSSelector<Object>("testDouble", new Class[] { double.class });
    obj = selector.invoke(target, new Object[] { Double.valueOf(0) });
    assertTrue(obj instanceof Double);
    assertEquals((double)0, (double)(Double)obj);
    selector = new NSSelector<Object>("testFloat", new Class[] { float.class });
    obj = selector.invoke(target, new Object[] { Float.valueOf(0) });
    assertTrue(obj instanceof Float);
    assertEquals((float)0, (float)(Float)obj);
    selector = new NSSelector<Object>("testInteger", new Class[] { int.class });
    obj = selector.invoke(target, new Object[] { Integer.valueOf(0) });
    assertTrue(obj instanceof Integer);
    assertEquals((int)0, (int)(Integer)obj);
    selector = new NSSelector<Object>("testLong", new Class[] { long.class });
    obj = selector.invoke(target, new Object[] { Long.valueOf(0) });
    assertTrue(obj instanceof Long);
    assertEquals((long)0, (long)(Long)obj);
    selector = new NSSelector<Object>("testShort", new Class[] { short.class });
    obj = selector.invoke(target, new Object[] { Short.valueOf("0") });
    assertTrue(obj instanceof Short);
    assertEquals((short)0, (short)(Short)obj);
  }
  
  public void testArrayParams() throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    int[] arg = new int[0];
    NSSelector<?> selector = new NSSelector<Object>("testIntArray", new Class[] { arg.getClass() });
    Object obj = selector.invoke(new TestSelectable(), new Object[] { arg });
    assertTrue(obj instanceof int[]);
    assertEquals(obj, arg);
  }
}
