package er.selenium.rc;

import org.apache.log4j.Logger;

import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;
import com.webobjects.foundation.NSArray;

import er.selenium.SeleniumTest;

public class SeleniumTestRCRunner {
	private static final Logger log = Logger.getLogger(SeleniumTestRCRunner.class);
	
	private HttpCommandProcessor browser;
	
	public SeleniumTestRCRunner(String host, int port, String browserType, String browserStartUrl) {
		browser = new HttpCommandProcessor(host, port, browserType, browserStartUrl);		
	}

	public void prepare() {
		browser.start();		
	}

	public void run(SeleniumTest test) {
		int processedCommands = 0;
		try {
			for (SeleniumTest.Element element : (NSArray<SeleniumTest.Element>)test.elements()) {
				if (element instanceof SeleniumTest.Command) {
					SeleniumTest.Command command = (SeleniumTest.Command)element;
					log.debug("original command: " + command);
					
					browser.doCommand(command.getName(), new String[] {command.getTarget(), command.getValue()} );
					++processedCommands;
				}
			}
		} catch (SeleniumException e) {
			throw new SeleniumTestFailureException(e, test, processedCommands);
		}
	}
	
	public void finish() {
		browser.stop();
	}
}
