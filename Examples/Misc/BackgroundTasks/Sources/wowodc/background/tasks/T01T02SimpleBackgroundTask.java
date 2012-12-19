package wowodc.background.tasks;

import org.apache.log4j.Logger;

import wowodc.background.utilities.Utilities;

/**
 * A basic Runnable that runs for a fixed period of time and finds Prime numbers.
 * 
 * @author kieran
 */
public class T01T02SimpleBackgroundTask implements Runnable {
	
	private static final Logger log = Logger.getLogger(T01T02SimpleBackgroundTask.class);

	public void run() {

		long i = 0;
		long currentTime = System.currentTimeMillis();
		long startTime = currentTime;

		// Loop for fixed period of time
		while ((currentTime - startTime) < 10000) {
			i++;
			currentTime = System.currentTimeMillis();
			
			if (Utilities.isPrime(i)) {
				log.info("==>> " + i + " is a PRIME number.");
			} else {
				log.debug(i + " is not a prime number but is a COMPOSITE number.");
			}
		}
		
		log.info("Task complete. Checked for primes in " + i + " numbers.");
	}
}
