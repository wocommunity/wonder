package er.selenium.rc;

import com.thoughtworks.selenium.SeleniumException;

import er.selenium.SeleniumTest;

public class SeleniumTestFailureException extends RuntimeException {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
