package er.neo4jadaptor.ersatz.lucene;

import junit.framework.TestCase;

public class StorableTypesTest extends TestCase {
	private static void assertOrderIsKept(long val1, long val2) {
		String rep1 = StorableTypes.LONG.encode(val1);
		String rep2 = StorableTypes.LONG.encode(val2);
		int result = rep1.compareTo(rep2);
		
		if (val1 < val2) {
			assertTrue(result < 0);
		} else if (val1 > val2) {
			assertTrue(result > 0);
		} else {
			assertTrue(result == 0);
		}
	}
	
	private static void assertOrderIsKept(double val1, double val2) {
		String rep1 = StorableTypes.DOUBLE.encode(val1);
		String rep2 = StorableTypes.DOUBLE.encode(val2);
		String msg = rep1 + " vs. " + rep2;
		int result = rep1.compareTo(rep2);
		
		if (val1 < val2) {
			assertTrue(msg, result < 0);
		} else if (val1 > val2) {
			assertTrue(msg, result > 0);
		} else {
			assertTrue(msg, result == 0);
		}
	}
	
	public void test1_longSimpleValues() {
		assertOrderIsKept(1, 3);
		assertOrderIsKept(3, 1);
		assertOrderIsKept(0, 0);
	}
	
	public void test2_longNegativeValues() {
		assertOrderIsKept(-1, 3);
		assertOrderIsKept(0, -0);
	}
	
	public void test3_longEdgeValues() {
		assertOrderIsKept(Long.MAX_VALUE, 0);
		assertOrderIsKept(Long.MIN_VALUE, 0);
		assertOrderIsKept(Long.MAX_VALUE, Long.MIN_VALUE);
		assertOrderIsKept(Long.MIN_VALUE, -1);
		assertOrderIsKept(-1, Long.MIN_VALUE);
		assertOrderIsKept(Long.MAX_VALUE, Long.MAX_VALUE-1);
	}
	
	public void test4_doubleSimpleValues() {
		assertOrderIsKept(0.0000001, Math.PI);
		assertOrderIsKept(Math.PI, 0.00001);
		assertOrderIsKept(0.0000000000000000000000000000001, 0.000000000000000000000000000099);
		assertOrderIsKept(1.0000000000000000000000000000001, 0.000000000000000000000000000099);
		
	}
}
