package er.chronic;

import junit.framework.TestCase;
import er.chronic.tags.Scalar;
import er.chronic.tags.StringTag;
import er.chronic.utils.Token;

public class TokenTest extends TestCase {

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
