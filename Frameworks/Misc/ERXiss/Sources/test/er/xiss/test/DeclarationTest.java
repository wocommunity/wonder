package er.xiss.test;

import junit.framework.TestCase;

import org.apache.commons.lang3.CharEncoding;

import er.xiss.ERXML;

public class DeclarationTest extends TestCase {
  public void testWrite() {
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", new ERXML.Declaration("1.0", CharEncoding.UTF_8).toString());
    assertEquals("<?xml version=\"1.0\"?>\n", new ERXML.Declaration("1.0", null).toString());
    assertEquals("<?xml?>\n", new ERXML.Declaration(null, null).toString());
  }
}
