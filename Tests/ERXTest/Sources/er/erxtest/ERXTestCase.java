package er.erxtest;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class ERXTestCase extends TestCase {
	static {
		ERXTestSuite.initialize();
	}

	public ERXTestCase() {
		super();
	}

	public ERXTestCase(String name) {
		super(name);
	}

	public static void assertEquals(Object[] arg0, Object[] arg1) { Arrays.equals(arg0, arg1); }

	public static String adaptorName() {
		return com.webobjects.eoaccess.EOModelGroup.defaultGroup().modelNamed(ERXTestSuite.ERXTEST_MODEL).adaptorName();
	}

	public static void assertEquals(Collection<?> arg0, Collection<?> arg1) {
		if (arg0 == null && arg1 == null)
			return;
		if ((arg0 != null && arg0.equals(arg1)) || (arg1 != null && arg1.equals(arg0)))
			return;
		TestCase.assertEquals(arg0, arg1);
	}
}
