package ns.foundation;

import ns.foundation.NSMutableRange;
import ns.foundation.NSRange;

public class TestNSMutableRange extends BaseTestCase {

	public void testNSMutableRange() {
		NSRange range = new NSRange();
		assertEquals(0, range.location());
		assertEquals(0, range.length());
	}

	public void testNSMutableRangeIntInt() {
		NSMutableRange range = new NSMutableRange(2,3);
		assertEquals(2, range.location());
		assertEquals(3, range.length());
	}

	public void testNSMutableRangeNSRange() {
		NSMutableRange range = new NSMutableRange(1,1);
		NSMutableRange otherRange = new NSMutableRange(range);
		assertEquals(range, otherRange);
	}

	public void testClone() {
		NSMutableRange range = new NSMutableRange(2,3);
		NSMutableRange clone = (NSMutableRange) range.clone();
		assertEquals(range, clone);
	}

	public void testIntersectRange() {
		NSMutableRange range = new NSMutableRange(2,3);
		NSRange otherRange = new NSRange(3, 3);
		range.intersectRange(otherRange);
		assertEquals(3, range.location());
		assertEquals(2, range.length());
	}

	public void testSetLength() {
		NSMutableRange range = new NSMutableRange(2,3);
		assertEquals(3, range.length());
		range.setLength(1);
		assertEquals(1, range.length());
	}

	public void testSetLocation() {
		NSMutableRange range = new NSMutableRange(2,3);
		assertEquals(2, range.location());
		range.setLocation(1);
		assertEquals(1, range.location());
	}

	public void testUnionRange() {
		NSMutableRange range = new NSMutableRange(2,3);
		NSRange otherRange = new NSRange(3, 3);
		range.unionRange(otherRange);
		assertEquals(2, range.location());
		assertEquals(4, range.length());
	}

}
