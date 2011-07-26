package wowodc.background.utilties;

import junit.framework.TestCase;
import wowodc.background.utilities.Utilities;

public class BusyUtilitiesTests extends TestCase {
	
	public void testIsPrime() {
		
		assertTrue(Utilities.isPrime(2));
		assertTrue(Utilities.isPrime(7));
		assertTrue(Utilities.isPrime(23));
		
		assertFalse(Utilities.isPrime(-1));
		assertFalse(Utilities.isPrime(0));
		assertFalse(Utilities.isPrime(1));
		assertFalse(Utilities.isPrime(10));
		assertFalse(Utilities.isPrime(144));
		assertFalse(Utilities.isPrime(100));
		assertFalse(Utilities.isPrime(27));
	}
}
