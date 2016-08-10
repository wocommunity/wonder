package wowodc.background.tasks;


public class T09RunnableWithSimulatedError implements Runnable {

	public void run() {
		// Simulate an exception
		throw new RuntimeException("This is a fake simulated exception in a Runnable task");
	}

}
