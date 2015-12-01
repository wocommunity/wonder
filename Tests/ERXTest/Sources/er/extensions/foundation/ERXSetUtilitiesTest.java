package er.extensions.foundation;

import java.util.Arrays;
import java.util.List;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSSet;

import er.erxtest.ERXTestCase;

public class ERXSetUtilitiesTest extends ERXTestCase {
	public void testSetFromArray() {
		NSArray<String> array1 = new NSArray<>("red", "blue");
		NSArray<String> array2 = new NSArray<>("red", "blue", "blue", "red", "blue");
		List<String> array3 = Arrays.asList("red", "blue", "blue", "red", "blue");
		NSSet<String> redBlueSet = new NSSet<>("red", "blue");
		
		assertEquals(NSSet.emptySet(), ERXSetUtilities.setFromArray(null));
		assertEquals(NSSet.emptySet(), ERXSetUtilities.setFromArray(new NSArray<>()));

		assertEquals(redBlueSet, ERXSetUtilities.setFromArray(array1));
		assertEquals(redBlueSet, ERXSetUtilities.setFromArray(array2));
		assertEquals(redBlueSet, ERXSetUtilities.setFromArray(array3));
	}
}
