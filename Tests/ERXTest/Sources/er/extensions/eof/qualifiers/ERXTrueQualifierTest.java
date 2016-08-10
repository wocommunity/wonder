package er.extensions.eof.qualifiers;

import junit.framework.TestCase;
import er.extensions.qualifiers.ERXTrueQualifier;

public class ERXTrueQualifierTest extends TestCase {
	public void testToString() {
		ERXTrueQualifier q = new ERXTrueQualifier();
		assertEquals("(true)", q.toString());
	}

	public void testEvaluateWithObject() {
		Object object = new Object();
		ERXTrueQualifier q = new ERXTrueQualifier();
		assertTrue(q.evaluateWithObject(object));
	}
}
