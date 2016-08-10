package wowodc.background.utilities;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXThreadStorage;

/**
 * A utility class for this example app.
 * 
 * @author kieran
 */
public class Utilities {
	private static final Logger log = LoggerFactory.getLogger(Utilities.class);
	public static final String ERRORS_KEY = "_ERRORS_KEY";
	
	// Random number generator shared instance
	private static class RANDOM {
		static Random GENERATOR = new Random();
	}
	/**
	 * A Prime Number can be divided evenly only by 1 or itself.
	 * And it must be greater than 1.
	 * Example: 7 can be divided evenly only by 1 or 7, so it is a prime number.
	 * If it is not a Prime Number it is called a Composite Number
	 * Example: 6 can be divided evenly by 1, 2, 3 and 6 so it is a composite number.
	 * 
	 * Refs:
	 * 		http://primes.utm.edu/
	 * 		http://primes.utm.edu/notes/faq/one.html
	 * 
	 * @param aNumber number to check
	 * @return <code>true</code> if a number is a prime number, <code>false</code> otherwise
	 */
	public static boolean isPrime(long aNumber) {
		if (aNumber < 2) {
			return false;
		}
		
		boolean result = true;
		long remainder;
		long checkValue;
		
		// We check all values up to the square root of the number since logically
		// if a number smaller than the square root does not fit, then one larger
		// than the square root will not fit.
		for (checkValue = 2; checkValue * checkValue < aNumber; checkValue++) {
			remainder = aNumber % checkValue;
			log.debug("aNumber = {}; checkValue = {}; remainder = {}", aNumber, checkValue, remainder);
			if (remainder == 0) {
				// aNumber can be divided evenly by checkValue, so it is not prime
				result = false;
				log.debug("{} is NOT a prime number. It is a composite number!", aNumber);
				break;
			}
		}
		
		if (result) {
			log.debug("{} IS prime!", aNumber);
		}
		
		return result;
	}
	
	/**
	 * @return a positive random Long between 0 and 1 million
	 */
	public static long newStartNumber() {
		return RANDOM.GENERATOR.nextInt(1000001);
	}
	
	public static Random sharedRandom() {
		return RANDOM.GENERATOR;
	}
	
	/**
	 * @return errors for the current request
	 */
	public static NSMutableArray<String> errorMessages() {
		NSMutableArray<String> errors = (NSMutableArray<String>) ERXThreadStorage.valueForKey(ERRORS_KEY);
		if (errors == null) {
			errors = new NSMutableArray<String>();
			ERXThreadStorage.takeValueForKey(errors, ERRORS_KEY);
		}
		return errors;
	}
	
	/**
	 * Adds an error message to current list for current request.
	 * 
	 * @param message error message
	 */
	public static void addErrorMessage(String message) {
		errorMessages().addObject(message);
	}
	
	/**
	 * @return true if we have accumulated any error messages
	 */
	public static boolean hasErrors() {
		boolean result = false;
		NSMutableArray<String> errors = (NSMutableArray<String>) ERXThreadStorage.valueForKey(ERRORS_KEY);
		if (errors != null && errors.count() > 0) {
			result = true;
		}
		return result;
	}
}
