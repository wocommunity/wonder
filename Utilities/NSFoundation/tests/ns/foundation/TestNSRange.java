package ns.foundation;

import ns.foundation.NSMutableRange;
import ns.foundation.NSRange;

public class TestNSRange extends BaseTestCase {

	public void testNSRange() {
		NSRange range = new NSRange();
		assertEquals(new NSRange(0,0), range);
	}

	public void testNSRangeIntInt() {
		NSRange range = new NSRange(2,3);
		assertEquals(2, range.location());
		assertEquals(3, range.length());
	}

	public void testNSRangeNSRange() {
		NSRange range = new NSRange(1,1);
		NSRange otherRange = new NSRange(range);
		assertEquals(range, otherRange);
	}

	public void testLocation() {
		NSRange range = new NSRange(2,3);
		assertEquals(2, range.location());
	}

	public void testLength() {
		NSRange range = new NSRange(2,3);
		assertEquals(3, range.length());
	}

	public void testEqualsObject() {
		NSRange range = new NSRange(2,3);
		NSRange otherRange = new NSRange(2, 3);
		assertTrue(range.equals(otherRange));
		
		otherRange = new NSRange(3, 3);
		assertFalse(range.equals(otherRange));
		
		otherRange = null;
		assertFalse(range.equals(otherRange));
		
		otherRange = new NSMutableRange(2, 3);
		assertTrue(range.equals(otherRange));
	}
	
	public void testIsEqualToRange() {
		NSRange range = new NSRange(2,3);
		NSRange otherRange = new NSRange(2, 3);
		assertTrue(range.isEqualToRange(otherRange));
		
		otherRange = new NSRange(3, 3);
		assertFalse(range.isEqualToRange(otherRange));
	}

	public void testToString() {
		NSRange range = new NSRange(2,3);
		assertEquals("{2, 3}", range.toString());
	}

	public void testClone() {
		NSRange range = new NSRange(2,3);
		NSRange clone = (NSRange) range.clone();
		assertEquals(range, clone);
	}

	public void testContainsLocation() {
		NSRange range = new NSRange(2,3);
		assertFalse(range.containsLocation(1));
		assertTrue(range.containsLocation(2));
		assertTrue(range.containsLocation(4));
		assertFalse(range.containsLocation(5));
	}

	public void testIntersectsRange() {
		NSRange range = new NSRange(2,3);
		NSRange otherRange = new NSRange(3, 3);
		assertTrue(range.intersectsRange(otherRange));
	}

	public void testIsEmpty() {
		NSRange range = new NSRange(2,3);
		assertFalse(range.isEmpty());
		
		range = new NSRange(2, 0);
		assertTrue(range.isEmpty());
		
		try {
		  range = new NSRange(0, -1);
		  fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testIsSubrangeOfRange() {
		NSRange range = new NSRange(2,3); //2,3,4
		NSRange otherRange = new NSRange(3, 2); //3,4
		assertTrue(otherRange.isSubrangeOfRange(range));
    assertFalse(range.isSubrangeOfRange(otherRange));
	}

	public void testMaxRange() {
		NSRange range = new NSRange(2,3);
		assertEquals(5, range.maxRange());
		
		range = new NSRange(0, 0);
		assertEquals(0, range.maxRange());
	}

	public void testRangeByIntersectingRange() {
		NSRange range = new NSRange(2,3); //2,3,4
		NSRange otherRange = new NSRange(3, 3); //3,4,5
		NSRange intersection = range.rangeByIntersectingRange(otherRange); //3,4
		assertEquals(new NSRange(3, 2), intersection);
	}

	public void testRangeByUnioningRange() {
		NSRange range = new NSRange(2,3); //2,3,4
		NSRange otherRange = new NSRange(3, 3); //3,4,5
		NSRange union = range.rangeByUnioningRange(otherRange); //2,3,4,5
		assertEquals(new NSRange(2, 4), union);
	}

	public void testZeroRange() {
		assertEquals(0, NSRange.ZeroRange.location());
		assertEquals(0, NSRange.ZeroRange.length());
	}
	
	public void testSubtractRangeNoIntersect() {
		NSMutableRange result1 = new NSMutableRange();
		NSMutableRange result2 = new NSMutableRange();
		
		// no intersection
		NSRange testRange = new NSRange(0, 2); // 0, 1
		NSRange subRange = new NSRange(2, 2);  // 2, 3
		testRange.subtractRange(subRange, result1, result2);
		assertEquals(result1, new NSRange(0, 0));
    assertEquals(result2, new NSRange(0, 0));
	}

	public void testSubtractRangeTotalIntersect() {
	  NSMutableRange result1 = new NSMutableRange();
	  NSMutableRange result2 = new NSMutableRange();

		// total intersection
	  NSRange testRange = new NSRange(1, 2); // 1, 2
	  NSRange subRange = new NSRange(0, 4);  // 0, 1, 2, 3, 4
		testRange.subtractRange(subRange, result1, result2);
		assertEquals(0, result1.length());
		assertEquals(0, result2.length());
	}

	public void testSubtractRangeStartIntersect() {
    NSMutableRange result1 = new NSMutableRange();
    NSMutableRange result2 = new NSMutableRange();

    // start intersection
    NSRange testRange = new NSRange(1, 2); // 1, 2
    NSRange subRange = new NSRange(0, 2);   // 0, 1
    testRange.subtractRange(subRange, result1, result2);
		assertEquals(new NSMutableRange(2, 1), result1);
		assertEquals(new NSMutableRange(0, 0), result2);
	}

	public void testSubtractRangeEndIntersect() {
	  NSMutableRange result1 = new NSMutableRange();
	  NSMutableRange result2 = new NSMutableRange();

		// end intersection
	  NSRange testRange = new NSRange(1, 2); // 1, 2
	  NSRange subRange = new NSRange(2,2);   // 2, 3
		testRange.subtractRange(subRange, result1, result2);
		assertEquals(new NSMutableRange(1, 1), result1);
		assertEquals(new NSMutableRange(0, 0), result2);
	}
	
	public void testFromString() {
		NSRange range = NSRange.fromString("{1,1}");
		assertEquals(new NSRange(1,1), range);

		range = NSRange.fromString("{1, 1}");
		assertEquals(new NSRange(1,1), range);
		
		try {
			NSRange.fromString("{1}");
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException e) {
		}
	}
	
}
