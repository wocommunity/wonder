package er.extensions.foundation;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.erxtest.ERXTestCase;

/**
 * Tests for ERXPropertyListSerialization.
 * 
 * @author jw
 */
public class ERXPropertyListSerializationTest extends ERXTestCase {

	public void testJsonStringFromPropertyList() {
		// Unicode string
		String stringObject = "français";
		String jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(stringObject);
		assertEquals("\"fran\\u00e7ais\"", jsonString);
		assertEquals(stringObject, ERXPropertyListSerialization.propertyListFromJSONString(jsonString));
		// Integer array
		NSArray<Integer> integerArray = new NSArray<Integer>(new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) } );
		jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(integerArray);
		assertEquals("[1,2,3]", jsonString);
		jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(integerArray, false);
		assertEquals("[\n\t1,\n\t2,\n\t3\n]", jsonString);
		// dictionary
		NSDictionary<String, Integer> integerDict = new NSDictionary<String, Integer>(new Integer[] {Integer.valueOf(1), Integer.valueOf(2)}, new String[] {"a", "b"});
		jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(integerDict);
		assertEquals("{\"a\" : 1,\"b\" : 2}", jsonString);
		jsonString = ERXPropertyListSerialization.jsonStringFromPropertyList(integerDict, false);
		assertEquals("{\n\t\"a\" : 1,\n\t\"b\" : 2\n}", jsonString);
	}
}
