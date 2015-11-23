package er.extensions.foundation;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

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

	/**
	 * Represents a simple encapsulation of two strings and their expected
	 * Levenshtein distance.
	 */
	private class LevenshteinExample {
		/**
		 * First string
		 */
		public String s1;

		/**
		 * Second string
		 */
		public String s2;

		/**
		 * Levenshtein distance between {@code s1} and {@code s2}
		 */
		public int d;

		/**
		 * Constructor
		 *
		 * @param s1
		 *            first string
		 * @param s2
		 *            second string
		 * @param d
		 *            Levenshtein distance
		 */
		public LevenshteinExample(String s1, String s2, int d) {
			this.s1 = s1;
			this.s2 = s2;
			this.d = d;
		}
	}

	/**
	 * An array of known strings and distances
	 */
	private NSArray<LevenshteinExample> levs;

	@Before
	public void setUp() throws Exception {
		// Set up the levs array
		NSMutableArray<LevenshteinExample> l = new NSMutableArray<ERXStringUtilitiesTest.LevenshteinExample>();

		// When the values are the same, the distance is zero.
		l.add(new LevenshteinExample("", "", 0));
		l.add(new LevenshteinExample("1", "1", 0));
		l.add(new LevenshteinExample("12", "12", 0));
		l.add(new LevenshteinExample("123", "123", 0));
		l.add(new LevenshteinExample("1234", "1234", 0));
		l.add(new LevenshteinExample("12345", "12345", 0));
		l.add(new LevenshteinExample("password", "password", 0));

		// When one of the values is empty, the distance is the length of the
		// other value.
		l.add(new LevenshteinExample("", "1", 1));
		l.add(new LevenshteinExample("", "12", 2));
		l.add(new LevenshteinExample("", "123", 3));
		l.add(new LevenshteinExample("", "1234", 4));
		l.add(new LevenshteinExample("", "12345", 5));
		l.add(new LevenshteinExample("", "password", 8));
		l.add(new LevenshteinExample("1", "", 1));
		l.add(new LevenshteinExample("12", "", 2));
		l.add(new LevenshteinExample("123", "", 3));
		l.add(new LevenshteinExample("1234", "", 4));
		l.add(new LevenshteinExample("12345", "", 5));
		l.add(new LevenshteinExample("password", "", 8));

		// Whenever a single character is inserted or removed, the distance is
		// one.
		l.add(new LevenshteinExample("password", "1password", 1));
		l.add(new LevenshteinExample("password", "p1assword", 1));
		l.add(new LevenshteinExample("password", "pa1ssword", 1));
		l.add(new LevenshteinExample("password", "pas1sword", 1));
		l.add(new LevenshteinExample("password", "pass1word", 1));
		l.add(new LevenshteinExample("password", "passw1ord", 1));
		l.add(new LevenshteinExample("password", "passwo1rd", 1));
		l.add(new LevenshteinExample("password", "passwor1d", 1));
		l.add(new LevenshteinExample("password", "password1", 1));
		l.add(new LevenshteinExample("password", "assword", 1));
		l.add(new LevenshteinExample("password", "pssword", 1));
		l.add(new LevenshteinExample("password", "pasword", 1));
		l.add(new LevenshteinExample("password", "pasword", 1));
		l.add(new LevenshteinExample("password", "passord", 1));
		l.add(new LevenshteinExample("password", "passwrd", 1));
		l.add(new LevenshteinExample("password", "passwod", 1));
		l.add(new LevenshteinExample("password", "passwor", 1));

		// Whenever a single character is replaced, the distance is one.
		l.add(new LevenshteinExample("password", "Xassword", 1));
		l.add(new LevenshteinExample("password", "pXssword", 1));
		l.add(new LevenshteinExample("password", "paXsword", 1));
		l.add(new LevenshteinExample("password", "pasXword", 1));
		l.add(new LevenshteinExample("password", "passXord", 1));
		l.add(new LevenshteinExample("password", "passwXrd", 1));
		l.add(new LevenshteinExample("password", "passwoXd", 1));
		l.add(new LevenshteinExample("password", "passworX", 1));

		// If characters are taken off the front and added to the back and all
		// of
		// the characters are unique, then the distance is two times the number
		// of
		// characters shifted, until you get halfway (and then it becomes easier
		// to shift from the other direction).
		l.add(new LevenshteinExample("12345678", "23456781", 2));
		l.add(new LevenshteinExample("12345678", "34567812", 4));
		l.add(new LevenshteinExample("12345678", "45678123", 6));
		l.add(new LevenshteinExample("12345678", "56781234", 8));
		l.add(new LevenshteinExample("12345678", "67812345", 6));
		l.add(new LevenshteinExample("12345678", "78123456", 4));
		l.add(new LevenshteinExample("12345678", "81234567", 2));

		// If all the characters are unique and the values are reversed, then
		// the
		// distance is the number of characters for an even number of
		// characters,
		// and one less for an odd number of characters (since the middle
		// character will stay the same).
		l.add(new LevenshteinExample("12", "21", 2));
		l.add(new LevenshteinExample("123", "321", 2));
		l.add(new LevenshteinExample("1234", "4321", 4));
		l.add(new LevenshteinExample("12345", "54321", 4));
		l.add(new LevenshteinExample("123456", "654321", 6));
		l.add(new LevenshteinExample("1234567", "7654321", 6));
		l.add(new LevenshteinExample("12345678", "87654321", 8));

		// The rest of these are miscellaneous interesting examples. They will
		// be illustrated using the following key:
		// = (the characters are equal)
		// + (the character is inserted)
		// - (the character is removed)
		// # (the character is replaced)

		// Mississippi
		// ippississiM
		// -=##====##=+ --> 6
		l.add(new LevenshteinExample("Mississippi", "ippississiM", 6));

		// eieio
		// oieie
		// #===# --> 2
		l.add(new LevenshteinExample("eieio", "oieie", 2));

		// brad+angelina
		// bra ngelina
		// ===+++======= --> 3
		l.add(new LevenshteinExample("brad+angelina", "brangelina", 3));

		// test international chars
		// ?e?uli?ka
		// e?uli?ka
		// -======== --> 1
		l.add(new LevenshteinExample("?e?uli?ka", "e?uli?ka", 1));

		levs = l.immutableClone();
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

	/**
	 * Tests {@link ERXStringUtilities#distance(String, String)}.
	 */
	@Test
	public void testDistance() {
		for (LevenshteinExample l : levs) {
			assertEquals(l.d, StringUtils.getLevenshteinDistance(l.s1, l.s2), 0.00001);
		}
	}

	/**
	 * Tests {@link ERXStringUtilities#levenshteinDistance(String, String)}.
	 */
	@Test
	public void testLevenshteinDistance() {
		for (LevenshteinExample l : levs) {
			assertEquals(l.d,
					StringUtils.getLevenshteinDistance(l.s1, l.s2));
		}
	}
	
	@Test
	public void testSafeIdentifierName() {
		String safeJavaIdentifierStart = "IamSafe";
		String safeJavaIdentifierStartWithUnsafeChars = "Iam Nearly+Safe";
		String unsafeJavaIdentifierStart = "0safe";
		String nullIdentifierStart = null;
		String emptyIdentifierStart = "";
		String prefix = "prefix";
		char replacement = '_';
		
		String resultWithSafeStart = ERXStringUtilities.safeIdentifierName(safeJavaIdentifierStart, prefix, replacement);
		String resultWithSafeStartUnsafeContent = ERXStringUtilities.safeIdentifierName(safeJavaIdentifierStartWithUnsafeChars, prefix, replacement);
		String resultWithUnsafeStart = ERXStringUtilities.safeIdentifierName(unsafeJavaIdentifierStart, prefix, replacement);
		String resultWithNullStart = ERXStringUtilities.safeIdentifierName(nullIdentifierStart, prefix, replacement);
		String resultWithEmptyStart = ERXStringUtilities.safeIdentifierName(emptyIdentifierStart, prefix, replacement);
		
		Assert.assertEquals(safeJavaIdentifierStart, resultWithSafeStart);
		
		Assert.assertNotSame(safeJavaIdentifierStartWithUnsafeChars, resultWithSafeStartUnsafeContent);
		Assert.assertEquals("Expected 2 replacements for unsafe characters", 2, resultWithSafeStartUnsafeContent.replaceAll("[^_]", "").length());
		Assert.assertFalse("Did not expect prefix as identifier starts with safe character.", resultWithSafeStartUnsafeContent.contains(prefix));

		Assert.assertNotSame(unsafeJavaIdentifierStart, resultWithUnsafeStart);
		Assert.assertFalse("Expected no replacement for unsafe characters", resultWithUnsafeStart.contains("_"));
		Assert.assertTrue("Did expect prefix as identifier starts with unsafe character.", resultWithUnsafeStart.contains(prefix));

		Assert.assertNotSame(nullIdentifierStart, resultWithNullStart);
		Assert.assertTrue("Did expect 'null' as identifier was null.", resultWithNullStart.contains("null"));
		Assert.assertTrue("Did expect prefix as identifier was null.", resultWithNullStart.contains(prefix));

		Assert.assertNotSame(emptyIdentifierStart, resultWithEmptyStart);
		Assert.assertEquals(prefix, resultWithEmptyStart);
	}
}
