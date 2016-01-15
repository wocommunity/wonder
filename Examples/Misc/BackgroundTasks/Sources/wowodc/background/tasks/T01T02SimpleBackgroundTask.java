package wowodc.background.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wowodc.background.utilities.Utilities;

/**
 * A basic Runnable that runs for a fixed period of time and finds Prime numbers.
 * 
 * @author kieran
 */
public class T01T02SimpleBackgroundTask implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(T01T02SimpleBackgroundTask.class);

	public void run() {

		long i = 0;
		long currentTime = System.currentTimeMillis();
		long startTime = currentTime;

		// Loop for fixed period of time
		while ((currentTime - startTime) < 10000) {
			i++;
			currentTime = System.currentTimeMillis();
			
			if (Utilities.isPrime(i)) {
				log.info("==>> {} is a PRIME number.", i);
			} else {
				log.debug("{} is not a prime number but is a COMPOSITE number.", i);
			}
		}
		
		log.info("Task complete. Checked for primes in {} numbers.", i);
	}
}
