
package er.extensions.foundation;

import org.junit.Assert;

import com.webobjects.foundation.NSKeyValueCoding;

import er.erxtest.ERXTestCase;

public class ERXValueUtilitiesTest extends ERXTestCase {

    public void testIsNull() {

        String obj = null;

        Assert.assertFalse(ERXValueUtilities.isNull(this));
        Assert.assertTrue(ERXValueUtilities.isNull(obj));
        Assert.assertTrue(ERXValueUtilities.isNull(NSKeyValueCoding.NullValue));
        Assert.assertTrue(ERXValueUtilities.isNull(null));
    }

    // Deprecated methods. No need to test.
    //
    // public static boolean booleanValueForBindingOnComponentWithDefault(java.lang.String, com.webobjects.appserver.WOComponent, boolean);

    public void testBooleanValue() {
    	
    	Assert.assertTrue(ERXValueUtilities.booleanValue(new Integer(1)));
    	Assert.assertTrue(ERXValueUtilities.booleanValue(new Byte("1")));
    	Assert.assertTrue(ERXValueUtilities.booleanValue(new Float(1.0f)));
    	Assert.assertTrue(ERXValueUtilities.booleanValue(new Long(1L)));
    	Assert.assertTrue(ERXValueUtilities.booleanValue(new Short("1")));

    	Assert.assertFalse(ERXValueUtilities.booleanValue(new Integer(0)));
    	Assert.assertFalse(ERXValueUtilities.booleanValue(new Byte("0")));
    	Assert.assertFalse(ERXValueUtilities.booleanValue(new Float(0.0f)));
    	Assert.assertFalse(ERXValueUtilities.booleanValue(new Long(0L)));
    	Assert.assertFalse(ERXValueUtilities.booleanValue(new Short("0")));

    	try {
    		ERXValueUtilities.booleanValue("hello");
    		Assert.fail("a boolean from hello?");
    	} catch (java.lang.IllegalArgumentException iae) { }
    
    	try {
    		ERXValueUtilities.booleanValue("0.0");
    		Assert.fail("a boolean from 0.0?");
    	} catch (java.lang.IllegalArgumentException iae) { }

    	Assert.assertFalse(ERXValueUtilities.booleanValue("0"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("000000"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("false"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("False"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("FALSE"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("fAlSe"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("NO"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("no"));
    	Assert.assertFalse(ERXValueUtilities.booleanValue("nO"));

    	Assert.assertTrue(ERXValueUtilities.booleanValue("1"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("11"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("true"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("TRUE"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("tRuE"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("YES"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("yes"));
    	Assert.assertTrue(ERXValueUtilities.booleanValue("yEs"));
    }

    public void testBooleanValueWithDefault() {
    	
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Integer(1), false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Byte("1"), false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Float(1.0f), false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Long(1L), false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Short("1"), false));

    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Integer(0), false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Byte("0"), false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Float(0.0f), false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Long(0L), false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Short("0"), false));

    	try {
    		ERXValueUtilities.booleanValueWithDefault("hello", false);
    		Assert.fail("a boolean from hello?");
    	} catch (java.lang.IllegalArgumentException iae) { }
    
    	try {
    		ERXValueUtilities.booleanValueWithDefault("0.0", false);
    		Assert.fail("a boolean from 0.0?");
    	} catch (java.lang.IllegalArgumentException iae) { }

    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("0", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("000000", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("false", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("False", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("FALSE", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("fAlSe", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("NO", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("no", false));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("nO", false));

    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("1", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("11", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("true", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("TRUE", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("tRuE", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("YES", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("yes", false));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("yEs", false));

    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Integer(1), true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Byte("1"), true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Float(1.0f), true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Long(1L), true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(new Short("1"), true));

    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Integer(0), true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Byte("0"), true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Float(0.0f), true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Long(0L), true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(new Short("0"), true));

    	try {
    		ERXValueUtilities.booleanValueWithDefault("hello", true);
    		Assert.fail("a boolean from hello?");
    	} catch (java.lang.IllegalArgumentException iae) { }
    
    	try {
    		ERXValueUtilities.booleanValueWithDefault("0.0", true);
    		Assert.fail("a boolean from 0.0?");
    	} catch (java.lang.IllegalArgumentException iae) { }

    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("0", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("000000", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("false", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("False", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("FALSE", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("fAlSe", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("NO", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("no", true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault("nO", true));

    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("1", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("11", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("true", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("TRUE", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("tRuE", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("YES", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("yes", true));
    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault("yEs", true));

    	Assert.assertTrue(ERXValueUtilities.booleanValueWithDefault(null, true));
    	Assert.assertFalse(ERXValueUtilities.booleanValueWithDefault(null, false));
    }

    public void testBooleanValueWithDefaultBoolean() {
    	
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Integer(1), Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Byte("1"), Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Float(1.0f), Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Long(1L), Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Short("1"), Boolean.FALSE));

    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Integer(0), Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Byte("0"), Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Float(0.0f), Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Long(0L), Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Short("0"), Boolean.FALSE));

    	try {
    		ERXValueUtilities.BooleanValueWithDefault("hello", Boolean.FALSE);
    		Assert.fail("a boolean from hello?");
    	} catch (java.lang.IllegalArgumentException iae) { }
    
    	try {
    		ERXValueUtilities.BooleanValueWithDefault("0.0", Boolean.FALSE);
    		Assert.fail("a boolean from 0.0?");
    	} catch (java.lang.IllegalArgumentException iae) { }

    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("0", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("000000", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("false", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("False", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("FALSE", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("fAlSe", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("NO", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("no", Boolean.FALSE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("nO", Boolean.FALSE));

    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("1", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("11", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("true", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("TRUE", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("tRuE", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("YES", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("yes", Boolean.FALSE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("yEs", Boolean.FALSE));

    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Integer(1), Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Byte("1"), Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Float(1.0f), Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Long(1L), Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(new Short("1"), Boolean.TRUE));

    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Integer(0), Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Byte("0"), Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Float(0.0f), Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Long(0L), Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(new Short("0"), Boolean.TRUE));

    	try {
    		ERXValueUtilities.BooleanValueWithDefault("hello", Boolean.TRUE);
    		Assert.fail("a boolean from hello?");
    	} catch (java.lang.IllegalArgumentException iae) { }
    
    	try {
    		ERXValueUtilities.BooleanValueWithDefault("0.0", Boolean.TRUE);
    		Assert.fail("a boolean from 0.0?");
    	} catch (java.lang.IllegalArgumentException iae) { }

    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("0", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("000000", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("false", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("False", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("FALSE", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("fAlSe", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("NO", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("no", Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault("nO", Boolean.TRUE));

    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("1", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("11", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("true", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("TRUE", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("tRuE", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("YES", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("yes", Boolean.TRUE));
    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault("yEs", Boolean.TRUE));

    	Assert.assertEquals(Boolean.TRUE, ERXValueUtilities.BooleanValueWithDefault(null, Boolean.TRUE));
    	Assert.assertEquals(Boolean.FALSE, ERXValueUtilities.BooleanValueWithDefault(null, Boolean.FALSE));
    }


/*
    public static int intValue(java.lang.Object);
    public static int intValueWithDefault(java.lang.Object, int);
    public static java.lang.Integer IntegerValueWithDefault(java.lang.Object, java.lang.Integer);
    public static float floatValue(java.lang.Object);
    public static float floatValueWithDefault(java.lang.Object, float);
    public static java.lang.Float FloatValueWithDefault(java.lang.Object, java.lang.Float);
    public static double doubleValue(java.lang.Object);
    public static double doubleValueWithDefault(java.lang.Object, double);
    public static java.lang.Double DoubleValueWithDefault(java.lang.Object, java.lang.Double);
    public static long longValue(java.lang.Object);
    public static long longValueWithDefault(java.lang.Object, long);
    public static java.lang.Long LongValueWithDefault(java.lang.Object, java.lang.Long);
    public static com.webobjects.foundation.NSArray arrayValue(java.lang.Object);
    public static com.webobjects.foundation.NSArray arrayValueWithDefault(java.lang.Object, com.webobjects.foundation.NSArray);
    public static com.webobjects.foundation.NSSet setValue(java.lang.Object);
    public static com.webobjects.foundation.NSSet setValueWithDefault(java.lang.Object, com.webobjects.foundation.NSSet);
    public static com.webobjects.foundation.NSDictionary dictionaryValue(java.lang.Object);
    public static com.webobjects.foundation.NSDictionary dictionaryValueWithDefault(java.lang.Object, com.webobjects.foundation.NSDictionary);
    public static com.webobjects.foundation.NSData dataValue(java.lang.Object);
    public static com.webobjects.foundation.NSData dataValueWithDefault(java.lang.Object, com.webobjects.foundation.NSData);
    public static java.math.BigDecimal bigDecimalValue(java.lang.Object);
    public static java.math.BigDecimal bigDecimalValueWithDefault(java.lang.Object, java.math.BigDecimal);
    public static int compare(int, int);
    public static java.lang.Enum enumValue(java.lang.Object, java.lang.Class);
    public static java.lang.Enum enumValueWithRequiredDefault(java.lang.Object, java.lang.Enum);
    public static java.lang.Enum enumValueWithDefault(java.lang.Object, java.lang.Class, java.lang.Enum);
*/
}
