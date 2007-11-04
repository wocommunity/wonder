package com.mdimension.jchronic;

import java.util.Calendar;

import junit.framework.TestCase;

import com.mdimension.jchronic.tags.Scalar;
import com.mdimension.jchronic.tags.StringTag;
import com.mdimension.jchronic.utils.Time;
import com.mdimension.jchronic.utils.Token;

public class TokenTestCase extends TestCase {
  private Calendar _now;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }

  public void testToken() {
    Token token = new Token("foo");
    assertEquals(0, token.getTags().size());
    assertFalse(token.isTagged());
    token.tag(new StringTag("mytag"));
    assertEquals(1, token.getTags().size());
    assertTrue(token.isTagged());
    assertEquals(StringTag.class, token.getTag(StringTag.class).getClass());
    token.tag(new Scalar(Integer.valueOf(5)));
    assertEquals(2, token.getTags().size());
    token.untag(StringTag.class);
    assertEquals(1, token.getTags().size());
    assertEquals("foo", token.getWord());
  }
}
