package er.prototypes;


import java.util.Date;

import junit.framework.Assert;

import org.joda.time.*;

import com.webobjects.foundation.*;

import er.erxtest.ERXTestCase;

public class ValueFactoryConversionTest extends ERXTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testJodaLocalTime() {
    	LocalTime time1 = new LocalTime(12, 0, 0);
    	Date javaTime = ValueConversion.jodaLocalTime(time1);
    	LocalTime time2 = ValueFactory.jodaLocalTime(javaTime);

    	Assert.assertEquals(time1, time2);
    }

    public void testJodaLocalDate() {
    	LocalDate date1 = new LocalDate(2012, 1, 1);
    	Date javaDate = ValueConversion.jodaLocalDate(date1);
    	LocalDate date2 = ValueFactory.jodaLocalDate(javaDate);

    	Assert.assertEquals(date1, date2);
    }

    public void testJodaLocalDateTime() {
    	LocalDateTime localDateTime1 = new LocalDateTime(2012, 1, 1, 12, 0, 0);
    	Date javaLocalDateTime = ValueConversion.jodaLocalDateTime(localDateTime1);
    	LocalDateTime localDateTime2 = ValueFactory.jodaLocalDateTime(javaLocalDateTime);

    	Assert.assertEquals(localDateTime1, localDateTime2);
    }

    public void testStringArray() {
    	NSArray array1 = new NSArray("obj1", "obj1");
    	String string = ValueConversion.stringArray(array1);
    	NSArray array2 = ValueFactory.stringArray(string);

    	Assert.assertEquals(array1, array2);
    }

    public void testBlobArray() {
    	NSArray array1 = new NSArray("obj1", "obj1");
    	NSData blob = ValueConversion.blobArray(array1);
    	NSArray array2 = ValueFactory.blobArray(blob);

    	Assert.assertEquals(array1, array2);
    }

    public void testStringDictionary() {
    	NSDictionary dict1 = new NSDictionary("obj1", "key1");
    	String string = ValueConversion.stringDictionary(dict1);
    	dict1 = ValueFactory.stringDictionary(string);

    	Assert.assertEquals(dict1, dict1);
    }

    public void testBlobDictionary() {
    	NSDictionary dict1 = new NSDictionary("obj1", "key1");
    	NSData blob = ValueConversion.blobDictionary(dict1);
    	NSDictionary dict2 = ValueFactory.blobDictionary(blob);

    	Assert.assertEquals(dict1, dict2);
    }

    public void testSerializable() {
    	NSDictionary dict1 = new NSDictionary(new NSArray<>("obj1", "obj2"), "key1");
    	byte[] blob = ValueConversion.serializable(dict1);
    	NSDictionary dict2 = (NSDictionary) ValueFactory.serializable(blob);

    	Assert.assertEquals(dict1, dict2);
    }
}
