package er.selenium.rc;

import com.thoughtworks.selenium.SeleniumException;

import er.selenium.SeleniumTest;

public class SeleniumTestFailureException extends RuntimeException {
	private final SeleniumTest test;
	private final int processedCommands;

	public SeleniumTestFailureException(SeleniumException cause, SeleniumTest test, int processedCommands) {
		super(cause);
		this.test = test;
		this.processedCommands = processedCommands;
	}
	
	public SeleniumTest test() {
		return test;
	}
	
	public int processedCommands() {
		return processedCommands;
	}
	
	@Override
	public SeleniumException getCause() {
		return (SeleniumException)super.getCause();
	}
}
