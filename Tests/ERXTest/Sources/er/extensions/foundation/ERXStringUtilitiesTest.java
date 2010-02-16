package er.extensions.foundation;

import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import er.erxtest.ERXTestSuite;

//This test class does not extend ERXTestCase because doing so borks the jUnit4 features
//used to test for exceptions. Since we aren't relying on ERXTestCase to do it for us, a 
//static initializer block is provided at the beginning of the class. A test suite adapter
//is also provided to maintain compatibility with the JUnit3 ERXTestSuite.

public class ERXStringUtilitiesTest {
	{
		ERXTestSuite.initialize();
	}
	
	public static junit.framework.Test suite() { 
	    return new JUnit4TestAdapter(ERXStringUtilitiesTest.class); 
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMaskStringWithCharacter() {
		String result;
		String arg = "0123456789";
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', 0, -1);
		assertEquals("*********9", result);
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', 0, arg.length());
		assertEquals("**********", result);
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', 0, -4);
		assertEquals("******6789", result);
		result= ERXStringUtilities.maskStringWithCharacter(arg, '*', 0, 5);
		assertEquals("*****56789", result);
		result= ERXStringUtilities.maskStringWithCharacter(arg, '*', 2, 5);
		assertEquals("01***56789", result);
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', -5, 9);
		assertEquals("01234****9", result);
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', -5, -5);
		assertEquals("0123456789", result);
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', -5, 5);
		assertEquals("0123456789", result);
		result = ERXStringUtilities.maskStringWithCharacter(arg, '*', -6, -5);
		assertEquals("0123*56789", result);
		result = ERXStringUtilities.maskStringWithCharacter("Visa 4111111111111111", '*', 5, -4);
		assertEquals("Visa ************1111", result);
	}
	
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testMaskStringWithCharacter2() {
		//Illegal arguments. endIndex < beginIndex
		ERXStringUtilities.maskStringWithCharacter("0123456789", '*', 6, 5);		
	}

	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testMaskStringWithCharacter3() {
		//Illegal arguments. endIndex < beginIndex
		ERXStringUtilities.maskStringWithCharacter("0123456789", '*', 6, 11);		
	}

	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testMaskStringWithCharacter4() {
		//Illegal arguments. endIndex < beginIndex
		ERXStringUtilities.maskStringWithCharacter("0123456789", '*', 11, 12);		
	}

}