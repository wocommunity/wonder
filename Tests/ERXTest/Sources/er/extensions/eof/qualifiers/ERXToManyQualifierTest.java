package er.extensions.eof.qualifiers;

import junit.framework.TestCase;

import com.webobjects.foundation.NSArray;

public class ERXToManyQualifierTest extends TestCase {
	
	NSArray<Integer> ints = new NSArray<>(new Integer[] {
			Integer.valueOf(1), 
			Integer.valueOf(2), 
			Integer.valueOf(3), 
			Integer.valueOf(4),
			});
	NSArray<Integer> ints2 = new NSArray<>(new Integer[] {
			Integer.valueOf(1), 
			Integer.valueOf(2), 
			Integer.valueOf(3), 
			Integer.valueOf(3),
			Integer.valueOf(4),
			});
	NSArray<Integer> test1 = new NSArray<>(new Integer[] {
			Integer.valueOf(1), 
			Integer.valueOf(2), 
			Integer.valueOf(3), 
			Integer.valueOf(4),
			Integer.valueOf(5),
			Integer.valueOf(6),
			});
	NSArray<Integer> test2 = new NSArray<>(new Integer[] {
			Integer.valueOf(1), 
			Integer.valueOf(2), 
			Integer.valueOf(3), 
			Integer.valueOf(5),
			});
	NSArray<Integer> test3 = new NSArray<>(new Integer[] {
			Integer.valueOf(1), 
			Integer.valueOf(2), 
			Integer.valueOf(3), 
			});
	
	ERXToManyQualifier q1 = new ERXToManyQualifier("intValue", ints);
	ERXToManyQualifier q2 = new ERXToManyQualifier("intValue", ints, 4);
	ERXToManyQualifier q3 = new ERXToManyQualifier("intValue", ints, 3);
	ERXToManyQualifier q4 = new ERXToManyQualifier("intValue", ints, 2);
	ERXToManyQualifier q5 = new ERXToManyQualifier("intValue", ints2);
	ERXToManyQualifier q6 = new ERXToManyQualifier("intValue", ints2, 4);
	ERXToManyQualifier q7 = new ERXToManyQualifier("intValue", ints2, 3);
	ERXToManyQualifier q8 = new ERXToManyQualifier("intValue", ints2, 2);		

	public void testEvaluate123456ContainsAllOf1234() {
		
		// 1, 2, 3, 4, 5, 6 contains all of 1, 2, 3, 4
		assertTrue(q1.evaluateWithObject(test1));
	}
	
	public void testEvaluate1235NotContainsAllOf1234() {
		// 1, 2, 3, 5 does not contain all of 1, 2, 3, 4
		assertFalse(q1.evaluateWithObject(test2));

	}
	
	public void testEvaluate1235NotContains4Of1234() {
		// 1, 2, 3, 5 does not contain 4 matches in 1, 2, 3, 4
		assertFalse(q2.evaluateWithObject(test2));
		
	}
	
	public void testEvaluate1235Contains3Of1234() {
		// 1, 2, 3, 5 does contain 3 matches in 1, 2, 3, 4		
		assertTrue(q3.evaluateWithObject(test2));

	}
	
	public void testEvaluate1235Contains2Of1234() {
		// 1, 2, 3, 5 does contain 2 matches in 1, 2, 3, 4		
		assertTrue(q4.evaluateWithObject(test2));
		
	}
	
	public void testEvaluate123NotContainsAllOf1234() {
		// 1, 2, 3 does not contain all of 1, 2, 3, 4
		assertFalse(q1.evaluateWithObject(test3));

	}
	
	public void testEvaluate123456ContainsAllOf12334() {
		// 1, 2, 3, 4, 5, 6 contains all of 1, 2, 3, 3, 4
		assertTrue(q5.evaluateWithObject(test1));

	}
	
	public void testEvaluate1235NotContainsAllOf12334() {
		// 1, 2, 3, 5 does not contain all of 1, 2, 3, 3, 4
		assertFalse(q5.evaluateWithObject(test2));

	}
	
	public void testEvaluate1235Contains4Of12334() {
		// 1, 2, 3, 5 does not contain 4 matches in 1, 2, 3, 3, 4
		assertFalse(q6.evaluateWithObject(test2));
		
	}
	
	public void testEvaluate1235Contains3Of12334() {
		// 1, 2, 3, 5 does contain 3 matches in 1, 2, 3, 3, 4		
		assertTrue(q7.evaluateWithObject(test2));

	}
	
	public void testEvaluate1235Contains2Of12334() {
		// 1, 2, 3, 5 does contain 2 matches in 1, 2, 3, 3, 4		
		assertTrue(q8.evaluateWithObject(test2));
		
	}
	
	public void testEvaluate123NotContainsAllOf12334() {
		// 1, 2, 3 does not contain all of 1, 2, 3, 3, 4
		assertFalse(q5.evaluateWithObject(test3));

	}
	
	public void testEvaluate1234ContainsAllOf12334() {
		// 1, 2, 3, 4 contains all of 1, 2, 3, 3, 4
		assertTrue(q5.evaluateWithObject(ints));

	}
	
	public void testEvaluate12334ContainsAllOf1234() {
		// 1, 2, 3, 3, 4 contains all of 1, 2, 3, 4
		assertTrue(q1.evaluateWithObject(ints2));
	
	}

}