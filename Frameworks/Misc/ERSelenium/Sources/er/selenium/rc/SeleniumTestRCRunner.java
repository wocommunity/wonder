package er.selenium.rc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.SeleniumException;

import er.selenium.SeleniumTest;

public class SeleniumTestRCRunner {
	private static final Logger log = LoggerFactory.getLogger(SeleniumTestRCRunner.class);
	
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
			for (SeleniumTest.Element element : test.elements()) {
				if (element instanceof SeleniumTest.Command) {
					SeleniumTest.Command command = (SeleniumTest.Command)element;
					log.debug("original command: {}", command);
					if (!command.getName().equals("pause")) {
						browser.doCommand(command.getName(), new String[] {command.getTarget(), command.getValue()} );
					} else {
						try {
							Thread.sleep(Long.parseLong(command.getTarget()));
						} catch (NumberFormatException e) {
							log.warn("invalid argument for pause command: {}", command.getTarget());
							throw new SeleniumException(e);
						} catch (InterruptedException e) {
							log.warn("pause command interrupted", e);
						}
					}
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
	
	public void captureScreenshot(String filename) {
    browser.doCommand("captureScreenshot", new String[] {filename});
  }
	
	public void captureEntirePageScreenshot(String filename) {
	   browser.doCommand("captureEntirePageScreenshot", new String[] {filename});
	}
}
