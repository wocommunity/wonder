package com.webobjects.foundation;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public abstract class BaseTestCase extends TestCase {

  public static void assertEquals(Object[] arg0, Object[] arg1) {
    if (Arrays.equals(arg0, arg1))
      return;
    TestCase.assertEquals(arg0, arg1);
  }

  public static void assertEquals(Collection<?> arg0, Collection<?> arg1) {
    if (arg0 == null && arg1 == null)
      return;
    if ((arg0 != null && arg0.equals(arg1)) || (arg1 != null && arg1.equals(arg0)))
      return;
    TestCase.assertEquals(arg0, arg1);
  }
}
