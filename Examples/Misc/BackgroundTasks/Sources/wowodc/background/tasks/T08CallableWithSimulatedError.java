package wowodc.background.tasks;

import java.util.concurrent.Callable;

public class T08CallableWithSimulatedError implements Callable<String> {

	public String call() throws Exception {
		// Simulate an exception
		throw new RuntimeException("This is a fake simulated exception in a Callable task");
	}

}
