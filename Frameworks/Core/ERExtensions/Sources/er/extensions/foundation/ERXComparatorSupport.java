package er.extensions.foundation;

import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSComparator.ComparisonException;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * Useful methods when working with NSComparator. 
 * 
 * @author Chuck Hill
 */
public class ERXComparatorSupport {
	
	
	/**
	 * Compares object1 and object2 using comparator.  If object1 or object2 is null/NSKeyValueCoding.NullValue it will
	 * sort before the other object.
	 *
	 * @param comparator NSComparator instance to use if both object1 and object2 are not null/NSKeyValueCoding.NullValue
	 * @param object1 first object to compare
	 * @param object2 second object to compare
	 * @return the result of comparing object1 to object2
	 * @throws ComparisonException if comparator throws this comparing object1 to object2
	 */
	public static int compareWithNullsFirst(NSComparator comparator, Object object1, Object object2) throws ComparisonException {
		if (object1 == null || object1 == NSKeyValueCoding.NullValue) {
			return (object2 == null || object2 == NSKeyValueCoding.NullValue) ? 0 : -1;
		}
        
		if (object2 == null || object2 == NSKeyValueCoding.NullValue) {
            	return 1;
		}
		
		return comparator.compare(object1, object2);
    }
	
	/**
	 * Compares object1 and object2 using comparator.  If object1 or object2 is null/NSKeyValueCoding.NullValue it will
	 * sort after the other object.
	 *
	 * @param comparator NSComparator instance to use if both object1 and object2 are not null/NSKeyValueCoding.NullValue
	 * @param object1 first object to compare
	 * @param object2 second object to compare
	 * @return the result of comparing object1 to object2
	 * @throws ComparisonException if comparator throws this comparing object1 to object2
	 */
	public static int compareWithNullsLast(NSComparator comparator, Object object1, Object object2) throws ComparisonException {
		if (object1 == null || object1 == NSKeyValueCoding.NullValue) {
			return (object2 == null || object2 == NSKeyValueCoding.NullValue) ? 0 : 1;
		}
        
		if (object2 == null || object2 == NSKeyValueCoding.NullValue) {
            	return -1;
		}
		
		return comparator.compare(object1, object2);
    }
	
	
}
