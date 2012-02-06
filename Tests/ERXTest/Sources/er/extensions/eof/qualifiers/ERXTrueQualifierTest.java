package er.extensions.eof.qualifiers;

import junit.framework.TestCase;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXQ;
import er.extensions.qualifiers.ERXFalseQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;
import er.extensions.qualifiers.ERXPrefixQualifierTraversal;
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
